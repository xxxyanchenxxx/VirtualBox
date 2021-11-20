package com.ft.mapp.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import com.ft.mapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Locale;

import androidx.annotation.NonNull;

public class LoadingBottomDialog extends BottomSheetDialog {
    private String message;
    private TextView mNameTv;
    private Context mContext;

    public LoadingBottomDialog(@NonNull Context context, String message) {
        this(context, R.style.MyDialogStyleBottom);
        this.message = message;
        mContext = context;
        initViews(context);
    }

    public LoadingBottomDialog(@NonNull Context context, int theme) {
        super(context, theme);
        initViews(context);

    }

    public void setMessage(String msg) {
        message = msg;
        if (TextUtils.isEmpty(message)) {
            mNameTv.setText(R.string.process_tip);
        } else {
            mNameTv.setText(String.format(Locale.getDefault(), mContext.getString(R.string.opening), message));
        }
    }

    private void initViews(Context context) {
        setContentView(R.layout.activity_loading);
        mNameTv = findViewById(R.id.app_name);
        if (mNameTv != null) {
            if (TextUtils.isEmpty(message)) {
                mNameTv.setText(R.string.process_tip);
            } else {
                mNameTv.setText(String.format(Locale.getDefault(), context.getString(R.string.opening), message));
            }
        }
    }
}
