package com.ft.mapp.splash;

import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import com.ft.mapp.R;
import com.ft.mapp.VCommends;
import com.ft.mapp.abs.ui.VActivity;
import com.ft.mapp.home.HomeActivity;
import com.ft.mapp.utils.VUiKit;
import com.fun.vbox.client.core.VCore;
import com.jaeger.library.StatusBarUtil;
import com.ft.mapp.utils.VUiKit;

import jonathanfinerty.once.Once;

public class SplashActivity extends VActivity {
    private static final String TAG = "SplashActivity";
    private FrameLayout mSplashContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        @SuppressWarnings("unused")
        boolean enterGuide = !Once.beenDone(Once.THIS_APP_INSTALL, VCommends.TAG_NEW_VERSION);
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTransparent(this);
        setContentView(R.layout.activity_splash);
        mSplashContainer = findViewById(R.id.splash_container);


        VUiKit.defer().when(() -> {
            if (!Once.beenDone("collect_flurry")) {
                Once.markDone("collect_flurry");
            }
            doActionInThread();
        }).done((res) -> {
            goToMainActivity();
        });
    }

    /**
     * 跳转到主页面
     */
    private void goToMainActivity() {
        Log.d(TAG, "goToMainActivity");
        HomeActivity.goHome(this);
        if (mSplashContainer != null) {
            mSplashContainer.removeAllViews();
        }
        finish();
    }

    private void showToast(String msg) {
        Log.d(TAG, msg);
        //TToast.show(this, msg);
    }

    private void doActionInThread() {
        if (!VCore.get().isEngineLaunched()) {
            VCore.get().waitForEngine();
        }
    }
}
