package com.example.administrator.yymusic.ui.detail;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.yymusic.R;
import com.example.administrator.yymusic.common.MusicConst;
import com.example.administrator.yymusic.dao.FavoriteDao;
import com.example.administrator.yymusic.dao.MusicDBMgr;
import com.example.administrator.yymusic.modle.MusicInfo;
import com.example.administrator.yymusic.modle.UpdateInfo;
import com.example.administrator.yymusic.sys.MusicPlayer;
import com.example.administrator.yymusic.sys.MusicSys;
import com.example.administrator.yymusic.tool.TapPagerAdapter;
import com.example.administrator.yymusic.ui.base.BaseActivity;
import com.example.administrator.yymusic.util.YLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/24.
 *
 * @author yysleep
 */
public class MusicDetailActivity extends BaseActivity {
    private SeekBar seekBar;
    private TextView tvMaxTime;
    private TextView tvProgressTime;
    private TextView tvSongTitle;
    private ProgressHandler handler;
    boolean isGone;
    private ImageView ivPlay;
    private ImageView ivCollect;
    AlertDialog.Builder mDialog;
    // 是否成功移除收藏
    boolean isRemove;
    boolean isLongClicking;
    boolean isFastToStop;
    Thread thread;
    MusicRunnable runnable;
    private int mProgress;
    private MusicPlayer instance;

    @Override
    public String TAG() {
        return "MusicDetailActivity";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_dital);

