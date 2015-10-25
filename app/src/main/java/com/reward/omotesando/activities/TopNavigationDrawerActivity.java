package com.reward.omotesando.activities;

import android.app.Activity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import com.reward.omotesando.commons.Logger;
import com.reward.omotesando.commons.VolleyUtils;
import com.reward.omotesando.components.GcmManager;
import com.reward.omotesando.components.api.PostUser;
import com.reward.omotesando.components.Terminal;
import com.reward.omotesando.fragments.AboutFragment;
import com.reward.omotesando.fragments.DebugFragment;
import com.reward.omotesando.fragments.HelpFragment;
import com.reward.omotesando.fragments.NavigationDrawerFragment;
import com.reward.omotesando.R;
import com.reward.omotesando.fragments.OfferDetailFragment;
import com.reward.omotesando.fragments.OfferListFragment;
import com.reward.omotesando.fragments.PointHistoryListFragment;
import com.reward.omotesando.models.NavigationMenu;
import com.reward.omotesando.models.Offer;
import com.reward.omotesando.models.User;

/**
 * トップ画面 (案件一覧) アクティビティ (廃棄)。
 *
 * - 初期処理
 * - メニューによるフラグメント切り替えの管理
 */
public class TopNavigationDrawerActivity extends BaseActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
                   GcmManager.GcmManagerCallback,
                   OfferListFragment.OnFragmentInteractionListener {

    private static final String TAG = TopNavigationDrawerActivity.class.getName();
    @Override
    protected String getLogTag() { return TAG; }

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;


    /*
     * ライフサイクル
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_navigation_drawer);

        //mTitle = getTitle();
        mTitle = getString(R.string.app_name);

        initNavigationDrawer();

        // TODO: 回転とかで毎回通信が走るのをまじめに対処。

        state.start(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 公式ドキュメントによると onResume() でもチェックするのがお作法らしい。
        // http://developer.android.com/google/gcm/client.html#sample-play
        GcmManager gcmManager = GcmManager.getInstance(getApplicationContext());
        gcmManager.checkPlayServices(this);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Logger.v(TAG, "[" + this.hashCode() + "] onNavigationDrawerItemSelected() position = " + position);

        if (state != State.READY) {
            return;
        }

        // update the main content by replacing fragments
        Fragment fragment;

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = fragmentManager.findFragmentByTag("main");
        Logger.e(TAG, "fragment = " + fragment);

        NavigationMenu[] values = NavigationMenu.values();
        NavigationMenu navigationMenu = values[position];
        switch (navigationMenu) {
            case OFFER_LIST:
                // オファー一覧は作り直さない
                // TODO: ここの挙動要検討
//                if (fragment instanceof OfferListFragment) {
//                    return;
//                }
                fragment = OfferListFragment.newInstance();
                break;
            case POINT_EXCHANGE:
                fragment = PlaceholderFragment.newInstance(0);
                break;
            case POINT_HISTORY:
                // ポイント履歴一覧は作り直さない
                // TODO: ここの挙動要検討
//                if (fragment instanceof PointHistoryListFragment) {
//                    return;
//                }
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

        //FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, "main")
                .commit();
    }

    public void onSectionAttached(int number) {
//        switch (number) {
//            case 1:
//                mTitle = getString(R.string.title_section1);
//                break;
//            case 2:
//                mTitle = getString(R.string.title_section2);
//                break;
//            case 3:
//                mTitle = getString(R.string.title_section3);
//                break;
//        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (true) return false;

        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            //getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            //return true;
            return false;
        }
        return false;
        //return super.onCreateOptionsMenu(menu);
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

    /*
     * 状態管理
     */

    private State state = State.INITIAL;

    enum State {
        // 初期状態
        INITIAL {
            @Override
            public void start(TopNavigationDrawerActivity activity) {

                GcmManager gcmManager = GcmManager.getInstance(activity.getApplicationContext());
                if (gcmManager.tryToRegister(activity, activity) == null) {
                    //activity.showProgressDialog(null, activity.getString(R.string.dialog_message_initializing));
                    activity.transit(GCM_REGISTERING);
                } else if (activity.tryToRegisterUser(null)) {
                    //activity.showProgressDialog(null, activity.getString(R.string.dialog_message_initializing));
                    activity.transit(USER_REGISTERING);
                } else {
                    activity.transit(READY);
                    activity.onNavigationDrawerItemSelected(0);  // TODO: もっとうまいやり方がないか？

                    // TODO: activity.showProgressDialog() の直後に呼ぶと落ちるのはなぜか要調査;

                    //activity.dismissProgressDialog();
                }
            }
        },

        // GCM 登録中
        GCM_REGISTERING {
            @Override
            public void gcmRegisterd(TopNavigationDrawerActivity activity, String regId) {
                if (activity.tryToRegisterUser(null)) {
                    activity.transit(USER_REGISTERING);
                } else {
                    activity.transit(READY);
                    activity.onNavigationDrawerItemSelected(0);  // TODO: もっとうまいやり方がないか？
                    //activity.dismissProgressDialog();
                }
            }
        },

        // ユーザー登録中
        USER_REGISTERING {
            @Override
            public void successUserRegister(TopNavigationDrawerActivity activity, User user) {
                // 登録に成功したら保存
                //MediaUser.storeMediaUserId(activity, mediaUser.mediaUserId, mediaUser.terminalId);

                activity.transit(READY);
                activity.onNavigationDrawerItemSelected(0);  // TODO: もっとうまいやり方がないか？
                //activity.dismissProgressDialog();
            }

            @Override
            public void failureUserRegister(TopNavigationDrawerActivity activity) {
                // TODO: ユーザー登録に失敗したときのエラー処理
                activity.transit(READY);
                activity.onNavigationDrawerItemSelected(0);  // TODO: もっとうまいやり方がないか？
                //activity.dismissProgressDialog();
            }
        },

        // 操作可能状態
        READY;

        /*
         * イベント
         */
        // 初期処理開始
        public void start(TopNavigationDrawerActivity activity) {
            throw new IllegalStateException();
        }

        // GCM 登録完了
        public void gcmRegisterd(TopNavigationDrawerActivity activity, String regId) {
            throw new IllegalStateException();
        }

        // ユーザー登録成功
        public void successUserRegister(TopNavigationDrawerActivity activity, User user) {
            throw new IllegalStateException();
        }

        // ユーザー登録失敗
        public void failureUserRegister(TopNavigationDrawerActivity activity) {
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
            // 登録済みなら送らない？でいいかなぁ。毎回送る？
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
                    Logger.e(TAG, "HTTP: body is " + response.toString());
                    //MediaUser mediaUser = api.parseJsonResponse(response);

                    //state.successUserRegister(MainActivity.this, mediaUser);
                }
            },

            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Logger.e(TAG, "HTTP: error = " + error.getMessage());

                    state.failureUserRegister(TopNavigationDrawerActivity.this);
                }
            });

        RequestQueue requestQueue = VolleyUtils.getRequestQueue(getApplicationContext());
        requestQueue.add(request);
    }

    private void initNavigationDrawer() {
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
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


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((TopNavigationDrawerActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
