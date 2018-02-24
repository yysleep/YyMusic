package com.example.administrator.yymusic.sys;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.example.administrator.yymusic.api.ITaskInterface;
import com.example.administrator.yymusic.tool.BitmapDownLoadTask;
import com.example.administrator.yymusic.util.LogUtil;

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

    private static LruCache<String, Bitmap> sMemoryCache;
    private static HashMap<String, ITaskInterface> sTaskMap;
    private static Map<String, SoftReference<Bitmap>> sSortReferenceCache;
    private Set<BitmapDownLoadTask> taskCollection;
    private static Map<String, Bitmap> sCoverCache;
    private static Context sContext;

    private static final String TAG = "LruCacheSys";

    private LruCacheSys() {

    }

    public static LruCacheSys getInstance() {
        if (instance == null) {
            synchronized (LruCacheSys.class) {
                if (instance == null) {
                    instance = new LruCacheSys();
                    sTaskMap = new HashMap<>();
                    sSortReferenceCache = new HashMap<>();
                    sCoverCache = new HashMap<>();
                    int maxMemory = (int) Runtime.getRuntime().maxMemory();
                    int cacheSize = maxMemory / 8;
                    // 设置图片缓存大小为程序最大可用内存的1/8
                    sMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                        @Override
                        protected int sizeOf(String key, Bitmap bitmap) {
                            return bitmap.getByteCount();
                        }

                        @Override
                        protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                            if (oldValue != null) {
                                SoftReference<Bitmap> s = new SoftReference<Bitmap>(oldValue);
                                sSortReferenceCache.put(key, s);
                            }
                        }
                    };
                }
            }
        }
        return instance;
    }

    public void initContext(Context appContext) {
        sContext = appContext;
    }

    public void registerMusicObserver(String name, ITaskInterface task) {
        if (name == null)
            return;

        if (sTaskMap.containsKey(name))
            return;

        sTaskMap.put(name, task);
    }

    public void unRegisterMusicObserver(String name) {
        if (name == null)
            return;

        if (sTaskMap.containsKey(name))
            sTaskMap.remove(name);

    }

    public Bitmap getBitmapFromMemoryCache(String key) {
        if (key == null)
            return null;
        Bitmap bmp = sMemoryCache.get(key);
        if (bmp == null) {
            SoftReference<Bitmap> s = sSortReferenceCache.get(key);
            if (s != null) {
                bmp = s.get();
                if (bmp != null)
                    sMemoryCache.put(key, bmp);
            }
        }
        return bmp;
    }

    /*
    * 获取唯一的一张大图缓存
    * @param key string 图片的路径
    * @return 大图 bitmap
    **/
    public Bitmap getBmpFromCoverCache(String key) {
        if (key == null)
            return null;
        return sCoverCache.get(key);
    }

    public void addBitmapToMemoryCache(String key, Bitmap bmp) {
        if (key != null && bmp != null && getBitmapFromMemoryCache(key) == null) {
            sMemoryCache.put(key, bmp);
        }
    }

    /*
    * 添加缓存大图(只存一张)
    * @param key string 图片的url
    * @param bmp Bitmap 位图
    **/
    public void addCoverBmpCache(String key, Bitmap bmp) {
        if (key == null || bmp == null)
            return;
        if (getBmpFromCoverCache(key) == null) {
            sCoverCache.clear();
            sCoverCache.put(key, bmp);
        }
    }

    public void refresh(BitmapDownLoadTask.Type t, Bitmap cover, String... params) {
        if (params.length < 2 || params[0] == null || params[1] == null)
            return;

        ITaskInterface task = sTaskMap.get(params[0]);
        if (task == null)
            return;

        LogUtil.i(TAG, "[refresh] name = " + params[0]);
        if (t == BitmapDownLoadTask.Type.Cover || getBitmapFromMemoryCache(params[1]) != null)
            task.getBmpSuccess(cover, params[1]);
        else
            task.getBmpFailed();

    }

    public void startTask(String name, String url, BitmapDownLoadTask.Type type) {
        LogUtil.i(TAG, "[startTask] name = " + name + " sTaskMap.get(name) = " + sTaskMap.get(name) +
                "   getBitmapFromMemoryCache(url) = " + getBitmapFromMemoryCache(url) + "  url = " + url);
        if (name == null || sTaskMap.get(name) == null || sContext == null)
            return;

        if (getBmpFromCoverCache(url) != null) {
            LogUtil.i(TAG, "[startTask] 已经有大图 Cover");
            return;
        }
        LogUtil.i(TAG, "[startTask] name = " + name + " url = " + url);
        BitmapDownLoadTask task = new BitmapDownLoadTask(sContext, type);
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
