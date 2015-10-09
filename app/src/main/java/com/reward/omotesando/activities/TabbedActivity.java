package com.reward.omotesando.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.reward.omotesando.R;
import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.commons.VolleyUtils;
import com.reward.omotesando.components.GcmManager;
import com.reward.omotesando.components.Terminal;
import com.reward.omotesando.components.VolleyApi;
import com.reward.omotesando.components.api.PostUser;
import com.reward.omotesando.fragments.AboutFragment;
import com.reward.omotesando.fragments.AlertDialogFragment;
import com.reward.omotesando.fragments.ExchangeDialogFragment;
import com.reward.omotesando.fragments.HelpFragment;
import com.reward.omotesando.fragments.OfferDetailFragment;
import com.reward.omotesando.fragments.OfferFragment;
import com.reward.omotesando.fragments.OfferListFragment;
import com.reward.omotesando.fragments.PointHistoryListFragment;
import com.reward.omotesando.models.NavigationMenu;
import com.reward.omotesando.models.Offer;
import com.reward.omotesando.models.OfferListTab;
import com.reward.omotesando.models.User;

import org.json.JSONObject;

/**
 * メイン (案件一覧表示) アクティビティ。
 */
public class TabbedActivity extends BaseActivity
        implements GcmManager.GcmManagerCallbacks,
                   OfferListFragment.OnFragmentInteractionListener {

    protected String TAG = TabbedActivity.class.getName();
    @Override
    protected String getLogTag() { return TAG; }

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


    /*
     * ライフサイクル
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.app_name));

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        //mViewPager.setAdapter(mSectionsPagerAdapter); → 初期化処理完了後。

        // TODO: 状態を保存しておかないと回転の時に状態が初期化されてしまう。
        if (state == State.INITIAL) {
            state.start(this);
        }
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
        Intent intent = new Intent(this, OfferDetailActivity.class);
        intent.putExtra("xxx", offer);  // TODO: 名前調整
        startActivity(intent);
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
                String regId;
                if ((regId = gcmManager.tryToRegister(activity, activity)) == null) {
                    //activity.showProgressDialog(null, activity.getString(R.string.dialog_message_initializing));
                    activity.transit(GCM_REGISTERING);
                } else if (activity.tryToRegisterUser(regId)) {
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
                if (activity.tryToRegisterUser(regId)) {
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
            public void successUserRegister(TabbedActivity activity, User user) {
                // 登録に成功したら保存
                User.storeUser(activity, user);

                activity.transit(READY);
                activity.readyGo();
                //activity.dismissProgressDialog();
            }

            @Override
            public void failureUserRegister(TabbedActivity activity) {
                //activity.transit(READY);
                //activity.readyGo();
                //activity.dismissProgressDialog();

                activity.transit(ERROR);

                // TODO: ダイアログを汎用にしておいて、複数の種類のダイアログのコールバック受けられるようにしておいた方がよさげ？
                AlertDialogFragment dialog = AlertDialogFragment.newInstance(
                        activity.getString(R.string.dialog_title_error_init),
                        activity.getString(R.string.dialog_message_error_init));
                dialog.show(activity.getSupportFragmentManager(), "alert_dialog");
            }
        },

        // 操作可能状態
        READY,

        // エラー状態
        ERROR;

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
        public void successUserRegister(TabbedActivity activity, User user) {
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
        if  (User.getUser(getApplicationContext()) != null) {
            // 登録済みなら、できるだけユーザー登録は送らないようにしたい。
            // 同一端末かどうかのチェックは完全にできないため。
            return false;
        }

        registerUser(regId);
        return true;
    }

    // ユーザー登録
    private void registerUser(String regId) {
        // ユーザー登録 API
        final PostUser api = new PostUser(this, Terminal.getTerminalId(this), new JSONObject(Terminal.getBuildInfo()), regId);

        JsonObjectRequest request = new JsonObjectRequest(api.getUrl(this), api.getJsonRequest(),

            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    VolleyApi.Log(TAG, api, response);

                    User user = api.parseJsonResponse(response);

                    state.successUserRegister(TabbedActivity.this, user);
                }
            },

            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyApi.Log(TAG, api, error);

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

            OfferListTab[] values = OfferListTab.values();
            OfferListTab tab = values[position];
            switch (tab) {
                case APP_DL:
                    // 現状、OfferFragment を同一画面に複数追加できない。
                    //fragment = OfferFragment.newInstance();
                    fragment = OfferListFragment.newInstance();
                    break;
//                case MOVIE:
//                    fragment = OfferListFragment.newInstance();
//                    break;
//                case FB:
//                    fragment = OfferListFragment.newInstance();
//                    break;
                default:
                    throw new IllegalStateException();
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return OfferListTab.values().length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // ナビゲーションメニューの文字列を取得
            String[] menuStrings = new String[OfferListTab.values().length];
            for (OfferListTab menu: OfferListTab.values()) {
                menuStrings[menu.ordinal()] = getString(menu.resource);
            }

            return menuStrings[position];
        }
    }

}
