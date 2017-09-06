package com.example.administrator.yymusic.ui.main;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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
import android.util.Log;

import com.example.administrator.yymusic.R;
import com.example.administrator.yymusic.common.MusicConst;
import com.example.administrator.yymusic.modle.MusicInfo;
import com.example.administrator.yymusic.modle.UpdateInfo;
import com.example.administrator.yymusic.service.MusicService;
import com.example.administrator.yymusic.sys.MusicPlayer;
import com.example.administrator.yymusic.sys.MusicSys;
import com.example.administrator.yymusic.tool.TapPagerAdapter;
import com.example.administrator.yymusic.ui.base.BaseActivity;
import com.example.administrator.yymusic.util.ShareUtil;
import com.example.administrator.yymusic.util.YLog;
import com.example.administrator.yymusic.widget.CircularProgressView;
import com.example.administrator.yymusic.ui.detail.MusicDetailActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import java.util.List;


public class MainActivity extends BaseActivity {
    private TextView tvSongTitle;
    private CircularProgressView cpProgress;
    ;
    private ImageView ivPlay;
    //    private ImageView ivPlayMode;
    public boolean isOutSide;
    Intent intent;
    Boolean isContinue;
    MusicInfo info;
    int progress;
    MusicPlayer instance;
    MusicHandler handler;
    ShareUtil shareUtil;
    static final int REQUEST_WRITE = 200;
    static final int REQUEST_READ = 201;

    private static final int NONE_P = 0;
    private static final int WRITE_P = 1;
    private static final int READ_P = 2;
    private static final int BOTH_P = 3;

    int type = NONE_P;


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
        init();
        isOutSide = true;
        if (shareUtil.getWritePermission()) {
            type = WRITE_P;
             ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE);
        }
        if (shareUtil.getReadPermission()) {
             ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ);
            if (type == WRITE_P)
                type = BOTH_P;
            else
                type = READ_P;
        }
        if (type != BOTH_P)
            showAlert();

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

    public void init() {
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
        String title = MusicPlayer.getInstance().getSongTitle();
        info = ShareUtil.getInstance().getSongInfo();

        if (title == null && info != null) {
            progress = ShareUtil.getInstance().getProgress();
            tvSongTitle.setText(info.getTitle());
            ivPlay.setImageResource(R.drawable.ic_music_play);
            isContinue = true;
            info.setIsPlaying(1);

        } else {
            progress = 0;
            isContinue = false;
            tvSongTitle.setText("快去听歌吧");
        }
        cpProgress.setProgress(progress);

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
    }

    public void onClickMain(View v) {

        switch (v.getId()) {
            case R.id.main_play_iv:
                if (instance.getSongInfo() != null) {
                    isContinue = false;
                }
                if (isContinue && info != null) {
                    instance.startMusic(this, MusicSys.getInstance().getPosition(0, info), 0);
                    instance.pause();
                    int musicMax = instance.getMediaPlayer().getDuration();
                    YLog.i(TAG(), "[onClickMain] progress = " + progress + " ---  seekBarMax = " + 100 + " ---  musicMax= " + musicMax);
                    instance.getMediaPlayer().seekTo(musicMax * progress / 100);
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
                } else {
                    MusicPlayer.getInstance().continuePlay();
                    ivPlay.setImageResource(R.drawable.ic_music_stop);
                    MusicPlayer.isPauseByMyself = false;
                }
                break;
            case R.id.main_next_iv:
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
    public void getBmpSuccess(String url) {
        // nothing
    }

    static class MusicHandler extends Handler {
        private final WeakReference<MainActivity> mainActivity;

        public MusicHandler(MainActivity activity) {
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
                    default:
                        break;
                }
            }
        }
    }

    class MusicThread implements Runnable {

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

    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).
                setTitle("权限说明").
                setMessage("开启访问音频文件的权限").
                setIcon(R.drawable.icon_launcher).
                setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE);
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ);
                    }
                }).
                setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this,"权限未获取到，请用户进入设置手动获取",Toast.LENGTH_LONG).show();
                    }
                }).
                create();
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        YLog.i(TAG(), "[onRequestPermissionsResult]  requestCode = " + requestCode + " permissions = " + permissions + "  grantResults =" + grantResults);
        switch (requestCode) {
            case REQUEST_WRITE:
                if (!shareUtil.getWritePermission())
                    shareUtil.saveWritePermission();
                break;

            case REQUEST_READ:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MusicSys.getInstance().initMusicList(getApplicationContext());
                        MusicPlayer.getInstance().update();
                        getApplicationContext().sendBroadcast(new Intent(MusicConst.ACTION_UPDATE_ALL_MUSIC_LIST));
                    }
                }).start();
                if (!shareUtil.getReadPermission())
                    shareUtil.saveReadPermission();

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }
}
