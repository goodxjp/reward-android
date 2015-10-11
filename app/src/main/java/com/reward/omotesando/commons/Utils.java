package com.reward.omotesando.commons;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.EditText;

public final class Utils {

    private Utils() {}

    // EditText に入力できなくする「スペースのみ」のパターン
    private final static Pattern ONLY_SPACE_PATTERN = Pattern.compile("[\\s　]*");

    // EditText に入力を許可されていない「スペースのみ」文字列かどうか？
    // true: 禁止されている文字列, false: 許可されている文字列
    public static boolean isForbiddenOnlySpace(String s) {
        Matcher matcher = ONLY_SPACE_PATTERN.matcher(s);
        return matcher.matches();
    }

    // EditText のスペースのみの入力をなかったことにする
    public static void checkWhiteSpace(EditText t) {
        // 禁止されている文字列だったら、空白に戻す。
        if (isForbiddenOnlySpace(t.getText().toString()) == true) {
            t.setText("");
        }
    }

    private static int versionCode = -1;

    public static int getVersionCode(Context context) {
        if (versionCode >= 0) {
            return versionCode;
        }

        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            versionCode = packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }

        return versionCode;
    }

    private static String versionName;

    public static String getVersionName(Context context) {
        if (versionName != null) {
            return versionName;
        }

        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            versionName = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        return versionName;
    }
}
