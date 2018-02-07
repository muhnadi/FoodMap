package com.nerdgeeks.foodmap.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.nerdgeeks.foodmap.R;
import com.nerdgeeks.foodmap.fragments.MainFragment;
import com.nerdgeeks.foodmap.helper.ConnectivityReceiver;
import com.nerdgeeks.foodmap.utils.InterstitialAdsHelper;
import java.io.File;
import java.util.Random;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MapsActivity extends AppCompatActivity {

    private Fragment fragment;
    private FragmentTransaction fragmentTransaction;
    private Toolbar toolbar;
    private boolean isConnected, isGpsEnabled;
    private int resumeCount;
    int adsDelay = 30000;
    private InterstitialAdsHelper interstitialAdsHelper;
    Drawer drawer;


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
        setContentView(R.layout.activity_maps);

        interstitialAdsHelper = new InterstitialAdsHelper(this);

        new Handler().postDelayed(this::showRandomInterstitialAds, adsDelay);

        //Google Analytics
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        Tracker tracker = analytics.newTracker("UA-72883943-9");
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);

        isConnected = ConnectivityReceiver.isConnected();
        isGpsEnabled = ConnectivityReceiver.isGPSConnected();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //adding navigation header
        View view = View.inflate(this,R.layout.nav_header,null);
        TextView navText = view.findViewById(R.id.nav_text);

        //Adding navigation drawer
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withTranslucentStatusBar(true)
                .withHeader(view)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName("Restaurant")
                                .withIcon(R.drawable.ic_restaurant)
                                .withIdentifier(1),
                        new PrimaryDrawerItem()
                                .withName("Bar")
                                .withIcon(R.drawable.ic_local_bar)
                                .withIdentifier(2),
                        new PrimaryDrawerItem()
                                .withName("Cafe")
                                .withIcon(R.drawable.ic_local_cafe)
                                .withIdentifier(3),
                        new PrimaryDrawerItem()
                                .withName("Grocery Store")
                                .withIcon(R.drawable.ic_local_grocery_store)
                                .withIdentifier(4),
                        new SectionDrawerItem()
                                .withName("MORE"),
                        new PrimaryDrawerItem()
                                .withName("Rate Me")
                                .withIcon(R.drawable.ic_rate_review)
                                .withIdentifier(5),
                        new PrimaryDrawerItem()
                                .withName("About")
                                .withIcon(R.drawable.ic_info)
                                .withIdentifier(6)

                )
                .withOnDrawerItemClickListener((view1, position, drawerItem) -> {
                    if (drawerItem != null) {

                        if (drawerItem.getIdentifier() == 1) {
                            toolbar.setSubtitle("Restaurants");
                            fragment = MainFragment
                                    .newInstance("restaurant");
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
                        } else if (drawerItem.getIdentifier() == 2) {
                            toolbar.setSubtitle("Bars");
                            fragment = MainFragment
                                    .newInstance("bar");
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, fragment).commit();
                        } else if (drawerItem.getIdentifier() == 3) {
                            toolbar.setSubtitle("Cafe");
                            fragment = MainFragment
                                    .newInstance("cafe");
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, fragment).commit();
                        } else if (drawerItem.getIdentifier() == 4) {
                            toolbar.setSubtitle("Stores");
                            fragment = MainFragment.newInstance("grocery_or_supermarket");
                            fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container, fragment).commit();
                        } else if (drawerItem.getIdentifier() == 5) {
                            Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
                            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                            // To count with Play market backstack, After pressing back button,
                            // to taken back to our application, we need to add following flags to intent.
                            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                            try {
                                startActivity(goToMarket);
                            } catch (ActivityNotFoundException e) {
                                startActivity(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
                            }
                        } else if (drawerItem.getIdentifier() == 6) {
                            new MaterialDialog.Builder(MapsActivity.this)
                                    .title("About Developer")
                                    .customView(R.layout.about, true)
                                    .positiveText("MORE APPS")
                                    .onPositive((dialog, which) -> {
                                        Uri uri = Uri.parse("market://search?q=pub:" + "NerdGeeks");
                                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                                        try {
                                            startActivity(goToMarket);
                                        } catch (ActivityNotFoundException e) {
                                            startActivity(new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("http://play.google.com/store/search?q=pub:" + "NerdGeeks")));
                                        }
                                    })
                                    .show();
                        }
                    }
                    return false;
                })
                .build();
        drawer.setSelection(1,true);
    }

    private void showQuitPopup(){

        MaterialDialog.Builder builder =  new MaterialDialog.Builder(MapsActivity.this)
                .canceledOnTouchOutside(false)
                .title("Are you sure you want to exit?")
                .customView(R.layout.exit_dialog, true)
                .negativeText("BACK")
                .onNegative((dialog, which) -> {})
                .positiveText("EXIT")
                .onPositive((dialog, which) -> {
                    finish();
                    System.exit(1);
                });

        MaterialDialog dialog = builder.build();
        LinearLayout view = (LinearLayout) dialog.findViewById(R.id.adViewContainer);
        AdView adView = new AdView(MapsActivity.this, "706320389533993_969293736569989", com.facebook.ads.AdSize.RECTANGLE_HEIGHT_250);
        view.addView(adView);
        adView.loadAd();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        showQuitPopup();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeCount++;
        if (resumeCount % 6 == 0){
            showRandomInterstitialAds();
        }
    }

    private void showRandomInterstitialAds(){
        Random random = new Random();
        int num = random.nextInt(2);
        if (num == 1){
            interstitialAdsHelper.showAds();
        } else {
            SplashActivity.interstitialAd.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.refresh:
                startRefresh();
                return true;
            case R.id.cached:
                deleteCache(this);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else
            return dir != null && dir.isFile() && dir.delete();
    }

    private void startRefresh() {
        new Handler().postDelayed(() -> {
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            startActivity(intent);
        }, 700);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainFragment.REQUEST_LOCATION){
            fragment.onActivityResult(requestCode, resultCode, data);
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}