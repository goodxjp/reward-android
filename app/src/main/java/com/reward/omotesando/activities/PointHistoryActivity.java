package com.reward.omotesando.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

import com.reward.omotesando.R;
import com.reward.omotesando.fragments.GiftListFragment;
import com.reward.omotesando.fragments.PointHistoryListFragment;

public class PointHistoryActivity extends BaseActivity {

    FragmentTabHost mTabHost;

    public static final String TAB_POINT_HISTORY    = "tab_point_history";
    public static final String TAB_EXCHANGE_HISTORY = "tab_exchange_history";

    private static final String ARG_START_TAB = "start_tab";

    public static void start(Context packageContext, String startTab, boolean noHistory) {
        Intent i = new Intent(packageContext, PointHistoryActivity.class);
        i.putExtra(ARG_START_TAB, startTab);
        if (noHistory) {
            i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        }
        packageContext.startActivity(i);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 引数処理
        Intent i = getIntent();
        String startTab = i.getStringExtra(ARG_START_TAB);
        if (startTab == null) {
            startTab = TAB_POINT_HISTORY;
        }

        setContentView(R.layout.activity_point_history);

        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.content);

        TabHost.TabSpec tabSpec1 = mTabHost.newTabSpec(TAB_POINT_HISTORY).setIndicator(getString(R.string.tab_point_history));
        mTabHost.addTab(tabSpec1, PointHistoryListFragment.class, null);

        TabHost.TabSpec tabSpec2 = mTabHost.newTabSpec(TAB_EXCHANGE_HISTORY).setIndicator(getString(R.string.tab_exchange_history));
        mTabHost.addTab(tabSpec2, GiftListFragment.class, null);

        mTabHost.setCurrentTabByTag(startTab);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_point_exchange, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
