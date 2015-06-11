package com.reward.omotesando.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.fragment.ProgressDialogFragment;

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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        //getMenuInflater().inflate(R.menu.menu_base, menu);
//        //return true;
//        return false;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    /*
     * ShowableProgressDialog
     */
    ProgressDialogFragment progressDialog;

    // 通信中ダイアログ
    @Override
    public void showProgressDialog(String title, String message) {
        Logger.v(TAG, "showProgressDialog()");
        progressDialog = ProgressDialogFragment.newInstance(title, message);
        progressDialog.show(getSupportFragmentManager(), "progress");
    }

    @Override
    public void dismissProgressDialog() {
        Logger.v(TAG, "dismissProgressDialog()");
        progressDialog.getDialog().dismiss();
        // http://furudate.hatenablog.com/entry/2014/01/09/162421
        // progressDialog.dismiss() がなぜダメか、仕組みがよくわかっていない。
    }
}
