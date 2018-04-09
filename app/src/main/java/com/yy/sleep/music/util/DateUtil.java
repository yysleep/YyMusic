package com.yy.sleep.music.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by YySleep on 2018/2/5.
 *
 * @author YySleep
 */

public class DateUtil {

    private final static String TAG = "DateUtil";
    private static SimpleDateFormat sDeteTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static SimpleDateFormat sDeteFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    /**
     * 获取当前Date Time
     */
    public static String getCurDateTime() {

        return sDeteTimeFormat.format(new java.util.Date());
    }

    /**
     * 获取当前Date
     */
    public static String getCurDate() {

        return sDeteFormat.format(new java.util.Date());
    }

    /**
     * 获取当前时间（精确到秒）
     */
    public static String getDateTimeS() {

        return sFormat.format(new java.util.Date());
    }

    /**
     * 获取昨天时间
     */
    public static String getYesterdayDate() {
        long now = System.currentTimeMillis();
        long oneDay = 60 * 60 * 24 * 1000;
        LogUtil.d(TAG, "[getYesterdayDate] now = " + now);
        Date date = new Date(now - oneDay);
        return sDeteFormat.format(date);
    }

    /**
     * 时间戳转换成字符窜
     */
    public static String getDateToString(long milSecond) {
        Date date = new Date(milSecond);
        return sDeteTimeFormat.format(date);
    }

    /**
     * 将字符串转为时间戳
     */
    public static long getStringToDate(String dateString) {
        Date date = new Date();
        try {
            date = sDeteTimeFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    /**
     * 获取合理的日期时间（不大于当前时间）
     */
    public static String getLegalDateTime(String dateString) {
        try {
            long time = sDeteTimeFormat.parse(dateString).getTime();
            dateString = time < System.currentTimeMillis() ? dateString : getCurDateTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString;
    }
    /**
     * 获取合理的日期时间（不大于当前时间）
     */
    public static String getLegalDate(String dateString) {
        try {
            long time = sDeteFormat.parse(dateString).getTime();
            dateString = time < System.currentTimeMillis() ? dateString : getCurDate();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString;
    }

    public static String dateToPath(String dateString) {
        if (dateString == null)
            return null;
        return dateString.replace("-", "").replace(" ", "").replace(":", "").trim();
    }

    // 去除秒数
    public static String getShortDate(String date) {
        if (date == null) {
            return "";
        }
        if (date.length() > 17) {
            date = date.substring(0, 16);
        }
        return date;
    }


}
