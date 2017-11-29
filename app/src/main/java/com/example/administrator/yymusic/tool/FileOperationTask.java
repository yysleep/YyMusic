package com.example.administrator.yymusic.tool;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.example.administrator.yymusic.api.IFileOperationCallback;
import com.example.administrator.yymusic.sys.MusicSys;
import com.example.administrator.yymusic.ui.base.BaseFragment;

import java.io.File;

/**
 * Created by archermind on 17-8-9.
 *
 * @author yysleep
 */

public class FileOperationTask extends AsyncTask<String, Void, Boolean> {

    private IFileOperationCallback callback;
    public static boolean sIsOurSelfDelete;
    public static boolean sAutoSync;
    public static boolean sCheckSelf;

    public FileOperationTask(IFileOperationCallback callback) {
        this.callback = callback;
    }

    public static final String DELETE_FILE = "delete";

    public static final String CREATE_FILE = "create";

    public static final String COPY_FILE = "copy";

    public static final String MOVE_FILE = "move";

    @Override
    protected Boolean doInBackground(String... task) {

        if (task[0] == null || task[1] == null)
            return false;

        Context context = null;
        if (callback instanceof BaseFragment)
            context = ((BaseFragment) callback).getActivity().getApplicationContext();

        switch (task[0]) {

            case DELETE_FILE:
                if (MusicSys.getInstance().checkIsPalying(task[1]))
                    return false;

                File file = new File(task[1]);
                if (file.exists()) {
                    sIsOurSelfDelete = true;
                    sAutoSync = true;
                    if (file.delete()) {
                        // 在对文件进行删除或保存后，需要对系统进行更新，是通过广播的形式来完成
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(file);
                        intent.setData(contentUri);
                        if (context != null)
                            context.sendBroadcast(intent);
                        return true;
                    }
                }
                break;

            default:
                break;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        callback.refreshDeleteFile(result);
    }
}
