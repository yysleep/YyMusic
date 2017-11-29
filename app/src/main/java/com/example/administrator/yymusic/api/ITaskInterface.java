package com.example.administrator.yymusic.api;

import android.graphics.Bitmap;

/**
 * Created by archermind on 17-6-8.
 *
 * @author yysleep
 */
public interface ITaskInterface {

    void getBmpSuccess(Bitmap bmp, String url);

    void getBmpFailed();
}
