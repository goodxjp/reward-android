package com.reward.omotesando.components;

import android.content.Context;

import com.reward.omotesando.R;

/**
 * エラー番号。
 *
 * - ここはアプリのエラー。サーバーから返ってくる API のエラーコードは ErrorCode を参照。
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
     * GET offers
     */
    // レスポンス不正
    public static final int GET_OFFERS_RESPONSE_WRONG = 2001;
    // エラーレスポンス不正
    public static final int GET_OFFERS_ERROR_RESPONSE_WRONG = 2002;

    /*
     * GET items
     */
    // レスポンス不正
    public static final int GET_ITEMS_RESPONSE_WRONG = 3001;
    // エラーレスポンス不正
    public static final int GET_ITEMS_ERROR_RESPONSE_WRONG = 3002;

    /*
     * POST purchases
     */
    // レスポンス不正 (レスポンス見てない)
    //public static final int POST_PURCHASES_RESPONSE_WRONG = 4001;
    // エラーレスポンス不正
    public static final int POST_PURCHASES_ERROR_RESPONSE_WRONG = 4002;

    /*
     * 共通
     */
    // 通信の一般的エラー
    // TODO: サーバー停止と端末の通信エラーでエラーコードを分けたい。
    public static final int COMMUNICATION_ERROR = 9001;


    // 通信エラー時のエラーメッセージ
    public static String getMessageCommunicationError(Context context) {
        int errorNo = COMMUNICATION_ERROR;
        return context.getString(R.string.message_critical_server_error) + " (E" + errorNo + ")";
    }

    // サーバーのバグでしかないエラーメッセージ (通常、起こりえない)
    // サーバー停止よりたちが悪い不具合。
    public static String getMessageCriticalSeverError(Context context, int errorNo) {
        return context.getString(R.string.message_critical_server_error) + " (E" + errorNo + ")";
    }
}
