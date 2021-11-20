package com.fun.vbox.helper.compat;

import java.lang.reflect.Method;
import java.util.List;

import mirror.vbox.content.pm.ParceledListSlice;
import mirror.vbox.content.pm.ParceledListSliceJBMR2;

/**
 *
 *
 */
public class ParceledListSliceCompat {

	public static boolean isReturnParceledListSlice(Method method) {
		return method != null && method.getReturnType() == ParceledListSlice.TYPE;
	}

	public static  Object create(List list) {
		if (ParceledListSliceJBMR2.ctor != null) {
			return ParceledListSliceJBMR2.ctor.newInstance(list);
		} else {
			Object slice = ParceledListSlice.ctor.newInstance();
			for (Object item : list) {
				ParceledListSlice.append.call(slice, item);
			}
			ParceledListSlice.setLastSlice.call(slice, true);
			return slice;
		}
	}

}
