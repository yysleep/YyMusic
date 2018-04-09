package com.yy.sleep.music.sys;

import com.yy.sleep.music.model.LocationInfo;
import com.yy.sleep.music.model.WeatherInfo;

/**
 * Created by Administrator on 2017/11/9.
 */

public class WeatherSys {

    private volatile static WeatherSys instance;
    private WeatherInfo mWeatherInfo;
    private LocationInfo mLocationInfo;

    private WeatherSys() {

    }

    public static WeatherSys getInstance() {
        if (instance == null) {
            synchronized (WeatherSys.class) {
                if (instance == null)
                    instance = new WeatherSys();
            }
        }
        return instance;
    }

    public WeatherInfo getWeatherInfo() {
        return mWeatherInfo;
    }

    public void setWeatherInfo(WeatherInfo mWeatherInfo) {
        this.mWeatherInfo = mWeatherInfo;
    }

    public LocationInfo getLocationInfo() {
        return mLocationInfo;
    }

    public void setLocationInfo(LocationInfo mLocationInfo) {
        this.mLocationInfo = mLocationInfo;
    }

    public String getStreetDetail() {
        if (mLocationInfo != null)
            return mLocationInfo.getDistrict() + "   " + mLocationInfo.getStreet();
        return "weizhi";
    }

    public String getTemperature(WeatherInfo.Data.Forecast forecast) {
        if (forecast != null) {
            return getHighTemperature(forecast) + " /" + getLowTemperature(forecast);
        }
        return " -- ";
    }

    public String getSimpleDay(WeatherInfo.Data.Forecast forecast){
        if (forecast != null) {
            return forecast.getDate().substring(3);
        }
        return " -- ";
    }

    public String getHighTemperature(WeatherInfo.Data.Forecast forecast) {
        if (forecast != null) {
            return forecast.getHigh().substring(2);
        }
        return " -- ";
    }

    public String getLowTemperature(WeatherInfo.Data.Forecast forecast) {
        if (forecast != null) {
            return forecast.getLow().substring(2);
        }
        return " -- ";
    }

    public String getWeatherDetail(WeatherInfo.Data.Forecast forecast) {
        if (forecast != null) {
            return forecast.getType() + "  |  " + "空气" + getApiState(forecast.getAqi()) + "  " + forecast.getAqi() + "  >";
        }

        return " --- ";
    }

    public String getApiState(int api) {
        String apiState = "优";
        if (api <= 50) {
            apiState = "优";
        } else if (api <= 100) {
            apiState = "良";
        } else if (api <= 150) {
            apiState = "轻微污染";
        } else if (api <= 200) {
            apiState = "轻度污染";
        } else if (api <= 300) {
            apiState = "中度重污染";
        } else {
            apiState = "重度污染";
        }
        return apiState;
    }
}
