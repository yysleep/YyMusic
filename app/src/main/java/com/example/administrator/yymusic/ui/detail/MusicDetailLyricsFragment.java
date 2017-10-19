package com.example.administrator.yymusic.ui.detail;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.administrator.yymusic.R;
import com.example.administrator.yymusic.model.UpdateInfo;
import com.example.administrator.yymusic.ui.base.BaseFragment;
import com.example.administrator.yymusic.util.YLog;

/**
 * Created by archermind on 17-6-9.
 *
 * @author yysleep
 */
public class MusicDetailLyricsFragment extends BaseFragment {

    @Override
    protected String TAG() {
        return "MusicDetailCoverFragment";
    }

    private View parentView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (parentView == null)
            parentView = inflater.inflate(R.layout.fragment_detail_lyrics, container, false);

        initView();
        return parentView;
    }

    private void initView() {
        if (parentView == null)
            return;


    }

    @Override
    public void refreshInfo(UpdateInfo info) {
        if (info == null || info.getUpdateTitle() == null)
            return;
        YLog.i(TAG(), "[refreshInfo]" + info.toString());


    }

    @Override
    public void getBmpSuccess(Bitmap cover, String url) {


    }

    @Override
    public void getBmpFailed() {

    }

    @Override
    public void refreshDeleteFile(Boolean result) {

    }
}
