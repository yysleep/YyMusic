package com.yy.sleep.music.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.yy.sleep.music.R;
import com.yy.sleep.music.api.ITaskInterface;
import com.yy.sleep.music.common.MusicConst;
import com.yy.sleep.music.model.UpdateInfo;
import com.yy.sleep.music.sys.MusicPlayer;
import com.yy.sleep.music.sys.MusicSys;
import com.yy.sleep.music.tool.MusicAdapter;
import com.yy.sleep.music.ui.base.BaseFragment;

/**
 * Created by Administrator on 2016/5/25.
 *
 * @author yysleep
 */
public class MusicDiscoverFragment extends BaseFragment implements ITaskInterface {

    private View view;

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(MusicConst.ACTION_UPDATE_ALL_MUSIC_LIST) && musicApapter != null) {
//            if(musicApapter==null)
//                return;
//            musicApapter.musicInfos = MusicSys.getInstance().getLocalMusics();
//            musicApapter.notifyDataSetChanged();
//            musicApapter.startTask();
//            }
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
        TextView tvDiscover = (TextView) view.findViewById(R.id.fragment_local_collect_tv);
        ListView lvMusic = (ListView) view.findViewById(R.id.music_local_frgment_lv);
        MusicAdapter musicApapter = new MusicAdapter(getActivity(), MusicSys.getInstance().getDiscoverMusics(), lvMusic, TAG());
        if (MusicSys.getInstance().getDiscoverMusics().size() <= 0) {
            lvMusic.setVisibility(View.GONE);
            tvDiscover.setVisibility(View.VISIBLE);
            tvDiscover.setText(R.string.discover_default_name);

        }
        getActivity().registerReceiver(updateReceiver, new IntentFilter(MusicConst.ACTION_UPDATE_ALL_MUSIC_LIST));
        if (lvMusic != null) {
            lvMusic.setAdapter(musicApapter);
            lvMusic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    MusicPlayer.getInstance().startMusic(i, 2);
                }
            });
        }
    }


    @Override
    protected String TAG() {
        return "MusicDiscoverFragment";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(updateReceiver);
    }

    @Override
    public void getBmpSuccess(Bitmap cover, String url) {
        if (cover != null)
            return;

//        if(musicApapter!=null)
//            musicApapter.getBmpSuccess(url);
    }

    @Override
    public void refreshInfo(UpdateInfo info) {

    }

    @Override
    public void refreshDeleteFile(Boolean result) {

    }
}
