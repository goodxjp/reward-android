package com.reward.omotesando.components.api;

import android.content.Context;
import android.net.Uri;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.commons.Utils;
import com.reward.omotesando.models.Media;
import com.reward.omotesando.models.User;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * リワードシステム API。
 *
 * - API 仕様にかかわる部分をここに集約。
 * - できれば、通信ライブラリに依存したくない。
 */
abstract public class RewardApi<T> {
    protected static final String TAG = RewardApi.class.getName();

    // TODO: まだ、どういう形式で共通化するかブレブレ。

    // API の向き先は configs.xml に記述してある。

    public String method;
    protected String path;
    protected TreeMap<String, String> query = null;  // ソート順が重要なので TreeMap 限定
    protected JSONObject jsonRequest;

    // クエリー文字と署名に効いてくる。
    Media media;  // null でなければ mid を送る
    User user;    // null でなければ uid を送る

    // 署名付きの URL を取得する
    public String getUrl(Context context) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(context.getString(R.string.api_scheme));
        builder.encodedAuthority(context.getString(R.string.api_encoded_authority));
        builder.path(path);
        for (Map.Entry<String, String> param: query.entrySet()) {
            builder.appendQueryParameter(param.getKey(), param.getValue());
        }

        // 署名作成
        String signature;
        if (this.user == null) {
            signature = makeSignature(media.mediaKey, null, method, path, query);
        } else {
            signature = makeSignature(media.mediaKey, user.userKey, method, path, query);
        }
        builder.appendQueryParameter("sig", signature);

        return builder.toString();
    };

    public JSONObject getJsonRequest() {
        return jsonRequest;
    };

    protected String queryPut(String key, String value) {
        if (query == null) {
            query = new TreeMap<>();
        }

        return query.put(key, value);
    };

    // 全 API で共通に付ける値をクエリー文字列に設定
    protected void setQueryCommonValues(Context context) {
        if (this.media != null) {
            this.queryPut("mid", String.valueOf(this.media.mediaId));
        }
        if (this.user != null) {
            this.queryPut("uid", String.valueOf(this.user.userId));
        }
        this.queryPut("avc", String.valueOf(Utils.getVersionCode(context)));
    }

    abstract public T parseJsonResponse(JSONObject jsonResponse);
    abstract public T parseJsonResponse(JSONArray jsonResponse);

    /*
     * 署名作成
     */
    private static String makeSignature(String mediaKey, String userKey, String method, String path, TreeMap<String, String> query) {
        // ソート済みクエリー文字列
        StringBuffer sortedQuery = null;
        for (String key : query.keySet()) {
            if (sortedQuery == null) {
                sortedQuery = new StringBuffer();
                sortedQuery.append(key);
                sortedQuery.append("=");
                sortedQuery.append(query.get(key));
            } else {
                sortedQuery.append("&");
                sortedQuery.append(key);
                sortedQuery.append("=");
                sortedQuery.append(query.get(key));
            }
        }

        // キー
        String key;
        if (userKey == null) {
            // ユーザーキーが決定していない場合は "<メディアキー>&" がキーとなる。
            key = mediaKey + "&";
        } else {
            key = mediaKey + "&" + userKey;
        }
        //Logger.v(TAG, key);

        // データ
        String data = method.toUpperCase() + "\n" + path + "\n" + sortedQuery;
        //Logger.v(TAG, data);

        // 署名作成
        SecretKey sk = new SecretKeySpec(key.getBytes(), "HmacSHA1");

        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            Logger.e(TAG, e.getMessage());
            // TODO: 致命的エラー通知
            return "NoSuchAlgorithmException";  // 変な署名をサーバーに送って、サーバーに検知させる。
        }
        try {
            mac.init(sk);
        } catch (InvalidKeyException e) {
            Logger.e(TAG, e.getMessage());
            // TODO: 致命的エラー通知
            return "InvalidKeyException";  // 変な署名をサーバーに送って、サーバーに検知させる。
        }

        byte[] result = mac.doFinal(data.getBytes());
        String resultString = new String(Hex.encodeHex(result));
        //Logger.v(TAG, resultString);

        return resultString;
    }

    /*
     * 日付の共通処理
     */
    // http://www.adakoda.com/adakoda/2010/02/android-iso-8601-parse.html
    //static FastDateFormat fastDateFormat1 = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT;  // yyyy-MM-dd'T'HH:mm:ssZZ

    // 2010-02-27T13:00:00Z がパースできない。 2010-02-27T13:00:00+00:00 と同義っぽいんだけど。
    // http://stackoverflow.com/questions/424522/how-can-i-recognize-the-zulu-time-zone-in-java-dateutils-parsedate
    //static FastDateFormat fastDateFormat2 = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'");

    // iOS 版サーバーからミリ秒がやってくるようになったのに対応
    //static FastDateFormat fastDateFormat3 = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    // 2015-05-08T16:56:08.590+09:00
    private static FastDateFormat fastDateFormat4 = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

    private static String patterns[] = { fastDateFormat4.getPattern() };

    // API 仕様変更されてもいいように、それなりの値を返してしまう。ただ、エラーはどこかで検知したい。
    public static Date parseDate(String s) {
        Date d;

        try {
            d = DateUtils.parseDate(s, patterns);
        } catch (ParseException e) {
            e.printStackTrace();
            d = null;
        }
        return d;
    }
}
