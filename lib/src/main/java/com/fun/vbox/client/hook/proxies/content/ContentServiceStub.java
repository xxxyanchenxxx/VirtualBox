package com.fun.vbox.client.hook.proxies.content;

import com.fun.vbox.client.hook.annotations.Inject;
import com.fun.vbox.client.hook.annotations.LogInvocation;
import com.fun.vbox.client.hook.base.BinderInvocationProxy;

import mirror.vbox.content.ContentResolver;
import mirror.vbox.content.IContentService;

/**
 *
 * @see IContentService
 */
@LogInvocation(LogInvocation.Condition.ALWAYS)
@Inject(MethodProxies.class)
public class ContentServiceStub extends BinderInvocationProxy {
    private static final String TAG = ContentServiceStub.class.getSimpleName();

    public ContentServiceStub() {
        super(IContentService.Stub.asInterface, "content");
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
        ContentResolver.sContentService.set(getInvocationStub().getProxyInterface());
    }


}
