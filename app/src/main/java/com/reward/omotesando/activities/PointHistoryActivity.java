package com.reward.omotesando.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

import com.reward.omotesando.R;
import com.reward.omotesando.fragments.GiftListFragment;
import com.reward.omotesando.fragments.PointHistoryListFragment;

public class PointHistoryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_history);

        FragmentTabHost host = (FragmentTabHost)findViewById(android.R.id.tabhost);
        host.setup(this, getSupportFragmentManager(), R.id.content);

        TabHost.TabSpec tabSpec1 = host.newTabSpec("tab_get_history").setIndicator(getString(R.string.tab_point_history));
        host.addTab(tabSpec1, PointHistoryListFragment.class, null);

        TabHost.TabSpec tabSpec2 = host.newTabSpec("tab_exchange_history").setIndicator(getString(R.string.tab_exchange_history));
        host.addTab(tabSpec2, GiftListFragment.class, null);
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
