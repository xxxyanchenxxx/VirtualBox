package com.fun.vbox.server.device;

import android.os.Parcel;

import com.fun.vbox.helper.PersistenceLayer;
import com.fun.vbox.helper.collection.SparseArray;
import com.fun.vbox.os.VEnvironment;
import com.fun.vbox.remote.VDeviceConfig;

/**
 * @author Lody
 */

public class DeviceInfoPersistenceLayer extends PersistenceLayer {

    private VDeviceManagerService mService;

    DeviceInfoPersistenceLayer(VDeviceManagerService service) {
        super(VEnvironment.getDeviceInfoFile());
        this.mService = service;
    }

    @Override
    public int getCurrentVersion() {
        return VDeviceConfig.VERSION;
    }

    @Override
    public void writeMagic(Parcel p) {

    }

    @Override
    public boolean verifyMagic(Parcel p) {
        return true;
    }

    @Override
    public void writePersistenceData(Parcel p) {
        SparseArray<VDeviceConfig> infos = mService.mDeviceConfigs;
        int size = infos.size();
        p.writeInt(size);
        for (int i = 0; i < size; i++) {
            int userId = infos.keyAt(i);
            VDeviceConfig info = infos.valueAt(i);
            p.writeInt(userId);
            info.writeToParcel(p, 0);
        }
    }

    @Override
    public void readPersistenceData(Parcel p, int version) {
        SparseArray<VDeviceConfig> infos = mService.mDeviceConfigs;
        infos.clear();
        int size = p.readInt();
        while (size-- > 0) {
            int userId = p.readInt();
            VDeviceConfig info = new VDeviceConfig(p);
            infos.put(userId, info);
        }
    }
    
    @Override
    public void onPersistenceFileDamage() {
        getPersistenceFile().delete();
    }
}
