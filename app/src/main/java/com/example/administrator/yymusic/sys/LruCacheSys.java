package com.example.administrator.yymusic.sys;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.example.administrator.yymusic.api.ITaskInterface;
import com.example.administrator.yymusic.tool.BitmapDownLoadTask;
import com.example.administrator.yymusic.util.YLog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by archermind on 17-6-8.
 * @author yysleep
 */
public class LruCacheSys {

    private static volatile LruCacheSys instance;

    private static LruCache<String, Bitmap> mMemoryCache;
    private static HashMap<String, ITaskInterface> hashMap;

    private Set<BitmapDownLoadTask> taskCollection;

    private static final String TAG = "LruCacheSys";

    private LruCacheSys() {

    }

    public static LruCacheSys getInstance() {
        if (instance == null) {
            synchronized (LruCacheSys.class) {
                if (instance == null) {
                    instance = new LruCacheSys();
                    hashMap = new HashMap<>();
                    int maxMemory = (int) Runtime.getRuntime().maxMemory();
                    int cacheSize = maxMemory / 8;
                    // 设置图片缓存大小为程序最大可用内存的1/8
                    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                        @Override
                        protected int sizeOf(String key, Bitmap bitmap) {
                            return bitmap.getByteCount();
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

        if (hashMap.containsKey(name))
            return;

        hashMap.put(name, task);
    }

    public void unregisMusicObserver(String name) {
        if (name == null)
            return;

        if (hashMap.containsKey(name))
            hashMap.remove(name);

    }

    public Bitmap getBitmapFromMemoryCache(String key) {
        if (key == null)
            return null;
        return mMemoryCache.get(key);
    }

    public void addBitmapToMemoryCache(String key, Bitmap bmp) {
        if (key != null && bmp != null && getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bmp);
        }
    }

    public void refresh(String... params) {
        if (params.length < 2 || params[0] == null || params[1] == null)
            return;

        ITaskInterface task = hashMap.get(params[0]);
        if (task == null)
            return;

        YLog.i(TAG, "[refresh] name = " + params[0]);
        if (LruCacheSys.getInstance().getBitmapFromMemoryCache(params[1]) != null)
            task.getBmpSuccess(params[1]);
        else
            task.getBmpFaild();

    }

    public void startTask(String name, String url) {
        YLog.i(TAG, "[startTask] name = " + name + " hashMap.get(name) = " + hashMap.get(name) +
                "   getBitmapFromMemoryCache(url) = " + getBitmapFromMemoryCache(url) + "  url = " + url);
        if (name == null || hashMap.get(name) == null)
            return;


        YLog.i(TAG, "[startTask] name = " + name + " url = " + url);
        BitmapDownLoadTask task = new BitmapDownLoadTask();
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
