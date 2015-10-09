package com.reward.omotesando.components;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.reward.omotesando.commons.Logger;

import org.apache.commons.lang3.time.FastDateFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
    private static final String TAG = Terminal.class.getName();

    // 2015-05-08T16:56:08.590+09:00
    private static FastDateFormat fastDateFormat4 = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

    // TODO: 端末 ID 情報の構造をどこかにまとめる
    public static final String KEY_VERSION    = "version";
    public static final String KEY_ID         = "id";
    public static final String KEY_CREATED_AT = "created_at";

    public static JSONObject getTerminalId(Context context) {
        JSONObject jsonObject;

        // 端末 ID 情報はファイルとして保存しておき、もし、ファイルが存在する場合はそちらを優先する。
        jsonObject = getTerminalIdFromFile();
        if (jsonObject != null) {
            return jsonObject;
        }

        String terminalId = getTerminalIdV1(context);

        // TODO: 端末 ID のチェック

        JSONObject o = new JSONObject();
        try {
            o.put(KEY_VERSION, 1);
            o.put(KEY_ID, terminalId);
            o.put(KEY_CREATED_AT, fastDateFormat4.format(System.currentTimeMillis()));
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.e(TAG, e.getMessage());
            return null;
        }

        // TODO: ユーザー登録が成功した時点で保存した方がよい？！
        if (saveTerminalIdToFile(o) == false) {
            Logger.e(TAG, "Fail to save terminalId file.");
            // 保存に失敗しても、端末 ID 情報はそのまま使う。保存できない端末は毎回取得し直す。
        }

        return o;
    }

    // ここのしくみを変えたらバージョンを変えること
    public static String getTerminalIdV1(Context context) {
        return getAndroidId(context);
    }

    // TODO: ファイルの中身を暗号化。したいけど、面倒くさいからやらなくていいや。
    private static final String FILENAME = "44d78098.dat";

    // ファイルから端末 ID 情報を取得
    // - ファイルの中身の整合性が取れていることの責任もここで持つ。
    // - 初めての場合やファイルがおかしい場合は null を返す。
    private static JSONObject getTerminalIdFromFile() {
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path, FILENAME);

        StringBuilder jsonString = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();

            Logger.v(TAG, jsonString.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e(TAG, e.getMessage());
            return null;
        }

        // ファイルの整合性チェック
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString.toString());

            if (jsonObject.getInt(KEY_VERSION) != 1) {
                return null;
            }

            // TODO 端末 ID 文字列チェックもっと厳密に。
            if (jsonObject.getString(KEY_ID) == null) {
                return null;
            }

            // TODO: もっと厳密に。
            if (jsonObject.getString(KEY_CREATED_AT) == null) {
                return null;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Logger.e(TAG, e.getMessage());
            Logger.e(TAG, "jsonString = " + jsonString.toString());
            return null;
        }

        return jsonObject;
    }

    // ファイルに端末 ID 情報を保存
    public static boolean saveTerminalIdToFile(JSONObject terminalId) {
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path, FILENAME);

        try {
            PrintWriter pw  = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

            pw.append(terminalId.toString());
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e(TAG, e.getMessage());
            return false;
        }

        return true;
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
