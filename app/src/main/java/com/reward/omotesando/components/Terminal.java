package com.reward.omotesando.components;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;

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
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

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
        jsonObject = getTerminalIdFromFile(context);
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
        if (!saveTerminalIdToFile(context, o)) {
            Logger.e(TAG, "Fail to save terminalId file.");
            // 保存に失敗しても、端末 ID 情報はそのまま使う。保存できない端末は毎回取得し直す。
        }

        return o;
    }

    // ここのしくみを変えたらバージョンを変えること
    public static String getTerminalIdV1(Context context) {
        return getAndroidId(context);
    }

    private static final String FILENAME = "44d78098.dat";
    private static final String SECRET_KEY = "1510ee3b1510ee3b";  // 16 文字
    // 簡易的に暗号化。ソースの中身から暗号化キーがわかるので端末 ID を変更しての送信が原理的に可能。

    // ファイルから端末 ID 情報を取得
    // - ファイルの中身の整合性が取れていることの責任もここで持つ。
    // - 初めての場合やファイルがおかしい場合は null を返す。
    private static JSONObject getTerminalIdFromFile(Context context) {
//        File path = Environment.getExternalStorageDirectory();
        File path = context.getExternalFilesDir(null);
        File file = new File(path, FILENAME);

        StringBuilder encryptedString = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

            String line;
            while ((line = reader.readLine()) != null) {
                encryptedString.append(line);
            }
            reader.close();

            Logger.v(TAG, "File data = " + encryptedString.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e(TAG, e.getMessage());
            return null;
        }

        String jsonString;
        try {
            jsonString = decrypt(encryptedString.toString());
        } catch (Exception e) {
            // ファイルを強制的に変更したりされると起こりうるので注意
            e.printStackTrace();
            Logger.e(TAG, e.getMessage());
            return null;
        }

        Logger.v(TAG, "File string = " + jsonString);

        // ファイルの整合性チェック
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);

            if (jsonObject.getInt(KEY_VERSION) != 1) {
                Logger.e(TAG, "KEY_VERSION is invalid.");
                return null;
            }

            // TODO 端末 ID 文字列チェックもっと厳密に。
            if (jsonObject.getString(KEY_ID) == null) {
                Logger.e(TAG, "KEY_ID is invalid.");
                return null;
            }

            // TODO: もっと厳密に。
            if (jsonObject.getString(KEY_CREATED_AT) == null) {
                Logger.e(TAG, "KEY_CREATED_AT is invalid.");
                return null;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Logger.e(TAG, e.getMessage());
            Logger.e(TAG, "jsonString = " + jsonString);
            return null;
        }

        return jsonObject;
    }

    // ファイルに端末 ID 情報を保存
    public static boolean saveTerminalIdToFile(Context context, JSONObject terminalId) {
//        File path = Environment.getExternalStorageDirectory();
        File path = context.getExternalFilesDir(null);
        File file = new File(path, FILENAME);

        String encrypted;
        try {
            encrypted = encrypt(terminalId.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(TAG, e.getMessage());
            return false;
        }

        try {
            PrintWriter pw  = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

            pw.append(encrypted);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e(TAG, e.getMessage());
            return false;
        }

        return true;
    }

    private static String encrypt(String string)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        byte[] encrypted = cipher.doFinal(string.getBytes());
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    private static String decrypt(String string)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        byte[] decrypted = cipher.doFinal(Base64.decode(string, Base64.DEFAULT));
        return new String(decrypted, "UTF-8");
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
//    public static String getMacAddress(Context context) {
//        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        return manager.getConnectionInfo().getMacAddress();
//    }

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
