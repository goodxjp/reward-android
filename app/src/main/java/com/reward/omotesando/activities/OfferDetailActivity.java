package com.reward.omotesando.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.Menu;

import com.reward.omotesando.R;
import com.reward.omotesando.fragments.OfferDetailFragment;
import com.reward.omotesando.models.Offer;

/**
 * 案件詳細アクティビティ。
 *
 * - 案件詳細の表示
 * - 案件の実行
 */
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

        // 引数処理
        Intent intent = getIntent();
        mOffer = (Offer) intent.getSerializableExtra(ARG_OFFER);

        setContentView(R.layout.activity_offer_detail);

        // アクションバー
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.title_offer_detail));
        // AndroidManifest.xml の android:label で指定しているから、いらないかも。

        // http://www.garunimo.com/program/p17.xhtml
        if (savedInstanceState == null) {
            Fragment fragment = OfferDetailFragment.newInstance(mOffer);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_point_exchange, menu);
        return true;
    }
}
