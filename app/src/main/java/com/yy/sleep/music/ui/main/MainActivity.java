package com.yy.sleep.music.ui.main;

import android.Manifest;
import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.support.design.widget.TabLayout;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.yy.sleep.music.R;
import com.yy.sleep.music.common.MusicConst;
import com.yy.sleep.music.model.LocationInfo;
import com.yy.sleep.music.model.MusicInfo;
import com.yy.sleep.music.model.UpdateInfo;
import com.yy.sleep.music.model.WeatherInfo;
import com.yy.sleep.music.service.MusicService;
import com.yy.sleep.music.sys.MusicPlayer;
import com.yy.sleep.music.sys.MusicSys;
import com.yy.sleep.music.sys.WeatherSys;
import com.yy.sleep.music.tool.TapPagerAdapter;
import com.yy.sleep.music.tool.WeatherTask;
import com.yy.sleep.music.ui.base.BaseActivity;
import com.yy.sleep.music.ui.weather.WeatherActivity;
import com.yy.sleep.music.util.LogUtil;
import com.yy.sleep.music.constant.YYConstant;
import com.yy.sleep.music.util.ShareUtil;
import com.yy.sleep.music.widget.CircularProgressView;
import com.yy.sleep.music.ui.detail.MusicDetailActivity;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;

import java.util.List;


public class MainActivity extends BaseActivity implements WeatherTask.ITaskWeather {
    private final static String TAG = "MainActivity";

    private CircularProgressView cpProgress;
    private DrawerLayout mDrawer;
    private View mDrawerLin;
    private Button btnMore;

    private ImageView ivPlay;
    private ImageView mIvNext;
    private ImageView mToolIconIv;


    private TextView tvSongTitle;
    private TextView tvLocation;
    private TextView tvDrawerTitle;
    private TextView tvWeather;
    private TextView tvTemperature;
    private TextView tvPower;
    private TextView tvDetail;

    //    private ImageView ivPlayMode;
    public boolean isOutSide;
    Intent intent;
    Boolean isContinue;
    MusicInfo mInfo;
    int mProgress;
    MusicPlayer instance;
    MusicHandler handler;
    ShareUtil shareUtil;
    private WeatherInfo mWeatherInfo;
    private LocationInfo mLocationInfo;
    private Interpolator mInterpolator;

    // 百度SDK 相关信息
    private LocationClient mLocationClient;
    private LocationListener mBDListener;
    private LocationClientOption mOption;

    private BroadcastReceiver mNetWorkReceiver;
    static final int REQUEST_READ = 301;
    static final int REQUEST_PHONE_STATE = 303;
    static final int REQUEST_LOCATION = 304;
    static final int REQUEST_ASK_WEATHER = 305;

    static final int MESSAGE_FIRST_INIT = 1000;
    static final int MESSAGE_LOCATION = 1001;
    static final int MESSAGE_NETWORK_CHANGE = 1002;
    private boolean needNetWork;
    private boolean canStartAnimation;

    static final String URL = "http://www.sojson.com/open/api/weather/json.shtml?city=";

