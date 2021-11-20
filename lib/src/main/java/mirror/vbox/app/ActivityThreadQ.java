package mirror.vbox.app;

import android.os.IBinder;

import java.util.List;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefMethod;
import mirror.RefObject;

public class ActivityThreadQ {
    public static Class<?> Class = RefClass.load(ActivityThreadQ.class, "android.app.ActivityThread");
    @MethodParams({IBinder.class, List.class})
    public static RefMethod<Void> handleNewIntent;

    public static class ActivityClientRecord {
        public static Class<?> TYPE =
                RefClass.load(ActivityClientRecord.class, "android.app.ActivityThread$ActivityClientRecord");
        public static RefObject<Boolean> isTopResumedActivity;
    }

}
