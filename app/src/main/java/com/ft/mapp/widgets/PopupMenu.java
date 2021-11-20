package com.ft.mapp.widgets;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;

import com.ft.mapp.R;

public class PopupMenu extends PopupWindow implements OnClickListener {

    private Context mContext;
    private View mPopView;

    private View mVItem1;
    private View mVItem2;
    private View mVItem3;
    private View mVItem4;
    private View mVItem5;
    private View mMultiLine;

    private OnItemClickListener mOnItemClickListener;

    public enum MENU_ITEM {
        SETTING, MULTI, NAME, SHORTCUT, DELETE
    }

    public PopupMenu(Context context) {
        super(context);
        this.mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        mPopView = inflater.inflate(R.layout.popup_menu, null);
        this.setContentView(mPopView);
        this.setWidth(dip2px(context, 120));
        this.setHeight(LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setTouchable(true);
        this.setOutsideTouchable(true);
        ColorDrawable dw = new ColorDrawable(0x00000000);
        this.setBackgroundDrawable(dw);

        mVItem1 = mPopView.findViewById(R.id.ly_item1);
        mVItem2 = mPopView.findViewById(R.id.ly_item2);
        mVItem3 = mPopView.findViewById(R.id.ly_item3);
        mVItem4 = mPopView.findViewById(R.id.ly_item4);
        mVItem5 = mPopView.findViewById(R.id.ly_item5);
        mMultiLine = mPopView.findViewById(R.id.multi_line);
        mVItem1.setOnClickListener(this);
        mVItem2.setOnClickListener(this);
        mVItem3.setOnClickListener(this);
        mVItem4.setOnClickListener(this);
        mVItem5.setOnClickListener(this);
    }

    public void showLocation(View view) {
        showAsDropDown(view, dip2px(mContext, 0), dip2px(mContext, -8));
    }

    public void showMultiMenu(boolean isShow) {
        if (isShow) {
            mVItem2.setVisibility(View.VISIBLE);
            mMultiLine.setVisibility(View.VISIBLE);
        } else {
            mVItem2.setVisibility(View.GONE);
            mMultiLine.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        MENU_ITEM menuItem = null;
        if (v == mVItem1) {
            menuItem = MENU_ITEM.SETTING;
        } else if (v == mVItem2) {
            menuItem = MENU_ITEM.MULTI;
        } else if (v == mVItem3) {
            menuItem = MENU_ITEM.NAME;
        } else if (v == mVItem4) {
            menuItem = MENU_ITEM.SHORTCUT;
        } else if (v == mVItem5) {
            menuItem = MENU_ITEM.DELETE;
        }
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onClick(menuItem);
        }
        dismiss();
    }

    private int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public interface OnItemClickListener {
        void onClick(MENU_ITEM item);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

}