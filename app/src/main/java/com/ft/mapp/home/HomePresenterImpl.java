package com.ft.mapp.home;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;

import com.ft.mapp.R;
import com.ft.mapp.VCommends;
import com.ft.mapp.dialog.LoadingDialog;
import com.ft.mapp.home.models.AppData;
import com.ft.mapp.home.models.AppInfoLite;
import com.ft.mapp.home.models.MultiplePackageAppData;
import com.ft.mapp.home.models.PackageAppData;
import com.ft.mapp.home.repo.AppRepository;
import com.ft.mapp.home.repo.PackageAppDataStorage;
import com.ft.mapp.open.MultiAppHelper;
import com.ft.mapp.utils.CommonUtil;
import com.ft.mapp.utils.VUiKit;
import com.ft.mapp.widgets.CommonDialog;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.helper.compat.PermissionCompat;
import com.fun.vbox.remote.InstallResult;
import com.fun.vbox.remote.InstalledAppInfo;
import com.fun.vbox.server.bit64.Bit64Utils;

import jonathanfinerty.once.Once;

class HomePresenterImpl implements HomeContract.HomePresenter {
    private final HomeContract.HomeView mView;
    private final Activity mActivity;
    private final AppRepository mRepo;

    HomePresenterImpl(HomeContract.HomeView view) {
        mView = view;
        mActivity = view.getActivity();
        mRepo = new AppRepository(mActivity);
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        dataChanged();
        if (!Once.beenDone(VCommends.TAG_SHOW_ADD_APP_GUIDE)) {
            mView.showGuide();
            Once.markDone(VCommends.TAG_SHOW_ADD_APP_GUIDE);
        }
        showWritePermission();
    }

    private void showWritePermission() {
        if (!PermissionCompat
                .checkPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, false)) {
            PermissionCompat.startRequestPermissions(mView.getContext(), false,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    (requestCode, permissions, grantResults) -> PermissionCompat.isRequestGranted(grantResults));
        }
    }

    @Override
    public void dataChanged() {
        mView.showLoading();
        mRepo.getVirtualApps().done(mView::loadFinish).fail(mView::loadError);
    }

    @Override
    public void addApp(AppInfoLite info) {
        class AddResult {
            private PackageAppData appData;
            private int userId;
        }
        AddResult addResult = new AddResult();
        Dialog dialog = new LoadingDialog(mActivity);
        dialog.show();
        VUiKit.defer().when(() -> {
            InstalledAppInfo installedAppInfo = VCore.get().getInstalledAppInfo(info.packageName, 0);
            if (installedAppInfo != null) {
                addResult.userId = MultiAppHelper.installExistedPackage(installedAppInfo);
            } else {
                InstallResult res = mRepo.addVirtualApp(info);
                if (!res.isSuccess) {
                    throw new IllegalStateException();
                }
            }
        }).then((res) ->
                addResult.appData = PackageAppDataStorage.get().acquire(info.packageName)).fail((e) -> {
            new CommonDialog(mActivity)
                    .setTitleId(R.string.notice)
                    .setMessage(R.string.tip_64)
                    .setPositiveButton(R.string.OK, (dialogInterface, i) -> {
                        if (Bit64Utils.isRunOn64BitProcess(info.packageName) && !VCore.get().is64BitEngineInstalled()) {
                            CommonUtil.install64Bit();
                        }
                    }).show();
            dialog.dismiss();
        }).done(res -> {
            dialog.dismiss();
            if (addResult.userId == 0) {
                PackageAppData data = addResult.appData;
                data.isLoading = true;
                mView.addAppToLauncher(data);
                handleOptApp(data, info.packageName, true);
            } else {
                MultiplePackageAppData data = new MultiplePackageAppData(addResult.appData, addResult.userId);
                data.isLoading = true;
                mView.addAppToLauncher(data);
                handleOptApp(data, info.packageName, false);
            }
        });
    }


    private void handleOptApp(AppData data, String packageName, boolean needOpt) {
        VUiKit.defer().when(() -> {
            long time = System.currentTimeMillis();
            if (needOpt) {
                try {
                    VCore.get().preOpt(packageName);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            time = System.currentTimeMillis() - time;
            if (time < 1500L) {
                try {
                    Thread.sleep(1500L - time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).done((res) -> {
            if (data instanceof PackageAppData) {
                ((PackageAppData) data).isLoading = false;
                ((PackageAppData) data).isFirstOpen = true;
            } else if (data instanceof MultiplePackageAppData) {
                ((MultiplePackageAppData) data).isLoading = false;
                ((MultiplePackageAppData) data).isFirstOpen = true;
            }
            mView.refreshLauncherItem(data);
        });
    }

    @Override
    public void deleteApp(AppData data) {
        try {
            mView.removeAppToLauncher(data);
            if (data instanceof PackageAppData) {
                mRepo.removeVirtualApp(((PackageAppData) data).packageName, 0);
            } else {
                MultiplePackageAppData appData = (MultiplePackageAppData) data;
                mRepo.removeVirtualApp(appData.appInfo.packageName, appData.userId);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createShortcut(AppData data) {
        VCore.OnEmitShortcutListener listener = new VCore.OnEmitShortcutListener() {
            @Override
            public Bitmap getIcon(Bitmap originIcon) {
                return originIcon;
            }

            @Override
            public String getName(String originName) {
                return originName + "(分身)";
            }
        };
        if (data instanceof PackageAppData) {
            VCore.get().createShortcut(0, ((PackageAppData) data).packageName, listener);
        } else if (data instanceof MultiplePackageAppData) {
            MultiplePackageAppData appData = (MultiplePackageAppData) data;
            VCore.get().createShortcut(appData.userId, appData.appInfo.packageName, listener);
        }
    }
}
