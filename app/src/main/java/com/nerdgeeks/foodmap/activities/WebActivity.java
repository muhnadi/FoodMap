package com.nerdgeeks.foodmap.activities;

import android.app.ProgressDialog;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.nerdgeeks.foodmap.R;

public class WebActivity extends AppCompatActivity {

    private WebView browser;
    private ProgressDialog progressDialog;
    InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

        launchInter();
        loadInterstitial();

        //get String by Data Passing
        String URL = getIntent().getStringExtra("url");

        //Initializing WebView
        browser = (WebView) findViewById(R.id.mWebView);
        browser.getSettings().setJavaScriptEnabled(true);

        //Load URL on WebView
        startWebView(URL);

        //Adding Fab Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog = new ProgressDialog(WebActivity.this);
                        progressDialog.setMessage("Loading...");
                        progressDialog.setIndeterminate(false);
                        progressDialog.show();
                        progressDialog.setCanceledOnTouchOutside(false);
                        browser.reload();
                    }
                }, 700);
                progressDialog.dismiss();
            }
        });

    }

    private void startWebView(String url) {
        browser.setWebViewClient(new WebViewClient() {
            //If you will not use this method url links are opeen in new brower not in webview
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            //Show loader on url load
            public void onLoadResource (WebView view, String url) {
                if (progressDialog == null) {
                    // in standard case YourActivity.this
                    progressDialog = new ProgressDialog(WebActivity.this);
                    progressDialog.setMessage("Loading...");
                    progressDialog.setIndeterminate(false);
                    progressDialog.show();
                    progressDialog.setCanceledOnTouchOutside(false);
                }
            }
            public void onPageFinished(WebView view, String url) {
                try{
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }catch(Exception exception){
                    exception.printStackTrace();
                }
            }

        });
        browser.loadUrl(url);
    }

    @Override
    // Detect when the back button is pressed
    public void onBackPressed() {
        if(browser.canGoBack()) {
            browser.goBack();
        } else {
            // Let the system handle the back button
            super.onBackPressed();
            overridePendingTransition(R.anim.anim_enter,R.anim.anim_leave);
            finish();
        }
    }

    public void launchInter(){
        interstitialAd =new InterstitialAd(this);
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

    private void showAdInter(){

        if(interstitialAd.isLoaded()){
            interstitialAd.show();
        }
        else{
            Log.d("", "ad was not ready to shown");
        }
    }

    public void loadInterstitial(){

        AdRequest adRequest= new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        //Load this Interstitial ad
        interstitialAd.loadAd(adRequest);
    }

    //Get a string error
    private String getErrorReason(int errorCode){

        String errorReason="";
        switch(errorCode){
            case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                errorReason="Internal Error";
                break;
            case AdRequest.ERROR_CODE_INVALID_REQUEST:
                errorReason="Invalid Request";
                break;
            case AdRequest.ERROR_CODE_NETWORK_ERROR:
                errorReason="Network Error";
                break;
            case AdRequest.ERROR_CODE_NO_FILL:
                errorReason="No Fill";
                break;
        }
        return errorReason;
    }
}
