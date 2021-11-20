package mirror.vbox.webkit;

import mirror.RefClass;
import mirror.RefStaticMethod;
import mirror.RefStaticObject;

public class WebViewFactory {
    public static Class<?> TYPE =
            RefClass.load(WebViewFactory.class, "android.webkit.WebViewFactory");
    public static RefStaticMethod<Object> getUpdateService;
    public static RefStaticObject<Boolean> sWebViewSupported;
}
