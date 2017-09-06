package com.example.administrator.yymusic.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.administrator.yymusic.model.MusicInfo;
import com.example.administrator.yymusic.model.YMBaseModel;
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
            values.put(COL_TITLE, info.getFragmentNum());
            values.put(COL_DIS_NAME, info.getFragmentNum());
            values.put(COL_ALBUM, info.getFragmentNum());
            values.put(COL_MUSIC_ID, info.getFragmentNum());
            values.put(COL_ALBUM_ID, info.getFragmentNum());
            values.put(COL_DURATION, info.getFragmentNum());
            values.put(COL_SIZE, info.getFragmentNum());
            values.put(COL_ARTIST, info.getFragmentNum());
            values.put(COL_URL, info.getFragmentNum());
            values.put(COL_IS_PLAYING, info.getFragmentNum());

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
            YLog.d(TAG, "[delete] 正在删除 id = " + info.getId() + " url = " + info.getUrl());
            db.delete(TABLE_FAVORITE_MUSIC, COL_ID + " = ? and " + COL_URL + " = ?",
                    new String[]{String.valueOf(info.getId()), info.getUrl()});
        }
    }

    @Override
    public List<MusicInfo> query(SQLiteDatabase db) {
        if (db == null)
            return null;
        Cursor cursor = db.rawQuery("select * from " + TABLE_FAVORITE_MUSIC, null);
        if (cursor == null)
            return null;
        List<MusicInfo> infos = new ArrayList<>();
        while (cursor.moveToNext()) {
            MusicInfo info = new MusicInfo(cursor.getInt(cursor.getColumnIndex(COL_FRAGMENT)));
            info.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
            if (info.getId() <= 0)
                continue;
            info.setTitle(cursor.getString(cursor.getColumnIndex(COL_TITLE)));
            info.setDis_name(cursor.getString(cursor.getColumnIndex(COL_DIS_NAME)));
            info.setAlbum(cursor.getString(cursor.getColumnIndex(COL_ALBUM)));
            info.setMusicId(cursor.getLong(cursor.getColumnIndex(COL_MUSIC_ID)));
            info.setAlbumId(cursor.getLong(cursor.getColumnIndex(COL_ALBUM_ID)));
            info.setDuration(cursor.getLong(cursor.getColumnIndex(COL_DURATION)));
            info.setSize(cursor.getLong(cursor.getColumnIndex(COL_SIZE)));
            info.setArtist(cursor.getString(cursor.getColumnIndex(COL_ARTIST)));
            info.setUrl(cursor.getString(cursor.getColumnIndex(COL_URL)));
            info.setIsPlaying(cursor.getInt(cursor.getColumnIndex(COL_IS_PLAYING)));
            infos.add(info);
        }
        cursor.close();
        if (infos.size() == 0)
            return null;
        return infos;

    }
}
