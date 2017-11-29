package com.example.administrator.yymusic.ui.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.yymusic.R;
import com.example.administrator.yymusic.api.ITaskInterface;
import com.example.administrator.yymusic.common.MusicConst;
import com.example.administrator.yymusic.dao.FavoriteDao;
import com.example.administrator.yymusic.dao.MusicDBMgr;
import com.example.administrator.yymusic.model.MusicInfo;
import com.example.administrator.yymusic.model.UpdateInfo;
import com.example.administrator.yymusic.sys.MusicPlayer;
import com.example.administrator.yymusic.sys.MusicSys;
import com.example.administrator.yymusic.tool.MusicAdapter;
import com.example.administrator.yymusic.ui.base.BaseFragment;
import com.example.administrator.yymusic.ui.detail.MusicDetailActivity;
import com.example.administrator.yymusic.util.YLog;

/**
 * Created by Administrator on 2016/5/25.
 *
 * @author yysleep
 */
public class MusicCollectFragment extends BaseFragment implements ITaskInterface {

    private View view;
    private MusicAdapter musicApapter;
    ListView lvMusic;
    TextView tvCollect;
    private int mPosition;
    private Activity mActivity;

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null)
                return;
            if (intent.getAction().equals(MusicConst.ACTION_UPDATE_ALL_MUSIC_LIST)) {
                if (musicApapter == null)
                    return;
                musicApapter.musicInfos = MusicSys.getInstance().getCollectMusics();
                if (musicApapter.musicInfos.size() > 0 && lvMusic != null && tvCollect != null) {
                    lvMusic.setVisibility(View.VISIBLE);
                    tvCollect.setVisibility(View.GONE);
                }
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
        mActivity = getActivity();
        if (mActivity != null)
            initView();
        YLog.d(TAG(), "[onCreateView] ");
        return view;
    }

    public void initView() {
        lvMusic = view.findViewById(R.id.music_local_frgment_lv);
        tvCollect = view.findViewById(R.id.fragment_local_collect_tv);
        IntentFilter intentFilter = new IntentFilter(MusicConst.ACTION_UPDATE_ALL_MUSIC_LIST);
        mActivity.registerReceiver(updateReceiver, intentFilter);
        musicApapter = new MusicAdapter(getActivity(), MusicSys.getInstance().getCollectMusics(), lvMusic, TAG());
        if (MusicSys.getInstance().getCollectMusics().size() <= 0) {
            lvMusic.setVisibility(View.GONE);
            tvCollect.setVisibility(View.VISIBLE);
            tvCollect.setText(R.string.collect_default_name);
        }
        if (lvMusic != null) {
            lvMusic.setAdapter(musicApapter);
            lvMusic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (MusicSys.getInstance().getCollectMusics().get(i).getIsPlaying() == 1) {
                        startActivity(new Intent(getActivity(), MusicDetailActivity.class));
                        return;
                    }

                    MusicPlayer.getInstance().startMusic(mActivity.getApplicationContext(), i, 1);
                    MusicPlayer.sIsPauseByMyself = false;
                }
            });
            lvMusic.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    MusicInfo info = (MusicInfo) parent.getAdapter().getItem(position);
                    if (info != null && position >= 0)
                        mPosition = position;
                    showAlert(info);
                    return true;
                }
            });
        }
    }

    @Override
    protected String TAG() {
        return "MusicCollectFragment";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mActivity != null)
            mActivity.unregisterReceiver(updateReceiver);
    }

    @Override
    public void refreshInfo(UpdateInfo info) {

        if (info == null || info.getUpdateTitle() == null)
            return;

        if (musicApapter == null) {
            return;
        }

        musicApapter.musicInfos = MusicSys.getInstance().getCollectMusics();
        if (musicApapter.musicInfos.size() > 0) {
            lvMusic.setVisibility(View.VISIBLE);
            tvCollect.setVisibility(View.GONE);
        } else {
            lvMusic.setVisibility(View.GONE);
            tvCollect.setVisibility(View.VISIBLE);
        }
        // 播放的item 颜色变化检测以及刷新
        // 判断是否有歌曲显示为播放状态
        if (info.getUpdateFragmentNum() != 1) {
            for (MusicInfo musicInfo : musicApapter.musicInfos) {
                if (musicInfo.getIsPlaying() != 0) {
                    musicInfo.setIsPlaying(0);
                    break;
                }
            }
            musicApapter.notifyDataSetChanged();
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
        musicApapter.musicInfos.get(info.getUpdatePosition()).setIsPlaying(MusicConst.PLAYING);
        musicApapter.notifyDataSetChanged();
    }

    private void showAlert(final MusicInfo info) {
        AlertDialog alertDialog = new AlertDialog.Builder(mActivity).
                setTitle("是否移除歌曲").
                setIcon(R.mipmap.icon_yymusic_launcher).
                setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        YLog.d(TAG(), "[showAlert] info = " + info);
                        if (info.getIsPlaying() == 1) {
                            Toast.makeText(MusicCollectFragment.this.getActivity(), "无法移除正在播放的歌曲", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        musicApapter.musicInfos.remove(info);
                        MusicDBMgr.getInstance().delete(FavoriteDao.TABLE_FAVORITE_MUSIC, info);
                        if (MusicPlayer.getInstance().getFragmentNum() == MusicPlayer.FRAGMENT_COLLECT) {
                            MusicPlayer.getInstance().refreshList(mPosition);
                            YLog.d(TAG(), "[showAlert] 当前列表为 收藏列表");
                        }
                        musicApapter.notifyDataSetChanged();

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
    public void getBmpSuccess(Bitmap cover, String url) {
        if (cover != null)
            return;

        if (musicApapter != null)
            musicApapter.downLoadSuccess(url);
    }

    @Override
    public void syncList(String path) {
        if (path == null || musicApapter.musicInfos == null || musicApapter.musicInfos.size() <= 0)
            return;
        MusicInfo info = null;
        for (MusicInfo i : musicApapter.musicInfos) {
            if (i.getUrl().equals(path)) {
                info = i;
                break;
            }

        }
        if (info != null) {
            musicApapter.musicInfos.remove(info);
            MusicDBMgr.getInstance().delete(FavoriteDao.TABLE_FAVORITE_MUSIC, info);
            if (MusicPlayer.getInstance().getFragmentNum() == MusicPlayer.FRAGMENT_COLLECT) {
                MusicPlayer.getInstance().refreshList(mPosition);
                YLog.d(TAG(), "[syncList] 当前列表为 收藏列表");
            }
            musicApapter.notifyDataSetChanged();
        }

    }
}
