package com.yy.sleep.music;

import android.os.Environment;

/**
 * Created by archermind on 17-9-6.
 *
 * @author yysleep
 */

public class Constant {

    public static final String F_TAG = "YY-MUSIC: ";

    public static final int READ_PERMISSION = 1;

    public static final String DIR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/YyMusic";

    public static final String CRASH_PATH = DIR_PATH  + "/CrashLog.txt";
}
