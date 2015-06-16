package com.reward.omotesando.components;

import android.content.Context;
import android.os.Handler;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.commons.VolleyUtils;
import com.reward.omotesando.components.api.GetItems;
import com.reward.omotesando.components.api.GetPointHistories;
import com.reward.omotesando.models.Item;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品マネージャー。
 *
 * - 賞品情報の取得、保存を管理する
 */
public class ItemManager {
    static final String TAG = ItemManager.class.getName();

    // 商品一覧を保持しておく
    private static List<Item> items;

    // 商品一覧取得
    // - 戻り値が null の場合はコールバックにて、商品一覧が通知される。
    public static List<Item> getItems(final Context context, final ItemManagerCallbacks callbacks) {
        if (items != null) {
            return items;
        }

        final GetItems api = new GetItems(context);

        JsonArrayRequest request = new JsonArrayRequest(VolleyApi.getVolleyMethod(api), api.getUrl(context),

                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        VolleyApi.Log(TAG, api, response);

                        items  = api.parseJsonResponse(response);
                        if (items != null) {
                            callbacks.onSuccessGetItems(items);
                        } else {
                            callbacks.onErrorGetItems(Error.getMessageCriticalSeverError(context, Error.API_GET_ITEMS));
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyApi.Log(TAG, api, error);

                        // TODO: 通信エラー内容によって、処理を変える
                        if (error.networkResponse == null) {
                            // 通信状態が悪いか、サーバー停止
                            // サーバーの停止は他の監視方法によって検知するので、通信状態が悪い方のケアを行う。
                            callbacks.onErrorGetItems(context.getString(R.string.error_communication));
                        } else {
                            // - アカウント停止
                            // - メンテナンス中
                            // - サーバーエラー (DB おかしいとか、予期せぬ例外発生)
                            callbacks.onErrorGetItems(Error.getMessageCriticalSeverError(context, 0));
                        }
                    }
                }
        );

        VolleyUtils.getRequestQueue(context).add(request);

        return null;
    }

    // テスト用
    public static List<Item> getItems2(Context context, final ItemManagerCallbacks callbacks) {
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

                        callbacks.onSuccessGetItems(items);
                    }
                });
            }
        });

        thread.start();

        return null;
    }

    // コールバック
    // http://d.hatena.ne.jp/esmasui/20130628/1372386328
    // - 冗長な方がわかりやすい気がするけど…
    public static interface ItemManagerCallbacks {
        public void onSuccessGetItems(List<Item> items);
        public void onErrorGetItems(String message);
    }
}
