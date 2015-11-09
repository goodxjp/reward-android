package com.reward.omotesando.models;

import com.reward.omotesando.R;

/**
 * 案件一覧に表示させるタブ。
 */
public enum OfferListTab {
    APP_DL(
            R.string.tab_app_dl
    ),
    MEMBERSHIP(
            R.string.tab_membership
    ),
    APPLICATION(
            R.string.tab_application
    ),
    SHOPPING(
            R.string.tab_shopping
    );

    public int resource;

    OfferListTab(int resource) {
        this.resource = resource;
    }
}