    @Override
    public String TAG() {
        return "MainActivity";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = MusicPlayer.getInstance();
        shareUtil = ShareUtil.getInstance();
        setContentView(R.layout.activity_main);
        intent = new Intent(MainActivity.this, MusicService.class);
        startService(intent);
        initToolBar();
        initView();
        isOutSide = true;
        init();
        initSdk();
        checkPermission();
    }


    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            LogUtil.d(TAG(), "[checkPermission] 已经拥有读取权限");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MusicSys.getInstance().initMusicList(getApplicationContext(), true, false);
                    MusicPlayer.getInstance().update();
                    Intent intent = new Intent(MusicConst.ACTION_UPDATE_ALL_MUSIC_LIST);
                    intent.putExtra(MusicConst.CHANGE_FROM_OUTSIDE, false);
                    getApplicationContext().sendBroadcast(intent);
                    Message msg = Message.obtain();
                    msg.what = MESSAGE_FIRST_INIT;
                    handler.sendMessage(msg);
                }
            }).start();
        } else {
            // showAlert(YYConstant.READ_PERMISSION);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ);
            LogUtil.d(TAG(), "[showAlert] 正在申请读取权限");
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_PHONE_STATE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mWeatherInfo == null)
                mLocationClient.start();
            LogUtil.d(TAG(), "[checkPermission] 获取位置权限成功, 开始获取地理位置");
        } else
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
    }

    void initToolBar() {
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        // toolbar.setTitle("音乐");
        //toolbar.setNavigationIcon(R.mipmap.icons_cloud);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolIconIv = new ImageView(this);
        mToolIconIv.setBackground(getResources().getDrawable(R.mipmap.icons_cloud));
        toolbar.addView(mToolIconIv);
        mToolIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawer.openDrawer(mDrawerLin);
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String msg = "";
                switch (item.getItemId()) {
                    case R.id.main_tool_menu_item_one:
                        msg += "单曲循环模式";
                        instance.setPlayMode(MusicConst.SINGLE_PLAY);
                        break;
                    case R.id.main_tool_menu_item_two:
                        msg += "全部循环模式";
                        instance.setPlayMode(MusicConst.SEQUENTIAL_PLAY);
                        break;
                    case R.id.main_tool_menu_item_three:
                        msg += "随机播放模式";
                        instance.setPlayMode(MusicConst.RANDOM_PLAY);
                        break;
                }

                if (!msg.equals("")) {
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
                ShareUtil.getInstance().savePlayModeInfo();
                return true;
            }
        });
    }

    public void initView() {
        ViewPager viewPager = findViewById(R.id.main_music_vp);
        if (viewPager != null) {
            viewPager.setAdapter(new TapPagerAdapter(getSupportFragmentManager(), getFragments(), true));
            viewPager.setCurrentItem(0);
        }
        TabLayout tableLayout = findViewById(R.id.main_tabs);
        if (tableLayout != null) {
            tableLayout.setupWithViewPager(viewPager);
        }

        tvSongTitle = findViewById(R.id.main_music_song_title_cv);
        cpProgress = findViewById(R.id.main_music_progress_cv);
        ivPlay = findViewById(R.id.main_play_iv);
        mIvNext = findViewById(R.id.main_next_iv);

        mDrawer = findViewById(R.id.main_drawer);
        mDrawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                LogUtil.d(TAG, "[initView][onDrawerSlide] slideOffset = " + slideOffset);
                if (mToolIconIv == null) {
                    return;
                }
                mToolIconIv.setRotation(slideOffset * 360);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {
                LogUtil.d(TAG, "[initView][onDrawerStateChanged] newState = " + newState);
            }
        });
        mDrawerLin = findViewById(R.id.main_drawer_ll);
        tvLocation = findViewById(R.id.main_drawer_location_tv);
        tvDrawerTitle = findViewById(R.id.main_drawer_title_tv);
        tvWeather = findViewById(R.id.main_drawer_weather_tv);
        tvTemperature = findViewById(R.id.main_drawer_temperature_tv);
        tvPower = findViewById(R.id.main_drawer_power_tv);
        tvDetail = findViewById(R.id.main_drawer_detail_tv);
        btnMore = findViewById(R.id.main_drawer_more_btn);

        handler = new MusicHandler(this);
        new Thread(new MusicThread()).start();
        initMode(ShareUtil.getInstance().getPlayMode());
    }

    private void init() {
        canStartAnimation = true;
        mLocationInfo = new LocationInfo();
        mNetWorkReceiver = new NetWorkStateReceiver();
        registerReceiver(mNetWorkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void initSdk() {
        if (mLocationClient != null) {
            if (mBDListener != null) {
                mLocationClient.unRegisterLocationListener(mBDListener);
                mBDListener = null;
            }
            mLocationClient = null;
        }

        if (mBDListener == null) {
            mBDListener = new LocationListener();
            mOption = new LocationClientOption();
            mOption.setIsNeedAddress(true);
        }

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(mBDListener);

        //可选，是否需要地址信息，默认为不需要，即参数为false
        //如果开发者需要获得当前点的地址信息，此处必须为true

        if (mOption != null)
            mLocationClient.setLocOption(mOption);
    }

    private List<Fragment> getFragments() {
        List<Fragment> fragments = new ArrayList<>();
        MusicDiscoverFragment fragment3 = new MusicDiscoverFragment();
        MusicCollectFragment fragment2 = new MusicCollectFragment();
        MusicLocalFragment fragment1 = new MusicLocalFragment();
        fragment1.setCallback(fragment2);

        fragments.add(fragment1);
        fragments.add(fragment2);
        fragments.add(fragment3);
        return fragments;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // todo 由外不直接点击音频文件所触发的播放
        if (getIntent().getData() != null && isOutSide) {
            int position = MusicSys.getInstance().getFileMusicPosition(this);
            if (position != -3) {
                Intent intent = new Intent(MainActivity.this, MusicDetailActivity.class);
                intent.putExtra(MusicConst.MUSIC_POSITION, position);
                startActivity(intent);
                isOutSide = false;

            }
        }

        // todo  控制播放的状态
        if (instance.isPauseing()) {
            ivPlay.setImageResource(R.drawable.ic_music_play);
        }
        if (instance.isPlaying()) {
            ivPlay.setImageResource(R.drawable.ic_music_stop);
            if (instance.getSongInfo() != null && instance.getSongTitle() != null)
                tvSongTitle.setText(instance.getSongTitle());
        }

        if (instance.getSongInfo() != null) {
            isContinue = false;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!mDrawer.isClickable())
            mDrawer.closeDrawer(mDrawerLin);
    }

    public void onClickMain(View v) {

        switch (v.getId()) {
            case R.id.main_play_iv:
                if (isContinue && mInfo != null) {
                    instance.startMusic(MusicSys.getInstance().getPosition(mInfo.getFragmentNum(), mInfo), mInfo.getFragmentNum());
                    instance.pause();
                    int musicMax = instance.getMediaPlayer().getDuration();
                    LogUtil.i(TAG(), "[onClickMain] mProgress = " + mProgress + " ---  seekBarMax = " + 100 + " ---  musicMax= " + musicMax);
                    instance.getMediaPlayer().seekTo(musicMax * mProgress / 100);
                    instance.continuePlay();
                    ivPlay.setImageResource(R.drawable.ic_music_stop);
                    isContinue = false;
                    MusicPlayer.sIsPauseByMyself = false;
                    return;
                }
                if (instance.isPlaying()) {
                    instance.pause();
                    ivPlay.setImageResource(R.drawable.ic_music_play);
                    MusicPlayer.sIsPauseByMyself = true;
                } else if (instance.isStarted()) {
                    MusicPlayer.getInstance().continuePlay();
                    ivPlay.setImageResource(R.drawable.ic_music_stop);
                    MusicPlayer.sIsPauseByMyself = false;
                } else {
                    Toast.makeText(this, " 请选择一首歌曲吧", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.main_next_iv:
                if (isContinue && mInfo != null) {
                    instance.startMusic(MusicSys.getInstance().getPosition(mInfo.getFragmentNum(), mInfo), mInfo.getFragmentNum());
                    instance.nextMusic();
                    isContinue = false;
                    break;
                }
                if (!instance.isStarted()) {
                    startActivity(new Intent(MainActivity.this, MusicDetailActivity.class));
                    break;
                }

                nextAnimation(mIvNext);
                if (instance.isPlaying()) {
                    ivPlay.setImageResource(R.drawable.ic_music_stop);
                    MusicPlayer.sIsPauseByMyself = false;
                }
                instance.nextMusic();
                break;

            case R.id.main_drawer_detail_tv:
                // startActivity(new Intent(MainActivity.this, MusicDetailActivity.class));
                break;

            default:
                startActivity(new Intent(MainActivity.this, MusicDetailActivity.class));
//                ActionBar actionBar = getSupportActionBar();
//                if (actionBar == null)
//                    return;
//                if (!isVisible) {
//                    actionBar.hide();
//                    isVisible = true;
//                } else {
//                    actionBar.show();
//                    isVisible = false;
//                }

                break;
        }

    }

    public void onClickMainDrawer(View v) {
        switch (v.getId()) {
            case R.id.main_drawer_location_tv:
                if (mLocationClient != null && mWeatherInfo == null) {
                    initSdk();
                    mLocationClient.start();
                    LogUtil.d(TAG(), "[onClickMainDrawer] 刷新位置");
                }
                break;
            case R.id.main_drawer_more_btn:
                if (mWeatherInfo == null || mWeatherInfo.getData() == null || mWeatherInfo.getData().getForecast() == null)
                    return;

                LogUtil.d(TAG(), "[onClickMainDrawer] mWeatherInfo = " + mWeatherInfo);
                startActivity(new Intent(MainActivity.this, WeatherActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    public void refreshInfo(UpdateInfo info) {
        if (info == null || info.getUpdateTitle() == null)
            return;

        if (tvSongTitle == null)
            return;

        tvSongTitle.setText(info.getUpdateTitle());
        ivPlay.setImageResource(R.drawable.ic_music_stop);
    }

    @Override
    public void getBmpSuccess(Bitmap cover, String url) {
        // todo
    }

    @Override
    public void weatherRespond(WeatherInfo info) {
        if (info == null || info.getData() == null)
            return;
        mWeatherInfo = info;
        WeatherSys.getInstance().setWeatherInfo(info);
        tvDrawerTitle.setText("今日天气");
        btnMore.setVisibility(View.VISIBLE);
        List<WeatherInfo.Data.Forecast> forecastList = info.getData().getForecast();
        if (forecastList == null || forecastList.size() <= 0)
            return;
        WeatherInfo.Data.Forecast todayW = forecastList.get(0);

        String weather = todayW.getType();
        tvWeather.setText("天气 ： " + (weather != null ? weather : "异常"));

        String temperature = todayW.getHigh() + " - " + todayW.getLow();
        tvTemperature.setText(temperature);

        String power = todayW.getFx() + " - " + todayW.getFl();
        tvPower.setText(power);

        String detail = todayW.getNotice();
        tvDetail.setText(detail != null ? detail : "异常");
    }

    @Override
    public void weatherFailed() {
        tvDrawerTitle.setText("网络异常，无法获取天气");
    }

    private static class MusicHandler extends Handler {
        private final WeakReference<MainActivity> mainActivity;

        private MusicHandler(MainActivity activity) {
            mainActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final MainActivity activity = mainActivity.get();
            if (activity != null && activity.cpProgress != null && activity.tvSongTitle != null) {
                switch (msg.what) {
                    case MusicConst.MUSIC_PLAYING:
                        int currentPosition = MusicPlayer.getInstance().getMediaPlayer().getCurrentPosition();
                        int max = MusicPlayer.getInstance().getMediaPlayer().getDuration();
                        int progress = (int) ((float) currentPosition * 100 / max);
                        activity.cpProgress.setProgress(progress);
                        break;
                    case MESSAGE_FIRST_INIT:
                        activity.mInfo = ShareUtil.getInstance().getSongInfo();
                        LogUtil.d(activity.TAG(), "[handleMessage] info = " + activity.mInfo);
                        if (activity.mInfo != null) {
                            activity.mProgress = ShareUtil.getInstance().getProgress();
                            activity.tvSongTitle.setText(activity.mInfo.getTitle());
                            activity.ivPlay.setImageResource(R.drawable.ic_music_play);
                            activity.isContinue = true;
                        } else {
                            activity.mProgress = 0;
                            activity.isContinue = false;
                            activity.tvSongTitle.setText("快去听歌吧");
                        }
                        activity.cpProgress.setProgress(activity.mProgress);
                        break;
                    case MESSAGE_LOCATION:
                        String city = (String) msg.obj;
                        if (city != null) {
                            activity.tvLocation.setText("当前城市：" + city);
                            String c = null;
                            try {
                                c = URLEncoder.encode(city, "UTF-8");
                                new WeatherTask(activity).execute(URL + c);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                                LogUtil.d("MainActivity", "[handleMessage]城市文字转化异常");
                            }
                        } else
                            activity.tvDrawerTitle.setText("网络异常，无法获取天气");
                        break;

                    case MESSAGE_NETWORK_CHANGE:
                        LogUtil.d("MainActivity", "[handleMessage][MESSAGE_NETWORK_CHANGE] ");
                        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            if (activity.mWeatherInfo == null) {
                                activity.mLocationClient.start();
                                LogUtil.d("MainActivity", "[handleMessage][MESSAGE_NETWORK_CHANGE] 获取位置");
                            }
                        } else
                            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private class MusicThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Message msg = new Message();
                    msg.what = 100;
                    if (handler != null && MusicPlayer.getInstance().isPlaying()) {
                        handler.sendMessage(msg);
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void initMode(int mode) {
        switch (mode) {
            case MusicConst.SINGLE_PLAY:
                instance.setPlayMode(MusicConst.SINGLE_PLAY);
//                ivPlayMode.setImageResource(R.drawable.ic_single_play);
          /*      MenuItem itemOne = (MenuItem) findViewById(R.id.main_tool_menu_item_one);
                itemOne.setTitle("当前：单曲循环");*/
                break;

            case MusicConst.RANDOM_PLAY:
                instance.setPlayMode(MusicConst.RANDOM_PLAY);
//                ivPlayMode.setImageResource(R.drawable.ic_random_play);
         /*       MenuItem itemTwo = (MenuItem) findViewById(R.id.main_tool_menu_item_three);
                itemTwo.setTitle("当前：随机播放");*/
                break;

            default:
                instance.setPlayMode(MusicConst.SEQUENTIAL_PLAY);
//                ivPlayMode.setImageResource(R.drawable.ic_sequential_play);
                /*MenuItem itemThree = (MenuItem) findViewById(R.id.main_tool_menu_item_three);
                itemThree.setTitle("当前：全部循环");*/
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        isOutSide = true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG(), "[onDestroy]");
        if (!instance.isPlaying()) {
            stopService(intent);
        }
        unregisterReceiver(mNetWorkReceiver);
        if (mLocationClient != null)
            mLocationClient.unRegisterLocationListener(mBDListener);
    }

    public void showAlert(final int type) {
        AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.YMAlertDialogStyle).
                setTitle("权限说明").
                setMessage("开启访问音频文件的权限").
                setIcon(R.mipmap.icon_yymusic_launcher).
                setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (type) {
                            case YYConstant.READ_PERMISSION:
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ);
                                LogUtil.d(TAG(), "[showAlert] 正在申请读取权限");
                                break;

                            default:
                                break;
                        }

                    }
                }).
                setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "权限未获取到，请用户进入设置手动获取", Toast.LENGTH_LONG).show();
                    }
                }).
                create();
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        LogUtil.i(TAG(), "[onRequestPermissionsResult]  requestCode = " + requestCode + " permissions = " + permissions + "  grantResults =" + grantResults);
        switch (requestCode) {
            case REQUEST_READ:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LogUtil.d(TAG(), "[onRequestPermissionsResult] 已经拥有读取权限");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            MusicSys.getInstance().initMusicList(getApplicationContext(), true, false);
                            MusicPlayer.getInstance().update();
                            Intent intent = new Intent(MusicConst.ACTION_UPDATE_ALL_MUSIC_LIST);
                            intent.putExtra(MusicConst.CHANGE_FROM_OUTSIDE, false);
                            getApplicationContext().sendBroadcast(intent);
                            Message msg = Message.obtain();
                            msg.what = MESSAGE_FIRST_INIT;
                            handler.sendMessage(msg);
                        }
                    }).start();
                } else {
                    LogUtil.d(TAG(), "[onRequestPermissionsResult] 获取读取权限失败");
                    Toast.makeText(MainActivity.this, "获取读取权限失败，请去设置界面手动获取", Toast.LENGTH_LONG).show();
                }

            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mWeatherInfo != null)
                        return;
                    initSdk();
                    mLocationClient.start();
                    LogUtil.d(TAG(), "[onRequestPermissionsResult] 获取位置权限成功，开始获取地理位置");
                } else
                    LogUtil.d(TAG(), "[onRequestPermissionsResult] 获取位置权限失败");
                break;

            case REQUEST_ASK_WEATHER:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && mBDListener != null && mLocationInfo.getCity() != null) {
                    try {
                        String c = URLEncoder.encode(mLocationInfo.getCity(), "UTF-8");
                        new WeatherTask(MainActivity.this).execute(URL + c);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    LogUtil.d(TAG(), "[onRequestPermissionsResult] 开始获取天气");
                } else
                    LogUtil.d(TAG(), "[onRequestPermissionsResult] 天气的网络权限获取失败");
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    public class LocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            mLocationInfo.setAddress(bdLocation.getAddrStr());
            mLocationInfo.setCountry(bdLocation.getCountry());
            mLocationInfo.setProvince(bdLocation.getProvince());
            mLocationInfo.setCity(bdLocation.getCity());
            mLocationInfo.setDistrict(bdLocation.getDistrict());
            mLocationInfo.setStreet(bdLocation.getStreet());
            LogUtil.d("MainActivity", "地理位置 = " + mLocationInfo.getCity());
            WeatherSys.getInstance().setLocationInfo(mLocationInfo);
            if (handler != null) {
                Message msg = handler.obtainMessage(MESSAGE_LOCATION, mLocationInfo.getCity());
                msg.sendToTarget();

            }
        }
    }

    public class NetWorkStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (!needNetWork) {
                needNetWork = true;
                return;
            }
            if (mWeatherInfo != null)
                return;
            LogUtil.d(TAG(), "[NetWorkStateReceiver][onReceive] action = " + intent.getAction());
            LogUtil.d(TAG(), "[NetWorkStateReceiver][onReceive] 网络状态发生变化");
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connMgr == null) {
                LogUtil.d(TAG(), "[NetWorkStateReceiver][onReceive] ConnectivityManager == null");
                return;
            }
            Network[] networks = connMgr.getAllNetworks();
            for (Network network : networks) {
                Boolean isConnected = connMgr.getNetworkInfo(network).isConnected();
                LogUtil.d(TAG(), "[NetWorkStateReceiver][onReceive] isConnected = " + isConnected);
                if (isConnected) {
                    initSdk();
                    if (mLocationClient != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                    if (handler != null) {
                                        Message msg = handler.obtainMessage(MESSAGE_NETWORK_CHANGE);
                                        msg.sendToTarget();
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                    LogUtil.d(TAG(), "[NetWorkStateReceiver][onReceive] 检测到手机网络");
                    Toast.makeText(context, "检测到手机网络", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    }

    private void nextAnimation(View v) {
        LogUtil.d(TAG, "[nextAnimation] canStartAnimation = " + canStartAnimation);
        if (v == null || !canStartAnimation)
            return;

        if (mInterpolator == null) {
            mInterpolator = new CycleInterpolator(0.5f);
        }
        v.animate().setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                canStartAnimation = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                canStartAnimation = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                canStartAnimation = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        })
                .setInterpolator(mInterpolator)
                .translationX(100);
    }
}
