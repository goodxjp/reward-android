package jp.kyuuki.reward.android.models;

import android.content.Context;
import android.content.SharedPreferences;

import jp.kyuuki.reward.android.commons.Logger;

/**
 * メディアユーザー。
 *
 * - TODO: 自分と一般的なメディアユーザーのモデルとをきちんとわけたほうがよさげ。
 */
public class MediaUser {
    static MediaUser mediaUser;

    public static final String PROPERTY_MEDIA_USER_ID = "media_user_id";
    public static final String PROPERTY_TERMINAL_ID = "terminal_id";

    public long mediaUserId;
    public String terminalId;

    // データモデルとしてのメディアユーザー
    public MediaUser(long mediaUserId, String terminalId) {
        this.mediaUserId = mediaUserId;
        this.terminalId = terminalId;
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

        final SharedPreferences prefs = getMediaUserPreferences(context);
        long mediaUserId = prefs.getLong(PROPERTY_MEDIA_USER_ID, -1);
        String terminalId = prefs.getString(PROPERTY_TERMINAL_ID, null);
        // TODO: データ不整合の場合 (片方のみおかしい場合) どうするか？
        if (mediaUserId < 0 || terminalId == null) {
            return null;
        }

        mediaUser = new MediaUser(mediaUserId, terminalId);

        return mediaUser;
    }

    public static void storeMediaUserId(Context context, long userId, String terminalId) {
        final SharedPreferences prefs = getMediaUserPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(PROPERTY_MEDIA_USER_ID, userId);
        editor.putString(PROPERTY_TERMINAL_ID, terminalId);
        editor.commit();
    }

    private static SharedPreferences getMediaUserPreferences(Context context) {
        return context.getSharedPreferences(MediaUser.class.getSimpleName(), Context.MODE_PRIVATE);
    }
}
