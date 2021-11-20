package com.fun.vbox.client.hook.proxies.graphics;

import com.fun.vbox.client.hook.base.BinderInvocationProxy;
import com.fun.vbox.client.hook.base.ReplaceCallingPkgMethodProxy;

import mirror.vbox.view.IGraphicsStats;


/**
 * @author Lody
 */
public class GraphicsStatsStub extends BinderInvocationProxy {

	public GraphicsStatsStub() {
		super(IGraphicsStats.Stub.asInterface, "graphicsstats");
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new ReplaceCallingPkgMethodProxy("requestBufferForProcess"));
	}
}
