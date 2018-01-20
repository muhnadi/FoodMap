package com.nerdgeeks.foodmap.activities;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nerdgeeks.foodmap.R;
import com.nerdgeeks.foodmap.adapter.ViewPagerAdapter;
import com.nerdgeeks.foodmap.model.PhotoModel;

import java.util.ArrayList;

public class FullImageActivity extends AppCompatActivity {

    private int vPosition;
    private int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

        vPosition = getIntent().getIntExtra("pos", 0);
        pos = getIntent().getIntExtra("position", 0);
        ArrayList<PhotoModel> mList = (ArrayList<PhotoModel>) getIntent().getSerializableExtra("arrayList");

        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new ViewPagerAdapter(getApplicationContext(), mList));
        mViewPager.setCurrentItem(vPosition);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                vPosition = position;
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_enter, R.anim.anim_leave);
        finish();
    }
}
