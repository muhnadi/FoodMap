package com.nerdgeeks.foodmap.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.nerdgeeks.foodmap.app.AppController;
import com.nerdgeeks.foodmap.view.OnItemClickListener;
import com.nerdgeeks.foodmap.R;
import com.nerdgeeks.foodmap.activities.InfoActivity;
import com.nerdgeeks.foodmap.adapter.GMapsAdapter;
import com.nerdgeeks.foodmap.app.PrefManager;
import com.nerdgeeks.foodmap.helper.ConnectivityReceiver;
import com.nerdgeeks.foodmap.model.PlaceDeatilsModel;
import com.nerdgeeks.foodmap.service.MapService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.nerdgeeks.foodmap.app.AppConfig.GEOMETRY;
import static com.nerdgeeks.foodmap.app.AppConfig.GOOGLE_MAP_API_KEY;
import static com.nerdgeeks.foodmap.app.AppConfig.ICON;
import static com.nerdgeeks.foodmap.app.AppConfig.LATITUDE;
import static com.nerdgeeks.foodmap.app.AppConfig.LOCATION;
import static com.nerdgeeks.foodmap.app.AppConfig.LONGITUDE;
import static com.nerdgeeks.foodmap.app.AppConfig.NAME;
import static com.nerdgeeks.foodmap.app.AppConfig.NEXT_PAGE_TOKEN;
import static com.nerdgeeks.foodmap.app.AppConfig.NO_RESULTS;
import static com.nerdgeeks.foodmap.app.AppConfig.OK;
import static com.nerdgeeks.foodmap.app.AppConfig.OPENING_HOURS;
import static com.nerdgeeks.foodmap.app.AppConfig.OPEN_NOW;
import static com.nerdgeeks.foodmap.app.AppConfig.PHOTOS;
import static com.nerdgeeks.foodmap.app.AppConfig.PHOTOS_REFERENCE;
import static com.nerdgeeks.foodmap.app.AppConfig.PLACE_ID;
import static com.nerdgeeks.foodmap.app.AppConfig.RATE;
import static com.nerdgeeks.foodmap.app.AppConfig.RESULTS;
import static com.nerdgeeks.foodmap.app.AppConfig.STATUS;
import static com.nerdgeeks.foodmap.app.AppConfig.TAG;
import static com.nerdgeeks.foodmap.app.AppConfig.VICINITY;
import static com.nerdgeeks.foodmap.app.AppConfig.ZERO_RESULTS;


