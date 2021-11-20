package com.ft.mapp.home.repo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.ft.mapp.BuildConfig;;
import com.ft.mapp.VConstant;
import com.fun.vbox.GmsSupport;
import com.fun.vbox.remote.InstallOptions;
import com.fun.vbox.remote.InstallResult;
import com.fun.vbox.remote.InstalledAppInfo;
import com.github.promeg.pinyinhelper.Pinyin;
import com.ft.mapp.utils.VUiKit;
import com.ft.mapp.home.models.AppData;
import com.ft.mapp.home.models.AppInfo;
import com.ft.mapp.home.models.AppInfoLite;
import com.ft.mapp.home.models.MultiplePackageAppData;
import com.ft.mapp.home.models.PackageAppData;
import com.fun.vbox.client.core.VCore;

import org.jdeferred2.Promise;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Lody
 */
public class AppRepository implements AppDataSource {

    private static final Collator COLLATOR = Collator.getInstance(Locale.CHINA);
    private static final List<String> SCAN_PATH_LIST = Arrays.asList(
            ".",
            "wandoujia/app",
            "tencent/tassistant/apk",
            "BaiduAsa9103056",
            "360Download",
            "pp/downloader",
            "pp/downloader/apk",
            "pp/downloader/silent/apk");

    private Context mContext;

    public AppRepository(Context context) {
        mContext = context;
    }

    private static boolean isSystemApplication(PackageInfo packageInfo) {
        return (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                && !GmsSupport.isGoogleAppOrService(packageInfo.packageName);
    }

    @Override
    public Promise<List<AppData>, Throwable, Void> getVirtualApps() {
        return VUiKit.defer().when(() -> {
            List<InstalledAppInfo> infos = VCore.get().getInstalledApps(0);
            List<AppData> models = new ArrayList<>();
            if (infos != null) {
                for (InstalledAppInfo info : infos) {
                    if (!VCore.get().isPackageLaunchable(info.packageName)) {
                        continue;
                    }
                    PackageAppData data = new PackageAppData(mContext, info);
                    if (VCore.get().isOutsideInstalled(info.packageName)) {
                        models.add(data);
                    }
                    int[] userIds = info.getInstalledUsers();
                    for (int userId : userIds) {
                        if (userId != 0) {
                            models.add(new MultiplePackageAppData(data, userId));
                        }
                    }
                }
            }
            return models;
        });
    }

    @Override
    public Promise<List<AppInfo>, Throwable, Void> getInstalledApps(Context context) {
        return VUiKit.defer().when(() -> convertPackageInfoToAppData(context,
                context.getPackageManager().getInstalledPackages(0),
                true, false));
    }

    @Override
    public Promise<List<AppInfo>, Throwable, Void> getStorageApps(Context context, File rootDir) {
        return VUiKit.defer().when(() -> convertPackageInfoToAppData(context,
                findAndParseAPKs(context, rootDir, SCAN_PATH_LIST),
                true, false));
    }

    private List<PackageInfo> findAndParseAPKs(Context context, File rootDir, List<String> paths) {
        List<PackageInfo> packageList = new ArrayList<>();
        if (paths == null) {
            return packageList;
        }
        for (String path : paths) {
            File[] dirFiles = new File(rootDir, path).listFiles();
            if (dirFiles == null) {
                continue;
            }
            for (File f : dirFiles) {
                if (!f.getName().toLowerCase().endsWith(".apk")) {
                    continue;
                }
                PackageInfo pkgInfo = null;
                try {
                    pkgInfo = context.getPackageManager()
                            .getPackageArchiveInfo(f.getAbsolutePath(), 0);
                    pkgInfo.applicationInfo.sourceDir = f.getAbsolutePath();
                    pkgInfo.applicationInfo.publicSourceDir = f.getAbsolutePath();
                } catch (Exception e) {
                    // Ignore
                }
                if (pkgInfo != null) {
                    packageList.add(pkgInfo);
                }
            }
        }
        return packageList;
    }

    private List<AppInfo> convertPackageInfoToAppData(Context context, List<PackageInfo> pkgList,
                                                      boolean fastOpen,
                                                      boolean isExcludeCloned) {
        PackageManager pm = context.getPackageManager();
        List<AppInfo> list = new ArrayList<>();
        String hostPkg = VCore.get().getHostPkg();
        for (PackageInfo pkg : pkgList) {
            // ignore the host package
            if (hostPkg.equals(pkg.packageName)) {
                continue;
            }
            if (hostPkg.equals(BuildConfig.PACKAGE_NAME_ARM64)) {
                continue;
            }
            // ignore the System package
            if (isSystemApplication(pkg)) {
                continue;
            }
            ApplicationInfo ai = pkg.applicationInfo;
            String path = ai.publicSourceDir != null ? ai.publicSourceDir : ai.sourceDir;
            if (path == null) {
                continue;
            }
            AppInfo info = new AppInfo();
            info.packageName = pkg.packageName;
            info.fastOpen = fastOpen;
            info.path = path;
            info.icon = ai.loadIcon(pm);
            info.name = ai.loadLabel(pm);

            if (Pinyin.isChinese(info.name.charAt(0))) {
                String pinyin = Pinyin.toPinyin(info.name.charAt(0));
                info.firstLetter = pinyin.substring(0, 1).toUpperCase();
            } else {
                info.firstLetter = info.name.toString().substring(0, 1).toUpperCase();
            }
            if (info.firstLetter.compareTo("A") < 0) {
                info.firstLetter = "#";
            }

            InstalledAppInfo installedAppInfo = VCore.get().getInstalledAppInfo(pkg.packageName, 0);
            if (installedAppInfo != null) {
                info.cloneCount = installedAppInfo.getInstalledUsers().length;
            }
            if (isExcludeCloned && info.cloneCount > 0) {
                continue;
            }
            list.add(info);
        }
        // Collections.sort(list, (lhs, rhs) -> {
        //     int compareCloneCount = Integer.compare(lhs.cloneCount, rhs.cloneCount);
        //     if (compareCloneCount != 0) {
        //         return -compareCloneCount;
        //     }
        //     return COLLATOR.compare(lhs.name, rhs.name);
        // });
        Collections.sort(list, new PinyinComparator());
        return list;
    }

    private static class PinyinComparator implements Comparator<AppInfo> {
        @Override
        public int compare(AppInfo o1, AppInfo o2) {
            if (o1.firstLetter.equals("#")) {
                return 1;
            } else if (o2.firstLetter.equals("#")) {
                return -1;
            } else {
                return o1.firstLetter.compareTo(o2.firstLetter);
            }
        }
    }

    @Override
    public InstallResult addVirtualApp(AppInfoLite info) {
        InstallOptions options = InstallOptions
                .makeOptions(info.fastOpen, false, InstallOptions.UpdateStrategy.COMPARE_VERSION);
        return VCore.get().installPackageSync(info.path, options);
    }

    @Override
    public boolean removeVirtualApp(String packageName, int userId) {
        return VCore.get().uninstallPackageAsUser(packageName, userId);
    }

}
