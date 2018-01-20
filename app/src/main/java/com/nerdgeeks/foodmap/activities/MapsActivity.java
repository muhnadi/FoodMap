package com.nerdgeeks.foodmap.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.nerdgeeks.foodmap.app.AppController;
import com.nerdgeeks.foodmap.R;
import com.nerdgeeks.foodmap.fragments.MainFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.nerdgeeks.foodmap.helper.ConnectivityReceiver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.nerdgeeks.foodmap.app.AppConfig.*;

public class MapsActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ConnectivityReceiver.ConnectivityReceiverListener,
        ConnectivityReceiver.GpsStatusReceiverListener,LocationListener {

    private FragmentTransaction fragmentTransaction;
    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences myPref;
    private boolean isConnected, isGpsEnabled;
    private Drawer drawer;
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 10000;
    private static final float LOCATION_DISTANCE = 10f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isGooglePlayServicesAvailable()) {
            return;
        }
        setContentView(R.layout.activity_maps);

        //Google Analytics
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        Tracker tracker = analytics.newTracker("UA-72883943-9");
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);

        //Google Ads
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

        //Accessing SharedPreference
        myPref = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        boolean isContain = myPref.contains("address");

        String address = "GPS is disabled";
        if (isContain){
            address = myPref.getString("address","");
        }

        //adding custom font
        final Typeface ThemeFont = Typeface.createFromAsset(getAssets(),"fonts/HelveticaNeue.ttf");

        isConnected = ConnectivityReceiver.isConnected();
        isGpsEnabled = ConnectivityReceiver.isGPSConnected();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //adding navigation header
        View view = View.inflate(this,R.layout.nav_header,null);
        TextView navText = (TextView) view.findViewById(R.id.nav_text);
        navText.setTypeface(ThemeFont);

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
                                .withIdentifier(1)
                                .withTypeface(ThemeFont),
                        new PrimaryDrawerItem()
                                .withName("Bar")
                                .withIcon(R.drawable.ic_local_bar)
                                .withIdentifier(2)
                                .withTypeface(ThemeFont),
                        new PrimaryDrawerItem()
                                .withName("Cafe")
                                .withIcon(R.drawable.ic_local_cafe)
                                .withIdentifier(3)
                                .withTypeface(ThemeFont),
                        new PrimaryDrawerItem()
                                .withName("Grocery Store")
                                .withIcon(R.drawable.ic_local_grocery_store)
                                .withIdentifier(4)
                                .withTypeface(ThemeFont),
                        new SectionDrawerItem()
                                .withName("MORE")
                                .withTypeface(ThemeFont),
                        new PrimaryDrawerItem()
                                .withName("Rate Me")
                                .withIcon(R.drawable.ic_rate_review)
                                .withIdentifier(5)
                                .withTypeface(ThemeFont),
                        new PrimaryDrawerItem()
                                .withName("About")
                                .withIcon(R.drawable.ic_info)
                                .withIdentifier(6)
                                .withTypeface(ThemeFont),
                        new SectionDrawerItem()
                                .withName("Last Location")
                                .withTypeface(ThemeFont),
                        new PrimaryDrawerItem()
                                .withName(address)
                                .withIcon(R.drawable.ic_my_location)
                                .withIdentifier(7)
                                .withTypeface(ThemeFont)

                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null) {
                            Fragment fragment;
                            if (drawerItem.getIdentifier() == 1) {
                                toolbar.setSubtitle("Restaurants");
                                fragment = MainFragment
                                        .newInstance("restaurant");
                                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                                fragmentTransaction.setCustomAnimations(R.anim.anim_enter, R.anim.anim_leave);
                                fragmentTransaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
                            } else if (drawerItem.getIdentifier() == 2) {
                                toolbar.setSubtitle("Bars");
                                fragment = MainFragment
                                        .newInstance("bar");
                                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                                fragmentTransaction.setCustomAnimations(R.anim.anim_enter, R.anim.anim_leave);
                                fragmentTransaction.replace(R.id.fragment_container, fragment).commit();
                            } else if (drawerItem.getIdentifier() == 3) {
                                toolbar.setSubtitle("Cafe");
                                fragment = MainFragment
                                        .newInstance("cafe");
                                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                                fragmentTransaction.setCustomAnimations(R.anim.anim_enter, R.anim.anim_leave);
                                fragmentTransaction.replace(R.id.fragment_container, fragment).commit();
                            } else if (drawerItem.getIdentifier() == 4) {
                                toolbar.setSubtitle("Stores");
                                fragment = MainFragment.newInstance("grocery_or_supermarket");
                                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                                fragmentTransaction.setCustomAnimations(R.anim.anim_enter, R.anim.anim_leave);
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
                                        .typeface(ThemeFont, ThemeFont)
                                        .positiveText("MORE APPS")
                                        .typeface(ThemeFont, ThemeFont)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                Uri uri = Uri.parse("market://search?q=pub:" + "NerdGeeks");
                                                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                                                try {
                                                    startActivity(goToMarket);
                                                } catch (ActivityNotFoundException e) {
                                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                                            Uri.parse("http://play.google.com/store/search?q=pub:" + "NerdGeeks")));
                                                }
                                            }
                                        })
                                        .show();
                            }
                        }
                        return false;
                    }
                })
                .withSelectedItem(1)
                .build();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        settingsRequest();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AppController.getInstance().setConnectivityListener(this);
        AppController.getInstance().setPermissionListener(this);
    }

    @Override
    protected void onDestroy() {
        if (mGoogleApiClient != null){
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    public void settingsRequest()
    {
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000); //5 seconds
        locationRequest.setFastestInterval(1000); //3 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        getLocationPermission();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    private void getLocationPermission(){
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION))
                    && (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) && (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.CALL_PHONE))) {

            } else {
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.CALL_PHONE
                        },
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getLocationService();
                    drawer.setSelection(1, true);
                }
            }, 700);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getLocationService();
                            drawer.setSelection(1, true);
                        }
                    }, 700);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getLocationPermission();
                        break;
                    case Activity.RESULT_CANCELED:
                        //keep asking if imp or do whatever
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                drawer.setSelection(1, true);
                            }
                        });
                        break;
                }
                break;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                overridePendingTransition(R.anim.anim_enter, R.anim.anim_leave);
                startActivity(intent);
            }
        }, 700);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect(i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        this.isConnected = isConnected;
    }

    @Override
    public void onGpsConnectionChanged(boolean isGPSEnabled) {
        this.isGpsEnabled = isGPSEnabled;
        if (isGPSEnabled){
            getLocationService();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        getCurrentAddress(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void getLocationService(){
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(!isGPSEnabled && !isNetworkEnabled){

            } else {
                boolean canGetLocation = true;

                if (isGPSEnabled){
                    mLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                            this);

                    if (mLocationManager != null){
                        Location mLastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if(mLastLocation != null){
                            getCurrentAddress(mLastLocation);
                        }
                    }
                }

                if (isNetworkEnabled){
                    mLocationManager.requestLocationUpdates(
                            LocationManager.PASSIVE_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                            this);

                    if (mLocationManager!= null){
                        Location mLastLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if(mLastLocation!=null){
                            getCurrentAddress(mLastLocation);
                        }
                    }
                }
            }

        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
    }

    private void getCurrentAddress(Location location){
        String errorMessage = "";
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Address found using the GeoCoder.
        List<Address> addresses = null;

        try {
            // Using getFromLocation() returns an array of Addresses for the area immediately
            // surrounding the given latitude and longitude. The results are a best guess and are
            // not guaranteed to be accurate.
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, we get just a single address.
                    5);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " + location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, getString(R.string.address_found));

            SharedPreferences.Editor edit = myPref.edit();
            edit.putString("address", TextUtils.join(System.getProperty("line.separator"), addressFragments));
            edit.putString("lat", String.valueOf(lat));
            edit.putString("lng", String.valueOf(lng));
            edit.apply();
        }
    }
}