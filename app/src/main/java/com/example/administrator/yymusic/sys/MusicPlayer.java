package com.example.administrator.yymusic.sys;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.example.administrator.yymusic.api.ITaskCallback;
import com.example.administrator.yymusic.common.MusicConst;
import com.example.administrator.yymusic.dao.FavoriteDao;
import com.example.administrator.yymusic.dao.MusicDBMgr;
import com.example.administrator.yymusic.modle.MusicInfo;
import com.example.administrator.yymusic.modle.UpdateInfo;
import com.example.administrator.yymusic.util.YLog;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2016/5/22.
 *
 * @author yysleep
 */
public class MusicPlayer implements AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = "MusicPlayer";
    public static volatile boolean isPauseByMyself;
    private static List<MusicInfo> musicInfos;
    private int mSongNum = -1;
    private int mpFragmentNum = -1;
    private boolean isPause;
    private static volatile MediaPlayer mediaPlayer;
    // 由next 和last 触发的切换歌曲
    private boolean isOurChange;
    private Context context;
    private AudioManager audioManager;
    // 是否是焦点
    private boolean isFocus;
    private UpdateInfo updateInfo;

    // 播放模式
    private int playMode = MusicConst.SEQUENTIAL_PLAY;

    public static final int FRAGMENT_LOCAL = 0;
    public static final int FRAGMENT_COLLECT = 1;
    public static final int FRAGMENT_DISCOVER = 2;

    private MusicPlayer() {
    }

    private boolean isStart;

    private static MusicPlayer instance;

    public static MusicPlayer getInstance() {
        if (instance == null) {
            synchronized (MusicPlayer.class) {
                if (instance == null) {
                    instance = new MusicPlayer();
                    mediaPlayer = new MediaPlayer();
                }
            }
        }
        return instance;
    }

    private HashMap<String, ITaskCallback> hashMap = new HashMap<>();

    public void registMusicObserver(String name, ITaskCallback iTaskCallback) {
        if (name == null)
            return;

        if (hashMap.containsKey(name))
            return;

        hashMap.put(name, iTaskCallback);
    }

    public void unregisMusicObserver(String name) {
        if (name == null)
            return;

        if (hashMap.containsKey(name))
            hashMap.remove(name);

    }


    public boolean addCollectMusic() {
        MusicInfo musicInfo = getMusicInfoNow();
        if (musicInfo == null) {
            return false;
        }
        for (MusicInfo info : MusicSys.getInstance().getCollectMusics()) {
            if (info.getDis_name().equals(musicInfo.getDis_name())) {
                return false;
            }
        }
        // todo
        MusicInfo info = new MusicInfo(1);
        info.setIsPlaying(0);
        if (musicInfo.getTitle() != null)
            info.setTitle(musicInfo.getTitle());

        if (musicInfo.getDis_name() != null)
            info.setDis_name(musicInfo.getDis_name());

        if (musicInfo.getAlbum() != null)
            info.setAlbum(musicInfo.getAlbum());


        info.setMusicId(musicInfo.getMusicId());
        info.setDuration(musicInfo.getDuration());
        info.setSize(musicInfo.getSize());

        if (musicInfo.getArtist() != null)
            info.setArtist(musicInfo.getArtist());

        if (musicInfo.getUrl() != null)
            info.setUrl(musicInfo.getUrl());

        if (musicInfo.getBitmap() != null)
            info.setBitmap(musicInfo.getBitmap());

        MusicSys.getInstance().getCollectMusics().add(info);
        MusicDBMgr.getInstance().insert(FavoriteDao.TABLE_FAVORITE_MUSIC, info);
        notifyObserver();
        return true;
    }

    public boolean removeMusicinfo(String titile) {
        YLog.i(TAG, "[removeMusicinfo] fragmentNum = " + mpFragmentNum + "  mSongNum = " + mSongNum);
        if (MusicSys.getInstance().getCollectMusics() == null || MusicSys.getInstance().getCollectMusics().size() == 0)
            return false;

        for (MusicInfo info : MusicSys.getInstance().getCollectMusics()) {
            if (info.getTitle().equals(titile)) {
                MusicSys.getInstance().getCollectMusics().remove(info);
                MusicDBMgr.getInstance().delete(FavoriteDao.TABLE_FAVORITE_MUSIC, info);
                return true;
            }
        }

        return false;
    }

    // 改变当前所属于的list
    public void changeList(List<MusicInfo> lists) {
        musicInfos = lists;
    }

    // 改变当前播放歌曲的position
    public void changeSongNum(int position) {

        mSongNum = position;
        notifyObserver();
    }

    public void changeFragmentNum(int fragmentNum) {
        mpFragmentNum = fragmentNum;
    }

    public void update() {
        String path = null;
        if (isPlaying() && musicInfos != null && musicInfos.size() > mSongNum) {
            path = musicInfos.get(mSongNum).getUrl();
        }
        musicInfos = null;
        switch (mpFragmentNum) {
            case FRAGMENT_LOCAL:
                musicInfos = MusicSys.getInstance().getLocalMusics();
                break;
            case FRAGMENT_COLLECT:
                musicInfos = MusicSys.getInstance().getCollectMusics();
                break;
            case FRAGMENT_DISCOVER:
                musicInfos = MusicSys.getInstance().getLocalMusics();
                break;
            default:
                musicInfos = MusicSys.getInstance().getLocalMusics();
                break;
        }
        if (path == null)
            return;

        for (int i = 0; i < musicInfos.size(); i++) {
            if (musicInfos.get(i).getUrl().equals(path)) {
                mSongNum = i;
                musicInfos.get(i).setIsPlaying(MusicInfo.IS_PLAYING);
                return;
            }
        }
    }

    public void requestAudioFocus() {
        if (audioManager != null && !isFocus) {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            isFocus = true;
        }
    }

    public void startMusic(final Context context, int position, int fragmentNum) {
        this.context = context;
        if (updateInfo == null) {
            updateInfo = new UpdateInfo();
        }
        if (audioManager == null) {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }
        requestAudioFocus();
        if (position == MusicConst.START_DEFAULT_POSITION && fragmentNum == MusicConst.START_DEFAULT_FRAGMENT) {
            // 这是由播放按键所触发的播放 并不是选取其中某个item
            try {
                mediaPlayer.start();
                isPause = false;
                isOurChange = false;
                if (!isStart) {
                    isStart = true;
                }
                // 播放完成后的监听
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        switch (playMode) {
                            case MusicConst.SINGLE_PLAY:
                                singleMusic();
                                break;

                            case MusicConst.RANDOM_PLAY:
                                randomMusic();
                                break;

                            default:
                                nextMusic();
                                break;
                        }
                    }
                });

            } catch (Exception e) {
                YLog.i(TAG, "[startMusic] 播放异常了 e = " + e.toString());
            }
            notifyObserver();
            return;
        }
        String lastName = null;
        if (isStart && !isOurChange) {
            lastName = musicInfos.get(mSongNum).getDis_name();
        }
        if (mpFragmentNum != fragmentNum) {
            mpFragmentNum = fragmentNum;
            switch (fragmentNum) {
                case FRAGMENT_LOCAL:
                    musicInfos = MusicSys.getInstance().getLocalMusics();
                    break;
                case FRAGMENT_COLLECT:
                    musicInfos = MusicSys.getInstance().getCollectMusics();
                    break;
                case FRAGMENT_DISCOVER:
                    musicInfos = MusicSys.getInstance().getLocalMusics();
                    break;
                default:
                    musicInfos = MusicSys.getInstance().getLocalMusics();
                    break;
            }
        }

        mSongNum = position;
        if ((mediaPlayer.isPlaying() || isPauseing()) && lastName != null && lastName.equals(musicInfos.get(position).getDis_name())) {
            if (isPauseing()) {
                mediaPlayer.start();
                isPause = false;
                isOurChange = false;
                if (!isStart) {
                    isStart = true;
                }
            }
            notifyObserver();
            return;
        }

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(musicInfos.get(mSongNum).getUrl());
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPause = false;
            isOurChange = false;

            if (!isStart) {
                isStart = true;
            }
            notifyObserver();

            // 播放完成后的监听
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    switch (playMode) {
                        case MusicConst.SINGLE_PLAY:
                            singleMusic();
                            break;

                        case MusicConst.RANDOM_PLAY:
                            randomMusic();
                            break;

                        default:
                            nextMusic();
                            break;
                    }

                }
            });

        } catch (Exception e) {
            YLog.i(TAG, "[startMusic] 播放异常 e1 = " + e.toString());
        }
    }

    public void pause() {
        if (isPause)
            return;

        mediaPlayer.pause();
        notifyObserver();
        isFocus = false;
        isPause = true;
        YLog.i(TAG, "[pause]");
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    // 播放下一首
    public void nextMusic() {
        if (mpFragmentNum < 0 || musicInfos == null) {
            return;
        }
        isOurChange = true;
        mSongNum = mSongNum == musicInfos.size() - 1 ? 0 : mSongNum + 1;
        startMusic(context, mSongNum, mpFragmentNum);
//        MainSys.getInstance().useCallback(MusicDetailActivity.class.getName(), 1);
    }

    // 播放上一首
    public void lastMusic() {
        if (mpFragmentNum < 0) {
            return;
        }
        isOurChange = true;
        mSongNum = mSongNum == 0 ? musicInfos.size() - 1 : mSongNum - 1;
        startMusic(context, mSongNum, mpFragmentNum);
//        MainSys.getInstance().useCallback(MusicDetailActivity.class.getName(), 1);
    }

    public void randomMusic() {
        if (mpFragmentNum < 0) {
            return;
        }
        isOurChange = true;
        int num = mSongNum;
        while (num == mSongNum && musicInfos.size() > 1) {
            mSongNum = new Random().nextInt(musicInfos.size());
        }
        startMusic(context, mSongNum, mpFragmentNum);
    }

    public void singleMusic() {
        if (mpFragmentNum < 0) {
            return;
        }
        isOurChange = true;
        startMusic(context, mSongNum, mpFragmentNum);
    }

    public MusicInfo getSongInfo() {
        if (mSongNum >= 0 && musicInfos != null && musicInfos.size() > mSongNum) {
            MusicInfo info = musicInfos.get(mSongNum);
            return info == null ? null : info;
        }
        return null;
    }

    public String getSongTitle() {
        if (mSongNum >= 0 && musicInfos != null && musicInfos.size() > mSongNum && musicInfos.get(mSongNum) != null) {
            return musicInfos.get(mSongNum).getTitle();
        }
        return null;
    }

    public String getUrl() {
        if (mSongNum >= 0 && musicInfos != null && musicInfos.size() > mSongNum && musicInfos.get(mSongNum) != null) {
            return musicInfos.get(mSongNum).getUrl();
        }
        return null;
    }

    public int getFragmentNum() {
        if (mpFragmentNum >= 0)
            return mpFragmentNum;

        return 0;
    }

    public long getSongId() {
        if (musicInfos != null && musicInfos.size() > 0) {
            return musicInfos.get(mSongNum).getMusicId();
        }
        return 0;
    }

    public boolean isPlaying() {
        if (mediaPlayer == null) {
            return false;
        }
        return mediaPlayer.isPlaying();
    }

    public void continuePlay() {
        mediaPlayer.start();
        requestAudioFocus();
        isPause = false;
    }

    // 获取第几首
    public int getmSongNum() {
        return mSongNum;
    }

    // 是否未曾播放过
    public boolean isStarted() {
        return isStart;
    }

    public MusicInfo getMusicInfoNow() {
        return musicInfos.get(mSongNum);
    }

    public boolean isPauseing() {
        return isPause;
    }

    public void setPlayMode(int i) {
        playMode = i;
    }

    public int getPlayMode() {
        return playMode;
    }

    // 是否被收藏
    public boolean checkIsCollcet() {
        for (MusicInfo musicInfo : MusicSys.getInstance().getCollectMusics()) {
            if (musicInfo.getTitle().equals(getSongTitle()) && musicInfo.getMusicId() == getSongId()) {
                return true;
            }
        }
        return false;

    }

    // 音频焦点事件
    @Override
    public void onAudioFocusChange(int focusChange) {
        if (isPauseByMyself || !isStart)
            return;

        switch (focusChange) {

            // 短暂失去焦点，允许低音量播放
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                pause();
                break;

            //  短暂失去焦点
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pause();
                break;

            // 重新获得焦点
            case AudioManager.AUDIOFOCUS_GAIN:
                if (isPause) {
                    if (!isFocus)
                        requestAudioFocus();
                    mediaPlayer.start();
                    notifyObserver();
                    isPause = false;
                    YLog.i("TAG", "[onAudioFocusChange]");
                }
                isFocus = true;
                break;

            // 长期失去焦点
            case AudioManager.AUDIOFOCUS_LOSS:
                pause();
                break;

        }
    }

    private void notifyObserver() {
        if (updateInfo == null)
            return;

        updateInfo.setUpdateFragmentNum(mpFragmentNum);
        updateInfo.setUpdatePosition(mSongNum);
        updateInfo.setUpdateTitle(getSongTitle());
        updateInfo.setUrl(getUrl());

        if (updateInfo.getUpdateTitle() == null)
            return;

/*        Iterator iter = hashMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, ITaskCallback> entry = (Map.Entry) iter.next();
            entry.getValue().getBmpSuccess(info);
        }*/

        for (ITaskCallback i : hashMap.values()) {
            i.refreshInfo(updateInfo);
        }

    }

    public void onDestroy() {
        if (isStart) {
            musicInfos.get(mSongNum).setIsPlaying(0);
            isStart = false;
            musicInfos = null;
        }
        mSongNum = -1;
        instance = null;
    }

    public void refreshList(int position) {
        if (position < 0)
            return;

        if (mSongNum >= position)
            mSongNum--;
    }

}
