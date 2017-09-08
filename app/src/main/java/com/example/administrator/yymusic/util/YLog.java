package com.example.administrator.yymusic.util;

import android.util.Log;

/**
 * Created by archermind on 17-9-6.
 *
 * @author yysleep
 */

public class YLog {

    private static final boolean DEBUG = true;

    public static void i(String tag, String content) {
        if (DEBUG && tag != null && content != null)
            Log.i(YYConstant.F_TAG + tag, content);
    }

    public static void d(String tag, String content) {
        if (DEBUG && tag != null && content != null)
            Log.d(YYConstant.F_TAG + tag, content);
    }

    public static void e(String tag, String content) {
        if (DEBUG && tag != null && content != null)
            Log.e(YYConstant.F_TAG + tag, content);
    }

    public static void w(String tag, String content) {
        if (DEBUG && tag != null && content != null)
            Log.w(YYConstant.F_TAG + tag, content);
    }
}
