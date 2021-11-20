package com.ft.mapp.home;

import android.app.ProgressDialog;
import android.os.Bundle;

import com.ft.mapp.R;
import com.ft.mapp.abs.ui.VActivity;
import com.ft.mapp.home.adapters.AppLaunchCfgAdapter;
import com.ft.mapp.home.adapters.decorations.ItemOffsetDecoration;
import com.ft.mapp.home.models.AppLaunchData;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.ipc.VActivityManager;
import com.fun.vbox.remote.InstalledAppInfo;
import com.jaeger.library.StatusBarUtil;
import com.ft.mapp.utils.VUiKit;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class AppLaunchConfigActivity extends VActivity {
    private AppLaunchCfgAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTranslucent(this);
        setContentView(R.layout.activity_app_launch_cfg);

        RecyclerView recyclerView = findViewById(R.id.app_recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1,
                OrientationHelper.VERTICAL));
        recyclerView.addItemDecoration(new ItemOffsetDecoration(VUiKit.dpToPx(getContext(), 2)));
        mAdapter = new AppLaunchCfgAdapter(this);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemSwitchListener((pluginInfo, isChecked) -> {
            if (isChecked) {
                VActivityManager.get().setLaunchMonopoly(pluginInfo.packageName, 1);
            } else {
                VActivityManager.get().setLaunchMonopoly(pluginInfo.packageName, 0);
            }
        });

        loadData();
    }

    private void loadData() {
        ProgressDialog dialog = ProgressDialog.show(this, null, "loading");
        VUiKit.defer().when(() -> {
            List<InstalledAppInfo> infos = VCore.get().getInstalledApps(0);
            List<AppLaunchData> models = new ArrayList<>();
            for (InstalledAppInfo info : infos) {
                AppLaunchData data = new AppLaunchData(this, info, 0);
                data.monopoly = VActivityManager.get().isLaunchMonopoly(info.packageName);
                models.add(data);
            }
            return models;
        }).done((list) -> {
            dialog.dismiss();
            mAdapter.setList(list);
        }).fail((e) -> {
            dialog.dismiss();
        });
    }

}
