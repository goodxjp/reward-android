package com.reward.omotesando.components.api;

import android.content.Context;

import com.reward.omotesando.R;
import com.reward.omotesando.models.Gift;
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
 * ギフト券取得 API
 */
public class GetGifts extends RewardApi<List<Gift>> {

    /*
     * HTTP リクエスト仕様
     */
    public GetGifts(Context context) {
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
        this.path = context.getString(R.string.api_path_base) + "/gifts.json";

        // クエリー文字
        setQueryCommonValues(context);

        // ボディ
        this.jsonRequest = null;
    }

    /*
     * HTTP レスポンス仕様
     */
    @Override
    public List<Gift> parseJsonResponse(JSONArray jsonResponse) {
        return json2Gifts(jsonResponse);
    }

    @Override
    public List<Gift> parseJsonResponse(JSONObject jsonResponse) {
        throw new IllegalAccessError();
    }

    // モデルと JSON (通信データ) の変換
    public static List<Gift> json2Gifts(JSONArray a) {
        ArrayList<Gift> list = new ArrayList<>();

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

            Gift gift = json2Gift(o);
            list.add(gift);
        }

        return list;
    }

    // TODO: データが不正の場合の処理
    public static Gift json2Gift(JSONObject o) {
        String name = "";
        String code = "";
        Date expirationAt = null;
        Date occurredAt = null;
        try {
            name = o.getString("name");
            code = o.getString("code");
            String s = o.getString("expiration_at");
            expirationAt = RewardApi.parseDate(s);
            s = o.getString("occurred_at");
            occurredAt = RewardApi.parseDate(s);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Gift gift = new Gift(name, code, expirationAt, occurredAt);

        return gift;
    }

}
