package com.ft.mapp.home;

import com.ft.mapp.abs.BasePresenter;
import com.ft.mapp.abs.BaseView;
import com.ft.mapp.home.models.AppInfo;

import java.util.List;

/**
 *
 * @version 1.0
 */
class ListAppContract {
    interface ListAppView extends BaseView<ListAppPresenter> {

        void startLoading();

        void loadFinish(List<AppInfo> infoList);
    }

    interface ListAppPresenter extends BasePresenter {

    }
}
