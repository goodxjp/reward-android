package com.reward.omotesando.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.reward.omotesando.commons.Logger;

/**
 * メディアユーザー。
 *
 * - 自分のこと。
 */
public class MediaUser {
    private static String TAG = MediaUser.class.getName();

    static MediaUser mediaUser;

    public static final String PROPERTY_MEDIA_USER_ID = "media_user_id";
    public static final String PROPERTY_TERMINAL_ID = "terminal_id";

    public long mediaUserId;
    public String terminalId;
    public long point;

    // データモデルとしてのメディアユーザー
    public MediaUser(long mediaUserId, String terminalId, long point) {
        this.mediaUserId = mediaUserId;
        this.terminalId  = terminalId;
        this.point       = point;
    }

    // 自分用
//    private MediaUser(long mediaUserId, String terminalId) {
//        this.mediaUserId = mediaUserId;
//        this.terminalId = terminalId;
//    }

    public static MediaUser getMediaUser(Context context) {
        if (mediaUser != null) {
            return mediaUser;
        }

        // TODO: 保持するデータはどこかで一元管理しておきたい。
        final SharedPreferences prefs = getMediaUserPreferences(context);
        long mediaUserId = prefs.getLong(PROPERTY_MEDIA_USER_ID, -1);
        String terminalId = prefs.getString(PROPERTY_TERMINAL_ID, null);
        // TODO: データ不整合の場合 (片方のみおかしい場合) どうするか？
        if (mediaUserId < 0 || terminalId == null) {
            Logger.e(TAG, "mediaUserId = " + mediaUserId);
            Logger.e(TAG, "terminalId = " + terminalId);

            // 変な文字列をサーバーに送って掲出する？
            //return new MediaUser(99999999, "InvalidTerminalId", 0);
            // 開発時は落とした方がわかりやすい。
            return null;
        }

        mediaUser = new MediaUser(mediaUserId, terminalId, 0);  // TODO: ポイントも保持

        return mediaUser;
    }

    public static void storeMediaUserId(Context context, long userId, String terminalId) {
        final SharedPreferences prefs = getMediaUserPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(PROPERTY_MEDIA_USER_ID, userId);
        editor.putString(PROPERTY_TERMINAL_ID, terminalId);
        editor.commit();

    }

    public static void clearMediaUser(Context context) {
        final SharedPreferences prefs = getMediaUserPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();

    }

    private static SharedPreferences getMediaUserPreferences(Context context) {
        return context.getSharedPreferences(MediaUser.class.getSimpleName(), Context.MODE_PRIVATE);
    }
}
