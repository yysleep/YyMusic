package com.example.administrator.yymusic.sys;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Looper;

import com.example.administrator.yymusic.MusicApplication;
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
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/5/22.
 *
 * @author yysleep
 */
public class MusicPlayer implements AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    private static final String TAG = "MusicPlayer";
    public static volatile boolean sIsPauseByMyself;
    private List<MusicInfo> mMusicInfoList;
    private MediaPlayer mMediaPlayer;

    private int mSongNum = -1;
    private int mpFragmentNum = -1;
    private boolean isPause;
    // 由next 和last 触发的切换歌曲
    private boolean isOurChange;
    private Context mContext;
    private AudioManager audioManager;
    // 是否是焦点
    private boolean isFocus;
    private UpdateInfo updateInfo;
    private boolean isStart;
    private Timer mTimer;

    // 播放模式
    private int playMode = MusicConst.SEQUENTIAL_PLAY;

    public static final int FRAGMENT_LOCAL = 0;
    public static final int FRAGMENT_COLLECT = 1;
    public static final int FRAGMENT_DISCOVER = 2;

    private MusicPlayer() {
        init();
    }

    private void init() {
        mMediaPlayer = new MediaPlayer();
        hashMap = new HashMap<>();
        updateInfo = new UpdateInfo();
        mTimer = new Timer();
    }

    @SuppressLint("StaticFieldLeak")
    private volatile static MusicPlayer instance;

    public static MusicPlayer getInstance() {
        if (instance == null) {
            synchronized (MusicPlayer.class) {
                if (instance == null) {
                    instance = new MusicPlayer();
                }
            }
        }
        return instance;
    }

    private HashMap<String, ITaskCallback> hashMap;

    public void init(Context context) {
        mContext = context.getApplicationContext();
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

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
        LogUtil.d(TAG,"[addCollectMusic] musicInfo = " + musicInfo);
        for (MusicInfo info : MusicSys.getInstance().getCollectMusics()) {
            if (info.getUrl().equals(musicInfo.getUrl())) {
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
        mMusicInfoList = lists;
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
        if (isPlaying() && mMusicInfoList != null && mMusicInfoList.size() > mSongNum) {
            path = mMusicInfoList.get(mSongNum).getUrl();
        }
        mMusicInfoList = null;
        switch (mpFragmentNum) {
            case FRAGMENT_LOCAL:
                mMusicInfoList = MusicSys.getInstance().getLocalMusics();
                break;
            case FRAGMENT_COLLECT:
                mMusicInfoList = MusicSys.getInstance().getCollectMusics();
                break;
            case FRAGMENT_DISCOVER:
                mMusicInfoList = MusicSys.getInstance().getLocalMusics();
                break;
            default:
                mMusicInfoList = MusicSys.getInstance().getLocalMusics();
                break;
        }
        if (path == null)
            return;

        for (int i = 0; i < mMusicInfoList.size(); i++) {
            if (mMusicInfoList.get(i).getUrl().equals(path)) {
                mSongNum = i;
                return;
            }
        }
    }

    private void requestAudioFocus() {
        if (audioManager != null && !isFocus) {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            isFocus = true;
        }
    }

    public void startMusic(final int position, final int fragmentNum) {
        if (mContext == null) {
            LogUtil.e(TAG, "[startMusic] mContext = null");
            return;
        }

        requestAudioFocus();
        if (position == MusicConst.START_DEFAULT_POSITION && fragmentNum == MusicConst.START_DEFAULT_FRAGMENT) {
            // 这是由播放按键所触发的播放 并不是选取其中某个item
            try {
                mMediaPlayer.start();
                isPause = false;
                isOurChange = false;
                if (!isStart) {
                    isStart = true;
                }
            } catch (Exception e) {
                LogUtil.e(TAG, "[startMusic] 播放异常0");
                e.printStackTrace();
            }
            notifyObserver();
            return;
        }
        String lastName = null;
        if (isStart && !isOurChange) {
            lastName = mMusicInfoList.get(mSongNum).getDis_name();
        }
        if (mpFragmentNum != fragmentNum) {
            mpFragmentNum = fragmentNum;
            switch (fragmentNum) {
                case FRAGMENT_LOCAL:
                    mMusicInfoList = MusicSys.getInstance().getLocalMusics();
                    break;
                case FRAGMENT_COLLECT:
                    mMusicInfoList = MusicSys.getInstance().getCollectMusics();
                    break;
                case FRAGMENT_DISCOVER:
                    mMusicInfoList = MusicSys.getInstance().getLocalMusics();
                    break;
                default:
                    mMusicInfoList = MusicSys.getInstance().getLocalMusics();
                    break;
            }
        }

        mSongNum = position;
        if ((mMediaPlayer.isPlaying() || isPauseing()) && lastName != null && lastName.equals(mMusicInfoList.get(position).getDis_name())) {
            if (isPauseing()) {
                mMediaPlayer.start();
                isPause = false;
                isOurChange = false;
                if (!isStart) {
                    isStart = true;
                }
                notifyObserver();
                return;
            }
        }

        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.setOnCompletionListener(null);
        }
        mTimer.schedule(new Task(), 300);

        isPause = false;
        isOurChange = false;

        if (!isStart) {
            isStart = true;
        }
        notifyObserver();
    }

    public void pause() {
        if (isPause)
            return;

        mMediaPlayer.pause();
        notifyObserver();
        isFocus = false;
        isPause = true;
        LogUtil.i(TAG, "[pause]");
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    // 播放下一首
    public void nextMusic() {
        if (mpFragmentNum < 0 || mMusicInfoList == null) {
            return;
        }
        isOurChange = true;
        mSongNum = mSongNum == mMusicInfoList.size() - 1 ? 0 : mSongNum + 1;
        startMusic(mSongNum, mpFragmentNum);
    }

    // 播放上一首
    public void lastMusic() {
        if (mpFragmentNum < 0) {
            return;
        }
        isOurChange = true;
        mSongNum = mSongNum == 0 ? mMusicInfoList.size() - 1 : mSongNum - 1;
        startMusic(mSongNum, mpFragmentNum);
//        MainSys.getInstance().useCallback(MusicDetailActivity.class.getName(), 1);
    }

    private void randomMusic() {
        if (mpFragmentNum < 0) {
            return;
        }
        isOurChange = true;
        int num = mSongNum;
        while (num == mSongNum && mMusicInfoList.size() > 1) {
            mSongNum = new Random().nextInt(mMusicInfoList.size());
        }
        startMusic(mSongNum, mpFragmentNum);
    }

    private void singleMusic() {
        if (mpFragmentNum < 0) {
            return;
        }
        isOurChange = true;
        startMusic(mSongNum, mpFragmentNum);
    }

    public MusicInfo getSongInfo() {
        if (mSongNum >= 0 && mMusicInfoList != null && mMusicInfoList.size() > mSongNum) {
            return mMusicInfoList.get(mSongNum);
        }
        return null;
    }

    public String getSongTitle() {
        if (mSongNum >= 0 && mMusicInfoList != null && mMusicInfoList.size() > mSongNum && mMusicInfoList.get(mSongNum) != null) {
            return mMusicInfoList.get(mSongNum).getTitle();
        }
        return null;
    }

    public String getUrl() {
        if (mSongNum >= 0 && mMusicInfoList != null && mMusicInfoList.size() > mSongNum && mMusicInfoList.get(mSongNum) != null) {
            return mMusicInfoList.get(mSongNum).getUrl();
        }
        return null;
    }

    public int getFragmentNum() {
        if (mpFragmentNum >= 0)
            return mpFragmentNum;

        return 0;
    }

    public long getSongId() {
        if (mMusicInfoList != null && mMusicInfoList.size() > 0) {
            return mMusicInfoList.get(mSongNum).getMusicId();
        }
        return 0;
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public void continuePlay() {
        mMediaPlayer.start();
        requestAudioFocus();
        isPause = false;
    }

    // 是否未曾播放过
    public boolean isStarted() {
        return isStart;
    }

    private MusicInfo getMusicInfoNow() {
        return mMusicInfoList.get(mSongNum);
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
                    mMediaPlayer.start();
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
        if (Looper.getMainLooper() == Looper.myLooper()) {
            LogUtil.d(TAG, "[notifyObserver]" + Thread.currentThread().getName());
            for (ITaskCallback i : hashMap.values()) {
                i.refreshInfo(updateInfo);
            }
        } else {
            MusicApplication.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    LogUtil.d(TAG, "[notifyObserver]" + Thread.currentThread().getName());
                    for (ITaskCallback i : hashMap.values()) {
                        i.refreshInfo(updateInfo);
                    }
                }
            });
        }


    }

    public void onDestroy() {
        if (isStart) {
            mMusicInfoList.get(mSongNum).setIsPlaying(0);
            isStart = false;
            mMusicInfoList = null;
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

    @Override
    public void onCompletion(MediaPlayer mp) {
        LogUtil.d(TAG, "[onCompletion]");
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

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    private class Task extends TimerTask {

        @Override
        public void run() {
            LogUtil.d(TAG, "[Task][run]" + Thread.currentThread().getName());
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(mMusicInfoList.get(mSongNum).getUrl());
                mMediaPlayer.setOnPreparedListener(MusicPlayer.this);
                mMediaPlayer.setOnCompletionListener(MusicPlayer.this);
                mMediaPlayer.prepare();
            } catch (Exception e) {
                LogUtil.e(TAG, "[startMusic] 播放异常1");
                e.printStackTrace();
            }
        }
    }
}
