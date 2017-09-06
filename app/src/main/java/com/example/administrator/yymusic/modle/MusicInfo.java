package com.example.administrator.yymusic.modle;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 音乐
 * Created by Administrator on 2016/5/22.
 */
public class MusicInfo extends YMBaseModle implements Parcelable {

    public MusicInfo(int fragmentNum) {
        this.fragmentNum = fragmentNum;
    }

    public static final int IS_PLAYING = 1;

    public static final int NO_PLAYING = 0;

    // 本地 DB 唯一 ID
    private int id;

    // 属于哪个fragment
    private int fragmentNum;
    // 名称
    private String title;

    // 文件名
    private String dis_name;

    // 歌曲的专辑名
    private String album;

    // 歌曲ID
    private long musicId;

    private long albumId;

    // 歌曲的总播放时长
    private long duration;

    // 歌曲文件的大小
    private long size;

    // 歌曲的歌手名
    private String artist;

    // 歌曲文件的路径
    private String url;

    // 是否正在播放 0 是默认不再播放 1为正在播放 所有lisct中一次只有一个1
    private int isPlaying;

    private Bitmap bitmap;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getMusicId() {
        return musicId;
    }

    public void setMusicId(long musicId) {
        this.musicId = musicId;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(int isPlaying) {
        this.isPlaying = isPlaying;
    }

    public String getDis_name() {
//        String result = dis_name.substring(0, dis_name.indexOf("."));
        return dis_name;
    }

    public void setDis_name(String dis_name) {
        this.dis_name = dis_name;
    }

    public int getFragmentNum() {
        return fragmentNum;
    }

    public void setFragmentNum(int fragmentNum) {
        this.fragmentNum = fragmentNum;
    }

    @Override
    public String toString() {
        return "MusicInfo{" +
                "id=" + id +
                ", fragmentNum=" + fragmentNum +
                ", title='" + title + '\'' +
                ", dis_name='" + dis_name + '\'' +
                ", album='" + album + '\'' +
                ", musicId=" + musicId +
                ", albumId=" + albumId +
                ", duration=" + duration +
                ", size=" + size +
                ", artist='" + artist + '\'' +
                ", url='" + url + '\'' +
                ", isPlaying=" + isPlaying +
                ", bitmap=" + bitmap +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(fragmentNum);
        dest.writeString(title);
        dest.writeString(dis_name);
        dest.writeString(album);
        dest.writeLong(musicId);
        dest.writeLong(albumId);
        dest.writeLong(duration);
        dest.writeLong(size);
        dest.writeString(artist);
        dest.writeString(url);
        dest.writeInt(isPlaying);
        dest.writeParcelable(bitmap, flags);
    }

    public static final Parcelable.Creator<MusicInfo> CREATOR = new Parcelable.Creator<MusicInfo>() {

        @Override
        public MusicInfo createFromParcel(Parcel source) {
            return new MusicInfo(source);
        }

        @Override
        public MusicInfo[] newArray(int size) {
            return new MusicInfo[0];
        }
    };

    public MusicInfo(Parcel source) {
        fragmentNum = source.readInt();
        title = source.readString();
        dis_name = source.readString();
        album = source.readString();
        musicId = source.readLong();
        albumId = source.readLong();
        duration = source.readLong();
        size = source.readLong();
        artist = source.readString();
        url = source.readString();
        isPlaying = source.readInt();
        bitmap = source.readParcelable(Bitmap.class.getClassLoader());
    }
}
