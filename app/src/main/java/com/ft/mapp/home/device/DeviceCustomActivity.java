package com.ft.mapp.home.device;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ft.mapp.R;
import com.ft.mapp.abs.ui.VActivity;
import com.ft.mapp.home.models.DeviceData;
import com.ft.mapp.widgets.CommonDialog;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.ipc.VDeviceManager;
import com.fun.vbox.remote.VDeviceConfig;
import com.jaeger.library.StatusBarUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

public class DeviceCustomActivity extends VActivity implements View.OnClickListener {

    public static void open(Activity activity, DeviceData data, int position, int requestCode) {
        open(activity, data.packageName, data.userId, position, requestCode);
    }

    public static void open(Activity activity, String packageName, int userId, int position,
                            int requestCode) {
        Intent intent = new Intent(activity, DeviceCustomActivity.class);
        intent.putExtra("pkg", packageName);
        intent.putExtra("user", userId);
        intent.putExtra("pos", position);
        activity.startActivityForResult(intent, requestCode);
    }

    private String mPackageName;
    private int mUserId;
    private int mPosition;
    private VDeviceConfig mDeviceConfig;
    private TelephonyManager mTelephonyManager;
    private WifiManager mWifiManager;
    private EditText edt_androidId, edt_imei, edt_imsi, edt_mac;
    private EditText edt_brand, edt_model, edt_name, edt_device, edt_board, edt_display, edt_id, edt_serial, edt_manufacturer, edt_fingerprint;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTranslucent(this);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_mock_custom_device);
        Toolbar toolbar = bind(R.id.task_top_toolbar);
        toolbar.setTitle(R.string.menu_mock_phone);
        setSupportActionBar(toolbar);
        enableBackHome();
        edt_androidId = findViewById(R.id.edt_androidId);
        edt_imei = findViewById(R.id.edt_imei);
        edt_imsi = findViewById(R.id.edt_imsi);
        edt_mac = findViewById(R.id.edt_mac);

        edt_brand = findViewById(R.id.edt_brand);
        edt_model = findViewById(R.id.edt_model);
        edt_name = findViewById(R.id.edt_name);
        edt_device = findViewById(R.id.edt_device);
        edt_board = findViewById(R.id.edt_board);
        edt_display = findViewById(R.id.edt_display);
        edt_id = findViewById(R.id.edt_id);
        edt_serial = findViewById(R.id.edt_serial);
        edt_manufacturer = findViewById(R.id.edt_manufacturer);
        edt_fingerprint = findViewById(R.id.edt_fingerprint);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (getIntent() != null) {
            mPackageName = getIntent().getStringExtra("pkg");
            mUserId = getIntent().getIntExtra("user", 0);
        }
        mDeviceConfig = VDeviceManager.get().getDeviceConfig(mUserId);
        updateConfig();

        findViewById(R.id.device_save_tv).setOnClickListener(this);
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

                            Intent intent = new Intent();
                            intent.putExtra("pkg", mPackageName);
                            intent.putExtra("user", mUserId);
                            intent.putExtra("pos", mPosition);
                            intent.putExtra("result", "reset");
                            setResult(RESULT_OK, intent);
                            killApp();
                            updateConfig();
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

    private String getValue(EditText text) {
        return text.getText().toString().trim();
    }

    private void setValue(EditText text, String value, String defValue) {
        if (TextUtils.isEmpty(value)) {
            text.setText(defValue);
            return;
        }
        text.setText(value);
    }

    private void fillConfig() {
        if (mDeviceConfig == null) {
            return;
        }
        mDeviceConfig.setProp("BRAND", getValue(edt_brand));
        mDeviceConfig.setProp("MODEL", getValue(edt_model));
        mDeviceConfig.setProp("PRODUCT", getValue(edt_name));
        mDeviceConfig.setProp("DEVICE", getValue(edt_device));
        mDeviceConfig.setProp("BOARD", getValue(edt_board));
        mDeviceConfig.setProp("DISPLAY", getValue(edt_display));
        mDeviceConfig.setProp("ID", getValue(edt_id));
        mDeviceConfig.setProp("MANUFACTURER", getValue(edt_manufacturer));
        mDeviceConfig.setProp("FINGERPRINT", getValue(edt_fingerprint));

        mDeviceConfig.serial = getValue(edt_serial);
        mDeviceConfig.deviceId = getValue(edt_imei);
        mDeviceConfig.iccId = getValue(edt_imsi);
        mDeviceConfig.wifiMac = getValue(edt_mac);
        mDeviceConfig.androidId = getValue(edt_androidId);
    }

    @SuppressLint("HardwareIds")
    private void updateConfig() {
        if (mDeviceConfig == null) {
            return;
        }
        setValue(edt_brand, mDeviceConfig.getProp("BRAND"), Build.BRAND);
        setValue(edt_model, mDeviceConfig.getProp("MODEL"), Build.MODEL);
        setValue(edt_name, mDeviceConfig.getProp("PRODUCT"), Build.PRODUCT);
        setValue(edt_device, mDeviceConfig.getProp("DEVICE"), Build.DEVICE);
        setValue(edt_board, mDeviceConfig.getProp("BOARD"), Build.BOARD);
        setValue(edt_display, mDeviceConfig.getProp("DISPLAY"), Build.DISPLAY);
        setValue(edt_id, mDeviceConfig.getProp("ID"), Build.ID);
        setValue(edt_manufacturer, mDeviceConfig.getProp("MANUFACTURER"), Build.MANUFACTURER);
        setValue(edt_fingerprint, mDeviceConfig.getProp("FINGERPRINT"), Build.FINGERPRINT);

        setValue(edt_serial, mDeviceConfig.serial, Build.SERIAL);
        setValue(edt_mac, mDeviceConfig.wifiMac, getDefaultWifiMac());
        setValue(edt_androidId, mDeviceConfig.androidId, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
                return;
            }
        }
        setValue(edt_imei, mDeviceConfig.deviceId, mTelephonyManager.getDeviceId());
        setValue(edt_imsi, mDeviceConfig.iccId, mTelephonyManager.getSimSerialNumber());
    }

    @SuppressLint("HardwareIds")
    private String getDefaultWifiMac() {
        String[] files = {"/sys/class/net/wlan0/address", "/sys/class/net/eth0/address", "/sys/class/net/wifi/address"};
        String mac = mWifiManager.getConnectionInfo().getMacAddress();
        if (TextUtils.isEmpty(mac)) {
            for (String file : files) {
                try {
                    mac = readFileAsString(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(mac)) {
                    break;
                }
            }
        }
        return mac;
    }

    private String readFileAsString(String filePath)
            throws IOException {
        StringBuilder sb = new StringBuilder(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            sb.append(readData);
        }
        reader.close();
        return sb.toString().trim();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_GRANTED) {
                updateConfig();
                break;
            }
        }
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
        fillConfig();
        updateConfig();
        VDeviceManager.get().updateDeviceConfig(mUserId, mDeviceConfig);
        Intent intent = new Intent();
        intent.putExtra("pkg", mPackageName);
        intent.putExtra("user", mUserId);
        intent.putExtra("pos", mPosition);
        intent.putExtra("result", "save");
        setResult(RESULT_OK, intent);
        if (TextUtils.isEmpty(mPackageName)) {
            VCore.get().killAllApps();
        } else {
            VCore.get().killApp(mPackageName, mUserId);
        }
        killApp();
        Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();
        finish();
    }
}
