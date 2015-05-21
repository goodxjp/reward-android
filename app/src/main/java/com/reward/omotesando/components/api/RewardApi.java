package com.reward.omotesando.components.api;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

import com.reward.omotesando.BuildConfig;

/**
 * リワードシステム API。
 *
 * - API 仕様にかかわる部分をここに集約。
 * - 通信ライブラリには依存したくない。
 */
abstract public class RewardApi<T> {
    private static final String TAG = RewardApi.class.getName();

    public static String BASE_URL = BuildConfig.BASE_URL;
    // TODO: BuildConfig で管理するか、strings.xml で管理するか
    //public static String BASE_URL = "http://kyuuki.jp:3000";
    //public static String BASE_URL = "http://admin.dolly-reward.com";

    protected String url;
    protected JSONObject jsonRequest;

    public String getUrl() {
        return url;
    };

    public JSONObject getJsonRequest() {
        return jsonRequest;
    };

    abstract public T parseJsonResponse(JSONObject jsonResponse);
    abstract public T parseJsonResponse(JSONArray jsonResponse);

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

