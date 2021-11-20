package mirror.vbox.content.pm;

import android.annotation.TargetApi;
import android.os.IInterface;
import mirror.RefClass;
import mirror.RefObject;

@TargetApi(28)
public class CrossProfileApps {
    public static Class<?> TYPE = RefClass.load(CrossProfileApps.class, "android.content.pm.CrossProfileApps");
    public static RefObject<IInterface> mService;
}