        init();
        runnable = new MusicRunnable();
        thread = new Thread(runnable);
        thread.start();
        if (getIntent() != null && getIntent().getIntExtra(MusicConst.MUSIC_POSITION, -3) != -3) {
            int position = getIntent().getIntExtra(MusicConst.MUSIC_POSITION, 0);
            MusicPlayer.getInstance().startMusic(getApplicationContext(), position, 0);
            ivPlay.setImageResource(R.drawable.ic_music_stop);
        }
    }

    public void init() {
        instance = MusicPlayer.getInstance();
        ViewPager viewPager = (ViewPager) findViewById(R.id.music_detail_vp);
        if (viewPager != null) {
            viewPager.setAdapter(new TapPagerAdapter(getSupportFragmentManager(), getFragments(), false));
            viewPager.setCurrentItem(0);
        }
        mDialog = new AlertDialog.Builder(this);
        mDialog.setTitle("是否取消收藏")
                .setMessage("   ")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = MusicPlayer.getInstance().getSongTitle();
                        Long id = MusicPlayer.getInstance().getSongId();
                        isRemove = MusicPlayer.getInstance().removeMusicinfo(title);
                        if (isRemove) {
                            MusicPlayer.getInstance().changeList(MusicSys.getInstance().getLocalMusics());
                            MusicPlayer.getInstance().changeFragmentNum(0);
                            for (int i = 0; i < MusicSys.getInstance().getLocalMusics().size(); i++) {
                                MusicInfo info = MusicSys.getInstance().getLocalMusics().get(i);
                                if (info.getTitle().equals(title) && info.getMusicId() == id) {
                                    MusicPlayer.getInstance().changeSongNum(i);
                                    break;
                                }
                            }
                            ivCollect.setImageResource(R.drawable.ic_music_new_collect);
                            isRemove = false;
                        } else {
                            Toast.makeText(MusicDetailActivity.this, "取消收藏失败", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        seekBar = (SeekBar) findViewById(R.id.music_detil_progress_sk);
        tvMaxTime = (TextView) findViewById(R.id.music_detil_alltime_tv);
        tvProgressTime = (TextView) findViewById(R.id.music_detil_progress_time_tv);
        ivPlay = (ImageView) findViewById(R.id.music_detil_play_tv);
        tvSongTitle = (TextView) findViewById(R.id.music_detil_song_title_tv);

        String title = MusicPlayer.getInstance().getSongTitle();
        if (title != null) {
            tvSongTitle.setText(title);
        } else {
            tvSongTitle.setText("开始选歌吧～");
        }

        handler = new ProgressHandler(MusicDetailActivity.this);
        ivCollect = (ImageView) findViewById(R.id.music_detil_collect_iv);
        ImageView ivNext = (ImageView) findViewById(R.id.music_detil_next_tv);
        ivNext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isLongClicking = false;
                        break;

                    case MotionEvent.ACTION_UP:
                        if (isLongClicking) {
                            moveSeekbar(seekBar);
                            if (isFastToStop) {
                                isFastToStop = false;
                                ivPlay.setImageResource(R.drawable.ic_music_stop);
                                MusicPlayer.getInstance().getMediaPlayer().start();
                            }
                            isLongClicking = false;
                        }
                        break;
                }
                return false;
            }
        });
        ivNext.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!MusicPlayer.getInstance().isStarted()) {
                    return false;
                }
                isLongClicking = true;
                if (MusicPlayer.getInstance().isPlaying()) {
                    MusicPlayer.getInstance().pause();
                    ivPlay.setImageResource(R.drawable.ic_music_play);
                    isFastToStop = true;
                }
                mProgress = seekBar.getProgress();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (isLongClicking) {
                            if (handler != null) {
                                handler.sendEmptyMessage(MusicConst.MUSIC_FAST);
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                return true;
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (MusicPlayer.getInstance().isStarted()) {
                    moveSeekbar(seekBar);
                }
            }
        });
        if (MusicPlayer.getInstance().isPauseing()) {
            ivPlay.setImageResource(R.drawable.ic_music_play);
        }
        if (MusicPlayer.getInstance().isPlaying()) {
            ivPlay.setImageResource(R.drawable.ic_music_stop);
        }
        if (MusicPlayer.getInstance().isStarted()) {
            if (MusicPlayer.getInstance().checkIsCollcet()) {
                ivCollect.setImageResource(R.drawable.ic_music_detilc_collected);
            }
        }
    }

    private List<Fragment> getFragments() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new MusicDetailCoverFragment());
        fragments.add(new MusicDetailLyricsFragment());
        return fragments;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (instance.isPauseing()) {
            ivPlay.setImageResource(R.drawable.ic_music_play);
        } else if (instance.isPlaying()) {
            ivPlay.setImageResource(R.drawable.ic_music_stop);
            if (instance.getSongInfo() != null && instance.getSongTitle() != null)
                tvSongTitle.setText(instance.getSongTitle());

        }
    }

    public void onClickDetil(View v) {
        if (!MusicPlayer.getInstance().isStarted()) {
            Toast.makeText(MusicDetailActivity.this, "没有正在播放的歌曲", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()) {
            case R.id.music_detil_last_tv:
                if (!MusicPlayer.getInstance().isPlaying()) {
                    MusicPlayer.getInstance().continuePlay();
                    ivPlay.setImageResource(R.drawable.ic_music_stop);
                    MusicPlayer.isPauseByMyself = false;
                }
                MusicPlayer.getInstance().lastMusic();
                break;

            case R.id.music_detil_play_tv:
                if (MusicPlayer.getInstance().isPlaying()) {
                    MusicPlayer.getInstance().pause();
                    ivPlay.setImageResource(R.drawable.ic_music_play);
                    MusicPlayer.isPauseByMyself = true;
                } else {
                    MusicPlayer.getInstance().continuePlay();
                    ivPlay.setImageResource(R.drawable.ic_music_stop);
                    MusicPlayer.isPauseByMyself = false;

                }

                break;

            case R.id.music_detil_next_tv:
                if (!MusicPlayer.getInstance().isPlaying()) {
                    MusicPlayer.getInstance().getMediaPlayer().start();
                    ivPlay.setImageResource(R.drawable.ic_music_stop);
                    MusicPlayer.isPauseByMyself = false;
                }
                MusicPlayer.getInstance().nextMusic();
                break;

            case R.id.music_detil_collect_iv:
                boolean add = MusicPlayer.getInstance().addCollectMusic();
                if (add) {
                    ivCollect.setImageResource(R.drawable.ic_music_detilc_collected);
                } else {
                    if (mDialog != null)
                        mDialog.show();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void refreshInfo(UpdateInfo info) {
        if (info == null || info.getUpdateTitle() == null)
            return;
        YLog.i(TAG(), "[refreshInfo]" + info.toString());
        String title = MusicPlayer.getInstance().getSongTitle();
        if (title != null)
            tvSongTitle.setText(title);

        if (MusicPlayer.getInstance().checkIsCollcet()) {
            ivCollect.setImageResource(R.drawable.ic_music_detilc_collected);
        } else {
            ivCollect.setImageResource(R.drawable.ic_music_new_collect);
        }
    }

    @Override
    public void getBmpSuccess(String url) {

    }


    private static class ProgressHandler extends Handler {
        int progress = 0;
        int max;
        private final WeakReference<MusicDetailActivity> detilActivity;

        private ProgressHandler(MusicDetailActivity activity) {
            detilActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final MusicDetailActivity activity = detilActivity.get();
            if (activity != null && activity.seekBar != null && activity.tvMaxTime != null) {
                switch (msg.what) {
                    case MusicConst.MUSIC_PLAYING:
                        int currentPosition = MusicPlayer.getInstance().getMediaPlayer().getCurrentPosition();
                        max = MusicPlayer.getInstance().getMediaPlayer().getDuration();
                        progress = (int) ((float) currentPosition * 100 / max);

                        activity.tvMaxTime.setText(activity.getTime(max));
                        activity.tvProgressTime.setText(activity.getTime(currentPosition));
                        activity.seekBar.setProgress(progress);

                        if (!activity.isGone) {
                            activity.isGone = false;
                            activity.tvMaxTime.setVisibility(View.VISIBLE);
                            activity.seekBar.setVisibility(View.VISIBLE);
                            activity.tvProgressTime.setVisibility(View.VISIBLE);
                        }
                        break;

                    case MusicConst.MUSIC_FAST:
                        YLog.i(activity.TAG(), "[ProgressHandler][handleMessage] mProgress : " + activity.mProgress);
                        if (activity.mProgress < 98) {
                            activity.mProgress = activity.mProgress + 3;
                        }
                        YLog.i(activity.TAG(), "[ProgressHandler][handleMessage] : " + activity.mProgress);
                        activity.tvProgressTime.setText(activity.getTime(max * activity.mProgress / 100));
                        activity.seekBar.setProgress(activity.mProgress);

                        break;

                    case MusicConst.MUSIC_NORMAL:

                        break;

                    default:
                        break;
                }
            }
        }
    }

    private class MusicRunnable implements Runnable {

        @Override
        public void run() {
            for (; ; ) {
                try {
                    if (handler != null && MusicPlayer.getInstance().getSongInfo() != null && !isLongClicking) {
                        handler.sendEmptyMessage(MusicConst.MUSIC_PLAYING);
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // 停止拖动
    public void moveSeekbar(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        // 得到该首歌曲最长秒数
        int musicMax = MusicPlayer.getInstance().getMediaPlayer().getDuration();
        int seekBarMax = seekBar.getMax();
        YLog.i(TAG(), "[moveSeekbar] progress = " + progress + " ---  seekBarMax= " + seekBarMax + " ---  musicMax= " + musicMax);
        // 跳到该曲该秒
        MusicPlayer.getInstance().getMediaPlayer().seekTo(musicMax * progress / seekBarMax);


    }

    private String getTime(int time) {
        int min = time / 60000;
        int sec = (time / 1000) % 60;
        String sMin;
        String sSec;
        if (min >= 10) {
            sMin = String.valueOf(min);
        } else {
            sMin = "0" + min;
        }
        if (sec >= 10) {
            sSec = String.valueOf(sec);
        } else {
            sSec = "0" + sec;
        }
        return sMin + ":" + sSec;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        thread = null;
        runnable = null;
        instance = null;
    }

}
