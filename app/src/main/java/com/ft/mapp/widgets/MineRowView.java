package com.ft.mapp.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ft.mapp.R;

/**
 * author : zchu
 * date   : 2017/8/23
 * desc   :
 */

public class MineRowView extends RelativeLayout {

    private ImageView ivIcon;
    private TextView tvTitle;
    private View arrow;
    private TextView mDetailTv;

    public MineRowView(Context context) {
        this(context, null);
    }

    public MineRowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MineRowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        initAttributeSet(context, attrs);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_mine_row, this, true);
        ivIcon = findViewById(R.id.iv_icon);
        tvTitle = findViewById(R.id.tv_title);
        arrow = findViewById(R.id.arrow);
        mDetailTv = findViewById(R.id.tv_detail);
    }

    private void initAttributeSet(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MineRowView);
        int iconResId = typedArray.getResourceId(R.styleable.MineRowView_mine_icon, 0);
        if (iconResId != 0) {
            ivIcon.setImageResource(iconResId);
        }

        String title = typedArray.getString(R.styleable.MineRowView_mine_title);
        String detail = typedArray.getString(R.styleable.MineRowView_mine_detail);
        int tvColor = typedArray.getResourceId(R.styleable.MineRowView_mine_tv_color, R.color.colorPrimary);
        tvTitle.setText(title);
        tvTitle.setTextColor(getResources().getColor(tvColor));
        if (!TextUtils.isEmpty(detail)) {
            mDetailTv.setText(detail);
            mDetailTv.setVisibility(View.VISIBLE);
        }
        boolean arrowVisibility = typedArray.getBoolean(R.styleable.MineRowView_mine_arrow_visibility, true);
        arrow.setVisibility(arrowVisibility ? View.VISIBLE : View.GONE);
        typedArray.recycle();


    }

    public void setTvTitle(String title) {
        tvTitle.setText(title);
    }

    public void setDetailMsg(String msg) {
        mDetailTv.setText(msg);
    }
}
