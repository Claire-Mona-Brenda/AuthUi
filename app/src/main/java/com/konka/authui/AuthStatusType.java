package com.konka.authui;

import android.support.annotation.IntDef;

@IntDef(flag = true, value = {
        Constants.AuthStatusType.NONE_IP,
        Constants.AuthStatusType.PUBLIC_NETWORK_ERROR,
        Constants.AuthStatusType.RMS_CONNECT_ERROR,
        Constants.AuthStatusType.LICENSE_ERROR
})
public @interface AuthStatusType {
}
