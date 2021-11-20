package com.fun.vbox.client.core;

import android.os.Bundle;

public interface V64BitHelperCallback {

    V64BitHelperCallback EMPTY = new EmptyDelegate();

    class EmptyDelegate implements V64BitHelperCallback {

        @Override
        public Bundle invokeFromAnyWhere(Bundle param) {
            return null;
        }
    }

    Bundle invokeFromAnyWhere(Bundle param);
}
