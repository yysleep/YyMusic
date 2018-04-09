package com.yy.sleep.music;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.yy.sleep.music.common.MusicConst;
import com.yy.sleep.music.dao.MusicDBMgr;
import com.yy.sleep.music.sys.LruCacheSys;
import com.yy.sleep.music.sys.MusicPlayer;
import com.yy.sleep.music.sys.MusicSys;
import com.yy.sleep.music.tool.FileOperationTask;
import com.yy.sleep.music.util.LogUtil;
import com.yy.sleep.music.util.ShareUtil;
import com.yy.sleep.music.util.ToastUtil;

import java.io.File;


/**
 * Created by Administrator on 2016/5/22.
 *
 * @author yysleep
 */
public class MusicApplication extends Application {

    private static final String TAG = "MusicApplication";
    private static Handler sHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        sHandler = new Handler(Looper.getMainLooper());
        initStorage();
        MusicPlayer.getInstance().init(this);
        ToastUtil.init(this);
        MusicDBMgr.getInstance().init(this);
        LruCacheSys.getInstance().initContext(getApplicationContext());

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
                LogUtil.i(TAG, "[MusicObserver][onChange] 第一次数据库发生了变化 为 yymusic 自身删除 ...");
                FileOperationTask.sIsOurSelfDelete = false;
                return;
            }
            if (FileOperationTask.sAutoSync) {
                LogUtil.i(TAG, "[MusicObserver][onChange] 第二次数据库发生了变化 为 yymusic 自身删除 ...");
                FileOperationTask.sAutoSync = false;
                return;
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                LogUtil.i(TAG, "[MusicObserver][onChange] 数据库发生了变化 但是没有读取权限");
                return;
            }
            LogUtil.i(TAG, "[MusicObserver][onChange] 数据库发生了变化 正在刷新本地数据 ...");
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

    public static Handler getHandler() {
        return sHandler;
    }

}
