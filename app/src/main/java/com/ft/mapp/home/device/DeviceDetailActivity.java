package com.ft.mapp.home.device;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ft.mapp.BrandConstant;
import com.ft.mapp.R;
import com.ft.mapp.abs.ui.VActivity;
import com.ft.mapp.home.adapters.ItemClickListener;
import com.ft.mapp.home.adapters.Section;
import com.ft.mapp.home.adapters.SectionedExpandableLayoutHelper;
import com.ft.mapp.home.models.DeviceData;
import com.ft.mapp.home.models.BrandItem;
import com.ft.mapp.widgets.CommonDialog;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.ipc.VDeviceManager;
import com.fun.vbox.remote.VDeviceConfig;
import com.jaeger.library.StatusBarUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

public class DeviceDetailActivity extends VActivity implements ItemClickListener, View.OnClickListener {

    public static final int REQUEST_CODE = 1002;

    public static void open(Activity activity, DeviceData data, int position, int requestCode) {
        Intent intent = new Intent(activity, DeviceDetailActivity.class);
        intent.putExtra("pkg", data.packageName);
        intent.putExtra("user", data.userId);
        intent.putExtra("pos", position);
        activity.startActivityForResult(intent, requestCode);
    }

    private String mPackageName;
    private int mUserId;
    private int mPosition;
    private VDeviceConfig mDeviceConfig;
    private RecyclerView mRecyclerView;
    private SectionedExpandableLayoutHelper mSectionedExpandableLayoutHelper;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTranslucent(this);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_mock_device);
        mToolbar = bind(R.id.task_top_toolbar);
        mToolbar.setTitle(R.string.menu_mock_phone);
        setSupportActionBar(mToolbar);
        enableBackHome();

        if (getIntent() != null) {
            mPackageName = getIntent().getStringExtra("pkg");
            mUserId = getIntent().getIntExtra("user", 0);
        }

        mDeviceConfig = VDeviceManager.get().getDeviceConfig(mUserId);
        mRecyclerView = findViewById(R.id.recycler_view);
        mSectionedExpandableLayoutHelper = new SectionedExpandableLayoutHelper(this,
                mRecyclerView, this, 3);

        BrandItem curBrandItem = initBrandItem();
        LinkedHashMap<String, ArrayList<BrandItem>> listLinkedHashMap = BrandConstant.get(this);
        for (LinkedHashMap.Entry<String, ArrayList<BrandItem>> entry :
                listLinkedHashMap.entrySet()) {
            boolean isExpanded = false;
            if (curBrandItem != null) {
                ArrayList<BrandItem> arrayList = entry.getValue();
                if (!arrayList.isEmpty()) {
                   BrandItem item = arrayList.get(0);
                    if (curBrandItem.getBrand().equalsIgnoreCase(item.getBrand())) {
                        isExpanded = true;
                    }
                }
            }
            mSectionedExpandableLayoutHelper.addSection(entry.getKey(), isExpanded,
                    entry.getValue());
        }
        mSectionedExpandableLayoutHelper.notifyDataSetChanged();

        findViewById(R.id.device_save_tv).setOnClickListener(this);
    }

    private BrandItem initBrandItem() {
        String brand = mDeviceConfig.getProp("BRAND");
        if (TextUtils.isEmpty(brand)) {
            brand = Build.BRAND;
        }
        String model = mDeviceConfig.getProp("MODEL");
        if (TextUtils.isEmpty(model)) {
            model = Build.MODEL;
        }
        BrandItem curBrandItem = null;
        if (!TextUtils.isEmpty(brand) && !TextUtils.isEmpty(model)) {
            mToolbar.setTitle(brand + " " + model);
            curBrandItem = new BrandItem(brand, model);
            mSectionedExpandableLayoutHelper.setSelectBrandItem(curBrandItem);
        }
        return curBrandItem;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mPackageName = intent.getStringExtra("pkg");
        mUserId = intent.getIntExtra("user", 0);
        mPosition = intent.getIntExtra("pos", -1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device, menu);
        return true;
    }

    private void killApp() {
        if (TextUtils.isEmpty(mPackageName)) {
            VCore.get().killAllApps();
        } else {
            VCore.get().killApp(mPackageName, mUserId);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset:
                new CommonDialog(this)
                        .setMessage(R.string.dlg_reset_device)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            if (mDeviceConfig != null) {
                                mDeviceConfig.enable = false;
                                reset();
                            }
                            VDeviceManager.get().updateDeviceConfig(mUserId, mDeviceConfig);
                            initBrandItem();
                            mSectionedExpandableLayoutHelper.notifyDataSetChanged();

                            Intent intent = new Intent();
                            intent.putExtra("pkg", mPackageName);
                            intent.putExtra("user", mUserId);
                            intent.putExtra("pos", mPosition);
                            intent.putExtra("result", "reset");
                            setResult(RESULT_OK, intent);
                            killApp();
                        })
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();

                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void reset() {
        mDeviceConfig.clear();
        mDeviceConfig.setProp("BRAND", Build.BRAND);
        mDeviceConfig.setProp("MODEL", Build.MODEL);
        mDeviceConfig.setProp("PRODUCT", Build.PRODUCT);
        mDeviceConfig.setProp("DEVICE", Build.DEVICE);
        mDeviceConfig.setProp("BOARD", Build.BOARD);
        mDeviceConfig.setProp("DISPLAY", Build.DISPLAY);
        mDeviceConfig.setProp("ID", Build.ID);
        mDeviceConfig.setProp("MANUFACTURER", Build.MANUFACTURER);
        mDeviceConfig.setProp("FINGERPRINT", Build.FINGERPRINT);
    }

    @Override
    public void itemClicked(BrandItem item) {
        if (item.getBrand().equals("custom")) {
            DeviceCustomActivity.open(this, mPackageName, mUserId, mPosition, REQUEST_CODE);
            return;
        }
        mSectionedExpandableLayoutHelper.notifyDataSetChanged();
        mToolbar.setTitle(item.getBrand() + " " + item.getModel());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            mDeviceConfig = VDeviceManager.get().getDeviceConfig(mUserId);
            initBrandItem();
            mSectionedExpandableLayoutHelper.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void itemClicked(Section section) {
        section.isExpanded = !section.isExpanded;
        mSectionedExpandableLayoutHelper.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.device_save_tv) {
            saveConfig();
        }
    }

    private void saveConfig() {
        if (mDeviceConfig != null) {
            mDeviceConfig.enable = true;
        }
        BrandItem brandItem = mSectionedExpandableLayoutHelper.getSelectBrandItem();
        if (brandItem != null) {
            mDeviceConfig.setProp("BRAND", brandItem.getBrand());
            mDeviceConfig.setProp("MODEL", brandItem.getModel());
            mDeviceConfig.setProp("MANUFACTURER", brandItem.getBrand());
            mDeviceConfig.setProp("PRODUCT", brandItem.getBrand());
            mDeviceConfig.setProp("DEVICE", brandItem.getBrand());
            mDeviceConfig.setProp("FINGERPRINT", deriveFingerprint(brandItem.getBrand()));
        }
        VDeviceManager.get().updateDeviceConfig(mUserId, mDeviceConfig);
        Intent intent = new Intent();
        intent.putExtra("pkg", mPackageName);
        intent.putExtra("user", mUserId);
        intent.putExtra("pos", mPosition);
        intent.putExtra("result", "save");
        setResult(RESULT_OK, intent);
        killApp();
        Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();
        finish();
    }

    private String deriveFingerprint(String brand) {
        return brand + '/' +
                brand + '/' +
                brand + ':' +
                Build.VERSION.RELEASE + '/' +
                Build.ID + '/' +
                Build.VERSION.INCREMENTAL + ':' +
                Build.TYPE + '/' +
                Build.TAGS;
    }
}
