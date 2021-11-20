package com.ft.mapp.home;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.ft.mapp.R;
import com.ft.mapp.abs.ui.VActivity;
import com.ft.mapp.dialog.LoadingBottomDialog;
import com.ft.mapp.dialog.ShortcutDialog;
import com.ft.mapp.engine.GlobalData;
import com.ft.mapp.home.adapters.AppPluginAdapter;
import com.ft.mapp.home.adapters.decorations.ItemOffsetDecoration;
import com.ft.mapp.home.device.DeviceDetailActivity;
import com.ft.mapp.home.models.DeviceData;
import com.ft.mapp.home.models.PluginInfo;
import com.ft.mapp.utils.AppPackageCompat;
import com.ft.mapp.utils.AppSharePref;
import com.ft.mapp.utils.CommonUtil;
import com.ft.mapp.utils.InstallHelper;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.ipc.VActivityManager;
import com.fun.vbox.os.VUserInfo;
import com.fun.vbox.os.VUserManager;
import com.fun.vbox.server.bit64.Bit64Utils;
import com.gw.swipeback.SwipeBackLayout;
import com.ft.mapp.utils.VUiKit;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class AppDetailActivity extends VActivity {
    private static final String TAG = "AppDetailActivity";
    private static final String PKG_NAME_ARGUMENT = "MODEL_ARGUMENT";
    private static final String NAME_ARGUMENT = "NAME_ARGUMENT";
    private static final String KEY_USER = "KEY_USER";
    private static final String APP_ICON = "app_icon";

    private final int MSG_WAIT_INSTALL = 0x10;

    private AppPluginAdapter mAdapter;
    private String mPkg;
    private String mName;
    private int mUserId;
    private LoadingBottomDialog mLoadingDialog;
    private volatile Intent mIntent;
    private volatile boolean mInstalling;
    private long mLaunchTimestamp;
    private PluginInfo mClickInfo;

    public static void gotoAppDetail(Context context, String name, String packageName, int userId,
                                     Drawable drawable) {
        String packageCompat = AppPackageCompat.getPackageName(packageName);

        Intent intent = new Intent(context, AppDetailActivity.class);
        intent.putExtra(PKG_NAME_ARGUMENT, packageCompat);
        intent.putExtra(NAME_ARGUMENT, name);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_USER, userId);
        if (drawable != null) {
            byte[] bytes = CommonUtil.drawable2Bytes(drawable);
            intent.putExtra(APP_ICON, bytes);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_bottom_in, R.anim.slide_bottom_out);
        setContentView(R.layout.layout_app_detail);

        int heightPixels = getResources()
                .getDisplayMetrics().heightPixels;

        Window win = this.getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = (int) (heightPixels * 0.9);
        lp.gravity = Gravity.BOTTOM;
        win.setAttributes(lp);

        initViews();
        initData();

        SwipeBackLayout swipeBackLayout = new SwipeBackLayout(this);
        swipeBackLayout.attachToActivity(this);
        swipeBackLayout.setDirectionMode(SwipeBackLayout.FROM_TOP);
        swipeBackLayout.setMaskAlpha(0);
    }

    private void initViews() {
        RecyclerView recyclerView = findViewById(R.id.plugin_recycler_view);
        recyclerView
                .setLayoutManager(new StaggeredGridLayoutManager(1, OrientationHelper.VERTICAL));
        recyclerView.addItemDecoration(new ItemOffsetDecoration(VUiKit.dpToPx(getContext(), 2)));
        mAdapter = new AppPluginAdapter(this);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((info, position) -> {
            mClickInfo = info;
            if (TextUtils.equals(info.name, getString(R.string.create_shortcut))) {
                handleCreateShortcut();
                return;
            }

            gotoFunction();
        });

        Button btnLaunch = findViewById(R.id.detail_launch);
        btnLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchApp();
            }
        });
    }

    private void gotoFunction() {
        if (mClickInfo == null) {
            return;
        }
        if (TextUtils.equals(mClickInfo.name, getString(R.string.menu_mock_phone))) {
            gotoDeviceSetting();
        }
    }

    private void handleCreateShortcut() {
        new ShortcutDialog(this, mUserId, mPkg, mName).show();
    }

    private void initData() {
        Intent intent = getIntent();
        mPkg = intent.getStringExtra(PKG_NAME_ARGUMENT);
        mName = intent.getStringExtra(NAME_ARGUMENT);
        mUserId = intent.getIntExtra(KEY_USER, 0);

        initPluginList();
        initDualApp(false);
    }

    private void gotoDeviceSetting() {
        VUserInfo userInfo = VUserManager.get().getUserInfo(mUserId);
        if (userInfo != null) {
            DeviceData deviceData = new DeviceData(getContext(), null, userInfo.id);
            deviceData.name = userInfo.name;
            DeviceDetailActivity.open(this, deviceData, 0, DeviceDetailActivity.REQUEST_CODE);
        } else {
            startActivityForResult(new Intent(this, DeviceDetailActivity.class),
                    DeviceDetailActivity.REQUEST_CODE);
        }
    }

    private void clearStepSetting() {
        AppSharePref.getInstance(this).putInt(mPkg + "_stepTimes", 1);
    }

    /**
     * 初始化分身
     *
     * @param launch 初始完是否立即启动
     */
    private void initDualApp(boolean launch) {
        VUiKit.defer().when(() -> {
            mInstalling = true;
            try {
                InstallHelper.installPackage(AppDetailActivity.this, mPkg);
            } catch (PackageManager.NameNotFoundException ignored) {
            }
            mIntent = VCore.get().getLaunchIntent(mPkg, 0);
            if (mIntent == null) {
                mInstalling = false;
                return;
            }
            VCore.get().setUiCallback(mIntent, mUiCallback);
            if (launch) {
                VActivityManager.get().startActivity(mIntent, mUserId);
                mLaunchTimestamp = System.currentTimeMillis();
            }
            mInstalling = false;
        }).done(result -> {
        }).fail((e) -> {
            e.printStackTrace();
            mInstalling = false;
        });
    }

    private void initPluginList() {
        mAdapter.clear();
        mAdapter.add(new PluginInfo(R.drawable.ic_plugin_shortcut, R.color.white,
                getString(R.string.create_shortcut),
                getString(R.string.create_shortcut_tip)));

        mAdapter.add(new PluginInfo(R.drawable.ic_plugin_phone, R.color.white,
                getString(R.string.menu_mock_phone), getString(R.string.mock_phone_des)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchApp() {
        launchAppInner();
    }

    private void launchAppInner() {
        boolean isBit64 = Bit64Utils.isRunOn64BitProcess(mPkg);
        if (isBit64) {
            if (!VCore.get().is64BitEngineInstalled()) {
                InstallHelper.install64Bit(this);
                return;
            } else if (CommonUtil.shouldUpdate64BitApk()) {
                InstallHelper.install64Bit(this);
                return;
            }
        }

        if (!CommonUtil.isAppInstalled(AppDetailActivity.this, mPkg)) {
            CommonUtil.launchAppMarket(mPkg, "", mName);
        } else {
            if (mLoadingDialog == null) {
                mLoadingDialog = new LoadingBottomDialog(this, mName);
                mLoadingDialog.setCanceledOnTouchOutside(false);
            }
            mLoadingDialog.setMessage(mName);
            mLoadingDialog.show();
            if (mIntent != null) {
                //已启动过，第二次就不展示loading框
                VUiKit.defer().when(() -> {
                    VActivityManager.get().startActivity(mIntent, mUserId);
                }).done(unit -> {
                    GlobalData.setLaunchedApp(mPkg);
                    mLaunchTimestamp = System.currentTimeMillis();
                });
            } else if (mInstalling) {
                mHandler.sendEmptyMessageDelayed(MSG_WAIT_INSTALL, 1000);
            } else {
                initDualApp(true);
            }
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WAIT_INSTALL:
                    if (mInstalling && mIntent == null) {
                        mHandler.sendEmptyMessageDelayed(MSG_WAIT_INSTALL, 500);
                    } else {
                        VUiKit.defer().when(() -> {
                            VActivityManager.get().startActivity(mIntent, mUserId);
                        }).done(unit -> {
                            mLaunchTimestamp = System.currentTimeMillis();
                        });
                    }
                    break;

            }
            return false;
        }
    });

    private final VCore.UiCallback mUiCallback = new VCore.UiCallback() {

        @Override
        public void onAppOpened(String packageName, int userId) {
            AndroidSchedulers.mainThread().scheduleDirect(() -> {
                if (mLoadingDialog != null) {
                    mLoadingDialog.dismiss();
                }
            });
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        //双开微信时，Activity access有时为null, mUiCallback 取消不了
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }

    }
}
