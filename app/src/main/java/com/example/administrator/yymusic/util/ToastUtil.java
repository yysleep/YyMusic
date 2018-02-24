package com.example.administrator.yymusic.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import com.example.administrator.yymusic.MusicApplication;

/**
 * Created by YySleep on 2018/1/17.
 *
 * @author YySleep
 */

public class ToastUtil {

    private static volatile Toast sToast;

    @SuppressLint("ShowToast")
    public static void toast(final String content) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            sToast.setText(content);
            sToast.show();
        } else {
            MusicApplication.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    sToast.setText(content);
                    sToast.show();
                }
            });
        }
    }

    @SuppressLint("ShowToast")
    public static void init(Context context) {
        if (sToast == null) {
            synchronized (ToastUtil.class) {
                if (sToast == null) {
                    sToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
                }
            }
        }
    }

}
