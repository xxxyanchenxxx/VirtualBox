package com.ft.mapp.open;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.ft.mapp.R;
import com.ft.mapp.dialog.LoadingBottomDialog;
import com.ft.mapp.utils.VUiKit;
import com.fun.vbox.client.core.VCore;
import com.fun.vbox.client.ipc.VActivityManager;

import java.net.URISyntaxException;

public class ShortcutHandleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        int userId = intent.getIntExtra("_VBOX_|_user_id_", 0);
        String splashUri = intent.getStringExtra("_VBOX_|_splash_");
        String targetUri = intent.getStringExtra("_VBOX_|_uri_");
        String pkg = intent.getStringExtra("_VBOX_|_pkg_");
        Intent splashIntent = null;
        Intent targetIntent = null;
        if (splashUri != null) {
            try {
                splashIntent = Intent.parseUri(splashUri, 0);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        if (targetUri != null) {
            try {
                targetIntent = Intent.parseUri(targetUri, 0);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        if (targetIntent == null) {
            return;
        }

        targetIntent.setSelector(null);

        setContentView(R.layout.activity_splash);

        LoadingBottomDialog loadingDialog = new LoadingBottomDialog(this, "App");
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();

        if (splashIntent == null) {
            final Intent launchIntent = targetIntent;
            VUiKit.defer().when(() -> {
                VCore.get().setUiCallback(launchIntent, mUiCallback);
                VActivityManager.get().startActivity(launchIntent, userId);
            }).fail(result -> finish());
        } else {
            splashIntent.putExtra(Intent.EXTRA_INTENT, targetIntent);
            splashIntent.putExtra(Intent.EXTRA_CC, userId);
            startActivity(splashIntent);
        }

    }

    private final VCore.UiCallback mUiCallback = new VCore.UiCallback() {

        @Override
        public void onAppOpened(String packageName, int userId) {
            finish();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
