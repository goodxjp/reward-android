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
    public static final int GET_USER_RESPONSE_WRONG = 1101;
    // エラーレスポンス不正
    public static final int GET_USER_ERROR_RESPONSE_WRONG = 1102;

    /*
     * GET offers
     */
    // レスポンス不正
    public static final int GET_OFFERS_RESPONSE_WRONG = 1201;
    // エラーレスポンス不正
    public static final int GET_OFFERS_ERROR_RESPONSE_WRONG = 1202;

    /*
     * GET point_histories
     */
    // レスポンス不正
    public static final int GET_POINT_HISTORIES_RESPONSE_WRONG = 1301;
    // エラーレスポンス不正
    public static final int GET_POINT_HISTORIES_ERROR_RESPONSE_WRONG = 1302;

    /*
     * GET gifts
     */
    // レスポンス不正
    public static final int GET_GIFTS_RESPONSE_WRONG = 1401;
    // エラーレスポンス不正
    public static final int GET_GIFTS_ERROR_RESPONSE_WRONG = 1402;

    /*
     * GET items
     */
    // レスポンス不正
    public static final int GET_ITEMS_RESPONSE_WRONG = 1501;
    // エラーレスポンス不正
    public static final int GET_ITEMS_ERROR_RESPONSE_WRONG = 1502;

    /*
     * POST purchases
     */
    // レスポンス不正 (レスポンス見てない)
    //public static final int POST_PURCHASES_RESPONSE_WRONG = 1601;
    // エラーレスポンス不正
    public static final int POST_PURCHASES_ERROR_RESPONSE_WRONG = 1602;

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
