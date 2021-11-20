package com.fun.vbox.client.hook.base;

/**
 * @author Lody
 */

public class StaticMethodProxy extends MethodProxy {

	private String mName;

	public StaticMethodProxy(String name) {
		this.mName = name;
	}

	@Override
	public String getMethodName() {
		return mName;
	}
}
