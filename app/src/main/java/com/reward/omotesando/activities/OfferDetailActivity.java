package com.reward.omotesando.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.reward.omotesando.R;
import com.reward.omotesando.fragments.OfferDetailFragment;
import com.reward.omotesando.models.Offer;

public class OfferDetailActivity extends BaseActivity {

    private static final String TAG = OfferDetailActivity.class.getName();
    @Override
    protected String getLogTag() { return TAG; }

    // Model
    Offer mOffer;

    /*
     * 初期処理
     */
    private static final String ARG_OFFER = "offer";

    public static void start(Context packageContext, Offer offer, boolean noHistory) {
        Intent i = new Intent(packageContext, OfferDetailActivity.class);
        i.putExtra(ARG_OFFER, offer);
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
        setContentView(R.layout.activity_offer_detail);

        Intent intent = getIntent();
        mOffer = (Offer) intent.getSerializableExtra(ARG_OFFER);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.title_offer_detail));

        Fragment fragment = OfferDetailFragment.newInstance(mOffer);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
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
