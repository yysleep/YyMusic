package com.yy.sleep.music;

import com.yy.sleep.music.util.LogUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";

    private static CrashHandler sInstance = new CrashHandler();

    public static void init() {
        Thread.setDefaultUncaughtExceptionHandler(sInstance);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LogUtil.d(TAG, "[uncaughtException] e = " + e);
        dumpException(e);
        MusicApplication.getApplication().killProcess();
    }

    private void dumpException(Throwable ex) {
        PrintWriter writer = null;
        File file = new File(Constant.DIR_PATH);
        boolean exists = file.exists();
        if (!exists) {
            exists = file.mkdirs();
        }
        LogUtil.d(TAG, "[dumpException] 文件夹创建" + exists);
        if (!exists) {
            return;
        }
        try {
            writer = new PrintWriter(new FileWriter(Constant.CRASH_PATH, true));
            writer.println();
            writer.println("------ start -------");
            writer.println(DateFormat.getDateTimeInstance().format(new Date()));
            writer.println();
            ex.printStackTrace(writer);
            writer.println("------ end ------");
            writer.println();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
