package com.ft.mapp.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ft.mapp.BuildConfig;;
import com.ft.mapp.R;
import com.ft.mapp.abs.ui.VActivity;
import com.jaeger.library.StatusBarUtil;

import java.util.Locale;

import androidx.annotation.Nullable;

public class AboutActivity extends VActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTranslucent(this);
        setContentView(R.layout.activity_about);
        TextView versionTv = findViewById(R.id.about_app_verion);
        versionTv.setText(String.format(Locale.getDefault(), "%s V%s",
                getString(R.string.app_name), BuildConfig.VERSION_NAME));
        findViewById(R.id.about_icon).setOnClickListener(this);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }
}
