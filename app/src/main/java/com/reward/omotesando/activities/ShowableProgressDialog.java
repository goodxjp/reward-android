package com.reward.omotesando.activities;

/**
 * 進捗ダイアログが表示可能な Activity。
 */
public interface ShowableProgressDialog {
    void showProgressDialog(String title, String message);
    void dismissProgressDialog();
}
