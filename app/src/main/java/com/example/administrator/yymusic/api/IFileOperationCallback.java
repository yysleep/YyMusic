package com.example.administrator.yymusic.api;

/**
 * Created by archermind on 17-8-9.
 *
 * @author yysleep
 */

public interface IFileOperationCallback {

    public void refreshDeleteFile(Boolean result);

    public void syncList(String path);

}
