package com.example.administrator.yymusic.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.administrator.yymusic.model.MusicInfo;
import com.example.administrator.yymusic.model.YMBaseModel;

import java.util.List;

/**
 * Created by archermind on 17-9-6.
 *
 * @author yysleep
 */

public class MusicDBMgr<T extends YMBaseModel> {

    private static final String TAG = "MusicDBMgr";
    private final String DB_NAME = "music.db";
    private final int DB_VERSION = 1;
    private SQLiteOpenHelper mHelper;
    private SQLiteDatabase mDbInstance;
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
        mDbInstance = mHelper.getWritableDatabase();
    }

    public SQLiteDatabase getmDbInstance() {
        return mDbInstance;
    }

    public void insert(String table, YMBaseModel model) {
        if (table == null || model == null)
            return;
        switch (table) {
            case FavoriteDao.TABLE_FAVORITE_MUSIC:
                FavoriteDao.getInstance().insert(mDbInstance, model);
                break;

            default:
                break;
        }
    }

    public void delete(String table, YMBaseModel model) {
        if (table == null || model == null)
            return;
        switch (table) {
            case FavoriteDao.TABLE_FAVORITE_MUSIC:
                FavoriteDao.getInstance().delete(mDbInstance, model);
                break;

            default:
                break;
        }
    }

    public void update(String table, YMBaseModel model) {
        if (table == null || model == null)
            return;
        switch (table) {
            case FavoriteDao.TABLE_FAVORITE_MUSIC:
                FavoriteDao.getInstance().update(mDbInstance, model);
                break;

            default:
                break;
        }
    }

    public List<MusicInfo> query(String table) {
        List<MusicInfo> models = null;
        if (table == null)
            return null;
        switch (table) {
            case FavoriteDao.TABLE_FAVORITE_MUSIC:
                models = FavoriteDao.getInstance().query(mDbInstance);
                break;
        }
        return models;
    }
}
