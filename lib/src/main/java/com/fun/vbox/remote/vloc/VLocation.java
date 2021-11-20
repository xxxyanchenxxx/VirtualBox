package com.fun.vbox.remote.vloc;

import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;

import com.fun.vbox.client.env.VirtualGPSSatalines;
import com.fun.vbox.helper.utils.Reflect;

import java.util.Random;

/**
 * @author Lody
 */

public class VLocation implements Parcelable {

    public double latitude = 0.0;
    public double longitude = 0.0;
    public double altitude = 0.0f;
    public float accuracy = 0.0f;
    public float speed;
    public float bearing;
    public String address;
    public String address_province;
    public String address_city;
    public String address_district;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeDouble(this.altitude);
        dest.writeFloat(this.accuracy);
        dest.writeFloat(this.speed);
        dest.writeFloat(this.bearing);
        dest.writeString(this.address);
        dest.writeString(this.address_province);
        dest.writeString(this.address_city);
        dest.writeString(this.address_district);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public VLocation() {
    }

    public VLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public VLocation(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.altitude = in.readDouble();
        this.accuracy = in.readFloat();
        this.speed = in.readFloat();
        this.bearing = in.readFloat();
        try {
            this.address = in.readString();
            this.address_province = in.readString();
            this.address_city = in.readString();
            this.address_district = in.readString();
        } catch (Throwable e) {
            Log.e("myne", "", e);
        }
    }

    public boolean isEmpty() {
        return latitude == 0 && longitude == 0;
    }

    public static final Parcelable.Creator<VLocation> CREATOR = new Parcelable.Creator<VLocation>() {
        @Override
        public VLocation createFromParcel(Parcel source) {
            return new VLocation(source);
        }

        @Override
        public VLocation[] newArray(int size) {
            return new VLocation[size];
        }
    };

    @Override
    public String toString() {
        return "VLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", accuracy=" + accuracy +
                ", speed=" + speed +
                ", bearing=" + bearing +
                '}';
    }

    public Location toSysLocation() {
        int nextInt = new Random().nextInt(10);
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setAccuracy(0.1f);
        Bundle extraBundle = new Bundle();
        location.setBearing(bearing);
        Reflect.on(location).call("setIsFromMockProvider", false);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setSpeed(speed);

        location.setAccuracy((float) ((nextInt / 5.676053805E-315d) + 8.0f));
        location.setAltitude((nextInt / 1000.0d) + 5.2d);
        location.setBearing((float) (((nextInt / 9.9f) * 5.611943214E-315d) + 0.0f));
        location.setSpeed((nextInt / 900.0f) + 0.0f);
        location.setAccuracy((float) ((nextInt / 5.676053805E-315d) + 8.0f));

        location.setTime(System.currentTimeMillis());
        int svCount = VirtualGPSSatalines.get().getSvCount();
        extraBundle.putInt("satellites", svCount);
        extraBundle.putInt("satellitesvalue", svCount);
        location.setExtras(extraBundle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                Reflect.on(location).call("makeComplete");
            } catch (Exception e){
                location.setTime(System.currentTimeMillis());
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }
        }
        return location;
    }
}
