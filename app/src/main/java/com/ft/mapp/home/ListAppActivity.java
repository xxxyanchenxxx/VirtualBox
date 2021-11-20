package com.ft.mapp.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import com.ft.mapp.R;
import com.ft.mapp.VCommends;
import com.ft.mapp.abs.ui.VActivity;
import com.jaeger.library.StatusBarUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

/**
 * @author Lody
 */
public class ListAppActivity extends VActivity {


    public static void gotoListApp(Activity activity) {
        Intent intent = new Intent(activity, ListAppActivity.class);
        activity.startActivityForResult(intent, VCommends.REQUEST_SELECT_APP);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTranslucent(this);
        setContentView(R.layout.activity_clone_app);

        // Request permission to access external storage
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.clone_content_layout, new ListAppFragment()).commit();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_GRANTED) {
                break;
            }
        }
    }
}
