package com.example.administrator.yymusic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.administrator.yymusic.common.MusicConst;
import com.example.administrator.yymusic.modle.MusicInfo;
import com.example.administrator.yymusic.sys.MusicPlayer;
import com.example.administrator.yymusic.sys.MusicSys;

import java.util.List;

/**
 * Created by archermind on 17-4-18.
 *
 * @author yysleep
 */
public class ShareUtils {

    private static final String  TAG = "ShareUtils";

    private ShareUtils() {

    }

    private SharedPreferences.Editor editor;
    private SharedPreferences sp;

    private static volatile ShareUtils instance;

    public static ShareUtils getInstance() {

        if (instance == null) {
            synchronized (ShareUtils.class) {
                if (instance == null) {
                    instance = new ShareUtils();
                }

            }
        }

        return instance;
    }

    public void init(Context context) {
        final int MODE_PRIVATE = 0x0000;
        sp = context.getSharedPreferences(MusicConst.PLAY_MODE, MODE_PRIVATE);
        editor = sp.edit();
        editor.apply();
    }

    public void saveSongInfo() {
        MusicPlayer instance = MusicPlayer.getInstance();
        if (instance.getSongInfo() == null)
            return;

        String titile = instance.getSongTitle();
        long id = instance.getSongId();
        if (titile == null) {
            titile = "快去听歌吧";
        }
        Log.i("ShareUtils", "[YYMusic][ShareUtils] saveSongInfo title = " + titile);
        int currentPosition = instance.getMediaPlayer().getCurrentPosition();
        int max = instance.getMediaPlayer().getDuration();
        int progress = (int) ((float) currentPosition * 100 / max);
        editor.putString(MusicConst.SONG_TITLE, titile);
        editor.putLong(MusicConst.SONG_ID, id);
        editor.putInt(MusicConst.PROGRESS, progress);

        editor.apply();
    }

    public void savePlayModeInfo() {
        MusicPlayer instance = MusicPlayer.getInstance();
        editor.putInt(MusicConst.PLAY_MODE, instance.getPlayMode());
        editor.apply();
    }

    public void saveWritePermission() {
        editor.putBoolean(MusicConst.WRITE_PERMISSION, true);
        editor.apply();
    }

    public boolean getWritePermission() {
        return sp.getBoolean(MusicConst.WRITE_PERMISSION, false);
    }

    public void saveReadPermission() {
        editor.putBoolean(MusicConst.READ_PERMISSION, true);
        editor.apply();
    }

    public boolean getReadPermission() {
        return sp.getBoolean(MusicConst.READ_PERMISSION, false);
    }

    public int getPlayMode() {
        return sp.getInt(MusicConst.PLAY_MODE, MusicConst.SEQUENTIAL_PLAY);
    }

    public MusicInfo getSongInfo() {
        List<MusicInfo> list = MusicSys.getInstance().getLocalMusics();
        if (list == null || list.size() <= 0)
            return null;

        String title = sp.getString(MusicConst.SONG_TITLE, null);
        Long id = sp.getLong(MusicConst.SONG_ID, 0);
        for (MusicInfo info : list) {
            if (title == null)
                continue;


            if (title.equals(info.getTitle()) && id == info.getId()) {

                return info;

            }
        }
        return null;
    }

    public int getProgress() {
        return sp.getInt(MusicConst.PROGRESS, 0);
    }
}
