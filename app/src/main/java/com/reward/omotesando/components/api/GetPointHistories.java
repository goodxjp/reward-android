package com.reward.omotesando.components.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.reward.omotesando.models.PointHistory;

/**
 * ポイント履歴取得 API
 */
public class GetPointHistories extends RewardApi<List<PointHistory>> {

    /*
     * HTTP リクエスト仕様
     */
    public static GetPointHistories create(long mid, long uid) {
        GetPointHistories api = new GetPointHistories();

        // TODO: mid, uid を全部クエリー文字列に付けるか URL に含めて REST っぽくするか悩み中。

        // URL
        api.url = BASE_URL + "/media_users/"+ uid + "/point_histories.json?mid=" + mid + "&uid=" + uid;
        // Volley では GET のクエリー文字列は自前で作らないとダメらしい。
        // TODO: 署名を付けるときに共通化

        // Body
        api.jsonRequest = null;

        return api;
    }

    /*
     * HTTP レスポンス仕様
     */
    @Override
    public List<PointHistory> parseJsonResponse(JSONArray jsonResponse) {
        return json2PointHistories(jsonResponse);
    }

    @Override
    public List<PointHistory> parseJsonResponse(JSONObject jsonResponse) {
        throw new IllegalAccessError();
    }

    // モデルと JSON (通信データ) の変換
    public static List<PointHistory> json2PointHistories(JSONArray a) {
        ArrayList<PointHistory> list = new ArrayList<>();

        // http://developer.android.com/training/articles/perf-tips.html#Loops
        // ArrayList では拡張 For 分より、こっちのほうが早いらしい。
        // JSONArray は ArrayList のはず…
        for (int i = 0, len = a.length(); i < len; i++) {
            JSONObject o = null;
            try {
                o = a.getJSONObject(i);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (o == null) {
                continue;
            }

            PointHistory pointHistory = json2PointHistory(o);
            list.add(pointHistory);
        }

        return list;
    }

    // TODO: データが不正の場合の処理
    public static PointHistory json2PointHistory(JSONObject o) {
        String detail = "";
        int pointChange = 0;
        Date createdAt = null;
        try {
            detail = o.getString("detail");
            pointChange = o.getInt("point_change");
            String s = o.getString("created_at");
            createdAt = RewardApi.parseDate(s);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        PointHistory pointHistory = new PointHistory(detail, pointChange, createdAt);

        return pointHistory;
    }

}
