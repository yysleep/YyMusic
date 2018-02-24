package com.example.administrator.yymusic.sys;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.example.administrator.yymusic.api.ITaskCallback;
import com.example.administrator.yymusic.common.MusicConst;
import com.example.administrator.yymusic.dao.FavoriteDao;
import com.example.administrator.yymusic.dao.MusicDBMgr;
import com.example.administrator.yymusic.model.MusicInfo;
import com.example.administrator.yymusic.model.UpdateInfo;
import com.example.administrator.yymusic.util.LogUtil;

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
    public static volatile boolean sIsPauseByMyself;
    private static List<MusicInfo> sMusicInfoList;
    private static volatile MediaPlayer sMediaPlayer;

    private int mSongNum = -1;
    private int mpFragmentNum = -1;
    private boolean isPause;
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
                    sMediaPlayer = new MediaPlayer();
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
        LogUtil.i(TAG, "[removeMusicinfo] fragmentNum = " + mpFragmentNum + "  mSongNum = " + mSongNum);
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
        sMusicInfoList = lists;
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
        if (isPlaying() && sMusicInfoList != null && sMusicInfoList.size() > mSongNum) {
            path = sMusicInfoList.get(mSongNum).getUrl();
        }
        sMusicInfoList = null;
        switch (mpFragmentNum) {
            case FRAGMENT_LOCAL:
                sMusicInfoList = MusicSys.getInstance().getLocalMusics();
                break;
            case FRAGMENT_COLLECT:
                sMusicInfoList = MusicSys.getInstance().getCollectMusics();
                break;
            case FRAGMENT_DISCOVER:
                sMusicInfoList = MusicSys.getInstance().getLocalMusics();
                break;
            default:
                sMusicInfoList = MusicSys.getInstance().getLocalMusics();
                break;
        }
        if (path == null)
            return;

        for (int i = 0; i < sMusicInfoList.size(); i++) {
            if (sMusicInfoList.get(i).getUrl().equals(path)) {
                mSongNum = i;
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
                sMediaPlayer.start();
                isPause = false;
                isOurChange = false;
                if (!isStart) {
                    isStart = true;
                }
                // 播放完成后的监听
                sMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
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
                LogUtil.i(TAG, "[startMusic] 播放异常了 e = " + e.toString());
            }
            notifyObserver();
            return;
        }
        String lastName = null;
        if (isStart && !isOurChange) {
            lastName = sMusicInfoList.get(mSongNum).getDis_name();
        }
        if (mpFragmentNum != fragmentNum) {
            mpFragmentNum = fragmentNum;
            switch (fragmentNum) {
                case FRAGMENT_LOCAL:
                    sMusicInfoList = MusicSys.getInstance().getLocalMusics();
                    break;
                case FRAGMENT_COLLECT:
                    sMusicInfoList = MusicSys.getInstance().getCollectMusics();
                    break;
                case FRAGMENT_DISCOVER:
                    sMusicInfoList = MusicSys.getInstance().getLocalMusics();
                    break;
                default:
                    sMusicInfoList = MusicSys.getInstance().getLocalMusics();
                    break;
            }
        }

        mSongNum = position;
        if ((sMediaPlayer.isPlaying() || isPauseing()) && lastName != null && lastName.equals(sMusicInfoList.get(position).getDis_name())) {
            if (isPauseing()) {
                sMediaPlayer.start();
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
            sMediaPlayer.reset();
            sMediaPlayer.setDataSource(sMusicInfoList.get(mSongNum).getUrl());
            sMediaPlayer.prepare();
            sMediaPlayer.start();
            isPause = false;
            isOurChange = false;

            if (!isStart) {
                isStart = true;
            }
            notifyObserver();

            // 播放完成后的监听
            sMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
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
            LogUtil.i(TAG, "[startMusic] 播放异常 e1 = " + e.toString());
        }
    }

    public void pause() {
        if (isPause)
            return;

        sMediaPlayer.pause();
        notifyObserver();
        isFocus = false;
        isPause = true;
        LogUtil.i(TAG, "[pause]");
    }

    public MediaPlayer getMediaPlayer() {
        return sMediaPlayer;
    }

    // 播放下一首
    public void nextMusic() {
        if (mpFragmentNum < 0 || sMusicInfoList == null) {
            return;
        }
        isOurChange = true;
        mSongNum = mSongNum == sMusicInfoList.size() - 1 ? 0 : mSongNum + 1;
        startMusic(context, mSongNum, mpFragmentNum);
//        MainSys.getInstance().useCallback(MusicDetailActivity.class.getName(), 1);
    }

    // 播放上一首
    public void lastMusic() {
        if (mpFragmentNum < 0) {
            return;
        }
        isOurChange = true;
        mSongNum = mSongNum == 0 ? sMusicInfoList.size() - 1 : mSongNum - 1;
        startMusic(context, mSongNum, mpFragmentNum);
//        MainSys.getInstance().useCallback(MusicDetailActivity.class.getName(), 1);
    }

    public void randomMusic() {
        if (mpFragmentNum < 0) {
            return;
        }
        isOurChange = true;
        int num = mSongNum;
        while (num == mSongNum && sMusicInfoList.size() > 1) {
            mSongNum = new Random().nextInt(sMusicInfoList.size());
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
        if (mSongNum >= 0 && sMusicInfoList != null && sMusicInfoList.size() > mSongNum) {
            MusicInfo info = sMusicInfoList.get(mSongNum);
            return info == null ? null : info;
        }
        return null;
    }

    public String getSongTitle() {
        if (mSongNum >= 0 && sMusicInfoList != null && sMusicInfoList.size() > mSongNum && sMusicInfoList.get(mSongNum) != null) {
            return sMusicInfoList.get(mSongNum).getTitle();
        }
        return null;
    }

    public String getUrl() {
        if (mSongNum >= 0 && sMusicInfoList != null && sMusicInfoList.size() > mSongNum && sMusicInfoList.get(mSongNum) != null) {
            return sMusicInfoList.get(mSongNum).getUrl();
        }
        return null;
    }

    public int getFragmentNum() {
        if (mpFragmentNum >= 0)
            return mpFragmentNum;

        return 0;
    }

    public long getSongId() {
        if (sMusicInfoList != null && sMusicInfoList.size() > 0) {
            return sMusicInfoList.get(mSongNum).getMusicId();
        }
        return 0;
    }

    public boolean isPlaying() {
        if (sMediaPlayer == null) {
            return false;
        }
        return sMediaPlayer.isPlaying();
    }

    public void continuePlay() {
        sMediaPlayer.start();
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
        return sMusicInfoList.get(mSongNum);
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
        if (sIsPauseByMyself || !isStart)
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
                    sMediaPlayer.start();
                    notifyObserver();
                    isPause = false;
                    LogUtil.i("TAG", "[onAudioFocusChange]");
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
            sMusicInfoList.get(mSongNum).setIsPlaying(0);
            isStart = false;
            sMusicInfoList = null;
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
