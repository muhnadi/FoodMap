package com.nerdgeeks.foodmap.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import com.nerdgeeks.foodmap.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by IMRAN on 6/20/2017.
 */

public class MapService extends Service implements LocationListener{

    private static final String TAG = "MapService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 10000;
    private static final float LOCATION_DISTANCE = 0f;
    private SharedPreferences myPref;

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Wait, wut?");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");

        //Accessing SharedPreference
        myPref = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        initializeLocationManager();

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

                    if (mLocationManager!= null){
                        Location mLastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(mLastLocation!=null){
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
            ArrayList<String> addressFragments = new ArrayList<String>();

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

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(this);
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listners, ignore", ex);
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
