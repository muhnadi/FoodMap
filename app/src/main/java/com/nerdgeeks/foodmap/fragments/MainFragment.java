package com.nerdgeeks.foodmap.fragments;

import android.Manifest;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.nerdgeeks.foodmap.R;
import com.nerdgeeks.foodmap.activities.MapsActivity;
import com.nerdgeeks.foodmap.adapter.PlaceAutocompleteAdapter;
import com.nerdgeeks.foodmap.app.AppData;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by TAOHID on 3/19/2018.
 */

public class MainFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks {

    private String NearbyFragmentTag = "MAP";
    private String ResultFragmentTag = "RESULT";
    private FragmentTransaction mTransaction;


    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));
    private CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    public static PlaceAutocompleteAdapter placesAutoCompleteAdapter;

    private FusedLocationProviderClient mFusedLocationClient;
    GoogleApiClient client;
    LocationRequest mLocationRequest;
    PendingResult<LocationSettingsResult> result;
    public final static int REQUEST_LOCATION = 199;
    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    private Context mContext;
    int bottomNavigationState = 1;
    public static ProgressDialog pDialog;
    private static final int LOCATION_PERMISSION_ID = 1001;

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

    // Initialise it from onAttach()
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
        buildGoogleApiClient();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
    }

    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .build();
        client.connect();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        pDialog = new ProgressDialog(mContext);
        pDialog.setMessage("Loading ");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.setCanceledOnTouchOutside(false);

        AdView mAdView = rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                mAdView.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
            }
        });

        BottomNavigationView navigation = rootView.findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        return rootView;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
            return;
        }
        askForGPS();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_ID && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            askForGPS();
        }
    }

    private void askForGPS(){

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        result = LocationServices.SettingsApi.checkLocationSettings(client, builder.build());
        result.setResultCallback(result -> {
            final Status status = result.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    loadFirstTime();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        status.startResolutionForResult(getActivity(), REQUEST_LOCATION);
                    } catch (IntentSender.SendIntentException e) {}
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    break;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
// Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        loadFirstTime();
                        break;
                    case Activity.RESULT_CANCELED:
                        askForGPS();
                        break;
                }
                break;
        }
    }


    private void loadFirstTime() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
            return;
        }

        pDialog.show();
        if (AppData.longitude == null && AppData.longitude == null) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        } else {
            // Default show nearby map fragment
            loadFragment();
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            Location mCurrentLocation = locationResult.getLastLocation();
            AppData.lattitude = AppData.currentLattitude = mCurrentLocation.getLatitude();
            AppData.longitude = AppData.currentLongitude =  mCurrentLocation.getLongitude();

            // Default show nearby map fragment
            loadFragment();

            if (mFusedLocationClient != null) {
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            }
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment nearbyFragment = getChildFragmentManager().findFragmentByTag(NearbyFragmentTag);
            Fragment resultFragment = getChildFragmentManager().findFragmentByTag(ResultFragmentTag);
            mTransaction = getChildFragmentManager().beginTransaction();

            switch (item.getItemId()) {
                case R.id.map:

                    if (bottomNavigationState == 0){
                        mTransaction.hide(resultFragment);
                        mTransaction.show(nearbyFragment);
                        mTransaction.commit();
                    }

                    bottomNavigationState = 1;
                    return true;
                case R.id.list:

                    if (bottomNavigationState == 1){
                        Fragment mFragment = ResultFragment.newInstance(mParam1);
                        mTransaction.hide(nearbyFragment);
                        if (resultFragment != null){
                            mTransaction.show(resultFragment);
                        } else {
                            mTransaction.add(R.id.frame_container, mFragment,ResultFragmentTag);
                            mTransaction.addToBackStack(null);
                        }
                        mTransaction.commit();
                    }

                    bottomNavigationState = 0;
                    return true;
            }
            return false;
        }
    };

    private void loadFragment() {
        setHasOptionsMenu(true);
        Fragment mFragment = NearbyFragment.newInstance(mParam1);
        mTransaction = getChildFragmentManager().beginTransaction();
        mTransaction.add(R.id.frame_container, mFragment,NearbyFragmentTag);
        mTransaction.addToBackStack(null);
        mTransaction.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkLocationPermission();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.options, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        SearchView.SearchAutoComplete searchAutoComplete = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setTextColor(Color.BLACK);
        SearchManager searchManager = (SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        AutocompleteFilter autocompleteFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(Place.TYPE_COUNTRY)
                .setCountry(getCountryCode())
                .build();
        placesAutoCompleteAdapter = new PlaceAutocompleteAdapter(mContext, client,
                LAT_LNG_BOUNDS, autocompleteFilter);
        searchAutoComplete.setAdapter(placesAutoCompleteAdapter);

        searchAutoComplete.setOnItemClickListener((adapterView, view, itemIndex, id) -> {
            try {
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
            }
            final AutocompletePrediction place = placesAutoCompleteAdapter.getItem(itemIndex);
            assert place != null;
            final String placeName = String.valueOf(place.getFullText(STYLE_BOLD));
            searchAutoComplete.setText("" + placeName);
            MapsActivity.drawer.updateName(7, new StringHolder(placeName));
            MapsActivity.drawer.updateName(8,new StringHolder("Remove Location"));
            geoLocate(place.getPlaceId());
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                final AutocompletePrediction place = placesAutoCompleteAdapter.getItem(0);
                if (place != null) {
                    final String placeName = String.valueOf(place.getFullText(STYLE_BOLD));
                    searchAutoComplete.setText("" + placeName);
                    MapsActivity.drawer.updateName(7, new StringHolder(placeName));
                    MapsActivity.drawer.updateName(8, new StringHolder("Remove Location"));
                    geoLocate(place.getPlaceId());
                } else {
                    Toast.makeText(mContext, "Google can't find this place", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private String getCountryCode(){
        String countryCode;
        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(mContext, Locale.getDefault());
            addresses = geocoder.getFromLocation(AppData.lattitude, AppData.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            countryCode = addresses.get(0).getCountryCode();
        } catch (IOException e) {
            e.printStackTrace();
            countryCode = "";
        }
        return countryCode;
    }

    private void geoLocate(String placeId){

        Places.GeoDataApi.getPlaceById(client, placeId)
                .setResultCallback(places -> {
                    if (places.getStatus().isSuccess()) {
                        final Place myPlace = places.get(0);
                        LatLng queriedLocation = myPlace.getLatLng();
                        AppData.lattitude = queriedLocation.latitude;
                        AppData.longitude = queriedLocation.longitude;

                        refreshData();
                    }
                    places.release();
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_search:
                return true;
            case R.id.refresh:
                refreshData();
                return true;
            case R.id.cached:
                deleteCache(mContext);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshData(){
        // clear the fragment manager stack
        getChildFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        bottomNavigationState = 1;
        AppData.placeModels.clear();
        askForGPS();
    }

    public void deleteCache(Context context) {

        try {
            File cacheDirectory = context.getCacheDir();
            File applicationDirectory = new File(cacheDirectory.getParent());
            if (applicationDirectory.exists()) {
                String[] fileNames = applicationDirectory.list();
                for (String fileName : fileNames) {
                    if (!fileName.equals("lib")) {
                        deleteFile(new File(applicationDirectory, fileName));
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public boolean deleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (String aChildren : children) {
                    deletedAll = deleteFile(new File(file, aChildren)) && deletedAll;
                }
            } else {
                deletedAll = file.delete();
            }
        }
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("foodmap", 0);
        sharedPreferences.edit().clear().apply();
        return deletedAll;
    }

}
