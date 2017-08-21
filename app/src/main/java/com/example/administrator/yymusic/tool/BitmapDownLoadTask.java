package com.example.administrator.yymusic.tool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;

import com.example.administrator.yymusic.sys.LruCacheSys;

/**
 * Created by archermind on 17-6-8.
 * @author yysleep
 */
public class BitmapDownLoadTask extends AsyncTask<String, Void, String[]> {

    private static final String TAG = "BitmapDownLoadTask";

    @Override
    protected String[] doInBackground(String... params) {
        Bitmap bmp = createAlbumArts(params[1]);
        if (bmp != null)
            LruCacheSys.getInstance().addBitmapToMemoryCache(params[1], bmp);
        return params;
    }

    @Override
    protected void onPostExecute(String[] params) {
        super.onPostExecute(params);
        if (params == null)
            return;

        Log.i(TAG, "[yymusic][BitmapDownLoadTask] name = " + params[0]);
        LruCacheSys.getInstance().refresh(params);
    }

    private Bitmap createAlbumArts(String filePath) {
        if (filePath == null)
            return null;
        Log.i(TAG, "[yymusic][createAlbumArts] filePath = " + filePath);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        byte[] bytes = retriever.getEmbeddedPicture();
        if (bytes == null) {
            return null;
        }

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
