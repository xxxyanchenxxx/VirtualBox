package com.fun.vbox.client.ipc;

import android.os.RemoteException;

import com.fun.vbox.client.env.VirtualRuntime;
import com.fun.vbox.client.hook.base.MethodProxy;
import com.fun.vbox.helper.Keep;
import com.fun.vbox.helper.utils.IInterfaceUtils;
import com.fun.vbox.remote.vloc.VCell;
import com.fun.vbox.remote.vloc.VLocation;
import com.fun.vbox.server.interfaces.IVirtualLocationManager;

import java.util.List;

@Keep
public class VirtualLocationManager {

    private static final VirtualLocationManager sInstance = new VirtualLocationManager();

    public static final int MODE_CLOSE = 0;
    public static final int MODE_USE_GLOBAL = 1;
    public static final int MODE_USE_SELF = 2;

    public static VirtualLocationManager get() {
        return sInstance;
    }

    private IVirtualLocationManager mService;

    public IVirtualLocationManager getService() {
        if (mService == null || !IInterfaceUtils.isAlive(mService)) {
            synchronized (this) {
                Object binder = getRemoteInterface();
                mService = LocalProxyUtils.genProxy(IVirtualLocationManager.class, binder);
            }
        }
        return mService;
    }

    private Object getRemoteInterface() {
        return IVirtualLocationManager.Stub
                .asInterface(ServiceManagerNative.getService(ServiceManagerNative.VIRTUAL_LOC));
    }

    public int getMode(int userId, String pkg) {
        try {
            return getService().getMode(userId, pkg);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e, 0);
        }
    }

    public int getMode() {
        return getMode(MethodProxy.getAppUserId(), MethodProxy.getAppPkg());
    }

    public boolean isUseVirtualLocation(int userId, String pkg){
        return getMode(userId, pkg) != MODE_CLOSE;
    }

    public void setMode(int userId, String pkg, int mode) {
        try {
            getService().setMode(userId, pkg, mode);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public boolean getEnableHook(int userId, String pkg) {
        try {
            return getService().getEnableHook(userId, pkg);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e, false);
        }
    }

    public void setEnableHook(int userId, String pkg, boolean enable) {
        try {
            getService().setEnableHook(userId, pkg, enable);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public void setCell(int userId, String pkg, VCell cell) {
        try {
            getService().setCell(userId, pkg, cell);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public void setAllCell(int userId, String pkg, List<VCell> cell) {
        try {
            getService().setAllCell(userId, pkg, cell);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public void setNeighboringCell(int userId, String pkg, List<VCell> cell) {
        try {
            getService().setNeighboringCell(userId, pkg, cell);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public VCell getCell(int userId, String pkg) {
        try {
            return getService().getCell(userId, pkg);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e, null);
        }
    }

    public List<VCell> getAllCell(int userId, String pkg) {
        try {
            return getService().getAllCell(userId, pkg);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e, null);
        }
    }

    public List<VCell> getNeighboringCell(int userId, String pkg) {
        try {
            return getService().getNeighboringCell(userId, pkg);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e, null);
        }
    }


    public void setGlobalCell(VCell cell) {
        try {
            getService().setGlobalCell(cell);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public void setGlobalAllCell(List<VCell> cell) {
        try {
            getService().setGlobalAllCell(cell);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public void setGlobalNeighboringCell(List<VCell> cell) {
        try {
            getService().setGlobalNeighboringCell(cell);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public void setLocation(int userId, String pkg, VLocation loc) {
        try {
            getService().setLocation(userId, pkg, loc);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public VLocation getLocation(int userId, String pkg) {
        try {
            return getService().getLocation(userId, pkg);
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e, null);
        }
    }

    public VLocation getLocation() {
        return getLocation(MethodProxy.getAppUserId(), MethodProxy.getAppPkg());
    }

    public void setGlobalLocation(VLocation loc) {
        try {
            getService().setGlobalLocation(loc);
        } catch (RemoteException e) {
            VirtualRuntime.crash(e);
        }
    }

    public VLocation getGlobalLocation() {
        try {
            return getService().getGlobalLocation();
        } catch (RemoteException e) {
            return VirtualRuntime.crash(e, null);
        }
    }
}
