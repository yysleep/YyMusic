package com.example.administrator.yymusic.ui.base;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.example.administrator.yymusic.api.IFileOperationCallback;
import com.example.administrator.yymusic.api.ITaskCallback;
import com.example.administrator.yymusic.api.ITaskInterface;
import com.example.administrator.yymusic.sys.LruCacheSys;
import com.example.administrator.yymusic.sys.MusicPlayer;

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
