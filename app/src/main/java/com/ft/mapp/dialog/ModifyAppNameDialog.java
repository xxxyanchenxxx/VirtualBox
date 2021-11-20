package com.ft.mapp.dialog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ft.mapp.R;
import com.ft.mapp.utils.AppDataUtils;

import androidx.appcompat.app.AppCompatDialog;

public class ModifyAppNameDialog extends AppCompatDialog implements View.OnClickListener {
    private ImageView mCloseIv;
    private TextView mOkTv;
    private Context mContext;
    private EditText mEditText;
    private int mUserId;
    private String mPkg;
    private String mName;
    private IModifyName mIModifyName;

    public ModifyAppNameDialog(Context context, int userId, String packageName, String appName) {
        super(context, R.style.VBDialogTheme);
        setContentView(R.layout.layout_custom_name);
        this.mContext = context;
        this.mUserId = userId;
        this.mPkg = packageName;
        this.mName = appName;

        initView();
        initData();

        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        setCanceledOnTouchOutside(false);
    }

    public ModifyAppNameDialog setModifyNameCallback(IModifyName iModifyName) {
        mIModifyName = iModifyName;
        return this;
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
            dismiss();
            String appName = mEditText.getText().toString();
            AppDataUtils.setAppName(mPkg, mUserId, appName);
            if (mIModifyName != null) {
                mIModifyName.onResult(appName);
            }
        }
    }

    public interface IModifyName {
        void onResult(String name);
    }
}
