-dontshrink
-keepattributes *Annotation*,InnerClasses
-keepattributes Signature,EnclosingMethod
-keepclassmembers class * implements java.io.Serializable {*;}

-dontwarn android.**
-dontwarn com.android.**
-dontwarn mirror.**

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.ContentProvider

# Parcelable
-keep class * implements android.os.Parcelable {
  public *;
}

-keepclassmembers class * extends android.os.Binder{
    public <methods>;
}

-keep @interface com.fun.vbox.client.hook.annotations.** {*;}

-keepclasseswithmembernames class * {
    native <methods>;
}
# android
-keep class android.**{
    *;
}
-keep class mirror.** {*;}
-keepclassmembers class mirror.**{
   public *;
}

-keep class com.fun.vbox.client.VClient{
    public Application getCurrentApplication();
}

-keep class com.fun.vbox.client.NativeEngine{
    public <methods>;
}

-keep @interface com.fun.vbox.helper.Keep {*;}
-keep @com.fun.vbox.helper.Keep class **{
    public <methods>;
    public static final *;
}

-keep class com.fun.vbox.server.interfaces.IUiCallback {*;}
-keep class com.zb.vv.client.stub.ChooseTypeAndAccountActivity {
    public static final *;
}

-keep class com.fun.vbox.client.core.HostApp {*;}

-repackageclass z1

#-keepattributes SourceFile,LineNumberTable