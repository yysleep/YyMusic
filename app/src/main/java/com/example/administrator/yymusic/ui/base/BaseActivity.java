package com.example.administrator.yymusic.ui.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.example.administrator.yymusic.R;
import com.example.administrator.yymusic.api.ITaskCallback;
import com.example.administrator.yymusic.api.ITaskInterface;
import com.example.administrator.yymusic.sys.LruCacheSys;
import com.example.administrator.yymusic.sys.MusicPlayer;

/**
 * Created by archermind on 16-10-5.
 *
 * @author yysleep
 */
public abstract class BaseActivity extends AppCompatActivity implements ITaskCallback, ITaskInterface {

    public abstract String TAG();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        MusicPlayer.getInstance().registMusicObserver(TAG(), this);
        LruCacheSys.getInstance(this).registMusicObserver(TAG(), this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        return id == R.id.main_tool_menu_item_one || id == R.id.main_tool_menu_item_two
                || id == R.id.main_tool_menu_item_three || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicPlayer.getInstance().unregisMusicObserver(TAG());
        LruCacheSys.getInstance(this).unregisMusicObserver(TAG());
    }

    @Override
    public void getBmpFaild() {

    }
}
