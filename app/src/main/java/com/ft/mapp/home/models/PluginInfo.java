package com.ft.mapp.home.models;


public class PluginInfo {

    public PluginInfo(int srcId, int bgId, String name, String desc) {
        iconId = srcId;
        corlorId = bgId;
        this.name = name;
        this.desc = desc;
    }

    public int iconId;
    public int corlorId;
    public String name;
    public String desc;
    public boolean vip;


}
