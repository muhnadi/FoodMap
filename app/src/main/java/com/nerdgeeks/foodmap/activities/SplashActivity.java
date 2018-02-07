package com.nerdgeeks.foodmap.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.facebook.ads.AbstractAdListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.nerdgeeks.foodmap.R;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SplashActivity extends Activity {

    public static InterstitialAd interstitialAd;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/HelveticaNeue.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_splash);

        interstitialAd = new InterstitialAd(this, "706320389533993_969561163209913");
        interstitialAd.setAdListener(new AbstractAdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                super.onError(ad, adError);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                super.onAdLoaded(ad);
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                super.onInterstitialDismissed(ad);
                interstitialAd.loadAd();
            }
        });
        interstitialAd.loadAd();

        View mSplashImage = findViewById(R.id.splash);
        TextView mSplashText = findViewById(R.id.splashText);
        Animation splashAnimImage = AnimationUtils.loadAnimation(this, R.anim.splash_anim_img);
        Animation splashAnimText = AnimationUtils.loadAnimation(this, R.anim.splash_anim);
        mSplashText.startAnimation(splashAnimText);
        mSplashImage.startAnimation(splashAnimImage);

        int SPLASH_DISPLAY_LENGTH = 1000;
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MapsActivity.class));
            finish();

        }, SPLASH_DISPLAY_LENGTH);
    }

}
