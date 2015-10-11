package com.reward.omotesando.components.api;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.models.Media;
import com.reward.omotesando.models.Offer;
import com.reward.omotesando.models.User;

/**
 * 案件情報取得 API
 */
public class GetOffers extends RewardApi<List<Offer>> {

    /*
     * HTTP リクエスト仕様
     */
    public GetOffers(Context context) {
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
        if (context == null) {
            Logger.e(TAG, "context is null.");
        }
        this.path = context.getString(R.string.api_path_base) + "/offers.json";

        // クエリー文字
        setQueryCommonValues(context);

        // ボディ
        this.jsonRequest = null;
    }

    /*
     * HTTP レスポンス仕様
     */
    @Override
    public List<Offer> parseJsonResponse(JSONArray jsonResponse) {
        return json2Offers(jsonResponse);
    }

    @Override
    public List<Offer> parseJsonResponse(JSONObject jsonResponse) {
        throw new IllegalAccessError();
    }

    public static List<Offer> json2Offers(JSONArray a) {
        ArrayList<Offer> list = new ArrayList<Offer>();
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

            Offer offer = json2Offer(o);
            list.add(offer);
        }

        return list;
    }

    // TODO: データが不正の場合の処理
    public static Offer json2Offer(JSONObject o) {
        String name = "";
        String detail = "";
        String iconUrl = "";
        String executeUrl = "";
        String requirement = "";
        String requirementDetail = "";
        String period = "";
        int point = 0;
        int price = 0;
        try {
            name = o.getString("name");
            detail = o.getString("detail");
            iconUrl = o.getString("icon_url");
            executeUrl = o.getString("execute_url");
//            JSONArray advertisements = o.getJSONArray("advertisements");
//            if (advertisements != null) {
//                if (advertisements.get(0) != null) {
//                    JSONObject oo = advertisements.getJSONObject(0);
//                    point = oo.getInt("point");
//                }
//            }
            point = o.getInt("point");
            price = o.getInt("price");
            requirement = o.getString("requirement");
            requirementDetail = o.getString("requirement_detail");
            period = o.getString("period");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Offer offer = new Offer(name, detail, price, point, iconUrl, executeUrl, requirement, requirementDetail, period);

        return offer;
    }

}
