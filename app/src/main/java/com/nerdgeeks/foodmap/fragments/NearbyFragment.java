package com.nerdgeeks.foodmap.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.nerdgeeks.foodmap.Api.ApiClient;
import com.nerdgeeks.foodmap.Api.ApiInterface;
import com.nerdgeeks.foodmap.R;
import com.nerdgeeks.foodmap.activities.InfoActivity;
import com.nerdgeeks.foodmap.app.AppData;
import com.nerdgeeks.foodmap.app.PrefManager;
import com.nerdgeeks.foodmap.helper.ConnectivityReceiver;
import com.nerdgeeks.foodmap.model.PlaceModel;
import com.nerdgeeks.foodmap.model.PlaceModelCall;

import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private View snackView;
    private Bitmap smallMarker;
    private String type;
    private ArrayList<PlaceModel> placeModels;
    private Context mContext;

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
            type = getArguments().getString(ARG_PARAM1);
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

        myPref = mContext.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        isConnected = ConnectivityReceiver.isConnected();

        lat = AppData.lattitude;
        lng = AppData.longitude;

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

        pDialog.show();
        mMap = googleMap;

        int height = 96;
        int width = 96;
        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_marker);
        Bitmap b = bitmapdraw.getBitmap();
        smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        if(!isConnected){
            if (prefManager.isPrefAvailable(type)){
                lat = Double.parseDouble(myPref.getString("lat",""));
                lng = Double.parseDouble(myPref.getString("lng",""));
                showDataIntoMap(prefManager.readData(type));
                Toast.makeText(mContext, "You are offline. Showing last data from cache", Toast.LENGTH_SHORT).show();
            } else {
                // stopping swipe refresh
                pDialog.dismiss();
                showSnackMessage(INTERNET_ERROR);
            }
            return;
        }

        if (AppData.placeModels.isEmpty()){
            getDataFromServer();
        } else {
            showDataIntoMap(AppData.placeModels);
        }
    }

    private void getDataFromServer(){
        String latLng = lat+","+lng;
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<PlaceModelCall> call = apiInterface.getNearbyPlaces(type,latLng,1000);
        call.enqueue(new Callback<PlaceModelCall>() {
            @Override
            public void onResponse(@NonNull Call<PlaceModelCall> call, @NonNull Response<PlaceModelCall> response) {

                placeModels = response.body().getResults();

                Toast.makeText(mContext, response.body().getStatus(), Toast.LENGTH_SHORT).show();

                SharedPreferences.Editor edit = myPref.edit();
                edit.putString("lat", String.valueOf(AppData.lattitude));
                edit.putString("lng", String.valueOf(AppData.longitude));
                edit.apply();

                if (response.body().getNextPageToken() != null){
                    new Handler().postDelayed(() -> getNextPageDataFromServer(response.body().getNextPageToken()),2000);
                } else {
                    // Store the data for offline uses
                    prefManager.storeData(placeModels,type);
                    // set this data to static Arraylist so that we can use it in our whole app
                    AppData.placeModels = placeModels;
                    //show the data into map
                    showDataIntoMap(placeModels);

                }
            }

            @Override
            public void onFailure(@NonNull Call<PlaceModelCall> call, @NonNull Throwable t) {
                Log.d(TAG, "Json Api get failed");
                // stopping swipe refresh
                pDialog.dismiss();
                showSnackMessage(t.getMessage());
                //Toast.makeText(mContext, "Json Api get failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getNextPageDataFromServer (String pageToken){
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<PlaceModelCall> call = apiInterface.getNextNearbyPlaces(pageToken);
        call.enqueue(new Callback<PlaceModelCall>() {
            @Override
            public void onResponse(@NonNull Call<PlaceModelCall> call, @NonNull Response<PlaceModelCall> response) {

                ArrayList<PlaceModel> nextPlaceModels = response.body().getResults();
                placeModels.addAll(nextPlaceModels);

                // Store the data for offline uses
                prefManager.storeData(placeModels,type);
                // set this data to static Arraylist so that we can use it in our whole app
                AppData.placeModels = placeModels;
                //show the data into map
                showDataIntoMap(placeModels);
            }

            @Override
            public void onFailure(@NonNull Call<PlaceModelCall> call, @NonNull Throwable t) {
                Log.d(TAG, "Json Api get failed");
                // stopping swipe refresh
                pDialog.dismiss();
                showSnackMessage(t.getMessage());
            }
        });
    }

    private void showDataIntoMap(ArrayList<PlaceModel> placeModels) {

        pDialog.dismiss();

        //zoom to current position:
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lat,lng)).zoom(16).build();

        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
        mMap.setInfoWindowAdapter(NearbyFragment.this);
        mMap.setOnInfoWindowClickListener(this);

        for (PlaceModel placeModel: placeModels) {
            MarkerOptions markerOptions = new MarkerOptions();
            LatLng latLng = new LatLng(placeModel.getGeometry().getLocation().getLat(), placeModel.getGeometry().getLocation().getLng());
            markerOptions.position(latLng);
            markerOptions.title(placeModel.getName());
            markerOptions.snippet(placeModel.getVicinity() + "\nRatings : " + placeModel.getRating());
            Marker marker = mMap.addMarker(markerOptions);
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            marker.showInfoWindow();
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

    @Override
    public void onInfoWindowClick(Marker marker) {

        if (!isConnected){
            showSnackMessage(INTERNET_ERROR);
            return;
        }

        int position = Integer.parseInt(marker.getId().replace("m", ""));
        Intent detailIntent = new Intent(getActivity(), InfoActivity.class);
        detailIntent.putExtra("position", position);
        detailIntent.putExtra("placeId", placeModels.get(position).getPlaceId());
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
