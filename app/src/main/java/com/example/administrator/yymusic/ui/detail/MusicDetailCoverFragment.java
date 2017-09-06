package com.example.administrator.yymusic.ui.detail;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.administrator.yymusic.R;
import com.example.administrator.yymusic.model.MusicInfo;
import com.example.administrator.yymusic.model.UpdateInfo;
import com.example.administrator.yymusic.sys.LruCacheSys;
import com.example.administrator.yymusic.sys.MusicPlayer;
import com.example.administrator.yymusic.ui.base.BaseFragment;
import com.example.administrator.yymusic.util.YLog;

/**
 * Created by archermind on 17-6-9.
 *
 * @author yysleep
 */
public class MusicDetailCoverFragment extends BaseFragment {

    @Override
    protected String TAG() {
        return "MusicDetailCoverFragment";
    }

    private View parentView;
    private ImageView mCoverIv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (parentView == null)
            parentView = inflater.inflate(R.layout.fragment_detail_cover, container, false);

        initView();
        return parentView;
    }

    private void initView() {
        if (parentView == null)
            return;

        mCoverIv = (ImageView) parentView.findViewById(R.id.fragment_detil_cover_iv);
        MusicInfo info = MusicPlayer.getInstance().getSongInfo();
        Bitmap bitmap = null;
        if (info != null)
            bitmap = LruCacheSys.getInstance().getBitmapFromMemoryCache(info.getUrl());
        if (bitmap != null)
            mCoverIv.setImageBitmap(bitmap);
        else
            mCoverIv.setImageResource(R.drawable.icon_default_album_art);

    }

    @Override
    public void refreshInfo(UpdateInfo info) {
        if (info == null || info.getUpdateTitle() == null)
            return;
        YLog.i(TAG(), "[refreshInfo]" + info.toString());
        Bitmap bitmap = LruCacheSys.getInstance().getBitmapFromMemoryCache(MusicPlayer.getInstance().getSongInfo().getUrl());
        if (bitmap != null)
            mCoverIv.setImageBitmap(bitmap);
        else {
            LruCacheSys.getInstance().startTask(TAG(), info.getUrl());
        }

    }

    @Override
    public void getBmpSuccess(String url) {
        if (url == null)
            return;
        Bitmap bitmap = LruCacheSys.getInstance().getBitmapFromMemoryCache(url);
        if (bitmap != null && mCoverIv != null)
            mCoverIv.setImageBitmap(bitmap);
    }

    @Override
    public void getBmpFaild() {
        if (mCoverIv != null)
            mCoverIv.setImageResource(R.drawable.icon_default_album_art);
    }

    @Override
    public void refreshDeleteFile(Boolean result) {

    }
}
