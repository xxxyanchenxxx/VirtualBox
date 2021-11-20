package com.fun.vbox.client.hook.proxies.media.router;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.fun.vbox.client.hook.base.BinderInvocationProxy;
import com.fun.vbox.client.hook.base.ReplaceCallingPkgMethodProxy;

import mirror.vbox.media.IMediaRouterService;

/**
 *
 * @see android.media.MediaRouter
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MediaRouterServiceStub extends BinderInvocationProxy {

    public MediaRouterServiceStub() {
        super(IMediaRouterService.Stub.asInterface, Context.MEDIA_ROUTER_SERVICE);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceCallingPkgMethodProxy("registerClientAsUser"));
    }
}
