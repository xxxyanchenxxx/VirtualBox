package com.ft.mapp.widgets.quicksidebar.listener;

public interface OnQuickSideBarTouchListener {
    void onLetterChanged(String letter, int position, float y);
    void onLetterTouching(boolean touching);
}
