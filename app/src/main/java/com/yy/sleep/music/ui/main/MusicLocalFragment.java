package com.yy.sleep.music.ui.main;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.yy.sleep.music.R;
import com.yy.sleep.music.api.IFileOperationCallback;
import com.yy.sleep.music.api.ITaskInterface;
import com.yy.sleep.music.common.MusicConst;
import com.yy.sleep.music.model.MusicInfo;
import com.yy.sleep.music.model.UpdateInfo;
import com.yy.sleep.music.sys.MusicPlayer;
import com.yy.sleep.music.sys.MusicSys;
import com.yy.sleep.music.tool.FileOperationTask;
import com.yy.sleep.music.tool.MusicAdapter;
import com.yy.sleep.music.ui.base.BaseFragment;
import com.yy.sleep.music.ui.detail.MusicDetailActivity;
import com.yy.sleep.music.util.LogUtil;
import com.yy.sleep.music.constant.YYConstant;

/**
 * Created by Administrator on 2016/5/25.
 *
 * @author yysleep
 */
public class MusicLocalFragment extends BaseFragment implements ITaskInterface {

    private View view;
    private MusicAdapter musicApapter;
    Handler handler;
    private MusicInfo mInfo;
    private int mPosition = -1;
    private FileOperationTask mfileTask;
    private IFileOperationCallback callback;

    public MusicLocalFragment() {
    }

    public void setCallback(IFileOperationCallback callback) {
        this.callback = callback;
    }

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MusicConst.ACTION_UPDATE_ALL_MUSIC_LIST)) {
                if (musicApapter == null)
                    return;
                musicApapter.musicInfos = MusicSys.getInstance().getLocalMusics();
                musicApapter.setOutsideChange(true);
                musicApapter.notifyDataSetChanged();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_local_music, container, false);
        }
        initView();
        return view;
    }

    public void initView() {
        ListView lvMusic = (ListView) view.findViewById(R.id.music_local_frgment_lv);
        musicApapter = new MusicAdapter(getActivity(), MusicSys.getInstance().getLocalMusics(), lvMusic, TAG());
        handler = new Handler();
        IntentFilter intentFilter = new IntentFilter(MusicConst.ACTION_UPDATE_ALL_MUSIC_LIST);
        getActivity().registerReceiver(updateReceiver, intentFilter);
        if (lvMusic != null) {
            lvMusic.setAdapter(musicApapter);
            lvMusic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (MusicSys.getInstance().getLocalMusics().get(i).getIsPlaying() == 1) {
                        startActivity(new Intent(getActivity(), MusicDetailActivity.class));
                        return;
                    }

                    MusicPlayer.getInstance().startMusic(i, 0);
                    MusicPlayer.sIsPauseByMyself = false;
                }
            });

            lvMusic.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    mInfo = (MusicInfo) parent.getAdapter().getItem(position);
                    if (mInfo != null && position >= 0) {
                        showAlert(position);
                        /*if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ((MainActivity) getActivity()).showAlert(YYConstant.READ_PERMISSION);
                        } else {
                            showAlert(position);
                        }*/
                    }
                    return true;
                }
            });

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (musicApapter.getItem(musicApapter.getCount() - 1).getBitmap() == null) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            musicApapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        }
    }

    @Override
    protected String TAG() {
        return "MusicLocalFragment";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(updateReceiver);
        if (musicApapter != null) {
            musicApapter.clear();
            musicApapter = null;
        }
    }

    @Override
    public void refreshInfo(UpdateInfo info) {
        if (info == null || info.getUpdateTitle() == null)
            return;

        if (musicApapter == null) {
            return;
        }

        LogUtil.i(TAG(), "[refreshInfo]" + info.toString());
        // 判断是否有歌曲显示为播放状态
        boolean isUpdate = false;
        if (info.getUpdateFragmentNum() != 0) {
            for (MusicInfo musicInfo : musicApapter.musicInfos) {
                if (musicInfo.getIsPlaying() != 0) {
                    musicInfo.setIsPlaying(0);
                    isUpdate = true;
                    break;
                }
            }
            if (isUpdate) {
                musicApapter.notifyDataSetChanged();
            }
            return;
        }
        if (info.getUpdatePosition() < 0) {
            return;
        }
        for (MusicInfo musicInfo : musicApapter.musicInfos) {
            if (musicInfo.getIsPlaying() != 0) {
                musicInfo.setIsPlaying(0);
            }
        }
        LogUtil.i(TAG(), "[refreshInfo] setIsPlaying 1");
        musicApapter.musicInfos.get(info.getUpdatePosition()).setIsPlaying(MusicInfo.IS_PLAYING);
        musicApapter.notifyDataSetChanged();

    }

    @Override
    public void getBmpSuccess(Bitmap cover, String url) {
        if (cover != null)
            return;

        if (musicApapter != null)
            musicApapter.downLoadSuccess(url);
    }

    private void showAlert(final int position) {
        AlertDialog alertDialog = new AlertDialog.Builder(MusicLocalFragment.this.getActivity(), R.style.YMAlertDialogStyle).
                setTitle("是否删除文件").
                setIcon(R.mipmap.icon_yymusic_launcher).
                setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogUtil.d(TAG(), "[showAlert] info = " + mInfo);
                        if (mInfo.getIsPlaying() == 1) {
                            Toast.makeText(MusicLocalFragment.this.getActivity(), "无法删除正在播放的歌曲", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mPosition = position;
                        mfileTask = new FileOperationTask(MusicLocalFragment.this);
                        String task[] = {FileOperationTask.DELETE_FILE, mInfo.getUrl()};
                        mfileTask.execute(task);

                    }
                }).
                setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).
                create();
        alertDialog.show();
    }

    @Override
    public void refreshDeleteFile(Boolean reslut) {
        if (reslut && mPosition >= 0) {
            String path = mInfo.getUrl();
            musicApapter.musicInfos.remove(mInfo);
            if (MusicPlayer.getInstance().getFragmentNum() == MusicPlayer.FRAGMENT_LOCAL) {
                MusicPlayer.getInstance().refreshList(mPosition);
                LogUtil.d(TAG(), "[refreshDeleteFile] 当前列表为 本地列表");
            }
            musicApapter.notifyDataSetChanged();
            if (callback != null)
                callback.syncList(path);

            Toast.makeText(MusicLocalFragment.this.getActivity(), "已删除", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MusicLocalFragment.this.getActivity(), "删除失败,该文件正在播放 或者正在被操作", Toast.LENGTH_SHORT).show();
        }
        if (mfileTask != null)
            mfileTask = null;

    }

}
