package com.nerdgeeks.foodmap.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.nerdgeeks.foodmap.Api.ApiClient;
import com.nerdgeeks.foodmap.Api.ApiInterface;
import com.nerdgeeks.foodmap.app.AppData;
import com.nerdgeeks.foodmap.model.PlaceModel;
import com.nerdgeeks.foodmap.model.PlaceModelCall;
import com.nerdgeeks.foodmap.view.OnItemClickListener;
import com.nerdgeeks.foodmap.R;
import com.nerdgeeks.foodmap.activities.InfoActivity;
import com.nerdgeeks.foodmap.adapter.GMapsAdapter;
import com.nerdgeeks.foodmap.app.PrefManager;
import com.nerdgeeks.foodmap.helper.ConnectivityReceiver;
import java.util.ArrayList;
import io.nlopez.smartlocation.SmartLocation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.nerdgeeks.foodmap.app.AppConfig.*;


public class ResultFragment extends Fragment implements
        ConnectivityReceiver.ConnectivityReceiverListener,SwipeRefreshLayout.OnRefreshListener {

    private static final String ARG_PARAM1 = "param1";
    private String type;
    private SwipeRefreshLayout swipeRefreshLayout;

    private GMapsAdapter mapAdapter;
    private RecyclerView mRecyclerView;
    private String nextPageToken = "";
    private PrefManager prefManager;
    private boolean isConnected;
    private double lat, lng;
    private View snackView;
    private ArrayList<String> placeId = new ArrayList<>();

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
            type = getArguments().getString(ARG_PARAM1);
        }
    }

    private Context mContext;

    // Initialise it from onAttach()
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        snackView = rootView.findViewById(R.id.fragment_list);
        prefManager = new PrefManager(getContext());
        isConnected = ConnectivityReceiver.isConnected();

        SharedPreferences myPref = mContext.getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        boolean isContain = myPref.contains("lat");

        if (isContain) {
            try {
                lat = Double.valueOf(myPref.getString("lat", ""));
                lng = Double.valueOf(myPref.getString("lng", ""));
            } catch (NumberFormatException ex) {
                showSnackMessage(ex.getMessage());
            }
        }

        //Adding RecyclerView
        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        swipeRefreshLayout = rootView.findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(() -> loadData());

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRefresh() {
        if (AppData.placeModels.isEmpty()) {
            loadData();
        } else {
            setRecyclerAdapter(AppData.placeModels);
        }
    }

