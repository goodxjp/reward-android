package com.reward.omotesando.activities;

import android.os.Bundle;
import android.view.Menu;

import com.reward.omotesando.R;

/**
 * ポイント交換アクティビティ。
 *
 * - 商品一覧の表示
 * - ポイント交換の実行
 */
public class PointExchangeActivity extends BaseActivity {

    private static final String TAG = PointExchangeActivity.class.getName();
    @Override
    protected String getLogTag() { return TAG; }


    /*
     * ライフサイクル
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_exchange);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_point_exchange, menu);
        return true;
    }
}
