package com.ft.mapp.home;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.ft.mapp.R;
import com.ft.mapp.VApp;
import com.ft.mapp.VCommends;
import com.ft.mapp.abs.nestedadapter.SmartRecyclerAdapter;
import com.ft.mapp.dialog.ModifyAppNameDialog;
import com.ft.mapp.dialog.ShortcutDialog;
import com.ft.mapp.home.adapters.LaunchpadAdapter;
import com.ft.mapp.home.adapters.decorations.ItemOffsetDecoration;
import com.ft.mapp.home.models.AddAppData;
import com.ft.mapp.home.models.AppData;
import com.ft.mapp.home.models.AppInfoLite;
import com.ft.mapp.home.models.EmptyAppData;
import com.ft.mapp.home.models.MultiplePackageAppData;
import com.ft.mapp.home.models.PackageAppData;
import com.ft.mapp.utils.VUiKit;
import com.ft.mapp.widgets.CommonDialog;
import com.ft.mapp.widgets.PopupMenu;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.remote.InstalledAppInfo;

import java.util.List;

public class LaunchFragment extends Fragment implements HomeContract.HomeView {
    private HomeContract.HomePresenter mPresenter;
    private ProgressBar mLoadingView;
    private RecyclerView mLauncherView;
    private LaunchpadAdapter mLaunchpadAdapter;
    private Handler mUiHandler;
    private int mLoadTimes;

    public static LaunchFragment newInstance() {
        return new LaunchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_launch, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUiHandler = new Handler(Looper.getMainLooper());
        bindViews(view);
        initLaunchpad();

        new HomePresenterImpl(this).start();
    }

    private void bindViews(View view) {
        mLoadingView = view.findViewById(R.id.pb_loading_app);
        mLauncherView = view.findViewById(R.id.home_launcher);
    }

