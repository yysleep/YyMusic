package com.yy.sleep.music.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.yy.sleep.music.common.MusicConst;
import com.yy.sleep.music.model.MusicInfo;
import com.yy.sleep.music.sys.MusicPlayer;
import com.yy.sleep.music.sys.MusicSys;

import java.util.List;

/**
 * Created by archermind on 17-4-18.
 *
 * @author yysleep
 */
public class ShareUtil {

    private static final String TAG = "ShareUtil";

    private ShareUtil() {

    }

    private SharedPreferences.Editor editor;
    private SharedPreferences sp;

    private static volatile ShareUtil instance;

    public static ShareUtil getInstance() {

        if (instance == null) {
            synchronized (ShareUtil.class) {
                if (instance == null) {
                    instance = new ShareUtil();
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
        LogUtil.i("ShareUtil", "[saveSongInfo] title = " + titile);
        int currentPosition = instance.getMediaPlayer().getCurrentPosition();
        int max = instance.getMediaPlayer().getDuration();
        int progress = (int) ((float) currentPosition * 100 / max);
        int fragment = instance.getFragmentNum();
        editor.putString(MusicConst.SONG_TITLE, titile);
        editor.putInt(MusicConst.SONG_FRAGMENT, fragment);
        editor.putLong(MusicConst.SONG_ID, id);
        editor.putInt(MusicConst.PROGRESS, progress);

        editor.apply();
    }

    public void savePlayModeInfo() {
        MusicPlayer instance = MusicPlayer.getInstance();
        editor.putInt(MusicConst.PLAY_MODE, instance.getPlayMode());
        editor.apply();
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
        int fragment = sp.getInt(MusicConst.SONG_FRAGMENT, 0);
        for (MusicInfo info : list) {
            if (title == null)
                continue;


            if (title.equals(info.getTitle()) && id == info.getMusicId()) {
                info.setFragmentNum(fragment);
                return info;
            }
        }
        return null;
    }

    public int getProgress() {
        return sp.getInt(MusicConst.PROGRESS, 0);
    }
}
