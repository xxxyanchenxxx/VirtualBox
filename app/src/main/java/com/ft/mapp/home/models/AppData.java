package com.ft.mapp.home.models;

import android.graphics.drawable.Drawable;

/**
 * @author Lody
 */

public interface AppData {

    boolean isLoading();

    boolean isFirstOpen();

    Drawable getIcon();

    String getName();

    String getPackageName();

    boolean canReorder();

    boolean canLaunch();

    boolean canDelete();

    boolean canCreateShortcut();
}
