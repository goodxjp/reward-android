package com.reward.omotesando.components.api;

import android.content.Context;

import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.models.Item;
import com.reward.omotesando.models.Media;
import com.reward.omotesando.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品一覧取得 API
 */
public class GetItems extends RewardApi<List<Item>> {
    private static final String TAG = GetItems.class.getName();

    /*
     * HTTP リクエスト仕様
     */
    public GetItems(Context context) {
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
        this.path = context.getString(R.string.api_path_base) + "/items.json";

        // クエリー文字
        setQueryMediaAndUser();

        // ボディ
        this.jsonRequest = null;
    }

    /*
     * HTTP レスポンス仕様
     */
    @Override
    public List<Item> parseJsonResponse(JSONArray jsonResponse) {
        return json2Items(jsonResponse);
    }

    @Override
    public List<Item> parseJsonResponse(JSONObject jsonResponse) {
        throw new IllegalAccessError();
    }

    // モデルと JSON (通信データ) の変換
    public static List<Item> json2Items(JSONArray jsonArray) {
        ArrayList<Item> list = new ArrayList<>();

        // http://developer.android.com/training/articles/perf-tips.html#Loops
        // ArrayList では拡張 For 分より、こっちのほうが早いらしい。まぁ、数が少ない場合はどっちでもいい。
        // JSONArray は ArrayList
        for (int i = 0, len = jsonArray.length(); i < len; i++) {
            JSONObject o;
            try {
                o = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                Logger.e(TAG, "JSON syntax error. " + jsonArray.toString());
                // TODO: 致命的エラー通知
                return null;
            }

            Item item = json2Item(o);
            if (item == null) {
                return null;
            }

            list.add(item);
        }

        return list;
    }

    public static Item json2Item(JSONObject jsonObject) {
        long id;
        String name;
        int point;
        try {
            id = jsonObject.getLong("id");
            name = jsonObject.getString("name");
            point = jsonObject.getInt("point");
        } catch (JSONException e) {
            Logger.e(TAG, "JSON syntax error. " + jsonObject.toString());
            // TODO: 致命的エラー通知
            return null;
        }

        Item item = new Item();
        item.id    = id;
        item.name  = name;
        item.point = point;

        return item;
    }

}
