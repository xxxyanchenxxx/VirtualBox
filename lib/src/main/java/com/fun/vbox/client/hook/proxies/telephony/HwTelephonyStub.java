package com.fun.vbox.client.hook.proxies.telephony;

import com.fun.vbox.client.hook.base.BinderInvocationProxy;
import com.fun.vbox.client.hook.annotations.Inject;

import mirror.com.android.internal.telephony.IHwTelephony;

/**
 *
 * @see android.telephony.TelephonyManager
 */
@Inject(MethodProxies.class)
public class HwTelephonyStub extends BinderInvocationProxy {

	public HwTelephonyStub() {
		super(IHwTelephony.Stub.TYPE, "phone_huawei");
	}

	@Override
	protected void onBindMethods() {
        addMethodProxy(new GetUniqueDeviceId());
	}

    private static class GetUniqueDeviceId extends MethodProxies.GetDeviceId{
        @Override
        public String getMethodName() {
            return "getUniqueDeviceId";
        }
    }

}
