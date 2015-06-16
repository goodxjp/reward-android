package com.reward.omotesando.activities;

import java.util.Locale;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.commons.VolleyUtils;
import com.reward.omotesando.components.GcmManager;
import com.reward.omotesando.components.Terminal;
import com.reward.omotesando.components.api.PostMediaUsers;
import com.reward.omotesando.fragment.AboutFragment;
import com.reward.omotesando.fragment.DebugFragment;
import com.reward.omotesando.fragment.HelpFragment;
import com.reward.omotesando.fragment.ItemFragment;
import com.reward.omotesando.fragment.OfferDetailFragment;
import com.reward.omotesando.fragment.OfferFragment;
import com.reward.omotesando.fragment.OfferListFragment;
import com.reward.omotesando.fragment.PointHistoryListFragment;
import com.reward.omotesando.models.MediaUser;
import com.reward.omotesando.models.NavigationMenu;
import com.reward.omotesando.models.Offer;

import org.json.JSONObject;

public class TabbedActivity extends BaseActivity
        implements ActionBar.TabListener,
                   GcmManager.GcmManagerCallbacks,
                   OfferListFragment.OnFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.setTitle(R.string.app_name);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        //mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        state.start(this);
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_tabbed, menu);
//        return true;
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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /*
     * GcmManager.GcmManagerCallbacks
     */
    @Override
    public void onRegistered(String regId) {
        Logger.v(getLogTag(), "[" + this.hashCode() + "] onRegistered");

        state.gcmRegisterd(this, regId);
    }

    /*
     * OfferListFragment.OnFragmentInteractionListener
     */
    @Override
    public void onFragmentInteraction(Offer offer) {
        Fragment fragment = OfferDetailFragment.newInstance(offer);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }


    /*
     * 状態管理
     */
    private State state = State.INITIAL;

    enum State {
        // 初期状態
        INITIAL {
            @Override
            public void start(TabbedActivity activity) {

                GcmManager gcmManager = GcmManager.getInstance(activity.getApplicationContext());
                if (gcmManager.tryToRegister(activity, activity)) {
                    //activity.showProgressDialog(null, activity.getString(R.string.dialog_message_initializing));
                    activity.transit(GCM_REGISTERING);
                } else if (activity.tryToRegisterUser(null)) {
                    //activity.showProgressDialog(null, activity.getString(R.string.dialog_message_initializing));
                    activity.transit(USER_REGISTERING);
                } else {
                    activity.transit(READY);
                    activity.readyGo();

                    // TODO: activity.showProgressDialog() の直後に呼ぶと落ちるのはなぜか要調査;

                    //activity.dismissProgressDialog();
                }
            }
        },

        // GCM 登録中
        GCM_REGISTERING {
            @Override
            public void gcmRegisterd(TabbedActivity activity, String regId) {
                if (activity.tryToRegisterUser(null)) {
                    activity.transit(USER_REGISTERING);
                } else {
                    activity.transit(READY);
                    activity.readyGo();
                    //activity.dismissProgressDialog();
                }
            }
        },

        // ユーザー登録中
        USER_REGISTERING {
            @Override
            public void successUserRegister(TabbedActivity activity, MediaUser mediaUser) {
                // 登録に成功したら保存
                MediaUser.storeMediaUserId(activity, mediaUser.mediaUserId, mediaUser.terminalId);

                activity.transit(READY);
                activity.readyGo();
                //activity.dismissProgressDialog();
            }

            @Override
            public void failureUserRegister(TabbedActivity activity) {
                // TODO: ユーザー登録に失敗したときのエラー処理
                activity.transit(READY);
                activity.readyGo();
                //activity.dismissProgressDialog();
            }
        },

        // 操作可能状態
        READY;

        /*
         * イベント
         */
        // 初期処理開始
        public void start(TabbedActivity activity) {
            throw new IllegalStateException();
        }

        // GCM 登録完了
        public void gcmRegisterd(TabbedActivity activity, String regId) {
            throw new IllegalStateException();
        }

        // ユーザー登録成功
        public void successUserRegister(TabbedActivity activity, MediaUser mediaUser) {
            throw new IllegalStateException();
        }

        // ユーザー登録失敗
        public void failureUserRegister(TabbedActivity activity) {
            throw new IllegalStateException();
        }
    }

    // 状態遷移 (enum State 内でのみ使用すること、なら中に入れちゃえばいいんじゃね？)
    private void transit(State nextState) {
        Logger.d(TAG, "STATE: " + state + " -> " + nextState);
        state = nextState;
    }

    // ユーザー登録してみる
    private boolean tryToRegisterUser(String regId) {
        if  (MediaUser.getMediaUser(getApplicationContext()) != null) {
            // 登録済みなら送らない？でいいかなぁ。毎回送る？
            return false;
        }

        registerUser(regId);
        return true;
    }

    // ユーザー登録
    private void registerUser(String regId) {
        // ユーザー登録 API
        final PostMediaUsers api = new PostMediaUsers(this, Terminal.getAndroidId(this), new JSONObject(Terminal.getBuildInfo()), regId);

        JsonObjectRequest request = new JsonObjectRequest(api.getUrl(this), api.getJsonRequest(),

            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Logger.e(TAG, "HTTP: body is " + response.toString());
                    MediaUser mediaUser = api.parseJsonResponse(response);

                    state.successUserRegister(TabbedActivity.this, mediaUser);
                }
            },

            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Logger.e(TAG, "HTTP: error = " + error.getMessage());

                    state.failureUserRegister(TabbedActivity.this);
                }
            });

        RequestQueue requestQueue = VolleyUtils.getRequestQueue(getApplicationContext());
        requestQueue.add(request);
    }

    // 準備完了後の初期処理
    private void readyGo() {
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;

            NavigationMenu[] values = NavigationMenu.values();
            NavigationMenu navigationMenu = values[position];
            switch (navigationMenu) {
                case OFFER_LIST:
                    fragment = OfferFragment.newInstance();
                    break;
                case POINT_EXCHANGE:
                    fragment = ItemFragment.newInstance();
                    break;
                case POINT_HISTORY:
                    fragment = PointHistoryListFragment.newInstance();
                    break;
                case HELP:
                    fragment = HelpFragment.newInstance();
                    break;
                case ABOUT:
                    fragment = AboutFragment.newInstance();
                    break;
                case DEBUG:
                    fragment = DebugFragment.newInstance();
                    break;
                default:
                    throw new IllegalStateException();
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return NavigationMenu.values().length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // ナビゲーションメニューの文字列を取得
            String[] menuStrings = new String[NavigationMenu.values().length];
            for (NavigationMenu menu: NavigationMenu.values()) {
                menuStrings[menu.ordinal()] = getString(menu.resource);
            }

            return menuStrings[position];
        }
    }

}
