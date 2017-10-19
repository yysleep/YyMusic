package com.example.administrator.yymusic.tool;

import android.os.AsyncTask;

import com.example.administrator.yymusic.model.WeatherInfo;
import com.example.administrator.yymusic.util.YLog;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by yysleep on 17-10-19.
 */

public class WeatherTask extends AsyncTask<String, Void, WeatherInfo> {

    private static final String TAG = "WeatherTask";

    private ITaskWeather mITaskWeather;

    public interface ITaskWeather {
        void weatherRespond(WeatherInfo info);
    }

    public WeatherTask(ITaskWeather iTaskWeather) {
        mITaskWeather = iTaskWeather;
    }

    @Override
    protected WeatherInfo doInBackground(String... strings) {
        if (strings[0] == null)
            return null;

        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(strings[0])
                .build();
        Call call = httpClient.newCall(request);
        Response response = null;
        try {
            response = call.execute();
            if (response == null)
                return null;
            if (response.body() == null)
                return null;
            String json = null;
            try {
                json = response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (json == null)
                return null;
            YLog.d(TAG, "[doInBackground] response = " + json);
            Gson gson = new Gson();
            WeatherInfo info = gson.fromJson(json, WeatherInfo.class);
            if (info != null)
                return info;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


        return null;
    }

    @Override
    protected void onPostExecute(WeatherInfo weatherInfo) {
        super.onPostExecute(weatherInfo);
        if (weatherInfo != null)
            mITaskWeather.weatherRespond(weatherInfo);
    }
}
