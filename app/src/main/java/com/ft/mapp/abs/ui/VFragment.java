package com.ft.mapp.abs.ui;


import android.app.Activity;

import com.ft.mapp.abs.BasePresenter;

import androidx.fragment.app.Fragment;

/**
 * @author Lody
 */
public class VFragment<T extends BasePresenter> extends Fragment {

	protected T mPresenter;

	public T getPresenter() {
		return mPresenter;
	}

	public void setPresenter(T presenter) {
		this.mPresenter = presenter;
	}

	public void finishActivity() {
		Activity activity = getActivity();
		if (activity != null) {
			activity.finish();
		}
	}

	public void destroy() {
		finishActivity();
	}
}
