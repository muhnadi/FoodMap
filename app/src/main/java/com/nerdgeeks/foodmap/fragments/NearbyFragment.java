package com.nerdgeeks.foodmap.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nerdgeeks.foodmap.R;
import com.nerdgeeks.foodmap.activities.InfoActivity;
import com.nerdgeeks.foodmap.app.PrefManager;
import com.nerdgeeks.foodmap.helper.ConnectivityReceiver;
import java.util.ArrayList;
import io.nlopez.smartlocation.SmartLocation;

import static com.nerdgeeks.foodmap.app.AppConfig.*;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NearbyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NearbyFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.InfoWindowAdapter{

    private static final String ARG_PARAM1 = "param1";
    private GoogleMap mMap;
    private ProgressDialog pDialog;
    private PrefManager prefManager;
    private boolean isConnected;
    private SharedPreferences myPref;
    private double lat, lng;
    private boolean isContain;
    private View snackView;
    private ArrayList<String> placeId = new ArrayList<>();
    private Bitmap smallMarker;


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

        myPref = mContext.getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        isContain = myPref.contains("lat");

        if (isContain){
            lat = Double.parseDouble(myPref.getString("lat",""));
            lng = Double.parseDouble(myPref.getString("lng",""));
        }

        if (mMap == null) {
            getActivity().runOnUiThread(() -> {
                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(NearbyFragment.this);
            });
        }

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        int height = 96;
        int width = 96;
        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_marker);
        Bitmap b = bitmapdraw.getBitmap();
        smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        try{
            if (isConnected) {
                showLast( true);
            } else {
                if (prefManager.isPrefAvailable()){
                    showLast(false);
                } else {
                    showSnackMessage(INTERNET_ERROR);
                }
            }
        } catch (Exception ex){
            showSnackMessage(ex.getMessage());
        }
    }

    private void startLocation() {

//        long mLocTrackingInterval = 10000; // 5 sec
//        float trackingDistance = 0f;
//        LocationAccuracy trackingAccuracy = LocationAccuracy.HIGH;
//
//        LocationParams.Builder builder = new LocationParams.Builder()
//                .setAccuracy(trackingAccuracy)
//                .setDistance(trackingDistance)
//                .setInterval(mLocTrackingInterval);
//
//        provider = new LocationGooglePlayServicesProvider();
//        provider.setCheckLocationSettings(true);
//        //SmartLocation smartLocation = new SmartLocation.Builder(mContext).logging(true).build();
//        SmartLocation.with(mContext).location(provider).oneFix().config(builder.build()).start(this);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == LOCATION_PERMISSION_ID && grantResults.length > 0
//                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            startLocation();
//        }
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (provider != null) {
//            provider.onActivityResult(requestCode, resultCode, data);
//        }
//    }

    private void showLast(boolean isNetwork) {

        Location lastLocation = SmartLocation.with(getContext()).location().getLastLocation();
        if (lastLocation != null) {
            lat=lastLocation.getLatitude();
            lng= lastLocation.getLongitude();
            Toast.makeText(mContext, lat+"-"+lng, Toast.LENGTH_SHORT).show();
        }

        //zoom to current position:
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lat,lng)).zoom(16).build();

        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
        mMap.setInfoWindowAdapter(NearbyFragment.this);
        mMap.setOnInfoWindowClickListener(this);

        if (isNetwork){
            //loadNearByPlaces(lat, lng, mParam1);
        } else {
            for (int i=0; i<prefManager.readData().size(); i++) {
                MarkerOptions markerOptions = new MarkerOptions();
                LatLng latLng = new LatLng(prefManager.readData().get(i).getGeometry().getLocation().getLat(), prefManager.readData().get(i).getGeometry().getLocation().getLng());
                markerOptions.position(latLng);
                markerOptions.title(prefManager.readData().get(i).getName());
                markerOptions.snippet(prefManager.readData().get(i).getVicinity() + "\nRatings : " + prefManager.readData().get(i).getRating());
                Marker marker = mMap.addMarker(markerOptions);
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                marker.showInfoWindow();
            }
        }

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void showSnackMessage(String message) {
        int color = Color.RED;
        int TIME_OUT = Snackbar.LENGTH_SHORT;

        Snackbar snackbar = Snackbar
                .make(snackView, message, TIME_OUT);
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

//    public void loadNearByPlaces(final double Latitude, final double Longitude, String type) {
//
//        pDialog.show();
//
//        final String googlePlacesUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" + "location="
//                + Latitude
//                + ","
//                + Longitude +
//                "&radius=1000"+
//                "&type=" + type +
//                "&sensor=false" +
//                "&key=" + GOOGLE_MAP_API_KEY;
//
//        JsonObjectRequest request = new JsonObjectRequest(googlePlacesUrl,
//
//                result -> {
//                    Log.i(TAG, "onResponse: Result= " + result.toString());
//
//                    String place_id, placeName = null, vicinity = null, rating=null;
//                    double latitude, longitude;
//
//                    try {
//                        if (!result.isNull(NEXT_PAGE_TOKEN)) {
//                            nextPageToken = result.getString(NEXT_PAGE_TOKEN);
//                        }
//
//                        JSONArray jsonArray = result.getJSONArray(RESULTS);
//
//                        if (result.getString(STATUS).equalsIgnoreCase(OK)) {
//
//                            for (int i = 0; i < jsonArray.length(); i++) {
//                                JSONObject place = jsonArray.getJSONObject(i);
//
//                                place_id = place.getString(PLACE_ID);
//                                placeId.add(place_id);
//
//                                if (!place.isNull(NAME)) {
//                                    placeName = place.getString(NAME);
//                                }
//                                if (!place.isNull(VICINITY)) {
//                                    vicinity = place.getString(VICINITY);
//                                }
//
//                                if (!place.isNull(RATE)){
//                                    rating = place.getString(RATE);
//                                }
//
//                                latitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
//                                        .getDouble(LATITUDE);
//                                longitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
//                                        .getDouble(LONGITUDE);
//
//                                MarkerOptions markerOptions = new MarkerOptions();
//                                LatLng latLng = new LatLng(latitude, longitude);
//                                markerOptions.position(latLng);
//                                markerOptions.title(placeName);
//                                markerOptions.snippet(vicinity + "\nRatings : " + rating);
//
//                                mMap.setInfoWindowAdapter(NearbyFragment.this);
//
//                                Marker marker = mMap.addMarker(markerOptions);
//                                marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
//                                marker.showInfoWindow();
//                            }
//                            if (nextPageToken != null) {
//                                new Handler().postDelayed(() -> nextPageLoad(),2000);
//                            } else {
//                                pDialog.dismiss();
//                            }
//                        } else  {
//                            pDialog.dismiss();
//                            showSnackMessage(NO_RESULTS);
//                        }
//                    } catch (JSONException e) {
//                        pDialog.dismiss();
//                        showSnackMessage(e.getMessage());
//                    }
//
//                },
//                error -> {
//                    pDialog.dismiss();
//                    showSnackMessage(error.getMessage());
//                });
//
//        AppController.getInstance().addToRequestQueue(request);
//    }
//
//    private void nextPageLoad() {
//
//        final String googlePlacesUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
//                "pagetoken=" + nextPageToken +
//                "&key=" + GOOGLE_MAP_API_KEY;
//
//        JsonObjectRequest request = new JsonObjectRequest(googlePlacesUrl,
//
//                result -> {
//                    Log.i(TAG, "onResponse: Result= " + result.toString());
//
//                    String placeName = null, place_id, vicinity = null, rating = null;
//                    double latitude, longitude;
//
//                    try {
//                        if (!result.isNull(NEXT_PAGE_TOKEN)) {
//                            nextPageToken = result.getString(NEXT_PAGE_TOKEN);
//                        }
//                        JSONArray jsonArray = result.getJSONArray(RESULTS);
//                        if (result.getString(STATUS).equalsIgnoreCase(OK)) {
//                            for (int i = 0; i < jsonArray.length(); i++) {
//                                JSONObject place = jsonArray.getJSONObject(i);
//
//                                if (!place.isNull(NAME)) {
//                                    placeName = place.getString(NAME);
//                                }
//
//                                place_id = place.getString(PLACE_ID);
//                                placeId.add(place_id);
//
//                                if (!place.isNull(VICINITY)) {
//                                    vicinity = place.getString(VICINITY);
//                                }
//                                if (!place.isNull(RATE)){
//                                    rating = place.getString(RATE);
//                                }
//
//                                latitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
//                                        .getDouble(LATITUDE);
//                                longitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
//                                        .getDouble(LONGITUDE);
//
//                                MarkerOptions markerOptions = new MarkerOptions();
//                                LatLng latLng = new LatLng(latitude, longitude);
//                                markerOptions.position(latLng);
//                                markerOptions.title(placeName);
//                                markerOptions.snippet(vicinity + "\nRatings : " + rating);
//
//                                mMap.setInfoWindowAdapter(NearbyFragment.this);
//
//                                Marker marker = mMap.addMarker(markerOptions);
//                                marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
//                                marker.showInfoWindow();
//                            }
//
//                            pDialog.dismiss();
//                            Log.e(TAG, "Next Page:" + googlePlacesUrl);
//                            showSnackMessage(MAP_UPDATED);
//
//                        } else {
//                            pDialog.dismiss();
//                            showSnackMessage(NO_RESULTS);
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        pDialog.dismiss();
//                        showSnackMessage(e.getMessage());
//                    }
//                },
//                error -> {
//                    pDialog.dismiss();
//                    showSnackMessage(error.getMessage());
//                });
//        AppController.getInstance().addToRequestQueue(request);
//    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        if (!isConnected){
            showSnackMessage(INTERNET_ERROR);
            return;
        }

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

        TextView title = info.findViewById(R.id.mtitle);
        title.setText(marker.getTitle());

        TextView snippet = info.findViewById(R.id.date);
        snippet.setText(marker.getSnippet());

        TextView roundTile = info.findViewById(R.id.msg_thumb);
        roundTile.setText(String.valueOf(marker.getTitle().charAt(0)));

        return info;
    }
}
