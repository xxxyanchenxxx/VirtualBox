package com.ft.mapp.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ft.mapp.R;



public class ActionView extends RelativeLayout implements View.OnClickListener {
    private Context mContext;
    private TextView mTitleTv;
    private ImageView mBackIv;
    private ImageView mRightIv;
    private String mTilte = "";

    public ActionView(Context context) {
        this(context, null);
    }

    public ActionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.mContext = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        layoutInflater.inflate(R.layout.layout_action_view, this, true);
        setBackgroundColor(getResources().getColor(R.color.transparent));

        mTitleTv = findViewById(R.id.action_title_tv);
        mBackIv = findViewById(R.id.action_back_iv);
        mRightIv = findViewById(R.id.action_right_iv);
        mBackIv.setOnClickListener(this);
        mRightIv.setOnClickListener(this);

        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.ActionView);
        mTilte = typedArray.getString(R.styleable.ActionView_title);
        int iconId = typedArray.getResourceId(R.styleable.ActionView_icon, R.drawable.main_setting);
        boolean iconVisible = typedArray.getBoolean(R.styleable.ActionView_icon_visible, true);
        boolean backVisible = typedArray.getBoolean(R.styleable.ActionView_back_visible, true);
        int color = typedArray.getColor(R.styleable.ActionView_text_color,
                getResources().getColor(R.color.white));
        int backSrc = typedArray.getResourceId(R.styleable.ActionView_back_src,
                R.drawable.ic_back_white);
        typedArray.recycle();
        mTitleTv.setText(mTilte);
        mTitleTv.setTextColor(color);
        mRightIv.setImageResource(iconId);
        mBackIv.setImageResource(backSrc);
        if (!iconVisible) {
            mRightIv.setVisibility(View.GONE);
        }
        if (!backVisible) {
            mBackIv.setVisibility(View.GONE);
        }
    }

    public void setTitle(String title) {
        mTitleTv.setText(title);
    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        if (vId == R.id.action_back_iv) {
            if (mContext instanceof Activity) {
                ((Activity) mContext).finish();
            }
        } else if (vId == R.id.action_right_iv) {

        }

    }

}
