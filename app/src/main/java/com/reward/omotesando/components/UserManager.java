package com.reward.omotesando.components;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.components.api.ErrorCode;
import com.reward.omotesando.components.api.GetUser;
import com.reward.omotesando.models.User;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.reward.omotesando.components.api.ErrorCode.*;

/**
 * ユーザーマネージャー。
 *
 * - ユーザー情報の取得、データを管理する
 * - ユーザーのポイント獲得とそれを通知したかも責任を持つ
 *
 * - シングルトンにすべきか、ユーティリティクラス (クラス変数、クラスメソッド) にすべきか？
 *   - http://qiita.com/mo12ino/items/abf2e31e34278ebea42c
 *   - http://qiita.com/trashtoy/items/eea5dcfb1eb003101cc9
 *     状態持っちゃあかんの？外部からこのオブジェクト引き回せって言うの？
 *     ポリモーフィズム絡まないからっていって、ユーティリティクラスにしちゃうと後で複数に変更しにくくない？ → とはいえ、これに従いユーティリティにしちゃおう。
 */
public class UserManager {
    static final String TAG = UserManager.class.getName();

    // ユーザー情報
    private static User user;

    // 通信中は非 null
    private static Request request = null;

    // コールバック対象
    private static List<Callback> callbackList = new ArrayList<>();

    // TODO: 他のは再取得すればいい情報だけど、これは static 変数だと初期化されちゃうので本当はダメ。
    // ポイント数が変更したか (かつ、未通知)
    // 上位に初期化まで責任転嫁。
    public static boolean isChangedPoint = false;

    /**
     * ユーザー情報取得。
     *
     * - 戻り値が null の場合はコールバックにて、取得完了が通知される。
     */
    public static User getUser(final Context context, final Callback callback) {
        // とりあえず、毎回取得
//        if (user != null) {
//            return user;
//        }

        // リクエスト送信中ならコールバック追加のみ
        if (request != null) {
            Logger.v(TAG, "Add callback");
            callbackList.add(callback);
            return null;
        }

        final GetUser api = new GetUser(context);

        JsonObjectRequest request = new JsonObjectRequest(VolleyApi.getVolleyMethod(api), api.getUrl(context),

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        VolleyApi.Log(TAG, api, response);
                        UserManager.request = null;

                        // ポイント数の変化をチェック (前半)
                        // TODO: ギフト件購入も考慮する必要もあるので、本当はサーバーでちゃんと管理しないとダメか。
                        // TODO: ポイント履歴取得をポイント取得監視に使えるように変更する。
                        long beforePoint = -1;
                        if (user != null) {
                            beforePoint = user.point;
                        }

                        user = api.parseJsonResponse(response);

                        if (user != null) {
                            // ポイント数の変化をチェック (後半)
                            Logger.e(TAG, "beforePoint = " + beforePoint);
                            Logger.e(TAG, "User.point = " + user.point);
                            if ((beforePoint >= 0) && (user.point > beforePoint)) {
                                isChangedPoint = true;
                            }

                            allCallbackOnSuccessGetUser(user);
                        } else {
                            allCallbackOnErrorGetUser(Error.getMessageCriticalSeverError(context, Error.GET_USER_RESPONSE_WRONG));
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyApi.Log(TAG, api, error);
                        UserManager.request = null;

                        VolleyApi.ApiError apiError = VolleyApi.parseVolleyError(error);

                        // TODO: 上位に伝えるエラー内容要検討。とりあえずはメッセージのみでいいのかな。
                        if (error.networkResponse == null) {
                            // レスポンスなし

                            // - 通信できない
                            // - サーバー停止
                            allCallbackOnErrorGetUser(Error.getMessageCommunicationError(context));
                        } else if (apiError == null) {
                            // レスポンスは返ってきてるけど、よくわかんないエラー (Heroku メンテナンス中に起こるかも)
                            allCallbackOnErrorGetUser(Error.getMessageCriticalSeverError(context, Error.GET_USER_ERROR_RESPONSE_WRONG));
                        } else {
                            // API からの正常なレスポンス

                            // - 強制終了
                            if (apiError.code == ERROR_CODE_9999.code) {
                                // 本当はダメ。上位にこの種類のエラーを渡して、上位で処理すべき。
                                System.exit(0);
                            // TODO: 全 API で共通で起こりうるエラーの処理を共通化したい。
                            } else {
                                // message をそのまま表示するエラーコード
                                // 未知 (多分、新設) のエラーコードは message をそのまま表示する。
                                allCallbackOnErrorGetUser(ErrorCode.getMessage(context, apiError.code, apiError.message));
                            }
                        }
                    }
                }
        );

        // 送信処理
        UserManager.request = VolleyApi.send(context, request);
        UserManager.callbackList.add(callback);

        return null;
    }

    public static void cancelGetUser(Callback callback) {
        Logger.e(TAG, "cancel");
        callbackList.remove(callback);
        // 待っている人が誰もいなくなったら、リクエスト自体もキャンセル
        if (callbackList.size() == 0) {
            if (request != null) {  // 無条件でこのメソッド呼んでいいのでリクエスト中じゃないこともある
                Logger.e(TAG, "request.cancel");
                request.cancel();
                request = null;
            }
        }
    }

    private static void allCallbackOnSuccessGetUser(User user) {
        for (Iterator<Callback> it = callbackList.iterator(); it.hasNext(); ) {
            Callback callback = it.next();
            Logger.e(TAG, "callback success");
            callback.onSuccessGetUser(user);
            it.remove();
        }
    }

    private static void allCallbackOnErrorGetUser(String message) {
        for (Iterator<Callback> it = callbackList.iterator(); it.hasNext(); ) {
            Callback callback = it.next();
            Logger.e(TAG, "callback error");
            callback.onErrorGetUser(message);
            it.remove();
        }
    }

    // コールバック
    // - http://d.hatena.ne.jp/esmasui/20130628/1372386328
    //   冗長な方がわかりやすい気がするけど…
    public static interface Callback {
        public void onSuccessGetUser(User user);
        public void onErrorGetUser(String message);
    }
}
