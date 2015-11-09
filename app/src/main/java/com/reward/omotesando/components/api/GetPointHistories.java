package com.reward.omotesando.components.api;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.reward.omotesando.R;
import com.reward.omotesando.models.Media;
import com.reward.omotesando.models.PointHistory;
import com.reward.omotesando.models.User;

/**
 * ポイント履歴取得 API
 */
public class GetPointHistories extends RewardApi<List<PointHistory>> {

    /*
     * HTTP リクエスト仕様
     */
    public GetPointHistories(Context context) {
        this.media = Media.getMedia(context);
        this.user = User.getUser(context);

        // mid と uid は必須
        if (this.media == null || this.user == null) {
            // バグ。本来はこのクラスの外で責任を持つ。
            throw new IllegalStateException("Media or User is null.");
        }

        // メソッド
        this.method = "GET";

        // パス
        this.path = context.getString(R.string.api_path_base) + "/point_histories.json";

        // クエリー文字
        setQueryCommonValues(context);

        // ボディ
        this.jsonRequest = null;
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

    /*
     * モデルと JSON (通信データ) の変換
     */

    /**
     * JSONArray -> ポイント履歴のリスト
     *
     * @param a
     * @return null 以外: ポイント履歴のリスト / null: JSON 不正
     */
    public static List<PointHistory> json2PointHistories(JSONArray a) {
        ArrayList<PointHistory> list = new ArrayList<>();

        // http://developer.android.com/training/articles/perf-tips.html#Loops
        // ArrayList では拡張 For 分より、こっちのほうが早いらしい。
        // JSONArray は ArrayList のはず…
        for (int i = 0, len = a.length(); i < len; i++) {
            JSONObject o;
            try {
                o = a.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

            PointHistory pointHistory = json2PointHistory(o);
            if (pointHistory == null) {
                return null;
            }

            list.add(pointHistory);
        }

        return list;
    }

    /**
     * JSONObject -> ポイント履歴。
     *
     * @param o  JSONObject
     * @return null 以外: ポイント履歴 / null: JSON 不正
     */
    public static PointHistory json2PointHistory(JSONObject o) {
        String detail;
        int pointChange;
        Date occurredAt;
        try {
            detail = o.getString("detail");
            pointChange = o.getInt("point_change");
            String s = o.getString("occurred_at");
            occurredAt = RewardApi.parseDate(s);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        PointHistory pointHistory = new PointHistory(detail, pointChange, occurredAt);

        return pointHistory;
    }
}
