package com.yy.sleep.music.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import com.yy.sleep.music.R;
import com.yy.sleep.music.ui.main.MainActivity;

/**
 * Created by Administrator on 2016/5/22.
 * @author yysleep
 */
public class SplashActivity extends AppCompatActivity {
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        progressBar = (ProgressBar) findViewById(R.id.splasd_pb);
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();

//        new AsyncTask<Void, Void, Void>() {
//
//
//            @Override
//            protected Void doInBackground(Void... params) {
//                while (true) {
//                    if (MusicSys.getInstance().getLocalMusics() == null || MusicSys.getInstance().getLocalMusics().size() == 0) {
//                        try {
//                            Thread.sleep(200);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    } else {
//                        break;
//                    }
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//                startActivity(new Intent(SplashActivity.this, MainActivity.class));
//                finish();
//            }
//        }.execute();

    }


}