//    private void loadData() {
//
//        //clear previous data
//        mapList.clear();
//
//        try {
//            if (isConnected) {
//                loadNearByPlaces(lat, lng);
//            } else {
//                if (prefManager.isPrefAvailable()){
//                    Toast.makeText(mContext, "Showing data from cache", Toast.LENGTH_SHORT).show();
//                    setRecyclerAdapter(prefManager.readData());
//                } else {
//                    showSnackMessage(INTERNET_ERROR);
//                }
//                // stopping swipe refresh
//                swipeRefreshLayout.setRefreshing(false);
//            }
//        } catch (Exception ex){
//            showSnackMessage(ex.getMessage());
//        }
//    }

    private void loadData(){

        Location lastLocation = SmartLocation.with(getContext()).location().getLastLocation();
        if (lastLocation != null) {
            lat=lastLocation.getLatitude();
            lng= lastLocation.getLongitude();
        }

        // showing refresh animation before making http call
        swipeRefreshLayout.setRefreshing(true);

        if(!isConnected){
            if (prefManager.isPrefAvailable()){
                Toast.makeText(mContext, "Showing data from cache", Toast.LENGTH_SHORT).show();
                setRecyclerAdapter(prefManager.readData());
            } else {
                showSnackMessage(INTERNET_ERROR);
            }
            // stopping swipe refresh
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        String latLng = lat+","+lng;
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<PlaceModelCall> call = apiInterface.getNearbyPlaces(type,latLng,1000);
        call.enqueue(new Callback<PlaceModelCall>() {
            @Override
            public void onResponse(@NonNull Call<PlaceModelCall> call, @NonNull Response<PlaceModelCall> response) {

                ArrayList<PlaceModel> placeModels = response.body().getResults();

                Toast.makeText(mContext, response.body().getStatus(), Toast.LENGTH_SHORT).show();

                // set this data to static Arraylist so that we can use it in our whole app
                AppData.placeModels = placeModels;

                // Store the data for offline uses
                prefManager.storeData(placeModels);

                // set the data to recyclerview
                setRecyclerAdapter(placeModels);
            }

            @Override
            public void onFailure(@NonNull Call<PlaceModelCall> call, @NonNull Throwable t) {
                Log.d(TAG, "Json Api get failed");
                // stopping swipe refresh
                swipeRefreshLayout.setRefreshing(false);
                showSnackMessage(t.getMessage());
                //Toast.makeText(mContext, "Json Api get failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    public void loadNearByPlaces(double lat, double lng) {
//
//        Location lastLocation = SmartLocation.with(getContext()).location().getLastLocation();
//        if (lastLocation != null) {
//            lat=lastLocation.getLatitude();
//            lng= lastLocation.getLongitude();
//        }
//
//        // showing refresh animation before making http call
//        swipeRefreshLayout.setRefreshing(true);
//
//        final String googlePlacesUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" + "location="
//                + lat
//                + ","
//                + lng +
//                "&radius=1000"+
//                "&type=" + type +
//                "&sensor=false" +
//                "&key=" + GOOGLE_MAP_API_KEY;
//
//        Toast.makeText(mContext, String.valueOf(lat)+"-"+String.valueOf(lng), Toast.LENGTH_SHORT).show();
//
//
//        JsonObjectRequest request = new JsonObjectRequest(googlePlacesUrl,
//
//                result -> {
//                    Log.i(TAG, "onResponse: Result= " + result.toString());
//
//                    String place_id, placeName, icon, vicinity, open, rating;
//                    double latitude, longitude;
//
//                    try {
//                        if (!result.isNull(NEXT_PAGE_TOKEN)) {
//                            nextPageToken = result.getString(NEXT_PAGE_TOKEN);
//                        }
//                        JSONArray jsonArray = result.getJSONArray(RESULTS);
//
//                        if (result.getString(STATUS).equalsIgnoreCase(OK)) {
//                            for (int i = 0; i < jsonArray.length(); i++) {
//                                JSONObject place = jsonArray.getJSONObject(i);
//
//                                PlaceDeatilsModel mapModel = new PlaceDeatilsModel();
//
//                                place_id = place.getString(PLACE_ID);
//                                placeId.add(place_id);
//                                mapModel.setId(place_id);
//
//                                if (!place.isNull(NAME)) {
//                                    placeName = place.getString(NAME);
//                                    mapModel.setResName(placeName);
//                                }
//                                if (!place.isNull(VICINITY)) {
//                                    vicinity = place.getString(VICINITY);
//                                    mapModel.setResVicnity(vicinity);
//                                }
//
//                                latitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
//                                        .getDouble(LATITUDE);
//                                mapModel.setLatitude(latitude);
//                                longitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
//                                        .getDouble(LONGITUDE);
//                                mapModel.setLongitude(longitude);
//
//
//                                if (!place.isNull(ICON)) {
//                                    icon = place.getString(ICON);
//                                    mapModel.setIconUrl(icon);
//                                }
//
//                                if (!place.isNull(OPENING_HOURS)) {
//                                    open = place.getJSONObject(OPENING_HOURS).getString(OPEN_NOW);
//                                    mapModel.setResOpen(open);
//                                }
//
//                                if (!place.isNull(RATE)) {
//                                    rating = place.getString(RATE);
//                                    mapModel.setResRating(rating);
//                                }
//
//                                if (!place.isNull(PHOTOS)) {
//                                    JSONArray photos = place.getJSONArray(PHOTOS);
//                                    for (int j=0; j<photos.length(); j++){
//                                        JSONObject photo_obj = photos.getJSONObject(j);
//                                        if(!photo_obj.isNull(PHOTOS_REFERENCE)){
//                                            String reference = photo_obj.getString(PHOTOS_REFERENCE);
//                                            mapModel.setRef(reference);
//                                        }
//                                    }
//                                }
//
//                                mapList.add(mapModel);
//                            }
//
//                            if (!nextPageToken.isEmpty()){
//                                new Handler().postDelayed(() -> nextPageLoad(),2000);
//                            } else {
//                                // stopping swipe refresh
//                                swipeRefreshLayout.setRefreshing(false);
//                                prefManager.storeData(mapList);
//                                setRecyclerAdapter(mapList);
//                            }
//
//                        } else {
//                            // stopping swipe refresh
//                            swipeRefreshLayout.setRefreshing(false);
//                            showSnackMessage(NO_RESULTS);
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        // stopping swipe refresh
//                        swipeRefreshLayout.setRefreshing(false);
//                        showSnackMessage(e.getMessage());
//                    }
//
//                },
//                error -> {
//                    // stopping swipe refresh
//                    swipeRefreshLayout.setRefreshing(false);
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
//                    String id, placeName, icon, vicinity, open, rating;
//                    double latitude, longitude;
//
//                    try {
//                        JSONArray jsonArray = result.getJSONArray(RESULTS);
//
//                        Toast.makeText(getActivity(), result.getString(STATUS), Toast.LENGTH_SHORT).show();
//
//                        if (result.getString(STATUS).equalsIgnoreCase(OK)) {
//
//                            for (int i = 0; i < jsonArray.length(); i++) {
//                                JSONObject place = jsonArray.getJSONObject(i);
//
//                                PlaceDeatilsModel mapModel1 = new PlaceDeatilsModel();
//
//                                id = place.getString(PLACE_ID);
//                                placeId.add(id);
//                                mapModel1.setId(id);
//
//                                if (!place.isNull(NAME)) {
//                                    placeName = place.getString(NAME);
//                                    mapModel1.setResName(placeName);
//                                }
//                                if (!place.isNull(VICINITY)) {
//                                    vicinity = place.getString(VICINITY);
//                                    mapModel1.setResVicnity(vicinity);
//                                }
//
//                                latitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
//                                        .getDouble(LATITUDE);
//                                mapModel1.setLatitude(latitude);
//                                longitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
//                                        .getDouble(LONGITUDE);
//                                mapModel1.setLongitude(longitude);
//
//                                if (!place.isNull(ICON)) {
//                                    icon = place.getString(ICON);
//                                    mapModel1.setIconUrl(icon);
//                                }
//
//                                if (!place.isNull(OPENING_HOURS)) {
//                                    open = place.getJSONObject(OPENING_HOURS).getString(OPEN_NOW);
//                                    mapModel1.setResOpen(open);
//                                }
//
//                                if (!place.isNull(RATE)) {
//                                    rating = place.getString(RATE);
//                                    mapModel1.setResRating(rating);
//                                }
//
//                                if (!place.isNull(PHOTOS)) {
//                                    JSONArray photos = place.getJSONArray(PHOTOS);
//                                    for (int j=0; j<photos.length(); j++){
//                                        JSONObject photo_obj = photos.getJSONObject(j);
//                                        if(!photo_obj.isNull(PHOTOS_REFERENCE)){
//                                            String reference = photo_obj.getString(PHOTOS_REFERENCE);
//                                            mapModel1.setRef(reference);
//                                        }
//                                    }
//                                }
//
//                                mapList.add(mapModel1);
//                            }
//                        }
//
//                        // stopping swipe refresh
//                        swipeRefreshLayout.setRefreshing(false);
//                        prefManager.storeData(mapList);
//                        setRecyclerAdapter(mapList);
//
//                        Toast.makeText(getActivity(), "Got " + mapAdapter.getItemCount() + " Results", Toast.LENGTH_SHORT).show();
//                        Log.e(TAG, "Next Page:" + googlePlacesUrl);
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        // stopping swipe refresh
//                        swipeRefreshLayout.setRefreshing(false);
//                        showSnackMessage(e.getMessage());
//                    }
//                },
//                error -> {
//                    // stopping swipe refresh
//                    swipeRefreshLayout.setRefreshing(false);
//                    showSnackMessage(error.getMessage());
//                });
//        AppController.getInstance().addToRequestQueue(request);
//    }

    private OnItemClickListener recyclerRowClickListener = new OnItemClickListener() {

        @Override
        public void onClick(View v, int position) {

            Intent detailIntent = new Intent(getActivity(), InfoActivity.class);
            detailIntent.putExtra("position", position);
            detailIntent.putExtra("placeId", placeId.get(position));
            detailIntent.putExtra("lat", lat);
            detailIntent.putExtra("lng", lng);
            startActivity(detailIntent);
        }
    };

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        this.isConnected = isConnected;
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

    private void setRecyclerAdapter(ArrayList<PlaceModel> placeModels){

        // stopping swipe refresh
        swipeRefreshLayout.setRefreshing(false);

        mapAdapter = new GMapsAdapter(placeModels, getActivity());
        mapAdapter.notifyDataSetChanged();
        //Nearby restaurant List added to RecyclerView
        if (isConnected){
            mapAdapter.isOnItemClickListener(true);
            mapAdapter.setOnItemClickListener(recyclerRowClickListener);
        } else {
            mapAdapter.isOnItemClickListener(false);
            showSnackMessage(INTERNET_ERROR);
        }
        mRecyclerView.setAdapter(mapAdapter);
    }

}
