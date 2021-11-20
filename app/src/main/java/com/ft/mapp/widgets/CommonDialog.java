package com.ft.mapp.widgets;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ft.mapp.R;

import androidx.appcompat.app.AppCompatDialog;

public class CommonDialog extends AppCompatDialog {
    private TextView titleTv;
    private TextView messageTv;
    private Button negativeBn, positiveBn;

    private View columnLineView;

    public CommonDialog(Context context) {
        super(context, R.style.CustomDialog);
    }

    private String message;
    private String title;
    private int negativeId = -1, positiveId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_dialog);
        setCanceledOnTouchOutside(false);
        initView();
        refreshView();
        initEvent();
    }

    private void initEvent() {
        positiveBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnPositiveClick != null) {
                    mOnPositiveClick.onClick(CommonDialog.this, Dialog.BUTTON_POSITIVE);
                }
                dismiss();
            }
        });
        negativeBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnNegativeClick != null) {
                    mOnNegativeClick.onClick(CommonDialog.this, Dialog.BUTTON_NEGATIVE);
                }
                dismiss();
            }
        });
    }

    private void refreshView() {
        if (!TextUtils.isEmpty(title)) {
            titleTv.setText(title);
            titleTv.setVisibility(View.VISIBLE);
        } else {
            titleTv.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(message)) {
            messageTv.setText(message);
        }


        if (positiveId != -1 && negativeId != -1) {
            columnLineView.setVisibility(View.VISIBLE);
        } else {
            columnLineView.setVisibility(View.GONE);
        }

        if (positiveId != -1) {
            positiveBn.setText(positiveId);
            positiveBn.setVisibility(View.VISIBLE);
        } else {
            positiveBn.setVisibility(View.GONE);
        }
        if (negativeId != -1) {
            negativeBn.setText(negativeId);
            negativeBn.setVisibility(View.VISIBLE);
        } else {
            negativeBn.setVisibility(View.GONE);
        }
    }

    @Override
    public void show() {
        super.show();
        refreshView();
    }

    private void initView() {
        negativeBn = findViewById(R.id.negtive);
        positiveBn = findViewById(R.id.positive);
        titleTv = findViewById(R.id.title);
        messageTv = findViewById(R.id.message);
        columnLineView = findViewById(R.id.column_line);
    }

    private OnClickListener mOnPositiveClick;
    private OnClickListener mOnNegativeClick;

    public CommonDialog setPositiveButton(int textId, final OnClickListener listener) {
        positiveId = textId;
        mOnPositiveClick = listener;
        return this;
    }

    public CommonDialog setNegativeButton(int textId, final OnClickListener listener) {
        negativeId = textId;
        mOnNegativeClick = listener;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public CommonDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public CommonDialog setMessage(int messageId) {
        this.message = getContext().getResources().getString(messageId);
        return this;
    }

    public String getTitle() {
        return title;
    }

    public CommonDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public CommonDialog setTitleId(int titleId) {
        this.title = getContext().getResources().getString(titleId);
        return this;
    }
}