package com.ft.mapp.abs.ui;

import android.app.Activity;
import android.content.Context;
import android.view.MenuItem;
import android.view.View;

import com.ft.mapp.abs.BaseView;

import androidx.annotation.IdRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * @author Lody
 */
public class VActivity extends AppCompatActivity {

    /**
     * Implement of {@link BaseView#getActivity()}
     */
    public Activity getActivity() {
        return this;
    }

    /**
     * Implement of {@link BaseView#getContext()} ()}
     */
    public Context getContext() {
        return this;
    }

    public Fragment findFragmentById(@IdRes int id) {
        return getSupportFragmentManager().findFragmentById(id);
    }

    public void replaceFragment(@IdRes int id, Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(id, fragment).commit();
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T bind(int id) {
        return (T) findViewById(id);
    }

    public void enableBackHome() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
