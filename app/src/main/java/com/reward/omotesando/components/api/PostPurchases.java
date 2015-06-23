package com.reward.omotesando.components.api;

import android.content.Context;

import com.reward.omotesando.R;
import com.reward.omotesando.models.Item;
import com.reward.omotesando.models.Media;
import com.reward.omotesando.models.PointHistory;
import com.reward.omotesando.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 購入 API
 */
public class PostPurchases extends RewardApi<Void> {

    /*
     * HTTP リクエスト仕様
     */
    public PostPurchases(Context context, Item item) {
        this.media = Media.getMedia(context);
        this.user = User.getUser(context);

        // メソッド
        this.method = "POST";

        // パス
        this.path = context.getString(R.string.api_path_base) + "/purchases.json";

        // クエリー文字
        setQueryMediaAndUser();

        // ボディ
        this.jsonRequest = new JSONObject();

        try {
            JSONObject o = new JSONObject();
            o.put("id", item.id);
            o.put("number", 1);  // 現在は 1 固定
            o.put("point", item.point);

            this.jsonRequest.put("item", o);
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
    public Void parseJsonResponse(JSONArray jsonResponse) {
        throw new IllegalAccessError();
    }

    @Override
    public Void parseJsonResponse(JSONObject jsonResponse) {
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
