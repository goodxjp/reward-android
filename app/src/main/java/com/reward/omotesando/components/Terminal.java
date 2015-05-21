package com.reward.omotesando.components;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 端末情報。
 *
 * - http://iridge.jp/blog/20140403/
 * - http://ohwhsmm7.blog28.fc2.com/blog-entry-365.html
 */
public class Terminal {
    Map<String, String> buildInfo = new HashMap<String, String>();

    public static String getTerminalId(Context context) {
        // TODO: アンインストール、初期化されたときでも一意になるようなしくみを考える
        return getAndroidId(context);
    }

    // <uses-permission android:name="android.permission.READ_PHONE_STATE"/> が必要
    public static String getDeviceId(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getDeviceId();
    }

    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    // <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
    public static String getMacAddress(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return manager.getConnectionInfo().getMacAddress();
    }

    // メインスレッドじゃダメ
    public static String getAdvertisingId(Context context) {
        try {
            AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(context);
            return info.getId();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            return null;
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, String> getBuildInfo() {
        Map<String, String> buildInfo = new HashMap<>();

        Field[] fields;

        fields = Build.class.getDeclaredFields();
        putFieldsToMap(fields, buildInfo, null);

        fields = Build.VERSION.class.getDeclaredFields();
        putFieldsToMap(fields, buildInfo, "VERSION.");

        fields = Build.VERSION_CODES.class.getDeclaredFields();
        putFieldsToMap(fields, buildInfo, "VERSION_CODES.");

        return buildInfo;
    }

    private static void putFieldsToMap(Field[] fields, Map<String, String> map, String keyPrefix) {
        for (Field f : fields) {
            f.setAccessible(true);
            String name = (keyPrefix == null) ? f.getName() : keyPrefix + f.getName();
            try {
                String value = f.get(null).toString();
                map.put(name, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
