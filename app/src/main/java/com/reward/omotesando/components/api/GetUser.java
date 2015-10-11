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
 * ユーザー取得 API
 */
public class GetUser extends RewardApi<User> {
    private static final String TAG = GetUser.class.getName();

    /*
     * HTTP リクエスト仕様
     */
    public GetUser(Context context) {
        this.media = Media.getMedia(context);
        this.user = User.getUser(context);

        // メソッド
        this.method = "GET";

        // パス
        this.path = context.getString(R.string.api_path_base) + "/user.json";

        // クエリー文字
        setQueryCommonValues(context);

        // ボディ
        this.jsonRequest = null;
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

        return new User(userId, userKey, point);  // TODO: やっぱり、端末 ID がモデルに入ってるのは微妙。
    }

    @Override
    public User parseJsonResponse(JSONArray jsonResponse) {
        throw new IllegalAccessError();
    }

}
