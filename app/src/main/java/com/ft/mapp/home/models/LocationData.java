package com.ft.mapp.home.models;

import com.fun.vbox.remote.vloc.VLocation;

public class LocationData {
    public String packageName;
    public int userId;
    public int mode;
    public VLocation location;

    public LocationData() {
    }

    public LocationData(String pkgName, int userId) {
        this.packageName = pkgName;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "LocationData{" +
                "packageName='" + packageName + '\'' +
                ", userId=" + userId +
                ", location=" + location +
                '}';
    }
}
