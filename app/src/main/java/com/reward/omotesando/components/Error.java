package com.reward.omotesando.components;

import android.content.Context;

import com.reward.omotesando.R;

/**
 * エラー番号。
 */
public final class Error {
    private Error() {}

    public static final int API_GET_ITEMS = 9005;  // API GetItems が致命的

    // サーバーのバグでしかないエラーメッセージ (通常、起こりえない)
    // サーバー停止よりたちが悪い不具合。
    public static String getMessageCriticalSeverError(Context context, int errorNo) {
        return context.getString(R.string.message_critical_server_error) + " (" + errorNo + ")";
    }
}
