package com.reward.omotesando.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.reward.omotesando.R;

/**
 * アプリ情報アクティビティ。
 */
public class AboutActivity extends BaseActivity {

    private static final String TAG = AboutActivity.class.getName();
    @Override
    protected String getLogTag() { return TAG; }


    /*
     * ライフサイクル
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_point_exchange, menu);
        return true;
    }
}