public class ResultFragment extends Fragment implements
        ConnectivityReceiver.ConnectivityReceiverListener, ConnectivityReceiver.GpsStatusReceiverListener {

    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    private InterstitialAd interstitialAd;
    private int click;

    private GMapsAdapter mapAdapter;
    private final ArrayList<PlaceDeatilsModel> mapList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private ProgressDialog pDialog;
    private String reference;
    private String nextPageToken;
    private PrefManager prefManager;
    private boolean isConnected, isGPSEnabled;
    private SharedPreferences myPref;
    private double lat, lng;
    private String mAddressOutput;
    private View snackView;

    public ResultFragment() {
        // Required empty public constructor
    }

    public static ResultFragment newInstance(String param1) {
        ResultFragment fragment = new ResultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        snackView = rootView.findViewById(R.id.fragment_list);
        prefManager = new PrefManager(getContext());
        isConnected = ConnectivityReceiver.isConnected();
        isGPSEnabled = ConnectivityReceiver.isGPSConnected();

        myPref = getActivity().getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        boolean isContain = myPref.contains("lat");

        if (isContain){
            try {
                lat = Double.valueOf(myPref.getString("lat",""));
                lng = Double.valueOf(myPref.getString("lng",""));
                mAddressOutput = myPref.getString("address","");
            } catch (NumberFormatException ex) {
                showSnackMessage(ex.getMessage());
            }
        }

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (nextPageToken != null) {
                    nextPageLoad(nextPageToken);
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    Toast.makeText(getContext(), "All Results Are Loaded", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Loading ");
        pDialog.setIndeterminate(false);

        //Adding RecyclerView
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        try {
            if (isConnected && isGPSEnabled){
                loadNearByPlaces(lat, lng, mParam1);
            } else if (isConnected){
                loadNearByPlaces(lat, lng, mParam1);
            } else if (isGPSEnabled){
                if (prefManager.isPrefAvailable()){
                    setRecyclerAdapter(prefManager.readData());
                }
            } else {
                if (prefManager.isPrefAvailable()){
                    setRecyclerAdapter(prefManager.readData());
                }
            }
        } catch (Exception ex){
            showSnackMessage(ex.getMessage());
        }
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        AppController.getInstance().setConnectivityListener(this);
        AppController.getInstance().setPermissionListener(this);
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

                        String id, place_id, placeName = null, icon, vicinity = null, open, rating;
                        double latitude, longitude;

                        try {
                            if (!result.isNull(NEXT_PAGE_TOKEN)) {
                                nextPageToken = result.getString(NEXT_PAGE_TOKEN);
                            }
                            JSONArray jsonArray = result.getJSONArray(RESULTS);

                            if (result.getString(STATUS).equalsIgnoreCase(OK)) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject place = jsonArray.getJSONObject(i);

                                    PlaceDeatilsModel mapModel = new PlaceDeatilsModel();
                                    //id = place.getString(ID);

                                    place_id = place.getString(PLACE_ID);
                                    mapModel.setId(place_id);

                                    if (!place.isNull(NAME)) {
                                        placeName = place.getString(NAME);
                                        mapModel.setResName(placeName);
                                    }
                                    if (!place.isNull(VICINITY)) {
                                        vicinity = place.getString(VICINITY);
                                        mapModel.setResVicnity(vicinity);
                                    }

                                    latitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                                            .getDouble(LATITUDE);
                                    mapModel.setLatitude(latitude);
                                    longitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                                            .getDouble(LONGITUDE);
                                    mapModel.setLongitude(longitude);


                                    if (!place.isNull(ICON)) {
                                        icon = place.getString(ICON);
                                        mapModel.setIconUrl(icon);
                                    }

                                    if (!place.isNull(OPENING_HOURS)) {
                                        open = place.getJSONObject(OPENING_HOURS).getString(OPEN_NOW);
                                        mapModel.setResOpen(open);
                                    }

                                    if (!place.isNull(RATE)) {
                                        rating = place.getString(RATE);
                                        mapModel.setResRating(rating);
                                    }

                                    if (!place.isNull(PHOTOS)) {
                                        JSONArray photos = place.getJSONArray(PHOTOS);
                                        for (int j=0; j<photos.length(); j++){
                                            JSONObject photo_obj = photos.getJSONObject(j);
                                            if(!photo_obj.isNull(PHOTOS_REFERENCE)){
                                                reference = photo_obj.getString(PHOTOS_REFERENCE);
                                                mapModel.setRef(reference);
                                            }
                                        }
                                    }

                                    mapList.add(mapModel);
                                }
                                pDialog.dismiss();
                                prefManager.storeData(mapList);
                                nextPageLoad(nextPageToken);

                                setRecyclerAdapter(mapList);

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

    private void nextPageLoad(String nextpage) {
        final String googlePlacesUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "&pagetoken=" + nextpage +
                "&key=" + GOOGLE_MAP_API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(googlePlacesUrl,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject result) {
                        Log.i(TAG, "onResponse: Result= " + result.toString());

                        String id, place_id, placeName = null, icon, vicinity = null, open, rating;
                        double latitude, longitude;
                        int position = mapAdapter.getItemCount();

                        try {
                            if (!result.isNull(NEXT_PAGE_TOKEN)) {
                                nextPageToken = result.getString(NEXT_PAGE_TOKEN);
                            }
                            JSONArray jsonArray = result.getJSONArray(RESULTS);
                            if (result.getString(STATUS).equalsIgnoreCase(OK)) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject place = jsonArray.getJSONObject(i);

                                    PlaceDeatilsModel mapModel1 = new PlaceDeatilsModel();
                                    //id = place.getString(ID);

                                    place_id = place.getString(PLACE_ID);
                                    mapModel1.setId(place_id);

                                    if (!place.isNull(NAME)) {
                                        placeName = place.getString(NAME);
                                        mapModel1.setResName(placeName);
                                    }
                                    if (!place.isNull(VICINITY)) {
                                        vicinity = place.getString(VICINITY);
                                        mapModel1.setResVicnity(vicinity);
                                    }

                                    latitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                                            .getDouble(LATITUDE);
                                    mapModel1.setLatitude(latitude);
                                    longitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                                            .getDouble(LONGITUDE);
                                    mapModel1.setLongitude(longitude);

                                    if (!place.isNull(ICON)) {
                                        icon = place.getString(ICON);
                                        mapModel1.setIconUrl(icon);
                                    }

                                    if (!place.isNull(OPENING_HOURS)) {
                                        open = place.getJSONObject(OPENING_HOURS).getString(OPEN_NOW);
                                        mapModel1.setResOpen(open);
                                    }

                                    if (!place.isNull(RATE)) {
                                        rating = place.getString(RATE);
                                        mapModel1.setResRating(rating);
                                    }
                                    mapAdapter.AddItems(position++, mapModel1);
                                }
                                mapAdapter.notifyDataSetChanged();
                                nextPageLoad(nextPageToken);
                                Toast.makeText(getActivity(), "Got " + mapAdapter.getItemCount() + " Results", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Next Page:" + googlePlacesUrl);
                            } else if (result.getString("status").equalsIgnoreCase("ZERO_RESULTS")) {
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

    private OnItemClickListener recyclerRowClickListener = new OnItemClickListener() {

        @Override
        public void onClick(View v, int position) {

            click++;
            if(click%4==0){
                launchInterstitial();
                loadInterstitial();
            }
            Intent detailIntent = new Intent(getActivity(), InfoActivity.class);
            detailIntent.putExtra("position", position);
            detailIntent.putExtra("placeId", prefManager.readData().get(position).getId());
            detailIntent.putExtra("lat", lat);
            detailIntent.putExtra("lng", lng);
            startActivity(detailIntent);
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);

        }
    };


    public void launchInterstitial() {
        interstitialAd = new InterstitialAd(getContext());
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

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        this.isConnected = isConnected;
    }

    @Override
    public void onGpsConnectionChanged(boolean isGPSEnabled) {
        this.isGPSEnabled = isGPSEnabled;
    }

    private void showSnackMessage(String message) {
        int color = Color.RED;
        int TIME_OUT = Snackbar.LENGTH_INDEFINITE;

        Snackbar snackbar = Snackbar
                .make(snackView, message, TIME_OUT);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    private void setRecyclerAdapter(ArrayList<PlaceDeatilsModel> arrayList){
        mapAdapter = new GMapsAdapter(arrayList, getActivity());
        mapAdapter.notifyDataSetChanged();
        //Nearby restaurant List added to RecyclerView
        if (isConnected){
            mapAdapter.isOnItemClickListener(true);
            mapAdapter.setOnItemClickListener(recyclerRowClickListener);
        } else {
            mapAdapter.isOnItemClickListener(false);
        }
        mRecyclerView.setAdapter(mapAdapter);
    }
}
