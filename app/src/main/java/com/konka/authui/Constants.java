package com.konka.authui;

public class Constants {

    public static final String AUTH_STATUS = "persist.sys.auth.finish";
    public static final String AUTH_STATUS_NO = "no";
    public static final String AUTH_STATUS_YES = "yes";

    public static final String RMS_FIRST_BOOT_PATH = "params/cwmp.conf";
    public static final String RMS_FIRST_BOOT_KEY = "firstboot";
    public static final String RMS_CONNECTED = "0";
    public static final String RMS_DISCONNECTED = "1";

    public static final String VERIFY_PASSWORD = "10086@hitv";
    public static final String PROPERTY_USERNAME = "persist.sys.bytuetech.username";
    public static final String PROPERTY_PASSWORD = " persist.sys.bytuetech.password";
    public static final String URI = "content://stbconfig/authentication";

    public static final String PING_DNS = "211.141.90.68";
//    public static final String PING_DNS = "223.5.5.5"; // test

    public static final long RETRY_CONNECT_IP_SECONDS = 60;
    public static final long RETRY_CONNECT_DNS_SECONDS = 3;

    public static final String NETWORK_SETTING_PKG = "com.android.settings";
    public static final String NETWORK_SETTING_UI = NETWORK_SETTING_PKG + ".Settings";

    public static final String ACTION_REAUTH = "com.android.action.IPTV.IPTV_REAUTH";

    public interface NetworkStatus {
        int CONNECTED = 0;
        int DISCONNECTED = 1;
    }

    public interface PingStatus {
        int SUCCESS = 0;
        int FAILURE = 1;
    }

    public interface RmsStatus {
        int CONNECTED = 0;
        int DISCONNECTED = 1;
    }

    public interface AccountStatus {
        int VALID = 0;
        int INVALID = 1;
    }

    public interface AuthStatusType {
        int NONE_IP = 0;
        int PUBLIC_NETWORK_ERROR = 1;
        int RMS_CONNECT_ERROR = 2;
        int LICENSE_ERROR = 3;
    }

    public interface RequestCode {
        int REQUEST_CODE_LOGIN = 0;
    }
}
