package com.example.administrator.yymusic.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.administrator.yymusic.api.ITaskCallback;
import com.example.administrator.yymusic.modle.MusicInfo;
import com.example.administrator.yymusic.modle.UpdateInfo;
import com.example.administrator.yymusic.sys.MusicPlayer;
import com.example.administrator.yymusic.sys.MusicSys;
import com.example.administrator.yymusic.sys.NotificationSys;
import com.example.administrator.yymusic.utils.ShareUtils;

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
        Log.i(TAG, "[YYMusic][MusicService] onCreate");
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
        ShareUtils.getInstance().saveSongInfo();
        notificationSys.onDestroy(this);
        MusicPlayer.getInstance().unregisMusicObserver(TAG);
        MusicPlayer.getInstance().onDestroy();
        Log.i(TAG, "[YYMusic][MusicService] onDestroy" );
        super.onDestroy();
    }

    @Override
    public void refreshInfo(UpdateInfo info) {
        if (info == null || info.getUpdateTitle() == null)
            return;

        if (musicInfos != null && musicInfos.size() > 0) {
            int position = info.getUpdatePosition();
            int fragmentNum = info.getUpdateFragmentNum();
            Log.i(TAG, "[yymusic][MusicService][getBmpSuccess] position = " + position + " fragmentNum = " + fragmentNum);
            notificationSys.notify(info.getUpdateTitle());
        }

    }

}
