package com.reward.omotesando.models;

import com.reward.omotesando.R;

/**
 * メニュー項目。
 */
public enum NavigationMenu {
    OFFER_LIST(
            R.string.menu_offer_list
    ),
    POINT_EXCHANGE(
            R.string.menu_point_exchange
    ),
    POINT_HISTORY(
            R.string.menu_point_history
    ),
    HELP(
            R.string.menu_help
    ),
    ABOUT(
            R.string.menu_about
    ),
    DEBUG(
            R.string.menu_debug
    );

    public int resource;

    private NavigationMenu(int resource) {
        this.resource = resource;
    }
}
