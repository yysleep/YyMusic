package com.yy.sleep.music.model;

/**
 * Created by archermind on 17-3-30.
 * @author yysleep
 */
public class UpdateInfo extends YMBaseModel {

    private int updateFragmentNum = -1;

    private int updatePosition = -1;

    private String updateTitle;

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getUpdateFragmentNum() {
        return updateFragmentNum;
    }

    public void setUpdateFragmentNum(int updateFragmentNum) {
        this.updateFragmentNum = updateFragmentNum;
    }

    public int getUpdatePosition() {
        return updatePosition;
    }

    public void setUpdatePosition(int updatePosition) {
        this.updatePosition = updatePosition;
    }

    public String getUpdateTitle() {
        return updateTitle;
    }

    public void setUpdateTitle(String updateTitle) {
        if(updateTitle==null)
            return;
        this.updateTitle = updateTitle;
    }

    @Override
    public String toString() {
        return "UpdateInfo {" +
                "updateFragmentNum=" + updateFragmentNum +
                ", updatePosition=" + updatePosition +
                ", updateTitle='" + updateTitle + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
