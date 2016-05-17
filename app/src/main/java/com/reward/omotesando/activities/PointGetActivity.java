package com.reward.omotesando.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;

import com.reward.omotesando.R;

/**
 * ポイント獲得通知アクティビティ。
 */
public class PointGetActivity extends BaseActivity {

    private static final String TAG = PointGetActivity.class.getName();
    @Override
    protected String getLogTag() { return TAG; }

    WaitAsyncTask mTask;

    /*
     * 初期処理
     */
    public static void start(Context packageContext) {
        Intent i = new Intent(packageContext, PointGetActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        packageContext.startActivity(i);
    }


    /*
     * ライフサイクル
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_get);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // TODO: 非同期のもっとお手軽な方法あるかも。
        mTask = new WaitAsyncTask(this);
        mTask.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_point_exchange, menu);
        return true;
    }

    class WaitAsyncTask extends AsyncTask<Void, Void, Void> {
        PointGetActivity mActivity;

        public WaitAsyncTask(PointGetActivity activity) {
            this.mActivity = activity;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            mActivity.finish();
        }
    }
}
