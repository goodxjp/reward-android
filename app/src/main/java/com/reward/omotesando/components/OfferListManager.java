package com.reward.omotesando.components;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.components.api.ErrorCode;
import com.reward.omotesando.components.api.GetOffers;
import com.reward.omotesando.models.Offer;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.reward.omotesando.components.api.ErrorCode.ERROR_CODE_9999;

/**
 * オファー一覧マネージャー。
 *
 * - オファー情報の取得、データを管理する
 */
public class OfferListManager {
    static final String TAG = OfferListManager.class.getName();

    // オファー情報
    private static List<Offer> offers = null;
    private static long offersGotAt = -1;  // オファー情報を取得した時刻

    private static int OFFERS_TIMEOUT_MS = 30 * 1000;  // 新たなオファー情報を取得するまでの時間 (ms)

    // 通信中は非 null
    private static Request request = null;

    // コールバック対象
    private static List<Callback> callbackList = new ArrayList<>();

    /**
     * オファー一覧取得。
     *
     * - 戻り値が null の場合はコールバックにて、取得完了が通知される。
     */
    public static List<Offer> getOfferList(final Context context, final Callback callback) {
        // すでに取得情報が存在し、そんなに時間が経っていなければ、取得済みの情報を返す。
        if (offers != null && System.currentTimeMillis() < offersGotAt + OFFERS_TIMEOUT_MS) {
            Logger.d(TAG, "getOfferList have already got offers. offersGotAt = " + offersGotAt);
            return offers;
        }

        // リクエスト送信中ならコールバック追加のみ
        if (request != null) {
            callbackList.add(callback);
            return null;
        }

        final GetOffers api = new GetOffers(context);

        // TODO: API によってコードが変わるのを API 内に閉じ込めたい。
        //JsonObjectRequest request = new JsonObjectRequest(VolleyApi.getVolleyMethod(api), api.getUrl(context),
        Request request = new JsonArrayRequest(VolleyApi.getVolleyMethod(api), api.getUrl(context),

                // TODO: API によってコードが変わるのを API 内に閉じ込めたい。
                //new Response.Listener<JSONObject>() {
                new Response.Listener<JSONArray>() {
                    @Override
                    // TODO: API によってコードが変わるのを API 内に閉じ込めたい。
                    //public void onResponse(JSONObject response) {
                    public void onResponse(JSONArray response) {
                        VolleyApi.Log(TAG, api, response);
                        OfferListManager.request = null;

                        setOffers(api.parseJsonResponse(response));
                        if (offers != null) {
                            allCallbackOnSuccess(offers);
                        } else {
                            allCallbackOnError(Error.getMessageCriticalSeverError(context, Error.GET_OFFERS_RESPONSE_WRONG));
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyApi.Log(TAG, api, error);
                        OfferListManager.request = null;

                        VolleyApi.ApiError apiError = VolleyApi.parseVolleyError(error);

                        // TODO: 上位に伝えるエラー内容要検討。とりあえずはメッセージのみでいいのかな。
                        if (error.networkResponse == null) {
                            // レスポンスなし

                            // - 通信できない
                            // - サーバー停止
                            allCallbackOnError(Error.getMessageCommunicationError(context));
                        } else if (apiError == null) {
                            // レスポンスは返ってきてるけど、よくわかんないエラー (Heroku メンテナンス中に起こるかも)
                            allCallbackOnError(Error.getMessageCriticalSeverError(context, Error.GET_OFFERS_ERROR_RESPONSE_WRONG));
                        } else {
                            // API からの正常なエラーレスポンス

                            // - 強制終了
                            if (apiError.code == ERROR_CODE_9999.code) {
                                // 本当はダメ。上位にこの種類のエラーを渡して、上位で処理すべき。
                                System.exit(0);
                            // TODO: 全 API で共通で起こりうるエラーの処理を共通化したい。
                            } else {
                                // message をそのまま表示するエラーコード
                                // 未知 (多分、新設) のエラーコードは message をそのまま表示する。
                                allCallbackOnError(ErrorCode.getMessage(context, apiError.code, apiError.message));
                            }
                        }
                    }
                }
        );

        // 送信処理
        OfferListManager.request = VolleyApi.send(context, request);
        OfferListManager.callbackList.add(callback);

        return null;
    }

    public static void cancelGetOfferList(Callback callback) {
        callbackList.remove(callback);
        // 待っている人が誰もいなくなったら、リクエスト自体もキャンセル
        if (callbackList.size() == 0) {
            if (request != null) {  // 無条件でこのメソッド呼んでいいのでリクエスト中じゃないこともある
                request.cancel();
                request = null;
            }
        }
    }

    private static void setOffers(List<Offer> offers) {
        OfferListManager.offers = offers;
        OfferListManager.offersGotAt = System.currentTimeMillis();
    }

    private static void allCallbackOnSuccess(List<Offer> offers) {
        for (Iterator<Callback> it = callbackList.iterator(); it.hasNext(); ) {
            Callback callback = it.next();
            callback.onSuccessGetOfferList(offers);
            it.remove();
        }
    }

    private static void allCallbackOnError(String message) {
        for (Iterator<Callback> it = callbackList.iterator(); it.hasNext(); ) {
            Callback callback = it.next();
            callback.onErrorGetOfferList(message);
            it.remove();
        }
    }

    // コールバック
    public static interface Callback {
        public void onSuccessGetOfferList(List<Offer> offers);
        public void onErrorGetOfferList(String message);
    }
}
