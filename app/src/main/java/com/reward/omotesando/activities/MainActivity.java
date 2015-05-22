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
import com.reward.omotesando.components.api.PostMediaUsers;
import com.reward.omotesando.components.Terminal;
import com.reward.omotesando.fragment.AboutFragment;
import com.reward.omotesando.fragment.HelpFragment;
import com.reward.omotesando.fragment.NavigationDrawerFragment;
import com.reward.omotesando.R;
import com.reward.omotesando.fragment.OfferDetailFragment;
import com.reward.omotesando.fragment.OfferListFragment;
import com.reward.omotesando.fragment.PointHistoryListFragment;
import com.reward.omotesando.models.MediaUser;
import com.reward.omotesando.models.NavigationMenu;
import com.reward.omotesando.models.Offer;

public class MainActivity extends BaseActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
                   GcmManager.GcmManagerCallbacks,
                   OfferListFragment.OnFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getName();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        //mTitle = getTitle();
        mTitle = getString(R.string.app_name);
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // TODO: 回転とかで毎回通信が走るのをまじめに対処。

        // TODO: GDM 状態遷移
        // TODO: 初期化処理中は通信中的な表示にする
        GcmManager gcmManager = GcmManager.getInstance(getApplicationContext(), this);
        gcmManager.tryToRegister(this);

        MediaUser mediaUser;
        if ((mediaUser = MediaUser.getMediaUser(this)) != null) {
            Logger.i("MediaUser", "mediaUserId = " + mediaUser.mediaUserId);
            Logger.i("MediaUser", "terminalId = " + mediaUser.terminalId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 公式ドキュメントによると onResume() でもチェックするのがお作法らしい。
        // http://developer.android.com/google/gcm/client.html#sample-play
        GcmManager gcmManager = GcmManager.getInstance(getApplicationContext(), this);
        gcmManager.checkPlayServices(this);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Logger.v(TAG, "[" + this.hashCode() + "] onNavigationDrawerItemSelected() position = " + position);

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
                if (fragment instanceof OfferListFragment) {
                    return;
                }
                fragment = OfferListFragment.newInstance();
                break;
            case POINT_EXCHANGE:
                fragment = PlaceholderFragment.newInstance(0);
                break;
            case POINT_HISTORY:
                // ポイント履歴一覧は作り直さない
                if (fragment instanceof PointHistoryListFragment) {
                    return;
                }
                fragment = PointHistoryListFragment.newInstance();
                break;
            case HELP:
                fragment = HelpFragment.newInstance();
                break;
            case ABOUT:
                fragment = AboutFragment.newInstance();
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

    // GcmManager.GcmManagerCallbacks コールバック
    @Override
    public void onRegistered(String regId) {
        RequestQueue requestQueue = VolleyUtils.getRequestQueue(getApplicationContext());

        // ユーザー登録 API
        final PostMediaUsers api = new PostMediaUsers(this, Terminal.getAndroidId(this), new JSONObject(Terminal.getBuildInfo()), regId);

        JsonObjectRequest request = new JsonObjectRequest(api.getUrl(this), api.getJsonRequest(),

            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Logger.e("HTTP", "body is " + response.toString());
                    MediaUser mediaUser = api.parseJsonResponse(response);

                    // 登録に成功したら保存
                    MediaUser.storeMediaUserId(MainActivity.this, mediaUser.mediaUserId, mediaUser.terminalId);
                }
            },

            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Logger.e("HTTP", "error = " + error.getMessage());
                    // TODO
                }
            });

        requestQueue.add(request);
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
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
