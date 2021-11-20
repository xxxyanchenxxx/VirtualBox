package com.ft.mapp.dialog;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.ft.mapp.R;
import com.ft.mapp.VApp;
import com.ft.mapp.utils.AppSharePref;

import androidx.appcompat.app.AppCompatDialog;



public class ShortcutPermissionDialog extends AppCompatDialog implements View.OnClickListener {
    private TextView mOkTv;
    private Context mContext;
    private CheckBox mCheckBox;


    public ShortcutPermissionDialog(Context context) {
        super(context, R.style.VBDialogTheme);
        setContentView(R.layout.layout_permission_shortcut);
        this.mContext = context;


        initView();
        initData();

        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }


    private void initData() {

    }

    private void initView() {
        mOkTv = findViewById(R.id.native_ok);
        mOkTv.setOnClickListener(this);
        findViewById(R.id.native_cancel).setOnClickListener(this);
        mCheckBox = findViewById(R.id.checkbox);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.native_cancel) {
            if (mCheckBox.isChecked()) {
                AppSharePref.getInstance(VApp.getApp()).putBoolean("shortcut_guide", false);
            }
            dismiss();
        } else if (v.getId() == R.id.native_ok) {
            if (mCheckBox.isChecked()) {
                AppSharePref.getInstance(VApp.getApp()).putBoolean("shortcut_guide", false);
            }
            gotoSetting();
            dismiss();
        }
    }


    private void gotoSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", VApp.getApp().getPackageName(), null);
        intent.setData(uri);
        try {
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
