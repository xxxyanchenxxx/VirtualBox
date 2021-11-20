package com.ft.mapp.dialog;

import android.content.Context;
import androidx.appcompat.app.AppCompatDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ft.mapp.R;

public class LoadingDialog extends AppCompatDialog {

    private TextView mMsgTv;

    public LoadingDialog(Context context) {
        this(context, context.getString(R.string.process_tip));
    }

    public LoadingDialog(Context context, String msg) {
        this(context, R.style.VBDialogTheme, msg);
    }

    public LoadingDialog(Context context, int theme, String msg) {
        super(context, theme);
        setContentView(R.layout.layout_loading);

        mMsgTv = findViewById(R.id.loading_title);
        if (mMsgTv != null) {
            if (TextUtils.isEmpty(msg)) {
                mMsgTv.setVisibility(View.GONE);
            } else {
                mMsgTv.setText(msg);
            }
        }
        fullWindow();
    }

    private void fullWindow() {
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    public void setMessage(String msg) {
        mMsgTv.setText(msg);
    }

}
