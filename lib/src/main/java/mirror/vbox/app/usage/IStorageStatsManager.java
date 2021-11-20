package mirror.vbox.app.usage;

import android.os.IBinder;
import android.os.IInterface;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;
import mirror.vbox.bluetooth.IBluetoothManager;

/**
 * @author Lody
 */
public class IStorageStatsManager {
    public static Class<?> TYPE = RefClass.load(IBluetoothManager.class, "android.app.usage.IStorageStatsManager");

    public static class Stub {
        public static Class<?> TYPE = RefClass.load(IBluetoothManager.Stub.class, "android.app.usage.IStorageStatsManager$Stub");
        @MethodParams({IBinder.class})
        public static RefStaticMethod<IInterface> asInterface;
    }
}
