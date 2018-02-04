package com.nerdgeeks.foodmap.fragments;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nerdgeeks.foodmap.app.AppController;
import com.nerdgeeks.foodmap.R;
import com.nerdgeeks.foodmap.activities.InfoActivity;
import com.nerdgeeks.foodmap.app.PrefManager;
import com.nerdgeeks.foodmap.helper.ConnectivityReceiver;
import com.nerdgeeks.foodmap.service.MapService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.util.ArrayList;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geofencing.utils.TransitionGeofence;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;

import static com.nerdgeeks.foodmap.app.AppConfig.GEOMETRY;
import static com.nerdgeeks.foodmap.app.AppConfig.GOOGLE_MAP_API_KEY;
import static com.nerdgeeks.foodmap.app.AppConfig.GPS_ERROR;
import static com.nerdgeeks.foodmap.app.AppConfig.INTERNET_ERROR;
import static com.nerdgeeks.foodmap.app.AppConfig.LATITUDE;
import static com.nerdgeeks.foodmap.app.AppConfig.LOCATION;
import static com.nerdgeeks.foodmap.app.AppConfig.LONGITUDE;
import static com.nerdgeeks.foodmap.app.AppConfig.MAP_UPDATED;
import static com.nerdgeeks.foodmap.app.AppConfig.NAME;
import static com.nerdgeeks.foodmap.app.AppConfig.NEXT_PAGE_TOKEN;
import static com.nerdgeeks.foodmap.app.AppConfig.NO_RESULTS;
import static com.nerdgeeks.foodmap.app.AppConfig.OK;
import static com.nerdgeeks.foodmap.app.AppConfig.PLACE_ID;
import static com.nerdgeeks.foodmap.app.AppConfig.RATE;
import static com.nerdgeeks.foodmap.app.AppConfig.RESULTS;
import static com.nerdgeeks.foodmap.app.AppConfig.STATUS;
import static com.nerdgeeks.foodmap.app.AppConfig.TAG;
import static com.nerdgeeks.foodmap.app.AppConfig.VICINITY;
import static com.nerdgeeks.foodmap.app.AppConfig.ZERO_RESULTS;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NearbyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NearbyFragment extends Fragment implements OnMapReadyCallback,
        ConnectivityReceiver.ConnectivityReceiverListener, ConnectivityReceiver.GpsStatusReceiverListener,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.InfoWindowAdapter,
        OnLocationUpdatedListener, OnGeofencingTransitionListener {

    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    private GoogleMap mMap;
    private ProgressDialog pDialog;
    private String nextPageToken;
    private PrefManager prefManager;
    private boolean isConnected, isGPSEnabled;
    private SharedPreferences myPref;
    private double lat, lng;
    private String mAddressOutput;
    private boolean isContain;
    private View snackView;
    private Typeface ThemeFont;

    private LocationGooglePlayServicesProvider provider;
    private static final int LOCATION_PERMISSION_ID = 1001;
    private Location lastLocation;
    private ArrayList<String> placeId = new ArrayList<>();
    private Bitmap smallMarker;
    private SmartLocation smartLocation;


    public NearbyFragment() {
        // Required empty public constructor
    }

    public static NearbyFragment newInstance(String param1) {
        NearbyFragment fragment = new NearbyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    private Context mContext;

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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_nearby, container, false);

        snackView = rootView.findViewById(R.id.fragment_nearby);
        prefManager = new PrefManager(getContext());
        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Loading ");
        pDialog.setIndeterminate(false);
        pDialog.setCanceledOnTouchOutside(false);

        isConnected = ConnectivityReceiver.isConnected();
        isGPSEnabled = ConnectivityReceiver.isGPSConnected();

        myPref = mContext.getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        isContain = myPref.contains("lat");

        if (isContain){
            lat = Double.parseDouble(myPref.getString("lat",""));
            lng = Double.parseDouble(myPref.getString("lng",""));
            mAddressOutput = myPref.getString("address","");
        }

        if (mMap == null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(NearbyFragment.this);
                }
            });
        }

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        int height = 72;
        int width = 72;
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.ic_marker);
        Bitmap b = bitmapdraw.getBitmap();
        smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
        } else {
            startLocation();
        }
    }

    private void startLocation() {

        long mLocTrackingInterval = 1000 * 5; // 5 sec
        float trackingDistance = 0;
        LocationAccuracy trackingAccuracy = LocationAccuracy.HIGH;

        LocationParams.Builder builder = new LocationParams.Builder()
                .setAccuracy(trackingAccuracy)
                .setDistance(trackingDistance)
                .setInterval(mLocTrackingInterval);

        provider = new LocationGooglePlayServicesProvider();
        provider.setCheckLocationSettings(true);
        smartLocation = new SmartLocation.Builder(mContext).logging(true).build();
        smartLocation.location(provider).continuous().config(builder.build()).start(this);

        showLast();
    }

    private void showLast() {
        lastLocation = SmartLocation.with(getContext()).location().getLastLocation();
        if (lastLocation != null) {
            try{
                if (isConnected ){
                    buildMapUI(mMap, true);
                } else {
                    if (prefManager.isPrefAvailable()){
                        buildMapUI(mMap, false);
                    } else {
                        showSnackMessage(INTERNET_ERROR);
                    }
                }
            } catch (Exception ex){
                showSnackMessage(ex.getMessage());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_ID && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocation();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (provider != null) {
            provider.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        smartLocation.location().stop();
        SmartLocation.with(mContext).location(provider).stop();
        SmartLocation.with(mContext).geocoding().stop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // register connection status listener
        AppController.getInstance().setConnectivityListener(this);
        AppController.getInstance().setPermissionListener(this);
    }

    private void buildMapUI(GoogleMap map, boolean isNetwork){
        //zoom to current position:
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude())).zoom(17).build();

        map.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
        map.setInfoWindowAdapter(NearbyFragment.this);

        if (isNetwork){
            map.setOnInfoWindowClickListener(this);
            loadNearByPlaces(lastLocation.getLatitude(), lastLocation.getLongitude(), mParam1);
        } else {
            for (int i=0; i<prefManager.readData().size(); i++){
                MarkerOptions markerOptions = new MarkerOptions();
                LatLng latLng = new LatLng(prefManager.readData().get(i).getLatitude(), prefManager.readData().get(i).getLongitude());
                markerOptions.position(latLng);
                markerOptions.title(prefManager.readData().get(i).getResName());
                markerOptions.snippet(prefManager.readData().get(i).getResVicnity() + "\nRatings : " + prefManager.readData().get(i).getResRating());

                Marker marker = mMap.addMarker(markerOptions);
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                marker.showInfoWindow();
            }
        }
    }

    private void showSnackMessage(String message) {
        int color = Color.RED;
        int TIME_OUT = Snackbar.LENGTH_LONG;

        Snackbar snackbar = Snackbar
                .make(snackView, message, TIME_OUT);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    public void loadNearByPlaces(final double Latitude, final double Longitude, String type) {

        pDialog.show();
        final String googlePlacesUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" + "location="
                + Latitude
                + ","
                + Longitude +
                "&rankby=distance" +
                "&type=" + type +
                "&sensor=false" +
                "&key=" + GOOGLE_MAP_API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(googlePlacesUrl,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject result) {
                        Log.i(TAG, "onResponse: Result= " + result.toString());

                        String id, place_id, placeName = null, icon, vicinity = null, open, rating=null;
                        double latitude, longitude;

                        try {
                            if (!result.isNull(NEXT_PAGE_TOKEN)) {
                                nextPageToken = result.getString(NEXT_PAGE_TOKEN);
                            }

                            JSONArray jsonArray = result.getJSONArray(RESULTS);

                            if (result.getString(STATUS).equalsIgnoreCase(OK)) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject place = jsonArray.getJSONObject(i);
                                    //id = place.getString(ID);
                                    place_id = place.getString(PLACE_ID);
                                    placeId.add(place_id);

                                    if (!place.isNull(NAME)) {
                                        placeName = place.getString(NAME);
                                    }
                                    if (!place.isNull(VICINITY)) {
                                        vicinity = place.getString(VICINITY);
                                    }

                                    if (!place.isNull(RATE)){
                                        rating = place.getString(RATE);
                                    }

                                    latitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                                            .getDouble(LATITUDE);
                                    longitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                                            .getDouble(LONGITUDE);

                                    MarkerOptions markerOptions = new MarkerOptions();
                                    LatLng latLng = new LatLng(latitude, longitude);
                                    markerOptions.position(latLng);
                                    markerOptions.title(placeName);
                                    markerOptions.snippet(vicinity + "\nRatings : " + rating);

                                    mMap.setInfoWindowAdapter(NearbyFragment.this);

                                    Marker marker = mMap.addMarker(markerOptions);
                                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                                    marker.showInfoWindow();
                                }
                                pDialog.dismiss();
                                if (nextPageToken != null) {
                                    nextPageLoad(nextPageToken);
                                    showSnackMessage(MAP_UPDATED);
                                }
                            } else if (result.getString(STATUS).equalsIgnoreCase(ZERO_RESULTS)) {
                                pDialog.dismiss();
                                showSnackMessage(NO_RESULTS);
                            }
                        } catch (JSONException e) {
                            pDialog.dismiss();
                            showSnackMessage(e.getMessage());
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.dismiss();
                        showSnackMessage(error.getMessage());
                    }
                });

        AppController.getInstance().addToRequestQueue(request);
    }

    private void nextPageLoad(String Token) {
        final String googlePlacesUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "&pagetoken=" + Token +
                "&key=" + GOOGLE_MAP_API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(googlePlacesUrl,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject result) {
                        Log.i(TAG, "onResponse: Result= " + result.toString());

                        String placeName = null, place_id, vicinity = null, rating = null;
                        double latitude, longitude;

                        try {
                            if (!result.isNull(NEXT_PAGE_TOKEN)) {
                                nextPageToken = result.getString(NEXT_PAGE_TOKEN);
                            }
                            JSONArray jsonArray = result.getJSONArray(RESULTS);
                            if (result.getString(STATUS).equalsIgnoreCase(OK)) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject place = jsonArray.getJSONObject(i);

                                    if (!place.isNull(NAME)) {
                                        placeName = place.getString(NAME);
                                    }

                                    place_id = place.getString(PLACE_ID);
                                    placeId.add(place_id);

                                    if (!place.isNull(VICINITY)) {
                                        vicinity = place.getString(VICINITY);
                                    }
                                    if (!place.isNull(RATE)){
                                        rating = place.getString(RATE);
                                    }

                                    latitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                                            .getDouble(LATITUDE);
                                    longitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                                            .getDouble(LONGITUDE);

                                    MarkerOptions markerOptions = new MarkerOptions();
                                    LatLng latLng = new LatLng(latitude, longitude);
                                    markerOptions.position(latLng);
                                    markerOptions.title(placeName);
                                    markerOptions.snippet(vicinity + "\nRatings : " + rating);

                                    mMap.setInfoWindowAdapter(NearbyFragment.this);

                                    Marker marker = mMap.addMarker(markerOptions);
                                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                                    marker.showInfoWindow();
                                }
                                Log.e(TAG, "Next Page:" + googlePlacesUrl);

                            } else if (result.getString(STATUS).equalsIgnoreCase(ZERO_RESULTS)) {
                                pDialog.dismiss();
                                showSnackMessage(NO_RESULTS);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            pDialog.dismiss();
                            showSnackMessage(e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.dismiss();
                        showSnackMessage(error.getMessage());
                    }
                });
        AppController.getInstance().addToRequestQueue(request);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        this.isConnected = isConnected;
    }

    @Override
    public void onGpsConnectionChanged(boolean isGPSEnabled) {
        this.isGPSEnabled = isGPSEnabled;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        int position = Integer.parseInt(marker.getId().replace("m", ""));
        Intent detailIntent = new Intent(getActivity(), InfoActivity.class);
        detailIntent.putExtra("position", position);
        detailIntent.putExtra("placeId", placeId.get(position));
        detailIntent.putExtra("lat", lat);
        detailIntent.putExtra("lng", lng);
        startActivity(detailIntent);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View info = View.inflate(getContext(),R.layout.item_info_window, null);

        TextView title = (TextView) info.findViewById(R.id.mtitle);
        title.setText(marker.getTitle());

        TextView snippet = (TextView) info.findViewById(R.id.date);
        snippet.setText(marker.getSnippet());
        snippet.setTypeface(ThemeFont);

        TextView roundTile = (TextView) info.findViewById(R.id.msg_thumb);
        roundTile.setText(String.valueOf(marker.getTitle().charAt(0)));
        roundTile.setTypeface(ThemeFont);

        return info;
    }

    @Override
    public void onGeofenceTransition(TransitionGeofence transitionGeofence) {

    }

    @Override
    public void onLocationUpdated(Location location) {

    }
}
