package com.nerdgeeks.foodmap.fragments;

import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nerdgeeks.foodmap.R;
import com.nerdgeeks.foodmap.adapter.TabsAdapter;
import com.nerdgeeks.foodmap.model.TabsItem;
import java.util.ArrayList;
import java.util.List;
import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;

public class MainFragment extends Fragment implements MaterialTabListener {

    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    private MaterialTabHost tabHost;
    private ViewPager viewPager;
    private List<TabsItem> mTabs = new ArrayList<>();
    private int[] icon = {R.drawable.ic_near_me, R.drawable.ic_list};


    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(String param1) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
        createTabsItem();
    }

    private void createTabsItem() {
        mTabs.add(new TabsItem(NearbyFragment.newInstance(mParam1)));
        mTabs.add(new TabsItem(ResultFragment.newInstance(mParam1)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        /**
         *Set an Adapter for the View Pager
         */
        viewPager = (ViewPager) rootView.findViewById(R.id.viewPager);
        TabsAdapter tabAdapter = new TabsAdapter(getChildFragmentManager(),mTabs);
        viewPager.setOffscreenPageLimit(mTabs.size());
        viewPager.setAdapter(tabAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // when user do a swipe the selected tab change
                tabHost.setSelectedNavigationItem(position);
            }
        });
        //Adding TabHost
        tabHost = (MaterialTabHost) rootView.findViewById(R.id.materialTabHost);
        // insert all tabs from pagerAdapter data
        for (int i = 0; i < tabAdapter.getCount(); i++) {
            tabHost.addTab(
                    tabHost.newTab()
                            .setIcon(getActivity().getResources().getDrawable(icon[i]))
                            .setTabListener(this)
            );
        }
        return rootView;
    }

    @Override
    public void onTabSelected(MaterialTab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(MaterialTab tab) {

    }

    @Override
    public void onTabUnselected(MaterialTab tab) {

    }
}
