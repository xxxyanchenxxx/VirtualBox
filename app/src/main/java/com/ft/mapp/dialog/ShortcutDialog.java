package com.ft.mapp.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ft.mapp.R;
import com.ft.mapp.utils.AppSharePref;
import com.fun.vbox.client.core.VCore;

import androidx.appcompat.app.AppCompatDialog;

public class ShortcutDialog extends AppCompatDialog implements View.OnClickListener {
    private ImageView mCloseIv;
    private TextView mOkTv;
    private Context mContext;
    private EditText mEditText;
    private int mUserId;
    private String mPkg;
    private String mName;

    public ShortcutDialog(Context context, int userId, String packagename, String appName) {
        super(context, R.style.VBDialogTheme);
        setContentView(R.layout.layout_custom_shortcut);
        this.mContext = context;
        this.mUserId = userId;
        this.mPkg = packagename;
        this.mName = appName;

        initView();
        initData();

        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        setCanceledOnTouchOutside(false);
    }


    private void initData() {
        if (mUserId != 0) {
            int nameIndex = mUserId + 1;
            mEditText.setText(mName + "(" + nameIndex + ")");
        } else {
            mEditText.setText(mName);
        }
    }

    private void initView() {
        mCloseIv = findViewById(R.id.native_close_iv);
        mOkTv = findViewById(R.id.native_ok);
        mCloseIv.setOnClickListener(this);
        mOkTv.setOnClickListener(this);
        mEditText = findViewById(R.id.cut_name_et);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.native_close_iv) {
            dismiss();
        } else if (v.getId() == R.id.native_ok) {
            handleCreateShortcutInner();
        }
    }

    private void handleCreateShortcutInner() {
        VCore.get().createShortcut(mUserId, mPkg, new VCore.OnEmitShortcutListener() {

            @Override
            public Bitmap getIcon(Bitmap bitmap) {
                return bitmap;
            }

            @Override
            public String getName(String s) {
                return mEditText.getText().toString();
            }
        });
        dismiss();

        boolean showShortcutGuide = AppSharePref.getInstance(mContext).getBoolean("shortcut_guide",
                true);
        if (showShortcutGuide) {
            new ShortcutPermissionDialog(mContext).show();
        }
    }



}
