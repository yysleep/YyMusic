package com.example.administrator.yymusic.dao;

import android.database.sqlite.SQLiteDatabase;

import com.example.administrator.yymusic.model.YMBaseModel;
import com.example.administrator.yymusic.util.LogUtil;

import java.util.List;

/**
 * Created by archermind on 17-9-6.
 *
 * @author yysleep
 */

public abstract class YMBaseDao<T extends YMBaseModel> {

    private static final String TAG = "YMBaseDao";

    public void init(SQLiteDatabase db) {
        LogUtil.i(TAG, "[init] 数据库第一次初始化");
    }

    public void upgrade() {
        LogUtil.i(TAG, "[upgrade] 数据库进行了升级");
    }

    public void insert(SQLiteDatabase db, YMBaseModel model) {
        LogUtil.i(TAG, "[insert] 数据库插入了新数据");
    }

    public void update(SQLiteDatabase db, YMBaseModel model) {
        LogUtil.i(TAG, "[update] 数据库更新了数据");
    }

    public void delete(SQLiteDatabase db, YMBaseModel model) {
        LogUtil.i(TAG, "[delete] 数据库删除了数据");
    }

    public abstract List<T> query(SQLiteDatabase db, boolean firstInit, boolean outside);
}
