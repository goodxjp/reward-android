package com.reward.omotesando.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.reward.omotesando.BuildConfig;
import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.fragments.ProgressDialogFragment;

public abstract class BaseActivity extends ActionBarActivity implements ShowableProgressDialog {

    protected String TAG = BaseActivity.class.getName();
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.v(getLogTag(), "[" + this.hashCode() + "] onCreate()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.v(getLogTag(), "[" + this.hashCode() + "] onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.v(getLogTag(), "[" + this.hashCode() + "] onPause()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.v(getLogTag(), "[" + this.hashCode() + "] onDestroy()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_base, menu);
        // TODO: 完成するまで非表示に
        menu.findItem(R.id.action_help).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);

        // デバッグ版でなければデバッグメニュー非表示
        if (BuildConfig.DEBUG == false) {
            menu.findItem(R.id.action_debug).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent intent;
        switch (id) {
            case R.id.action_point_exchange:
                intent = new Intent(this, PointExchangeActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_point_history:
                intent = new Intent(this, PointHistoryActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_help:
                intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_about:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_debug:
                intent = new Intent(this, DebugActivity.class);
                startActivity(intent);
                return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * ShowableProgressDialog
     */
    ProgressDialogFragment progressDialog;

    // 通信中ダイアログ
    @Override
    public void showProgressDialog(String title, String message) {
        Logger.v(TAG, "showProgressDialog()");
        // TODO: 画面構成が固まるまでは、通信中ダイアログは外す
        //progressDialog = ProgressDialogFragment.newInstance(title, message);
        //progressDialog.show(getSupportFragmentManager(), "progress");
    }

    @Override
    public void dismissProgressDialog() {
        Logger.v(TAG, "dismissProgressDialog()");
        // TODO: 画面構成が固まるまでは、通信中ダイアログは外す
        //progressDialog.getDialog().dismiss();
        // http://furudate.hatenablog.com/entry/2014/01/09/162421
        // progressDialog.dismiss() がなぜダメか、仕組みがよくわかっていない。
    }
}
