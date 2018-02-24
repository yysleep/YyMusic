package com.example.administrator.yymusic.sys;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.administrator.yymusic.dao.FavoriteDao;
import com.example.administrator.yymusic.dao.MusicDBMgr;
import com.example.administrator.yymusic.model.MusicInfo;
import com.example.administrator.yymusic.util.ShareUtil;
import com.example.administrator.yymusic.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/22.
 *
 * @author yysleep
 */
public class MusicSys {

    private MusicSys() {

    }

    private static MusicSys instance;

    public static MusicSys getInstance() {
        if (instance == null) {
            synchronized (MusicSys.class) {
                if (instance == null) {
                    instance = new MusicSys();
                }
            }
        }
        return instance;
    }

    public void onDestroy() {
        instance = null;
    }

    private List<MusicInfo> localMusics;

    // 本地音乐列表
    public List<MusicInfo> getLocalMusics() {
        return localMusics;
    }

    private List<MusicInfo> collectMusics = new ArrayList<>();

    // 本地音乐列表
    public List<MusicInfo> getCollectMusics() {
        return collectMusics;
    }

    private List<MusicInfo> discoverMusics = new ArrayList<>();

    public List<MusicInfo> getDiscoverMusics() {
        return discoverMusics;
    }

    public int getPosition(int fragment, MusicInfo info) {
        List<MusicInfo> list;
        switch (fragment) {
            case 1:
                list = collectMusics;
                break;
            case 2:
                list = discoverMusics;
                break;

            default:
                list = localMusics;
                break;
        }
        if (list == null)
            return 0;

        for (int i = 0; i < list.size(); i++) {
            if (info.getMusicId() == list.get(i).getMusicId()) {
                return i;
            }
        }
        return 0;
    }


    // 初始化数据库所有音乐
    public synchronized void initMusicList(Context context, boolean firstInit, boolean outsideInit) {
        String url = null;
        MusicInfo info = ShareUtil.getInstance().getSongInfo();
        if (localMusics != null) {
            if (outsideInit && localMusics.size() > 0 && MusicPlayer.getInstance().isStarted()) {
                MusicInfo i = MusicPlayer.getInstance().getSongInfo();
                if (i != null)
                    url = i.getUrl();
                i = null;
            }

            localMusics = null;
        }
        localMusics = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                return;
            }

            do {

                // 歌曲的总播放时长 ：MediaStore.Audio.Media.DURATION
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                // 歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));

                // 如果音乐小于60秒或者小于100KB的自动过滤
                if ((duration / 60000) < 1 || size < (100 * 1024)) {
                    continue;
                }

                // 歌曲ID：MediaStore.Audio.Media._ID
                long id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));

                long albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

                // 歌曲的名称 ：MediaStore.Audio.Media.TITLE
                String tilte = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));

                String dis_name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));

                // 歌曲的专辑名：MediaStore.Audio.Media.ALBUM
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));

                // 歌曲的歌手名： MediaStore.Audio.Media.ARTIST
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));

                // 歌曲文件的路径 ：MediaStore.Audio.Media.DATA
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

                MusicInfo musicInfo = new MusicInfo(0);
                musicInfo.setAlbum(album);
                musicInfo.setMusicId(id);
                musicInfo.setAlbumId(albumId);
                musicInfo.setTitle(tilte);
                musicInfo.setDuration(duration);
                musicInfo.setSize(size);
                musicInfo.setArtist(artist);
                musicInfo.setUrl(path);
                musicInfo.setDis_name(dis_name);

                if (outsideInit && url != null && MusicPlayer.getInstance().getFragmentNum() == 0 && url.equals(path)) {
                    musicInfo.setIsPlaying(MusicInfo.IS_PLAYING);
                    MusicPlayer.getInstance().changeFragmentNum(0);
                    outsideInit = false;
                    firstInit = false;
                } else if (firstInit && info != null && info.getFragmentNum() == 0 && info.getUrl().equals(path)) {
                    musicInfo.setIsPlaying(MusicInfo.IS_PLAYING);
                    MusicPlayer.getInstance().changeFragmentNum(0);
                    firstInit = false;
                    outsideInit = false;
                }
                LogUtil.i("yymusic", "[initMusicList] 本地music 数据 musicInfo = " + musicInfo);
                localMusics.add(musicInfo);
            } while (cursor.moveToNext());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        List<MusicInfo> locals = MusicDBMgr.getInstance().query(FavoriteDao.TABLE_FAVORITE_MUSIC, firstInit, outsideInit);
        if (locals != null && locals.size() > 0)
            collectMusics = locals;

    }


    // 从外界直接点击音乐文件开始播放
    public int getFileMusicPosition(Activity activity) {
        Uri uri = activity.getIntent().getData();
        String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = activity.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        if (data != null) {
            return getPositionWithName(data.substring(data.lastIndexOf("/") + 1, data.length()));

        }
        return -3;
    }

    private int getPositionWithName(String musicName) {
        int position = 0;
        if (localMusics == null || localMusics.size() == 0) {
            return -3;
        }
        for (MusicInfo musicInfo : localMusics) {
            if (musicInfo.getDis_name().equals(musicName)) {
                return position;
            }
            position++;
        }

        return -3;
    }


    /**
     * 获得指定大小的bitmap
     */
    private Bitmap loadBitmap(String uri, int length) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 仅获取大小
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri, options);
        int maxLength = options.outWidth > options.outHeight ? options.outWidth : options.outHeight;
        // 压缩尺寸，避免卡顿
        int inSampleSize = maxLength / length;
        if (inSampleSize < 1) {
            inSampleSize = 1;
        }
        options.inSampleSize = inSampleSize;
        // 获取bitmap
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(uri, options);
    }

    public boolean checkIsPalying(String path) {
        if (localMusics != null && localMusics.size() > 0) {
            for (MusicInfo info : localMusics) {
                if (info.getUrl().equals(path) && info.getIsPlaying() == MusicInfo.IS_PLAYING)
                    return true;
            }
        }
        if (collectMusics != null && collectMusics.size() > 0) {
            for (MusicInfo info : collectMusics) {
                if (info.getUrl().equals(path) && info.getIsPlaying() == MusicInfo.IS_PLAYING)
                    return true;
            }
        }

        if (discoverMusics != null && discoverMusics.size() > 0) {
            for (MusicInfo info : discoverMusics) {
                if (info.getUrl().equals(path) && info.getIsPlaying() == MusicInfo.IS_PLAYING)
                    return true;
            }
        }
        return false;
    }

}
