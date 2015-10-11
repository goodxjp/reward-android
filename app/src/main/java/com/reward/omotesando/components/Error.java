package com.reward.omotesando.components;

import android.content.Context;

import com.reward.omotesando.R;

/**
 * エラー番号。
 */
public final class Error {
    private Error() {}

    // エラー番号を不具合の原因解析に使用するために全て別の番号をつける。
    // そのために各マネージャではなく、ここでエラー番号を一元管理する。

    /*
     * GET user
     */
    // レスポンス不正
    public static final int GET_USER_RESPONSE_WRONG = 1001;
    // エラーレスポンス不正
    public static final int GET_USER_ERROR_RESPONSE_WRONG = 1002;

    /*
     * GET items
     */
    public static final int API_GET_ITEMS = 9005;  // API GetItems が致命的

    // サーバーのバグでしかないエラーメッセージ (通常、起こりえない)
    // サーバー停止よりたちが悪い不具合。
    public static String getMessageCriticalSeverError(Context context, int errorNo) {
        return context.getString(R.string.message_critical_server_error) + " (E" + errorNo + ")";
    }
}
