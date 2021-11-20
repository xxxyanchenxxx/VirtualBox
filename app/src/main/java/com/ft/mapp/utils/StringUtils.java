package com.ft.mapp.utils;

import android.content.Context;

import com.ft.mapp.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class StringUtils {
    public static boolean isString(String tag) {
        if (tag != null && !"".equals(tag)) {
            return true;
        }
        return false;
    }


    public static String getTimeNoHour(long time) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
        calendar.setTimeInMillis(time);
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        int s = calendar.get(Calendar.SECOND);

        return String.format("%02d:%02d", h * 60 + m, s);
    }

    public static String getTime(long time) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
        calendar.setTimeInMillis(time);
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        int s = calendar.get(Calendar.SECOND);

        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public static String timeForString(Context context, long tag) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(tag);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        String[] strs = context.getResources().getStringArray(R.array.weeks);
        String str = strs[week - 1];
        SimpleDateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd [" + str + "] hh:mm"); //格式化当前系统日期
        return dateFm.format(tag);
    }
}
