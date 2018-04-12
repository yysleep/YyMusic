package com.yy.sleep.music.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.yy.sleep.music.model.MusicInfo;
import com.yy.sleep.music.model.YMBaseModel;

import java.util.List;

/**
 * Created by yysleep on 17-9-6.
 *
 * @author yysleep
 */

public class MusicDBMgr {

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
                /*File file = new File(Constant.DB_PATH);
                boolean isExists = file.exists();
                if (!isExists) {
                    try {
                        isExists = file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                LogUtil.d(TAG, "[MusicDBMgr] isExists = " + isExists);
                if (isExists) {
                    db = SQLiteDatabase.openOrCreateDatabase(Constant.DB_PATH, null);
                }*/
                FavoriteDao.getInstance().init(db);
            }

            @Override
            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

            }
        };
        mDbInstance = mHelper.getWritableDatabase();
    }

    public SQLiteDatabase getDbInstance() {
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

    public List<MusicInfo> query(String table, boolean firstInit, boolean outside) {
        List<MusicInfo> models = null;
        if (table == null)
            return null;
        switch (table) {
            case FavoriteDao.TABLE_FAVORITE_MUSIC:
                models = FavoriteDao.getInstance().query(mDbInstance, firstInit, outside);
                break;
        }
        return models;
    }
}
