package mirror.vbox.app;

import android.os.IInterface;

import mirror.RefClass;
import mirror.RefStaticMethod;
import mirror.RefStaticObject;

public class UriGrantsManager {
    public static RefStaticObject<Object> IUriGrantsManagerSingleton;
    public static Class<?> TYPE = RefClass
            .load(UriGrantsManager.class, "android.app.UriGrantsManager");
    public static RefStaticMethod<IInterface> getService;
}