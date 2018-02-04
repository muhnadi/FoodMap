package com.nerdgeeks.foodmap.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

public class MainFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private String mParam1;

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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        BottomNavigationView navigation = rootView.findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        loadFragment(NearbyFragment.newInstance(mParam1));
        return rootView;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment mFragment;
            switch (item.getItemId()) {
                case R.id.map:
                    mFragment = NearbyFragment.newInstance(mParam1);
                    loadFragment(mFragment);
                    return true;

                case R.id.list:
                    mFragment = ResultFragment.newInstance(mParam1);
                    loadFragment(mFragment);
                    return true;
            }
            return false;
        }
    };

    private void loadFragment(Fragment mFragment){
        FragmentTransaction mTransaction = getChildFragmentManager().beginTransaction();
        mTransaction.replace(R.id.frame_container, mFragment);
        mTransaction.addToBackStack(null);
        mTransaction.commit();
    }
}
