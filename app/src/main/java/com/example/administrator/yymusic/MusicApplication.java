package com.example.administrator.yymusic;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;

import com.example.administrator.yymusic.common.MusicConst;
import com.example.administrator.yymusic.dao.MusicDBMgr;
import com.example.administrator.yymusic.sys.LruCacheSys;
import com.example.administrator.yymusic.sys.MusicPlayer;
import com.example.administrator.yymusic.sys.MusicSys;
import com.example.administrator.yymusic.tool.FileOperationTask;
import com.example.administrator.yymusic.util.LogHelper;
import com.example.administrator.yymusic.util.ShareUtil;

import java.io.File;


/**
 * Created by Administrator on 2016/5/22.
 *
 * @author yysleep
 */
public class MusicApplication extends Application {

    private static final String TAG = "MusicApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        initStorage();
        MusicDBMgr.getInstance().init(this);
        LruCacheSys.getInstance().initContext(getApplicationContext());
        // 注册观察者
        getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, new MusicObserver(null));
        ShareUtil.getInstance().init(getApplicationContext());
    }


    private class MusicObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        MusicObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            if (FileOperationTask.sIsOurSelfDelete) {
                LogHelper.i(TAG, "[MusicObserver][onChange] 第一次数据库发生了变化 为 yymusic 自身删除 ...");
                FileOperationTask.sIsOurSelfDelete = false;
                return;
            }
            if (FileOperationTask.sAutoSync) {
                LogHelper.i(TAG, "[MusicObserver][onChange] 第二次数据库发生了变化 为 yymusic 自身删除 ...");
                FileOperationTask.sAutoSync = false;
                return;
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                LogHelper.i(TAG, "[MusicObserver][onChange] 数据库发生了变化 但是没有读取权限");
                return;
            }
            LogHelper.i(TAG, "[MusicObserver][onChange] 数据库发生了变化 正在刷新本地数据 ...");
            MusicSys.getInstance().initMusicList(getApplicationContext(), false, true);
            MusicPlayer.getInstance().update();
            Intent intent = new Intent(MusicConst.ACTION_UPDATE_ALL_MUSIC_LIST);
            intent.putExtra(MusicConst.CHANGE_FROM_OUTSIDE, true);
            getApplicationContext().sendBroadcast(intent);
        }
    }

    private boolean initStorage() {
        // 图片存储路径
        String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/yymusic/album/";
        File fileImage = new File(imagePath);
        if (!fileImage.exists()) {
            if (!fileImage.mkdirs()) {
                return false;
            }
        }
        return true;
    }

}
