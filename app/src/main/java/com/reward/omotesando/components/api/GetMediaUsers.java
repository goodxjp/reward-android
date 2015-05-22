package com.reward.omotesando.components.api;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.reward.omotesando.R;
import com.reward.omotesando.models.Media;
import com.reward.omotesando.models.MediaUser;

/**
 * ユーザー取得 API
 */
public class GetMediaUsers extends RewardApi<MediaUser> {

    /*
     * HTTP リクエスト仕様
     */
    public GetMediaUsers(Context context) {
        // メソッド
        this.method = "GET";

        // パス
        Media media = Media.getMedia(context);
        MediaUser mediaUser = MediaUser.getMediaUser(context);

        this.path = context.getString(R.string.api_path_base) + "/media_users/"+ String.valueOf(mediaUser.mediaUserId) + ".json";

        // クエリー文字
        this.queryPut("mid", String.valueOf(media.mediaId));
        this.queryPut("uid", String.valueOf(mediaUser.mediaUserId));

        // ボディ
        this.jsonRequest = null;
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
