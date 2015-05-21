package com.reward.omotesando.components.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.reward.omotesando.models.MediaUser;

/**
 * ユーザー登録 API
 */
public class PostMediaUsers extends RewardApi<MediaUser> {

    /*
     * HTTP リクエスト仕様
     */
    public static PostMediaUsers create(String terminalId, JSONObject terminalInfo, String androidRegistrationId) {
        PostMediaUsers api = new PostMediaUsers();

        // URL
        api.url = BASE_URL + "/media_users.json";

        // Body
        JSONObject jsonRequest = new JSONObject();

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

        api.jsonRequest = jsonRequest;

        return api;
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
