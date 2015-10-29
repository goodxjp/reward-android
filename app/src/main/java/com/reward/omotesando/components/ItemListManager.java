package com.reward.omotesando.components;

import android.content.Context;
import android.os.Handler;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.components.api.ErrorCode;
import com.reward.omotesando.components.api.GetItems;
import com.reward.omotesando.models.Item;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 商品一覧マネージャー。
 *
 * - 商品情報の取得、データを管理する
 */
public class ItemListManager {
    static final String TAG = ItemListManager.class.getName();

    // 商品一覧
    private static List<Item> itemList;

    // 通信中は非 null
    private static Request request = null;

    // コールバック対象
    private static List<Callback> callbackList = new ArrayList<>();

    /**
     * 商品一覧取得。
     *
     * - 戻り値が null の場合はコールバックにて、取得完了が通知される。
     */
    public static List<Item> getItemList(final Context context, final Callback callback) {
        if (itemList != null) {
            return itemList;
        }

        // リクエスト送信中ならコールバック追加のみ
        if (request != null) {
            Logger.e(TAG, "getItemList() Already sending request.");
            callbackList.add(callback);
            return null;
        }

        final GetItems api = new GetItems(context);

        JsonArrayRequest request = new JsonArrayRequest(VolleyApi.getVolleyMethod(api), api.getUrl(context),

                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        VolleyApi.Log(TAG, api, response);
                        ItemListManager.request = null;

                        itemList = api.parseJsonResponse(response);
                        if (itemList != null) {
                            allCallbackOnSuccess(itemList);
                        } else {
                            allCallbackOnError(Error.getMessageCriticalSeverError(context, Error.GET_ITEMS_RESPONSE_WRONG));
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyApi.Log(TAG, api, error);
                        ItemListManager.request = null;

                        VolleyApi.ApiError apiError = VolleyApi.parseVolleyError(error);

                        if (error.networkResponse == null) {
                            // レスポンスなし

                            // - 通信できない
                            // - サーバー停止
                            allCallbackOnError(Error.getMessageCommunicationError(context));
                        } else if (apiError == null) {
                            // レスポンスは返ってきてるけど、よくわかんないエラー (Heroku メンテナンス中に起こるかも)
                            allCallbackOnError(Error.getMessageCriticalSeverError(context, Error.GET_ITEMS_ERROR_RESPONSE_WRONG));
                        } else {
                            // message をそのまま表示するエラーコード
                            // 未知 (多分、新設) のエラーコードは message をそのまま表示する。
                            allCallbackOnError(ErrorCode.getMessage(context, apiError.code, apiError.message));
                        }
                    }
                }
        );

        // 送信処理
        ItemListManager.request = VolleyApi.send(context, request);
        ItemListManager.callbackList.add(callback);

        return null;
    }

    public static void cancelGetItems(Callback callback) {
        callbackList.remove(callback);
        // 待っている人が誰もいなくなったら、リクエスト自体もキャンセル
        if (callbackList.size() == 0) {
            if (request != null) {  // 無条件でこのメソッド呼んでいいのでリクエスト中じゃないこともある
                request.cancel();
                request = null;
            }
        }
    }

    private static void allCallbackOnSuccess(List<Item> items) {
        for (Iterator<Callback> it = callbackList.iterator(); it.hasNext(); ) {
            Callback callback = it.next();
            callback.onSuccessGetItemList(items);
            it.remove();
        }
    }

    private static void allCallbackOnError(String message) {
        for (Iterator<Callback> it = callbackList.iterator(); it.hasNext(); ) {
            Callback callback = it.next();
            callback.onErrorGetItemList(message);
            it.remove();
        }
    }

    // テスト用
    public static List<Item> getItems2(Context context, final Callback callback) {
        final Handler mHandler = new Handler();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        List<Item> items = new ArrayList<>();
                        Item item;

                        item = new Item();
                        item.id = 1;
                        item.name = "XXX";
                        item.point = 100;
                        items.add(item);

                        item = new Item();
                        item.id = 2;
                        item.name = "YYY";
                        item.point = 500;
                        items.add(item);

                        item = new Item();
                        item.id = 3;
                        item.name = "ZZZ";
                        item.point = 1000;
                        items.add(item);

                        callback.onSuccessGetItemList(items);
                    }
                });
            }
        });

        thread.start();

        return null;
    }

    // コールバック
    // - http://d.hatena.ne.jp/esmasui/20130628/1372386328
    //   冗長な方がわかりやすい気がするけど…
    public static interface Callback {
        public void onSuccessGetItemList(List<Item> items);
        public void onErrorGetItemList(String message);
    }
}
