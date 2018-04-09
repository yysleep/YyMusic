package com.yy.sleep.music.ui.base;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.yy.sleep.music.api.IFileOperationCallback;
import com.yy.sleep.music.api.ITaskCallback;
import com.yy.sleep.music.api.ITaskInterface;
import com.yy.sleep.music.sys.LruCacheSys;
import com.yy.sleep.music.sys.MusicPlayer;

/**
 * Created by archermind on 17-3-30.
 *
 * @author yysleep
 */
public abstract class BaseFragment extends Fragment implements ITaskCallback ,ITaskInterface ,IFileOperationCallback{

    abstract protected String TAG();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MusicPlayer.getInstance().registMusicObserver(TAG(), this);
        LruCacheSys.getInstance().registerMusicObserver(TAG(), this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MusicPlayer.getInstance().unregisMusicObserver(TAG());
        LruCacheSys.getInstance().unRegisterMusicObserver(TAG());
    }

    @Override
    public void getBmpFailed() {

    }

    @Override
    public void refreshDeleteFile(Boolean result) {

    }

    @Override
    public void syncList(String path) {

    }
}
