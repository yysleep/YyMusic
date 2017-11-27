package com.example.administrator.yymusic.tool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.yymusic.R;
import com.example.administrator.yymusic.model.MusicInfo;
import com.example.administrator.yymusic.sys.LruCacheSys;
import com.example.administrator.yymusic.util.YLog;

import java.util.List;

/**
 * Created by Administrator on 2016/5/22.
 *
 * @author yysleep
 */
public class MusicAdapter extends BaseAdapter implements AbsListView.OnScrollListener {
    private Context mContext;
    private LruCacheSys mLruCacheSys;
    private AbsListView mView;

    public List<MusicInfo> musicInfos;

    private boolean isFristEnter = true;
    private int mFristVisibleItem;
    private int mVisibleCount;
    private String name;
    private static final String TAG = "MusicAdapter";

    public MusicAdapter(Context context, List<MusicInfo> musicInfos, AbsListView view, String name) {
        this.mContext = context;
        this.musicInfos = musicInfos;
        this.mView = view;
        mLruCacheSys = LruCacheSys.getInstance();
        view.setOnScrollListener(this);
        this.name = name;
    }

    @Override
    public int getCount() {
        return musicInfos == null ? 0 : musicInfos.size();
    }

    @Override
    public MusicInfo getItem(int i) {
        return musicInfos == null ? null : musicInfos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return musicInfos == null ? 0 : i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHodler hodler;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_music, viewGroup, false);
            hodler = new ViewHodler();
            hodler.tvSongTitle = (TextView) view.findViewById(R.id.item_song_title_tv);
            hodler.tvArtist = (TextView) view.findViewById(R.id.item_artist_tv);
            hodler.iv = (ImageView) view.findViewById(R.id.item_song_photo_iv);
            hodler.v = view.findViewById(R.id.item_v);
            view.setTag(hodler);
        } else {
            hodler = (ViewHodler) view.getTag();
        }
        if (musicInfos != null) {
            MusicInfo info = musicInfos.get(i);

            String songTitle = info.getTitle() + "";
            hodler.tvSongTitle.setText(songTitle);

            String aritist = info.getArtist() + "";
            hodler.tvArtist.setText(aritist);

            hodler.iv.setTag(info.getUrl());
            setImageView(info.getUrl(), hodler.iv);

            if (musicInfos.get(i).getIsPlaying() == 1) {
                // 正在播放的歌曲
                hodler.tvSongTitle.setTextColor(Color.argb(255, 0x63, 0xbf, 0xa7));
                hodler.tvArtist.setTextColor(Color.argb(255, 0x63, 0xbf, 0xa7));
            } else {
                // 正常状态的歌曲（未播放）
                hodler.tvSongTitle.setTextColor(Color.argb(180, 0x13, 0x13, 0x13));
                hodler.tvArtist.setTextColor(Color.argb(180, 0x13, 0x13, 0x13));
            }
        }

        return view;
    }

    private void setImageView(String url, ImageView iv) {
        Bitmap bmp = mLruCacheSys.getBitmapFromMemoryCache(url);
        if (bmp != null) {
            iv.setImageBitmap(bmp);
        } else {
            iv.setImageResource(R.drawable.icon_default_album_art);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (musicInfos.size() <= 0)
            return;

        YLog.i(TAG, "[onScrollStateChanged] scrollState = " + scrollState);
        if (scrollState == SCROLL_STATE_IDLE) {
            startTask();
        } else {
            mLruCacheSys.cancelAllTasks();
        }
    }

    public void setOutsideChange(boolean isFristEnter) {
        this.isFristEnter = isFristEnter;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mFristVisibleItem = firstVisibleItem;
        mVisibleCount = visibleItemCount;
        // YLog.i(TAG, "[onScroll] firstVisibleItem = " + firstVisibleItem + " visibleItemCount = " + visibleItemCount);
        if (isFristEnter && mVisibleCount != 0) {
            startTask();
            isFristEnter = false;
        }
    }

    public void startTask() {
        if (getCount() == 0)
            return;

        YLog.i(TAG, "[startTask] musicInfos.size = " + musicInfos.size());
        for (int i = mFristVisibleItem; i < mFristVisibleItem + mVisibleCount; i++) {
            if (i >= musicInfos.size())
                return;

            String url = musicInfos.get(i).getUrl();
            if (mLruCacheSys.getBitmapFromMemoryCache(url) != null) {
                downLoadSuccess(url);
                continue;
            }
            mLruCacheSys.startTask(name, url, BitmapDownLoadTask.Type.Thumbnails);
        }
    }

    public void downLoadSuccess(String url) {
        if (url != null) {
            YLog.i(TAG, "[downLoadSuccess] url = " + url);
            ImageView iv = (ImageView) mView.findViewWithTag(url);
            if (iv == null) {
                YLog.i(TAG, "[downLoadSuccess] iv is null ");
                return;
            }
            setImageView(url, iv);
        }
    }

    public void clear() {
        mLruCacheSys.unRegisterMusicObserver(this.toString());
        mContext = null;
        mView = null;
        mLruCacheSys = null;
    }

    private static class ViewHodler {
        // 歌曲封面
        ImageView iv;
        // 歌名
        TextView tvSongTitle;
        // 歌手名
        TextView tvArtist;

        View v;
    }

}