    private void initLaunchpad() {
        Context context = getContext();
        if (context == null) {
            return;
        }

        mLauncherView.setHasFixedSize(true);
        StaggeredGridLayoutManager
                layoutManager = new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL);
        mLauncherView.setLayoutManager(layoutManager);
        mLaunchpadAdapter = new LaunchpadAdapter(context);
        SmartRecyclerAdapter wrap = new SmartRecyclerAdapter(mLaunchpadAdapter);
        View footer = new View(context);
        footer.setLayoutParams(
                new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        VUiKit.dpToPx(context, 60)));
        wrap.setFooterView(footer);
        mLauncherView.setAdapter(wrap);
        mLauncherView.addItemDecoration(new ItemOffsetDecoration(context, R.dimen.desktop_divider));
        mLaunchpadAdapter.setAppClickListener((view, pos, data) -> {
            if (data == null) {
                ListAppActivity.gotoListApp(getActivity());
            } else {
                gotoAppDetail(context, data);
            }
        });

        PopupMenu menu = new PopupMenu(context);
        menu.showMultiMenu(false);
        mLaunchpadAdapter.setMoreClickListener((view, pos, data) -> {
            InstalledAppInfo installedAppInfo =
                    VCore.get().getInstalledAppInfo(data.getPackageName(), 0);
            menu.showMultiMenu(installedAppInfo != null);
            menu.setOnItemClickListener(item -> {
                if (item == PopupMenu.MENU_ITEM.SETTING) {
                    gotoAppDetail(context, data);
                } else if (item == PopupMenu.MENU_ITEM.SHORTCUT) {
                    int userId = 0;
                    if (data instanceof MultiplePackageAppData) {
                        userId = ((MultiplePackageAppData) data).userId;
                    }
                    new ShortcutDialog(getContext(), userId, data.getPackageName(),
                            data.getName()).show();
                } else if (item == PopupMenu.MENU_ITEM.NAME) {
                    int userId = 0;
                    if (data instanceof MultiplePackageAppData) {
                        userId = ((MultiplePackageAppData) data).userId;
                    }
                    new ModifyAppNameDialog(getContext(), userId, data.getPackageName(),
                            data.getName()).setModifyNameCallback(
                            name -> mLaunchpadAdapter.notifyItemChanged(pos)).show();
                } else if (item == PopupMenu.MENU_ITEM.DELETE) {
                    deleteApp(pos);
                } else if (item == PopupMenu.MENU_ITEM.MULTI) {
                    try {
                        PackageInfo pkg = VApp.getApp().getPackageManager()
                                .getPackageInfo(data.getPackageName(), 0);
                        ApplicationInfo ai = pkg.applicationInfo;
                        String path =
                                ai.publicSourceDir != null ? ai.publicSourceDir : ai.sourceDir;
                        if (!TextUtils.isEmpty(path)) {
                            AppInfoLite lite = new AppInfoLite(data.getPackageName(), path,
                                    true);
                            mPresenter.addApp(lite);
                        }
                    } catch (Throwable ignore) {
                        //
                    }
                }
            });
            menu.showLocation(view);
        });
    }

    private void gotoAppDetail(Context context, AppData data) {
        try {
            if (data instanceof PackageAppData) {
                PackageAppData appData = (PackageAppData) data;
                appData.isFirstOpen = false;
                AppDetailActivity
                        .gotoAppDetail(context, appData.name, appData.packageName, 0, appData.icon);
            } else if (data instanceof MultiplePackageAppData) {
                MultiplePackageAppData multipleData = (MultiplePackageAppData) data;
                multipleData.isFirstOpen = false;
                AppDetailActivity
                        .gotoAppDetail(context, multipleData.name, multipleData.appInfo.packageName,
                                multipleData.userId, multipleData.icon);
            } else {
                AppDetailActivity.gotoAppDetail(context, data.getName(), data.getPackageName(), 0,
                        data.getIcon());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void deleteApp(int position) {
        AppData data = mLaunchpadAdapter.getList().get(position);
        String name = data.getName();
        if (data instanceof MultiplePackageAppData) {
            int nameIndex = ((MultiplePackageAppData) data).userId + 1;
            name = name + "(" + nameIndex + ")";
        }
        new CommonDialog(getContext())
                .setTitleId(R.string.delete_app)
                .setMessage(getString(R.string.delete_app_msg, name))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> mPresenter.deleteApp(data))
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @Override
    public void setPresenter(HomeContract.HomePresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        mLoadingView.setVisibility(View.GONE);
    }

    @Override
    public void loadFinish(List<AppData> list) {
        list.add(new AddAppData());
        mLaunchpadAdapter.setList(list);
        hideLoading();
    }

    @Override
    public void loadError(Throwable err) {
        hideLoading();
        if (mLoadTimes > 3) {
            mUiHandler.postDelayed(() -> {
                mPresenter.dataChanged();
                mLoadTimes++;
            }, 300);
        }
        err.printStackTrace();
    }

    @Override
    public void showGuide() {

    }

    @Override
    public void addAppToLauncher(AppData model) {
        List<AppData> dataList = mLaunchpadAdapter.getList();
        boolean replaced = false;
        if (dataList != null) {
            for (int i = 0; i < dataList.size(); i++) {
                AppData data = dataList.get(i);
                if (data instanceof EmptyAppData) {
                    mLaunchpadAdapter.replace(i, model);
                    replaced = true;
                    break;
                }
            }
        }
        if (!replaced) {
            mLaunchpadAdapter.add(model);
            if (mLaunchpadAdapter.getItemCount() > 0) {
                mLauncherView.smoothScrollToPosition(mLaunchpadAdapter.getItemCount() - 1);
            }
        }
    }

    @Override
    public void removeAppToLauncher(AppData model) {
        mLaunchpadAdapter.remove(model);
    }

    @Override
    public void refreshLauncherItem(AppData model) {
        mLaunchpadAdapter.refresh(model);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            List<AppInfoLite> appList =
                    data.getParcelableArrayListExtra(VCommends.EXTRA_APP_INFO_LIST);
            if (appList != null) {
                for (AppInfoLite info : appList) {
                    mPresenter.addApp(info);
                }
            }
        }
    }
}
