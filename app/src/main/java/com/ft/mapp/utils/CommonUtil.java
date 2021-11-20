package com.ft.mapp.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.ft.mapp.BuildConfig;;
import com.ft.mapp.VApp;
import com.ft.mapp.VConstant;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.helper.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Pattern;

import androidx.core.content.FileProvider;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class CommonUtil {

    /**
     * 获得手机注册网络的所在国家代码(大写)，错误返回'??'
     */
    public static String getCountryCode(Context context) {
        if (context == null) {
            return "";
        }

        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (manager == null) {
            return "";
        }
        String country = manager.getNetworkCountryIso();
        if (country == null) {
            return "";
        }

        return country.toUpperCase(Locale.US);
    }

    /**
     * 检测当前是否连接到wifi
     */
    public static int getNetworkType(Context context) {
        if (context == null) {
            return -1;
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return -1;
        }

        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return -1;
        }
        return ni.getType();
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, 0) != null;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * 获得当前Apk的版本
     */
    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS).versionName;
        } catch (Exception e) {
            return "1.0.0";
        }
    }

    public static int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getCameraPath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + File.separator + "Camera";
    }

    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append(0);
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public static void launchAppMarket(String packageName, String marketPkg, String searchGameName) {
        if (TextUtils.isEmpty(packageName)) return;

        Uri uri;
        if (TextUtils.isEmpty(searchGameName)) {
            uri = Uri.parse("market://details?id=" + packageName);
        } else {
            uri = Uri.parse("market://search?q=" + searchGameName);
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            //intent.setPackage("com.android.vending");
            intent.setPackage("com.xiaomi.market");
            VApp.getApp().startActivity(intent);
        } catch (Exception e) {
            try {
                intent.setPackage(marketPkg);
                VApp.getApp().startActivity(intent);
            } catch (Exception t) {
                Uri webUri = Uri.parse("https://play.google.com/store/apps/details?id=" + packageName);
                if (!TextUtils.isEmpty(searchGameName)) {
                    webUri = Uri.parse("https://play.google.com/store/search?q=" + searchGameName + "&c=apps");
                }
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    VApp.getApp().startActivity(webIntent);
                } catch (Exception ex) {

                }
            }
        }
    }

    public static boolean shouldUpdate64BitApk() {
        PackageManager pm = VCore.get().getUnHookPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(BuildConfig.PACKAGE_NAME_ARM64, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
        return VConstant.BIT_64_VERSION > packageInfo.versionCode;
    }

    public static void install64Bit() {
        Schedulers.io().scheduleDirect(() -> {
            AssetManager assetManager = VApp.getApp().getAssets();
            InputStream inputStream = null;
            try {
                inputStream = assetManager.open("app64.pak");
                File file = new File(VApp.getApp().getFilesDir(), "app64.apk");
                copyInputStreamToFile(inputStream, file);
                install(file);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private static long copyInputStreamToFile(InputStream inputStream, File file) throws Exception {
        long length = 0;
        FileOutputStream fileOutputStream = null;
        FileChannel fileChannel = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileChannel = fileOutputStream.getChannel();
            byte[] bArr = new byte[4096];
            while (true) {
                int read = inputStream.read(bArr);
                if (read <= 0) break;
                fileChannel.write(ByteBuffer.wrap(bArr, 0, read));
                length += read;
            }
            return length;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fileChannel != null) {
                try {
                    fileChannel.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void install(File apkFile) {
        Context context = VApp.getApp();
        File tempApk = new File(getDownloadDir(), apkFile.getName());
        try {
            FileUtils.copyFile(apkFile, tempApk);
            AndroidSchedulers.mainThread().scheduleDirect(() -> {
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri contentUri = FileProvider.getUriForFile(
                            context,
                            context.getPackageName() + ".provider",
                            tempApk
                    );
                    install.setDataAndType(contentUri, "application/vnd.android.package-archive");
                } else {
                    install.setDataAndType(Uri.fromFile(tempApk), "application/vnd.android.package-archive");
                }
                context.startActivity(install);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 传入的file已经在dowmload目录下
     *
     * @param realFile
     */
    public static void install2(File realFile) {
        Context context = VApp.getApp();
        try {
            AndroidSchedulers.mainThread().scheduleDirect(() -> {
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri contentUri = FileProvider.getUriForFile(
                            context,
                            context.getPackageName() + ".provider",
                            realFile
                    );
                    install.setDataAndType(contentUri, "application/vnd.android.package-archive");
                } else {
                    install.setDataAndType(Uri.fromFile(realFile), "application/vnd.android.package-archive");
                }
                context.startActivity(install);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File getDownloadDir() {
        File downDir = VApp.getApp().getExternalFilesDir("download");
        if (downDir == null) {
            downDir = VApp.getApp().getCacheDir();
        }
        if (!downDir.exists()) {
            downDir.mkdir();
        }
        return downDir;
    }

    public static void startAppByPkgName(Context context, String pkgName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static byte[] drawable2Bytes(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = drawableToBitmap(drawable);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }


    public static String getInstaller(Context context) {
        PackageManager pkg = context.getPackageManager();
        String installer = pkg.getInstallerPackageName(context.getPackageName());
        if (TextUtils.isEmpty(installer)) {
            installer = "Umeng";
        }
        return installer;
    }

    /**
     * 验证手机号码（支持国际格式，+86135xxxx...（中国内地），+00852137xxxx...（中国香港））
     *
     * @param mobile 移动、联通、电信运营商的号码段
     *               <p>移动的号段：134(0-8)、135、136、137、138、139、147（预计用于TD上网卡）
     *               、150、151、152、157（TD专用）、158、159、187（未启用）、188（TD专用）</p>
     *               <p>联通的号段：130、131、132、155、156（世界风专用）、185（未启用）、186（3g）</p>
     *               <p>电信的号段：133、153、180（未启用）、189</p>
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkMobile(String mobile) {
        String regex = "(\\+\\d+)?1[3456789]\\d{9}$";
        return Pattern.matches(regex, mobile);
    }

    /**
     * 验证Email
     *
     * @param email email地址，格式：zhangsan@sina.com，zhangsan@xxx.com.cn，xxx代表邮件服务商
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkEmail(String email) {
        String regex = "\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?";
        return Pattern.matches(regex, email);
    }


    /**
     * 获得Manifest文件中的Meta Data
     */
    public static String getMetaData(Context context, String name) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            return String.valueOf(ai.metaData.get(name));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "common";
    }

    public static boolean isFirstInstall(Context context) {
        String name = context.getPackageName();
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(name, 0);
            return info.firstInstallTime == info.lastUpdateTime;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
