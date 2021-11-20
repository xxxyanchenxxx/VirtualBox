package com.ft.mapp.widgets;

import com.ft.mapp.VApp;

public class ViewHelper {

    public static int dip2px(float dpValue) {
        final float scale = VApp.getApp().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
