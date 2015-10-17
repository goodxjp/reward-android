package com.reward.omotesando.components.api;

import android.content.Context;

import com.reward.omotesando.R;

/**
 * リワードシステム API エラーコード。
 *
 * - app/controllers/api/v1/api_controler.rb 参照
 */
public enum ErrorCode {
    /*
     * 購入系
     */
    ERROR_CODE_2005(2005, R.string.message_restricted_one_day),
    ERROR_CODE_2007(2007, R.string.message_out_of_stock),

    /*
     * 共通
     */
    // 想定外の例外
    ERROR_CODE_9001(9001, R.string.message_sorry_inconvenience),
    // ActiveRecord::RecordNotFound, ActionController::RoutingError
    ERROR_CODE_9002(9002, R.string.message_sorry_inconvenience),
    // データベースのデータ不整合
    ERROR_CODE_9003(9003, R.string.message_repair_work),
    // 署名エラー
    ERROR_CODE_9004(9004, R.string.message_please_retry),
    // 強制終了
    ERROR_CODE_9999(9999, 0);

    public int code;
    public int messageResourceId;

    ErrorCode(int code, int messageResourceId) {
        this.code = code;
        this.messageResourceId = messageResourceId;
    }

    public static String getMessage(Context context, int code, String defaultMessage) {
        // ハッシュにすべきだけど、そんなに頻繁に起こるわけじゃないからぐるぐる回しでいいや。
        ErrorCode errorCode = null;
        for (ErrorCode ec : ErrorCode.values()) {
            if (ec.code == code) {
                errorCode = ec;
                break;
            }
        }

        if (errorCode == null) {
            // バグか、アプリに未登録のエラーコード
            return defaultMessage + " (A" + code + ")";
        }

        return getMessage(context, errorCode);
    }

    public static String getMessage(Context context, ErrorCode errorCode) {
        return context.getString(errorCode.messageResourceId) + " (A" + errorCode.code + ")";
    }
}
