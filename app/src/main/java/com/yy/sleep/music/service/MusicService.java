package com.yy.sleep.music.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.yy.sleep.music.api.ITaskCallback;
import com.yy.sleep.music.model.MusicInfo;
import com.yy.sleep.music.model.UpdateInfo;
import com.yy.sleep.music.sys.MusicPlayer;
import com.yy.sleep.music.sys.MusicSys;
import com.yy.sleep.music.sys.NotificationSys;
import com.yy.sleep.music.util.LogUtil;
import com.yy.sleep.music.util.ShareUtil;

import java.util.List;

/**
 * Created by Administrator on 2016/5/22.
 * @author yysleep
 */
public class MusicService extends Service implements ITaskCallback {
    private List<MusicInfo> musicInfos;
    private NotificationSys notificationSys;
    private String TAG = "MusicService";
    private MusicPlayer instance;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance=MusicPlayer.getInstance();
        LogUtil.i(TAG, "[onCreate]");
        MusicPlayer.getInstance().registMusicObserver(TAG, this);
        musicInfos = MusicSys.getInstance().getLocalMusics();
        notificationSys = NotificationSys.getInstance();
        notificationSys.onCreate(MusicService.this);
    }

    @Override
    public void onDestroy() {
        if(instance.isPlaying()){
            instance.getMediaPlayer().stop();
        }
        ShareUtil.getInstance().saveSongInfo();
        notificationSys.onDestroy(this);
        MusicPlayer.getInstance().unregisMusicObserver(TAG);
        MusicPlayer.getInstance().onDestroy();
        LogUtil.i(TAG, "[onDestroy]" );
        super.onDestroy();
    }

    @Override
    public void refreshInfo(UpdateInfo info) {
        if (info == null || info.getUpdateTitle() == null)
            return;

        if (musicInfos != null && musicInfos.size() > 0) {
            int position = info.getUpdatePosition();
            int fragmentNum = info.getUpdateFragmentNum();
            LogUtil.i(TAG, "[refreshInfo] position = " + position + " fragmentNum = " + fragmentNum);
            notificationSys.notify(info.getUpdateTitle());
        }

    }

}
