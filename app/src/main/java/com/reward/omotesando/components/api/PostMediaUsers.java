package com.reward.omotesando.components.api;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.reward.omotesando.R;
import com.reward.omotesando.models.Media;
import com.reward.omotesando.models.MediaUser;

/**
 * ユーザー登録 API
 */
public class PostMediaUsers extends RewardApi<MediaUser> {

    /*
     * HTTP リクエスト仕様
     */
    public PostMediaUsers(Context context, String terminalId, JSONObject terminalInfo, String androidRegistrationId) {
        // メソッド
        this.method = "POST";

        // パス
        this.path = context.getString(R.string.api_path_base) + "/media_users.json";

        // クエリー文字
        Media media = Media.getMedia(context);
        this.queryPut("mid", String.valueOf(media.mediaId));

        // ボディ
        this.jsonRequest = new JSONObject();

        try {
            jsonRequest.put("terminal_id", terminalId);
            jsonRequest.put("terminal_info", terminalInfo);
            jsonRequest.put("android_registration_id", androidRegistrationId);
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
    public MediaUser parseJsonResponse(JSONObject jsonResponse) {
        long mediaUserId;
        String terminalId;
        long point;

        try {
            mediaUserId = jsonResponse.getLong("id");
            terminalId = jsonResponse.getString("terminal_id");  // 使わない。端末 ID がうまく取れない端末がでてきたら、こっちを使うかも。
            point = jsonResponse.getLong("point");
        } catch (JSONException e) {
            // TODO: サーバーエラーのときどうするか。
            e.printStackTrace();
            return null;
        }

        return new MediaUser(mediaUserId, terminalId, point);
    }

    @Override
    public MediaUser parseJsonResponse(JSONArray jsonResponse) {
        throw new IllegalAccessError();
    }

}
