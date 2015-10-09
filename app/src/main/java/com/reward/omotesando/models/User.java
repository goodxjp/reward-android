package com.reward.omotesando.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.reward.omotesando.commons.Logger;

/**
 * ユーザー。
 *
 * - サーバーでは MediaUser だが、アプリでは自分のことしか意識しないので名前を変えよう。
 */
public class User {
    private static String TAG = User.class.getName();

    static User user;

    public static final String PROPERTY_USER_ID     = "user_id";
    public static final String PROPERTY_USER_KEY    = "user_key";
    public static final String PROPERTY_POINT       = "point";

    public long userId;
    public String userKey;
    public long point;

    // データモデルとしてのメディアユーザー
    public User(long userId, String userKey, long point) {
        this.userId      = userId;
        this.userKey     = userKey;
        this.point       = point;
    }

    public static User getUser(Context context) {
        if (user != null) {
            return user;
        }

        // TODO: 保持するデータはどこかで一元管理しておきたい。
        final SharedPreferences prefs = getUserPreferences(context);
        long userId = prefs.getLong(PROPERTY_USER_ID, -1);
        String userKey = prefs.getString(PROPERTY_USER_KEY, null);
        long point = prefs.getLong(PROPERTY_POINT, -1);
        // TODO: データ不整合の場合 (片方のみおかしい場合) どうするか？
        if (userId < 0 || userKey == null || point < 0) {
            Logger.e(TAG, "userId = " + userId);
            Logger.e(TAG, "userKey = " + userKey);
            Logger.e(TAG, "point = " + point);

            // 変な文字列をサーバーに送って掲出する？
            //return new MediaUser(99999999, "InvalidTerminalId", 0);
            // 開発時は落とした方がわかりやすい。
            return null;
        }

        user = new User(userId, userKey, point);

        return user;
    }

    public static void storeUser(Context context, User user) {
        final SharedPreferences prefs = getUserPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(PROPERTY_USER_ID, user.userId);
        editor.putString(PROPERTY_USER_KEY, user.userKey);
        editor.putLong(PROPERTY_POINT, user.point);
        editor.commit();

    }

    public static void clearUser(Context context) {
        final SharedPreferences prefs = getUserPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();

    }

    private static SharedPreferences getUserPreferences(Context context) {
        return context.getSharedPreferences(User.class.getSimpleName(), Context.MODE_PRIVATE);
    }
}
