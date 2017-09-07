package com.example.administrator.yymusic.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.administrator.yymusic.model.MusicInfo;
import com.example.administrator.yymusic.model.YMBaseModel;
import com.example.administrator.yymusic.sys.MusicPlayer;
import com.example.administrator.yymusic.util.ShareUtil;
import com.example.administrator.yymusic.util.YLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by archermind on 17-9-6.
 *
 * @author yysleep
 */

public class FavoriteDao extends YMBaseDao {

    private static final String TAG = "FavoriteDao";

    public static final String TABLE_FAVORITE_MUSIC = "favorite";

    public static final String COL_ID = "_id";
    public static final String COL_FRAGMENT = "fragment_num";
    public static final String COL_TITLE = "title";
    public static final String COL_DIS_NAME = "dis_name";
    public static final String COL_ALBUM = "album";
    public static final String COL_MUSIC_ID = "music_id";
    public static final String COL_ALBUM_ID = "albumId";
    public static final String COL_DURATION = "duration";
    public static final String COL_SIZE = "size";
    public static final String COL_ARTIST = "artist";
    public static final String COL_URL = "url";
    public static final String COL_IS_PLAYING = "isPlaying";

    public static final String SQL_TABLE_FAVORITE = " create table "
            + TABLE_FAVORITE_MUSIC + " ("
            + COL_ID + " integer primary key autoincrement, "
            + COL_FRAGMENT + " int, "
            + COL_TITLE + " varchar(128), "
            + COL_DIS_NAME + " varchar(128), "
            + COL_ALBUM + " varchar(128), "
            + COL_MUSIC_ID + " long, "
            + COL_ALBUM_ID + " long, "
            + COL_DURATION + " long, "
            + COL_SIZE + " long, "
            + COL_ARTIST + " varchar(128), "
            + COL_URL + " varchar(256), "
            + COL_IS_PLAYING + " int) ";

    private static volatile FavoriteDao instance;

    private FavoriteDao() {

    }

    public static FavoriteDao getInstance() {
        if (instance == null) {
            synchronized (FavoriteDao.class) {
                if (instance == null)
                    instance = new FavoriteDao();
            }
        }
        return instance;
    }

    @Override
    public void init(SQLiteDatabase db) {
        super.init(db);
        db.execSQL(" drop table if exists " + TABLE_FAVORITE_MUSIC + ";");
        db.execSQL(SQL_TABLE_FAVORITE);
    }

    @Override
    public void upgrade() {
        super.upgrade();
    }

    @Override
    public void insert(SQLiteDatabase db, YMBaseModel modle) {
        super.insert(db, modle);
        if (modle instanceof MusicInfo) {
            MusicInfo info = (MusicInfo) modle;
            ContentValues values = new ContentValues();
            values.put(COL_FRAGMENT, info.getFragmentNum());
            values.put(COL_TITLE, info.getTitle());
            values.put(COL_DIS_NAME, info.getDis_name());
            values.put(COL_ALBUM, info.getAlbum());
            values.put(COL_MUSIC_ID, info.getMusicId());
            values.put(COL_ALBUM_ID, info.getAlbumId());
            values.put(COL_DURATION, info.getDuration());
            values.put(COL_SIZE, info.getSize());
            values.put(COL_ARTIST, info.getArtist());
            values.put(COL_URL, info.getUrl());
            values.put(COL_IS_PLAYING, info.getIsPlaying());

            db.insert(TABLE_FAVORITE_MUSIC, null, values);
        }
    }

    @Override
    public void update(SQLiteDatabase db, YMBaseModel model) {
        super.insert(db, model);
        if (model instanceof MusicInfo) {
            MusicInfo info = (MusicInfo) model;
            ContentValues values = new ContentValues();
            values.put(COL_IS_PLAYING, info.getIsPlaying());
            String whereClause = COL_ID + " = ? and " + COL_URL + " = ?";
            String[] whereArgs = new String[]{String.valueOf(info.getId()), info.getUrl()};
            db.update(TABLE_FAVORITE_MUSIC, values, whereClause, whereArgs);
        }
    }

    @Override
    public void delete(SQLiteDatabase db, YMBaseModel model) {
        super.delete(db, model);
        if (model instanceof MusicInfo) {
            MusicInfo info = (MusicInfo) model;
            YLog.d(TAG, "[delete] 正在删除 title = " + info.getTitle() + " url = " + info.getUrl());
            db.delete(TABLE_FAVORITE_MUSIC, COL_TITLE + " = ? and " + COL_URL + " = ?",
                    new String[]{info.getTitle(), info.getUrl()});
        }
    }

    @Override
    public List<MusicInfo> query(SQLiteDatabase db, boolean firstInit, boolean outside) {
        if (db == null)
            return null;
        Cursor cursor = db.rawQuery("select * from " + TABLE_FAVORITE_MUSIC, null);
        if (cursor == null)
            return null;

        String url = null;
        MusicInfo info = ShareUtil.getInstance().getSongInfo();
        if (outside && MusicPlayer.getInstance().isStarted()) {
            MusicInfo i = MusicPlayer.getInstance().getSongInfo();
            if (i != null)
                url = i.getUrl();
            i = null;
        }

        List<MusicInfo> infos = new ArrayList<>();
        while (cursor.moveToNext()) {
            MusicInfo i = new MusicInfo(cursor.getInt(cursor.getColumnIndex(COL_FRAGMENT)));
            i.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
            if (i.getId() <= 0)
                continue;
            i.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
            i.setDis_name(cursor.getString(cursor.getColumnIndex(COL_DIS_NAME)));
            i.setAlbum(cursor.getString(cursor.getColumnIndex(COL_ALBUM)));
            i.setMusicId(cursor.getLong(cursor.getColumnIndex(COL_MUSIC_ID)));
            i.setAlbumId(cursor.getLong(cursor.getColumnIndex(COL_ALBUM_ID)));
            i.setDuration(cursor.getLong(cursor.getColumnIndex(COL_DURATION)));
            i.setSize(cursor.getLong(cursor.getColumnIndex(COL_SIZE)));
            i.setArtist(cursor.getString(cursor.getColumnIndex(COL_ARTIST)));
            String path = cursor.getString(cursor.getColumnIndex(COL_URL));
            i.setUrl(path);
            i.setIsPlaying(cursor.getInt(cursor.getColumnIndex(COL_IS_PLAYING)));
            if (outside && url != null && MusicPlayer.getInstance().getFragmentNum() == 1 && url.equals(path)) {
                i.setIsPlaying(MusicInfo.IS_PLAYING);
                MusicPlayer.getInstance().changeFragmentNum(1);
                firstInit = false;
                outside = false;
            } else if (firstInit && info != null && info.getFragmentNum() == 1 && info.getUrl().equals(path)) {
                i.setIsPlaying(MusicInfo.IS_PLAYING);
                MusicPlayer.getInstance().changeFragmentNum(1);
                outside = false;
                firstInit = false;
            }
            YLog.d(TAG, " [query] db info = " + i.toString());
            infos.add(i);
        }
        cursor.close();
        if (infos.size() == 0)
            return null;
        return infos;

    }
}
