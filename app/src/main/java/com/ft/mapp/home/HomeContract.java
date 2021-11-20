package com.ft.mapp.home;


import com.ft.mapp.abs.BasePresenter;
import com.ft.mapp.abs.BaseView;
import com.ft.mapp.home.models.AppData;
import com.ft.mapp.home.models.AppInfoLite;

import java.util.List;

/* package */ class HomeContract {

    /* package */ interface HomeView extends BaseView<HomePresenter> {

        void showLoading();

        void hideLoading();

        void loadFinish(List<AppData> appModels);

        void loadError(Throwable err);

        void showGuide();

        void addAppToLauncher(AppData model);

        void removeAppToLauncher(AppData model);

        void refreshLauncherItem(AppData model);
    }

    /* package */ interface HomePresenter extends BasePresenter {

        void dataChanged();

        void addApp(AppInfoLite info);

        void deleteApp(AppData data);

        void createShortcut(AppData data);
    }

}
