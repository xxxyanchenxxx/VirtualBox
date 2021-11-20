package mirror.vbox.os;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefStaticMethod;

public class Message {
    public static Class<?> TYPE = RefClass.load(Message.class, android.os.Message.class);
    @MethodParams({int.class})
    public static RefStaticMethod<Void> updateCheckRecycle;
}