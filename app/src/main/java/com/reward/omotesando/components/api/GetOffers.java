package com.reward.omotesando.components.api;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.models.Offer;

/**
 * 案件情報取得 API
 */
public class GetOffers extends RewardApi<List<Offer>> {

    /*
     * HTTP リクエスト仕様
     */
    public static GetOffers create(long mid, long uid) {
        GetOffers api = new GetOffers();
        Logger.e("---------------------", "test");
        Logger.d("---------------------", "test");

        // URL
        api.url = BASE_URL + "/offers.json?mid=" + mid + "&uid=" + uid;
        // Volley では GET のクエリー文字列は自前で作らないとダメらしい。
        // TODO: 署名を付けるときに共通化

        // 署名
        String method = "GET";
        String path = "/api/v1/offers.json";
        String sortedQuery = "mid=1&uid=17";
        String data = method + "\n" + path + "\n" + sortedQuery;

        KeyGenerator kg = null;

        try {
            kg = KeyGenerator.getInstance("HmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        SecretKey sk = new SecretKeySpec("xxx&2273000afa576152".getBytes(), "HmacSHA1");

        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            mac.init(sk);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        byte[] result = mac.doFinal(data.getBytes()); // "hoge"が認証メッセージ
        Logger.e("---------------------", new String(Hex.encodeHex(result)));


        // Body
        api.jsonRequest = null;

        return api;
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
