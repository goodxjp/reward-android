package com.reward.omotesando.components.api;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.models.Media;
import com.reward.omotesando.models.User;

/**
 * ユーザー登録 API
 */
public class PostUser extends RewardApi<User> {
    private static final String TAG = PostUser.class.getName();

    /*
     * HTTP リクエスト仕様
     */
    public PostUser(Context context, JSONObject terminalId, JSONObject terminalInfo, String androidRegistrationId) {
        this.media = Media.getMedia(context);
        this.user = null;

        // mid は必須
        if (this.media == null) {
            // バグ。本来はこのクラスの外で責任を持つ。
            throw new IllegalStateException("Media is null.");
        }

        // メソッド
        this.method = "POST";

        // パス
        this.path = context.getString(R.string.api_path_base) + "/user.json";

        // クエリー文字
        setQueryMediaAndUser();

        // ボディ
        this.jsonRequest = new JSONObject();

        try {
            JSONObject o = new JSONObject();
            o.put("terminal_id", terminalId);
            o.put("terminal_info", terminalInfo);
            if (androidRegistrationId != null) {
                o.put("android_registration_id", androidRegistrationId);
            }

            this.jsonRequest.put("user", o);
        } catch (JSONException e) {
            // 値が数値の時にしか発生しない、致命的なエラーなので、落としてしまってよい。
            // TODO: 致命的なバグに気付くしくみ、共通ライブラリ化
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }

    /*
     * HTTP レスポンス仕様
     */
    @Override
    public User parseJsonResponse(JSONObject jsonResponse) {
        long userId;
        String userKey;
        long point;

        try {
            userId = jsonResponse.getLong("id");
            userKey = jsonResponse.getString("key");
            point = jsonResponse.getLong("point");
        } catch (JSONException e) {
            Logger.e(TAG, "JSON syntax error. " + jsonResponse.toString());
            // TODO: 致命的エラー通知
            return null;
        }

        return new User(userId, userKey, point);
    }

    @Override
    public User parseJsonResponse(JSONArray jsonResponse) {
        throw new IllegalAccessError();
    }

}
