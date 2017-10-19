package com.example.administrator.yymusic.ui.main;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.support.design.widget.TabLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.yymusic.R;
import com.example.administrator.yymusic.common.MusicConst;
import com.example.administrator.yymusic.model.MusicInfo;
import com.example.administrator.yymusic.model.UpdateInfo;
import com.example.administrator.yymusic.model.WeatherInfo;
import com.example.administrator.yymusic.service.MusicService;
import com.example.administrator.yymusic.sys.MusicPlayer;
import com.example.administrator.yymusic.sys.MusicSys;
import com.example.administrator.yymusic.tool.TapPagerAdapter;
import com.example.administrator.yymusic.tool.WeatherTask;
import com.example.administrator.yymusic.ui.base.BaseActivity;
import com.example.administrator.yymusic.util.YYConstant;
import com.example.administrator.yymusic.util.ShareUtil;
import com.example.administrator.yymusic.util.YLog;
import com.example.administrator.yymusic.widget.CircularProgressView;
import com.example.administrator.yymusic.ui.detail.MusicDetailActivity;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import java.util.List;


public class MainActivity extends BaseActivity implements WeatherTask.ITaskWeather {
    private TextView tvSongTitle;
    private CircularProgressView cpProgress;
    private ImageView ivPlay;

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
    static final int REQUEST_WRITE = 300;
    static final int REQUEST_READ = 301;
    static final int REQUEST_INTENT = 400;

