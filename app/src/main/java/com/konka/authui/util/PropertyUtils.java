package com.konka.authui.util;

import android.annotation.SuppressLint;

import com.konka.authui.Constants;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

public class PropertyUtils {

    public static String getProperty(String key) {
        try {
            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            return (String) get.invoke(c, key, "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void setProperty(String key, String value) {
        try {
            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String isRMSConnected(String key, String defValue) {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(Constants.RMS_FIRST_BOOT_PATH));
            return properties.getProperty(key, defValue);
        } catch (IOException e) {
            e.printStackTrace();
            return Constants.RMS_DISCONNECTED;
        }
    }
}
