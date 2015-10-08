package com.reward.omotesando.models;

import com.reward.omotesando.R;

/**
 * 案件一覧に表示させるタブ。
 */
public enum OfferListTab {
    APP_DL(
            R.string.tab_app_dl
//    ),
//    MOVIE(
//            R.string.tab_movie
//    ),
//    FB(
//            R.string.tab_fb
    );

    public int resource;

    private OfferListTab(int resource) {
        this.resource = resource;
    }
}