    static final int FRIST_INIT = 1000;

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
        initToolBar();
        intent = new Intent(MainActivity.this, MusicService.class);
        startService(intent);
        initView();
        isOutSide = true;
        checkPermission();
    }


    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            YLog.d(TAG(), "[checkPermission] 已经拥有读取权限");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MusicSys.getInstance().initMusicList(getApplicationContext(), true, false);
                    MusicPlayer.getInstance().update();
                    Intent intent = new Intent(MusicConst.ACTION_UPDATE_ALL_MUSIC_LIST);
                    intent.putExtra(MusicConst.CHANGE_FROM_OUTSIDE, false);
                    getApplicationContext().sendBroadcast(intent);
                    Message msg = Message.obtain();
                    msg.what = FRIST_INIT;
                    handler.sendMessage(msg);
                }
            }).start();
        } else {
            showAlert(YYConstant.READ_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            try {
                String c = URLEncoder.encode("南京", "UTF-8");
                new WeatherTask(MainActivity.this).execute(URL + c);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, REQUEST_WRITE);

    }

    void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("音乐");
        setSupportActionBar(toolbar);
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
        ViewPager viewPager = (ViewPager) findViewById(R.id.main_music_vp);
        if (viewPager != null) {
            viewPager.setAdapter(new TapPagerAdapter(getSupportFragmentManager(), getFragments(), true));
            viewPager.setCurrentItem(0);
        }
        TabLayout tableLayout = (TabLayout) findViewById(R.id.main_tabs);
        if (tableLayout != null) {
            tableLayout.setupWithViewPager(viewPager);
        }

        tvSongTitle = (TextView) findViewById(R.id.main_music_song_title_cv);
        cpProgress = (CircularProgressView) findViewById(R.id.main_music_progress_cv);
        ivPlay = (ImageView) findViewById(R.id.main_play_iv);

        tvWeather = (TextView) findViewById(R.id.main_drawer_weather_tv);
        tvTemperature = (TextView) findViewById(R.id.main_drawer_temperature_tv);
        tvPower = (TextView) findViewById(R.id.main_drawer_power_tv);
        tvDetail = (TextView) findViewById(R.id.main_drawer_detail_tv);
//        ivPlayMode = (ImageView) findViewById(R.id.main_play_mode_iv);

//        ivPlayMode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switch (MusicPlayer.getInstance().getPlayMode()) {
//                    case MusicConst.SEQUENTIAL_PLAY:
//                        MusicPlayer.getInstance().setPlayMode(MusicConst.RANDOM_PLAY);
//                        ivPlayMode.setImageResource(R.drawable.ic_random_play);
//                        break;
//
//                    case MusicConst.RANDOM_PLAY:
//                        MusicPlayer.getInstance().setPlayMode(MusicConst.SINGLE_PLAY);
//                        ivPlayMode.setImageResource(R.drawable.ic_single_play);
//                        break;
//
//                    case MusicConst.SINGLE_PLAY:
//                        MusicPlayer.getInstance().setPlayMode(MusicConst.SEQUENTIAL_PLAY);
//                        ivPlayMode.setImageResource(R.drawable.ic_sequential_play);
//                        break;
//
//                    default:
//                        MusicPlayer.getInstance().setPlayMode(MusicConst.SEQUENTIAL_PLAY);
//                        ivPlayMode.setImageResource(R.drawable.ic_sequential_play);
//                        break;
//                }
//            }
//        });
        handler = new MusicHandler(this);
        new Thread(new MusicThread()).start();
        initMode(ShareUtil.getInstance().getPlayMode());
    }

    private List<Fragment> getFragments() {
        List<Fragment> fragments = new ArrayList<>();
        MusicDiscoverFragment fragment3 = new MusicDiscoverFragment();
        MusicCollectFragment fragment2 = new MusicCollectFragment();
        MusicLocalFragment fragment1 = new MusicLocalFragment(fragment2);

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

    public void onClickMain(View v) {

        switch (v.getId()) {
            case R.id.main_play_iv:
                if (isContinue && mInfo != null) {
                    instance.startMusic(this, MusicSys.getInstance().getPosition(mInfo.getFragmentNum(), mInfo), mInfo.getFragmentNum());
                    instance.pause();
                    int musicMax = instance.getMediaPlayer().getDuration();
                    YLog.i(TAG(), "[onClickMain] mProgress = " + mProgress + " ---  seekBarMax = " + 100 + " ---  musicMax= " + musicMax);
                    instance.getMediaPlayer().seekTo(musicMax * mProgress / 100);
                    instance.continuePlay();
                    ivPlay.setImageResource(R.drawable.ic_music_stop);
                    isContinue = false;
                    MusicPlayer.isPauseByMyself = false;
                    return;
                }
                if (instance.isPlaying()) {
                    instance.pause();
                    ivPlay.setImageResource(R.drawable.ic_music_play);
                    MusicPlayer.isPauseByMyself = true;
                } else if (instance.isStarted()) {
                    MusicPlayer.getInstance().continuePlay();
                    ivPlay.setImageResource(R.drawable.ic_music_stop);
                    MusicPlayer.isPauseByMyself = false;
                } else {
                    Toast.makeText(this, " 请选择一首歌曲吧", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.main_next_iv:
                if (isContinue && mInfo != null) {
                    instance.startMusic(this, MusicSys.getInstance().getPosition(mInfo.getFragmentNum(), mInfo), mInfo.getFragmentNum());
                    instance.nextMusic();
                    isContinue = false;
                    break;
                }
                if (!instance.isStarted()) {
                    startActivity(new Intent(MainActivity.this, MusicDetailActivity.class));
                    break;
                }

                if (instance.isPlaying()) {
                    ivPlay.setImageResource(R.drawable.ic_music_stop);
                    MusicPlayer.isPauseByMyself = false;
                }
                instance.nextMusic();
                break;

            case R.id.main_drawer_detail_tv:
                // startActivity(new Intent(MainActivity.this, MusicDetailActivity.class));
                break;

            case R.id.main_drawer:
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

    @Override
    public void refreshInfo(UpdateInfo info) {
        if (info == null || info.getUpdateTitle() == null)
            return;

        if (tvSongTitle == null)
            return;

        tvSongTitle.setText(info.getUpdateTitle());
        if (instance.isPlaying()) {
            ivPlay.setImageResource(R.drawable.ic_music_stop);
        }
    }

    @Override
    public void getBmpSuccess(Bitmap cover, String url) {
        // todo
    }

    @Override
    public void weatherRespond(WeatherInfo info) {
        if (info == null || info.getData() == null)
            return;
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
                    case FRIST_INIT:
                        activity.mInfo = ShareUtil.getInstance().getSongInfo();
                        YLog.d(activity.TAG(), "[handleMessage] info = " + activity.mInfo);
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
        YLog.i(TAG(), "[onDestroy]");
        if (!instance.isPlaying()) {
            stopService(intent);
        }
    }

    public void showAlert(final int type) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).
                setTitle("权限说明").
                setMessage("开启访问音频文件的权限").
                setIcon(R.drawable.icon_launcher).
                setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (type) {
                            case YYConstant.READ_PERMISSION:
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ);
                                YLog.d(TAG(), "[showAlert] 正在申请读取权限");
                                break;

                            case YYConstant.WRITE_PERMISSON:
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE);
                                YLog.d(TAG(), "[showAlert] 正在申请删除权限");
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
        YLog.i(TAG(), "[onRequestPermissionsResult]  requestCode = " + requestCode + " permissions = " + permissions + "  grantResults =" + grantResults);
        switch (requestCode) {
            case REQUEST_READ:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    YLog.d(TAG(), "[onRequestPermissionsResult] 已经拥有读取权限");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            MusicSys.getInstance().initMusicList(getApplicationContext(), true, false);
                            MusicPlayer.getInstance().update();
                            Intent intent = new Intent(MusicConst.ACTION_UPDATE_ALL_MUSIC_LIST);
                            intent.putExtra(MusicConst.CHANGE_FROM_OUTSIDE, false);
                            getApplicationContext().sendBroadcast(intent);
                            Message msg = Message.obtain();
                            msg.what = FRIST_INIT;
                            handler.sendMessage(msg);
                        }
                    }).start();
                } else {
                    YLog.d(TAG(), "[onRequestPermissionsResult] 获取读取权限失败");
                    Toast.makeText(MainActivity.this, "获取读取权限失败，请去设置界面手动获取", Toast.LENGTH_LONG).show();
                }

            case REQUEST_WRITE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    YLog.d(TAG(), "[onRequestPermissionsResult] 已经拥有删除权限");
                    Toast.makeText(MainActivity.this, "已获得删除权限", Toast.LENGTH_LONG).show();
                } else {
                    YLog.d(TAG(), "[onRequestPermissionsResult] 获取删除权限失败");
                    Toast.makeText(MainActivity.this, "获取删除权限失败，请去设置界面手动获取", Toast.LENGTH_LONG).show();
                }
                break;

            case REQUEST_INTENT:
                try {
                    String c = URLEncoder.encode("南京", "UTF-8");
                    new WeatherTask(MainActivity.this).execute(URL + c);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }
}
