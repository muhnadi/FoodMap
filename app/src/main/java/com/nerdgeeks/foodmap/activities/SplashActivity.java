package com.nerdgeeks.foodmap.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.nerdgeeks.foodmap.R;

public class SplashActivity extends Activity {

    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //adding custom font
        final Typeface ThemeFont = Typeface.createFromAsset(getAssets(), "fonts/HelveticaNeue.ttf");

        View mSplashImage = findViewById(R.id.splash);
        TextView mSplashText = (TextView) findViewById(R.id.splashText);
        mSplashText.setTypeface(ThemeFont);
        Animation splashAnimImage = AnimationUtils.loadAnimation(this, R.anim.splash_anim_img);
        Animation splashAnimText = AnimationUtils.loadAnimation(this, R.anim.splash_anim);
        mSplashText.startAnimation(splashAnimText);
        mSplashImage.startAnimation(splashAnimImage);

        int SPLASH_DISPLAY_LENGTH = 700;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MapsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                finish();
                launchInterstitial();
                loadInterstitial();

            }
        }, SPLASH_DISPLAY_LENGTH);
    }


    public void launchInterstitial() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.Interstitial_test));

        //Set the adListener
        interstitialAd.setAdListener(new AdListener() {


            public void onAdLoaded() {
                showAdInter();
            }

            public void onAdFailedToLoad(int errorCode) {
                String message = String.format("onAdFailedToLoad(%s)", getErrorReason(errorCode));

            }

            @Override
            public void onAdClosed() {

            }
        });

    }

    private void showAdInter() {

        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            Log.d("", "ad was not ready to shown");
        }
    }

    public void loadInterstitial() {

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        //Load this Interstitial ad
        interstitialAd.loadAd(adRequest);
    }

    //Get a string error
    private String getErrorReason(int errorCode) {

        String errorReason = "";
        switch (errorCode) {
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                errorReason = "Internal Error";
                break;
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                errorReason = "Invalid Request";
                break;
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                errorReason = "Network Error";
                break;
            case AdRequest.ERROR_CODE_NO_FILL:
                errorReason = "No Fill";
                break;
        }
        return errorReason;
    }

}
