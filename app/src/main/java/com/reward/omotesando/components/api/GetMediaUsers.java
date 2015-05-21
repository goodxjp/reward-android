package com.reward.omotesando.components.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.reward.omotesando.models.MediaUser;

/**
 * ユーザー取得 API
 */
public class GetMediaUsers extends RewardApi<MediaUser> {

    /*
     * HTTP リクエスト仕様
     */
    public static GetMediaUsers create(long uid) {
        GetMediaUsers api = new GetMediaUsers();

        // URL
        api.url = BASE_URL + "/media_users/" + uid + ".json";

        // Body
        api.jsonRequest = null;

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
