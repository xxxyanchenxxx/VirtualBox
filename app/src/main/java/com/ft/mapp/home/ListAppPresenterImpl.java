package com.ft.mapp.home;

import android.app.Activity;

import com.ft.mapp.home.repo.AppDataSource;
import com.ft.mapp.home.repo.AppRepository;

/**
 * @author Lody
 */
class ListAppPresenterImpl implements ListAppContract.ListAppPresenter {

    private Activity mActivity;
    private ListAppContract.ListAppView mView;
    private AppDataSource mRepository;

    ListAppPresenterImpl(Activity activity, ListAppContract.ListAppView view) {
        mActivity = activity;
        mView = view;
        mRepository = new AppRepository(activity);
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        mView.setPresenter(this);
        mView.startLoading();
        mRepository.getInstalledApps(mActivity).done(mView::loadFinish);
    }
}
