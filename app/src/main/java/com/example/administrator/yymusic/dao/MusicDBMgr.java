package com.example.administrator.yymusic.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.administrator.yymusic.modle.YMBaseModle;

/**
 * Created by archermind on 17-9-6.
 *
 * @author yysleep
 */

public class MusicDBMgr {

    private static final String TAG = "MusicDBMgr";
    private final String DB_NAME = "music.db";
    private final int DB_VERSION = 1;
    private SQLiteOpenHelper mHelper;
    private SQLiteDatabase dbInstance;
    private static volatile MusicDBMgr instance;

    private MusicDBMgr() {

    }

    public static MusicDBMgr getInstance() {
        if (instance == null) {
            synchronized (MusicDBMgr.class) {
                if (instance == null)
                    instance = new MusicDBMgr();
            }
        }
        return instance;
    }

    public void init(Context context) {
        mHelper = new SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                FavoriteDao.getInstance().init(db);
            }

            @Override
            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

            }
        };
        dbInstance = mHelper.getWritableDatabase();
    }

    public SQLiteDatabase getDbInstance() {
        return dbInstance;
    }

    public void insert(String table, YMBaseModle modle) {
        if (table == null || modle == null)
            return;
        switch (table) {
            case FavoriteDao.TABLE_FAVORITE_MUSIC:
                FavoriteDao.getInstance().insert(dbInstance, modle);
                break;

            default:
                break;
        }
    }

    public void delete(String table, YMBaseModle modle) {
        if (table == null || modle == null)
            return;
        switch (table) {
            case FavoriteDao.TABLE_FAVORITE_MUSIC:
                FavoriteDao.getInstance().delete(dbInstance, modle);
                break;

            default:
                break;
        }
    }

    public void update(String table, YMBaseModle modle) {
        if (table == null || modle == null)
            return;
        switch (table) {
            case FavoriteDao.TABLE_FAVORITE_MUSIC:
                FavoriteDao.getInstance().update(dbInstance, modle);
                break;

            default:
                break;
        }
    }
}
