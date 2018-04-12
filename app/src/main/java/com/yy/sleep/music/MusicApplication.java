package com.yy.sleep.music;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;

import com.yy.sleep.music.common.MusicConst;
import com.yy.sleep.music.dao.MusicDBMgr;
import com.yy.sleep.music.sys.LruCacheSys;
import com.yy.sleep.music.sys.MusicPlayer;
import com.yy.sleep.music.sys.MusicSys;
import com.yy.sleep.music.tool.FileOperationTask;
import com.yy.sleep.music.util.LogUtil;
import com.yy.sleep.music.util.ShareUtil;
import com.yy.sleep.music.util.ToastUtil;

import java.util.LinkedList;


/**
 * Created by Administrator on 2016/5/22.
 *
 * @author yysleep
 */
public class MusicApplication extends Application {

    private static final String TAG = "MusicApplication";
    private Handler mHandler;
    private LinkedList<Activity> mActivityList;
    private static MusicApplication sApplication;

    @Override
    public void onCreate() {
        super.onCreate();

        sApplication = this;
        mHandler = new Handler(Looper.getMainLooper());
        mActivityList = new LinkedList<>();

        MusicPlayer.getInstance().init(this);
        ToastUtil.init(this);
        MusicDBMgr.getInstance().init(this);
        LruCacheSys.getInstance().initContext(getApplicationContext());
        ShareUtil.getInstance().init(getApplicationContext());
        CrashHandler.init();

        getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mObserver);
        registerActivityLifecycleCallbacks(mCallbacks);
    }

    public static MusicApplication getApplication() {
        return sApplication;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void killProcess() {
        LogUtil.d(TAG, "[killProcess]" + mActivityList.size());
        while (!mActivityList.isEmpty()) {
            mActivityList.pollLast().finish();
        }
        Process.killProcess(Process.myPid());
    }

    private ActivityLifecycleCallbacks mCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            mActivityList.add(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            mActivityList.remove(activity);
        }
    };

    private ContentObserver mObserver =  new ContentObserver(null) {


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
    };
}
