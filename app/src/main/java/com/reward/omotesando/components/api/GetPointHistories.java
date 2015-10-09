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
        setQueryMediaAndUser();

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
