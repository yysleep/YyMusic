package com.example.administrator.yymusic.tool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.view.WindowManager;

import com.example.administrator.yymusic.sys.LruCacheSys;
import com.example.administrator.yymusic.util.LogUtil;

/**
 * Created by archermind on 17-6-8.
 *
 * @author yysleep
 */
public class BitmapDownLoadTask extends AsyncTask<String, Void, String[]> {

    private static final String TAG = "BitmapDownLoadTask";
    private Type mT;
    private Context mContext;
    private Bitmap mBmpCover;

    public enum Type {
        Thumbnails,
        Cover
    }

    public BitmapDownLoadTask(Context c, Type t) {
        mContext = c;
        mT = t;
    }

    @Override
    protected String[] doInBackground(String... params) {
        if (mContext == null)
            return null;

        Bitmap bmp = createAlbumArts(params[1]);
        if (bmp == null) {
            if (mT == Type.Cover)
                return params;
            else
                return null;
        }

        if (mT == Type.Thumbnails)
            LruCacheSys.getInstance().addBitmapToMemoryCache(params[1], bmp);
        else {
            LruCacheSys.getInstance().addCoverBmpCache(params[1], bmp);
            mBmpCover = bmp;
        }
        return params;
    }

    @Override
    protected void onPostExecute(String[] params) {
        super.onPostExecute(params);
        if (params == null || params.length <= 0) {
            mContext = null;
            return;
        }
        LogUtil.i(TAG, "[onPostExecute] name = " + params[0] + " mBmpCover = " + mBmpCover);
        LruCacheSys.getInstance().refresh(mT, mBmpCover, params);
        mBmpCover = null;
        mContext = null;

    }

    private Bitmap createAlbumArts(String filePath) {
        if (filePath == null)
            return null;
        LogUtil.i(TAG, "[createAlbumArts] filePath = " + filePath);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
        } catch (Exception e) {
            LogUtil.e(TAG, "[createAlbumArts] filePath = " + filePath + " 解析地址出错");
            return null;
        }
        byte[] bytes = retriever.getEmbeddedPicture();
        if (bytes == null) {
            // Todo http 获取图片
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        switch (mT) {
            case Thumbnails:
                options.inSampleSize = calculateInSampleSize(options, px(32), px(32));
                break;

            case Cover:
                int width = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
                if (width <= 0)
                    width = 200;
                options.inSampleSize = calculateInSampleSize(options, width, width);
                break;

        }
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        } catch (OutOfMemoryError error) {
            LogUtil.i(TAG, "[createAlbumArts] 解析结束 OutOfMemoryError ");
        }

        if (bmp != null)
            LogUtil.i(TAG, "[createAlbumArts] 解析结束 bmp = " + bmp + " --- 大小 = " + bmp.getByteCount());
        return bmp;
    }

    /*
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private int px(float dp) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int initSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                initSize = Math.round((float) height / (float) reqHeight);
            } else {
                initSize = Math.round((float) width / (float) reqWidth);
            }
        }
        int roundedSize;
        if (initSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

}
