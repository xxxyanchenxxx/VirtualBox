package com.fun.vbox.helper.compat;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.env.VirtualRuntime;
import com.fun.vbox.helper.utils.FileUtils;
import com.fun.vbox.helper.utils.Reflect;
import com.fun.vbox.helper.utils.VLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import mirror.com.android.internal.content.NativeLibraryHelper;

public class NativeLibraryHelperCompat {

    private static String TAG = NativeLibraryHelperCompat.class.getSimpleName();

    public static int copyNativeBinaries(File apkFile, File sharedLibraryDir) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return copyNativeBinariesAfterL(apkFile, sharedLibraryDir);
        } else {
            return copyNativeBinariesBeforeL(apkFile, sharedLibraryDir);
        }
    }

    private static int copyNativeBinariesBeforeL(File apkFile, File sharedLibraryDir) {
        try {
            return Reflect.on(NativeLibraryHelper.TYPE).call("copyNativeBinariesIfNeededLI", apkFile, sharedLibraryDir)
                    .get();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return -1;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static int copyNativeBinariesAfterL(File apkFile, File sharedLibraryDir) {
        try {
            Object handle = NativeLibraryHelper.Handle.create.call(apkFile);
            if (handle == null) {
                return -1;
            }

            String abi = null;
            Set<String> abiSet = getSupportAbiList(apkFile.getAbsolutePath());
            if (abiSet == null || abiSet.isEmpty()) {
                return 0;
            }
            boolean is64Bit = VirtualRuntime.is64bit();
            if (is64Bit && contain64bitAbi(abiSet)) {
                if (Build.SUPPORTED_64_BIT_ABIS.length > 0) {
                    int abiIndex = NativeLibraryHelper.findSupportedAbi.call(handle, Build.SUPPORTED_64_BIT_ABIS);
                    if (abiIndex >= 0) {
                        abi = Build.SUPPORTED_64_BIT_ABIS[abiIndex];
                    }
                }
            } else {
                if (Build.SUPPORTED_32_BIT_ABIS.length > 0) {
                    int abiIndex = NativeLibraryHelper.findSupportedAbi.call(handle, Build.SUPPORTED_32_BIT_ABIS);
                    if (abiIndex >= 0) {
                        abi = Build.SUPPORTED_32_BIT_ABIS[abiIndex];
                    }
                }
            }
            if (abi == null) {
                VLog.e(TAG, "Not match any abi [%s].", apkFile.getAbsolutePath());
                return -1;
            }
            return NativeLibraryHelper.copyNativeBinaries.call(handle, sharedLibraryDir, abi);
        } catch (Throwable e) {
            VLog.d(TAG, "copyNativeBinaries with error : %s", e.getLocalizedMessage());
            e.printStackTrace();
        }

        return -1;
    }

    public static void copyNativeBinaries2(String packageName, File libDir) {
        File[] libDirFiles = libDir.listFiles();
        String appLibPath = "";
        if (libDirFiles == null || libDirFiles.length == 0) {
            try {
                appLibPath = VCore.get().getUnHookPackageManager().getApplicationInfo(packageName, 0).nativeLibraryDir;
            } catch (PackageManager.NameNotFoundException e) {
                //
            }
            if (!TextUtils.isEmpty(appLibPath)) {
                File[] libFiles = new File(appLibPath).listFiles();
                if (libFiles != null && libFiles.length != 0) {
                    for (File file : libFiles) {
                        File dst = new File(libDir, file.getName());
                        try {
                            FileUtils.copyFile(file, dst);
                        } catch (IOException e) {
                            dst.delete();
                        }
                    }
                }
            }
        }

        libDirFiles = libDir.listFiles();
        String apkPath = "";
        if (libDirFiles == null || libDirFiles.length == 0) {
            VLog.e(TAG, "copyNativeBinaries 2_1 failed!!! for" + appLibPath);
            try {
                try {
                    apkPath = VCore.get().getUnHookPackageManager().getApplicationInfo(packageName, 0).sourceDir;
                } catch (PackageManager.NameNotFoundException e) {
                    //
                }
                if (!TextUtils.isEmpty(apkPath)) {
                    File appSourceDir = new File(apkPath).getParentFile();
                    File[] libFiles = appSourceDir.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith("apk");
                        }
                    });
                    if (libFiles != null) {
                        for (File file : libFiles) {
                            NativeLibraryHelperCompat.copyNativeBinaries(file, libDir);
                        }
                    }
                }
            } catch (Exception e) {
                //
            }
        }

        // libDirFiles = libDir.listFiles();
        // if (libDirFiles == null || libDirFiles.length == 0) {
        //     Log.e(TAG, "copyNativeBinaries 2_2 failed!!! for" + apkPath);
        //     if (!TextUtils.isEmpty(apkPath)) {
        //         copyNativeBinaries3(new File(apkPath), libDir);
        //     }
        // }
    }

    private static void copyNativeBinaries3(File archiveFile, File libDir) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(archiveFile);
            Enumeration entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) entries.nextElement();
                String name = zipEntry.getName();
                String abi = "armeabi";
                if (name.contains(String.format("%s%s", "lib/", abi))) {
                    abi = String
                            .format("%s%s%s",
                                    libDir,
                                    File.separator,
                                    name.substring(
                                            name.lastIndexOf(File.separator) + 1
                                    ));
                    if (zipEntry.isDirectory()) {
                        File abiFolder = new File(abi);
                        if (!abiFolder.exists()) {
                            abiFolder.mkdirs();
                        }
                    } else {
                        File abiFolder = new File(abi.substring(0, abi.lastIndexOf("/")));
                        if (!abiFolder.exists()) {
                            abiFolder.mkdirs();
                        }
                        extractZipEntry(zipFile.getInputStream(zipEntry), new File(abi));
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (Exception e) {
                //
            }
        }
    }

    private static void extractZipEntry(InputStream ins, File destFile) throws IOException {
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destFile));
        extractZipEntry(ins, outputStream);
    }

    private static void extractZipEntry(InputStream ins, OutputStream outputStream) throws IOException {
        BufferedInputStream bufferedInputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(ins);

            byte[] bArr = new byte[4096];
            for (int read = bufferedInputStream.read(bArr); read != -1;
                 read = bufferedInputStream.read(bArr)) {
                outputStream.write(bArr, 0, read);
            }
        } finally {
            try {
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
            } catch (IOException e) {
                //
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                //
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean is64bitAbi(String abi) {
        return "arm64-v8a".equals(abi)
                || "x86_64".equals(abi)
                || "mips64".equals(abi);
    }

    public static boolean is32bitAbi(String abi) {
        return "armeabi".equals(abi)
                || "armeabi-v7a".equals(abi)
                || "mips".equals(abi)
                || "x86".equals(abi);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean contain64bitAbi(Set<String> supportedABIs) {
        for (String supportedAbi : supportedABIs) {
            if (is64bitAbi(supportedAbi)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contain32bitAbi(Set<String> abiList) {
        for (String supportedAbi : abiList) {
            if (is32bitAbi(supportedAbi)) {
                return true;
            }
        }
        return false;
    }


    public static Set<String> getSupportAbiList(String apk) {
        try {
            ZipFile apkFile = new ZipFile(apk);
            Enumeration<? extends ZipEntry> entries = apkFile.entries();
            Set<String> supportedABIs = new HashSet<String>();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.contains("../")) {
                    continue;
                }
                if (name.startsWith("lib/") && !entry.isDirectory() && name.endsWith(".so")) {
                    String supportedAbi = name.substring(name.indexOf("/") + 1, name.lastIndexOf("/"));
                    supportedABIs.add(supportedAbi);
                }
            }
            return supportedABIs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptySet();
    }
}
