package com.reward.omotesando.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;

import com.reward.omotesando.R;
import com.reward.omotesando.fragments.GiftListFragment;
import com.reward.omotesando.fragments.PointHistoryListFragment;

/**
 * ポイント履歴アクティビティ。
 */
public class PointHistoryActivity extends BaseActivity {

    private static final String TAG = PointHistoryActivity.class.getName();
    @Override
    protected String getLogTag() { return TAG; }

    //FragmentTabHost mTabHost;

    /*
     * 初期処理
     */
    private static final String ARG_START_TAB = "start_tab";

    public static void start(Context packageContext, PointHistoryTab startTab, boolean noHistory) {
        Intent i = new Intent(packageContext, PointHistoryActivity.class);
        i.putExtra(ARG_START_TAB, startTab.ordinal());
        if (noHistory) {
            i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        }
        packageContext.startActivity(i);
    }


    /*
     * ライフサイクル
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 引数処理
        Intent i = getIntent();
        int startTab = i.getIntExtra(ARG_START_TAB, -1);
        if (startTab < 0) {
            startTab = 0;
        }

        setContentView(R.layout.activity_point_history);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        // タブの初期位置をパラメータにより変更
        viewPager.setCurrentItem(startTab);

        /* TabHost 版
        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.content);

        TabHost.TabSpec tabSpecPointHistory = mTabHost.newTabSpec(TAB_POINT_HISTORY).setIndicator(getString(R.string.tab_point_history));
        mTabHost.addTab(tabSpecPointHistory, PointHistoryListFragment.class, null);

        TabHost.TabSpec tabSpecExchangeHistory = mTabHost.newTabSpec(TAB_EXCHANGE_HISTORY).setIndicator(getString(R.string.tab_exchange_history));
        mTabHost.addTab(tabSpecExchangeHistory, GiftListFragment.class, null);

        mTabHost.setCurrentTabByTag(startTab);
        */
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

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;

            PointHistoryTab[] values = PointHistoryTab.values();
            PointHistoryTab tab = values[position];
            switch (tab) {
                case POINT_HISTORY:
                    fragment =  PointHistoryListFragment.newInstance();
                    break;
                case EXCHANGE_HISTORY:
                    fragment = GiftListFragment.newInstance();
                    break;
                default:
                    throw new IllegalStateException();
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return PointHistoryTab.values().length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            PointHistoryTab[] values = PointHistoryTab.values();
            return getString(values[position].resource);
        }
    }

    public enum PointHistoryTab {
        POINT_HISTORY(
                R.string.tab_point_history
        ),
        EXCHANGE_HISTORY(
                R.string.tab_exchange_history
        );

        public int resource;

        PointHistoryTab(int resource) {
            this.resource = resource;
        }
    }
}
