package com.reward.omotesando.components;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.reward.omotesando.R;
import com.reward.omotesando.commons.VolleyUtils;
import com.reward.omotesando.components.api.ErrorCode;
import com.reward.omotesando.components.api.GetUser;
import com.reward.omotesando.models.User;

import org.json.JSONObject;

import static com.reward.omotesando.components.api.ErrorCode.*;

/**
 * ユーザーマネージャー。
 *
 * - ユーザー情報の取得、保存を管理する
 */
public class UserManager {
    static final String TAG = UserManager.class.getName();

    // ユーザー情報を保持しておく
    private static User user;

    // ユーザー情報取得
    // - 戻り値が null の場合はコールバックにて、取得完了が通知される。
    public static User getUser(final Context context, final UserManagerCallbacks callbacks) {
        // とりあえず、毎回取得
//        if (user != null) {
//            return user;
//        }

        final GetUser api = new GetUser(context);

        JsonObjectRequest request = new JsonObjectRequest(VolleyApi.getVolleyMethod(api), api.getUrl(context),

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        VolleyApi.Log(TAG, api, response);

                        user = api.parseJsonResponse(response);
                        if (user != null) {
                            callbacks.onSuccessGetUser(user);
                        } else {
                            callbacks.onErrorGetUser(Error.getMessageCriticalSeverError(context, Error.GET_USER_RESPONSE_WRONG));
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyApi.Log(TAG, api, error);
                        VolleyApi.ApiError apiError = VolleyApi.parseVolleyError(error);

                        // TODO: 上位に伝えるエラー内容要検討。とりあえずはメッセージのみでいいのかな。
                        if (error.networkResponse == null) {
                            // - 通信できない
                            // - サーバー停止
                            callbacks.onErrorGetUser(context.getString(R.string.error_communication));
                        } else {
                            // レスポンスは返ってきてるけど、よくわかんないエラー (Heroku メンテナンス中に起こるかも)
                            if (apiError == null) {
                                callbacks.onErrorGetUser(Error.getMessageCriticalSeverError(context, Error.GET_USER_ERROR_RESPONSE_WRONG));
                                return;
                            }

                            // - 強制終了
                            if (apiError.code == ERROR_CODE_9999.code) {
                                // 本当はダメ。上位にこの種類のエラーを渡して、上位で処理すべき。
                                System.exit(0);
                            // TODO: 全 API で共通で起こりうるエラーの処理を共通化したい。
                            } else {
                                // message をそのまま表示するエラーコード
                                // 未知 (多分、新設) のエラーコードは message をそのまま表示する。
                                callbacks.onErrorGetUser(ErrorCode.getMessage(context, apiError.code));
                            }
                        }
                    }
                }
        );

        VolleyUtils.getRequestQueue(context).add(request);

        return null;
    }

    // コールバック
    // http://d.hatena.ne.jp/esmasui/20130628/1372386328
    // - 冗長な方がわかりやすい気がするけど…
    public static interface UserManagerCallbacks {
        public void onSuccessGetUser(User user);
        public void onErrorGetUser(String message);
    }
}
