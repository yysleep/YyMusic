package com.example.administrator.yymusic.sys;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.example.administrator.yymusic.api.ITaskInterface;
import com.example.administrator.yymusic.tool.BitmapDownLoadTask;
import com.example.administrator.yymusic.util.YLog;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by archermind on 17-6-8.
 *
 * @author yysleep
 */
public class LruCacheSys {

    private static volatile LruCacheSys instance;

    private static LruCache<String, Bitmap> mMemoryCache;
    private static HashMap<String, ITaskInterface> mTaskMap;
    private static Map<String, SoftReference<Bitmap>> mSortReferenceCache;

    private Set<BitmapDownLoadTask> taskCollection;
    private static Context mContext;

    private static final String TAG = "LruCacheSys";

    private LruCacheSys() {

    }

    public static LruCacheSys getInstance(Context context) {
        if (instance == null) {
            synchronized (LruCacheSys.class) {
                if (instance == null) {
                    mContext = context.getApplicationContext();
                    instance = new LruCacheSys();
                    mTaskMap = new HashMap<>();
                    mSortReferenceCache = new HashMap<>();
                    int maxMemory = (int) Runtime.getRuntime().maxMemory();
                    int cacheSize = maxMemory / 8;
                    // 设置图片缓存大小为程序最大可用内存的1/8
                    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                        @Override
                        protected int sizeOf(String key, Bitmap bitmap) {
                            return bitmap.getByteCount();
                        }

                        @Override
                        protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                            if (oldValue != null) {
                                SoftReference<Bitmap> s = new SoftReference<Bitmap>(oldValue);
                                mSortReferenceCache.put(key, s);
                            }
                        }
                    };
                }
            }
        }
        return instance;
    }

    public void registMusicObserver(String name, ITaskInterface task) {
        if (name == null)
            return;

        if (mTaskMap.containsKey(name))
            return;

        mTaskMap.put(name, task);
    }

    public void unregisMusicObserver(String name) {
        if (name == null)
            return;

        if (mTaskMap.containsKey(name))
            mTaskMap.remove(name);

    }

    public Bitmap getBitmapFromMemoryCache(String key) {
        if (key == null)
            return null;
        Bitmap bmp = mMemoryCache.get(key);
        if (bmp == null) {
            SoftReference<Bitmap> s = mSortReferenceCache.get(key);
            if (s != null) {
                bmp = s.get();
                if (bmp != null)
                    mMemoryCache.put(key, bmp);
            }
        }
        return bmp;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bmp) {
        if (key != null && bmp != null && getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bmp);
        }
    }

    public void refresh(BitmapDownLoadTask.Type type, String... params) {
        if (params.length < 2 || params[0] == null || params[1] == null)
            return;

        ITaskInterface task = mTaskMap.get(params[0]);
        if (task == null)
            return;

        YLog.i(TAG, "[refresh] name = " + params[0]);
        if (getBitmapFromMemoryCache(params[1]) != null)
            task.getBmpSuccess(params[1]);
        else
            task.getBmpFaild();

    }

    public void startTask(String name, String url, BitmapDownLoadTask.Type type) {
        YLog.i(TAG, "[startTask] name = " + name + " mTaskMap.get(name) = " + mTaskMap.get(name) +
                "   getBitmapFromMemoryCache(url) = " + getBitmapFromMemoryCache(url) + "  url = " + url);
        if (name == null || mTaskMap.get(name) == null)
            return;


        YLog.i(TAG, "[startTask] name = " + name + " url = " + url);
        BitmapDownLoadTask task = new BitmapDownLoadTask(mContext, type);
        if (taskCollection == null)
            taskCollection = new HashSet<>();

        taskCollection.add(task);
        String params[] = {name, url};
        task.execute(params);

    }

    public void cancelAllTasks() {
        if (taskCollection != null) {
            for (BitmapDownLoadTask task : taskCollection) {
                task.cancel(false);
            }
        }
    }
}
