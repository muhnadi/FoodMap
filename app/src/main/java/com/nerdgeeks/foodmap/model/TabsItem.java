package com.nerdgeeks.foodmap.model;

import android.support.v4.app.Fragment;

/**
 * Created by hp on 12/5/2016.
 */
public class TabsItem {
    private final Fragment mFragment;

    public TabsItem(Fragment fragment) {
        this.mFragment = fragment;
    }

    public Fragment getFragment() {
        return mFragment;
    }
}
