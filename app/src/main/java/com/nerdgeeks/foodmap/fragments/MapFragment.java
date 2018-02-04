package com.nerdgeeks.foodmap.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.nerdgeeks.foodmap.app.AppController;
import com.nerdgeeks.foodmap.R;
import com.nerdgeeks.foodmap.app.PrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.nerdgeeks.foodmap.app.AppConfig.GOOGLE_MAP_API_KEY;
import static com.nerdgeeks.foodmap.app.AppConfig.OK;
import static com.nerdgeeks.foodmap.app.AppConfig.OVERVIEW_POLYLINE;
import static com.nerdgeeks.foodmap.app.AppConfig.POINTS;
import static com.nerdgeeks.foodmap.app.AppConfig.ROUTES;
import static com.nerdgeeks.foodmap.app.AppConfig.STATUS;
import static com.nerdgeeks.foodmap.app.AppConfig.TAG;
import static com.nerdgeeks.foodmap.app.AppConfig.ZERO_RESULTS;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    private String mParam1;
    private String mParam2;
    private String mParam3;
    private GoogleMap gMap;
    private Marker currentLocationMarker;
    private PrefManager prefManager;
    private SharedPreferences myPref;
    private double lat,lng;
    private String mAddressOutput;
    private Typeface ThemeFont;
    private Bitmap smallMarker;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance(String param1,String param2,String param3) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mParam3 = getArguments().getString(ARG_PARAM3);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        prefManager = new PrefManager(getContext());
        ThemeFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/HelveticaNeue.ttf");
        myPref = getActivity().getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        boolean isContain = myPref.contains("lat");

        if (isContain){
            lat = Double.parseDouble(myPref.getString("lat",""));
            lng = Double.parseDouble(myPref.getString("lng",""));
            mAddressOutput = myPref.getString("address","");
        }

        if (gMap == null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(MapFragment.this);
                }
            });
        }

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setInfoWindowAdapter(this);
        int height = 72;
        int width = 72;
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.ic_marker);
        Bitmap b = bitmapdraw.getBitmap();
        smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        LoadInformation(mParam1);
    }

    private void LoadInformation(String placeId) {
        String googlePlaceDetails = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" +
                placeId +
                "&sensor=true" +
                "&key=" +
                GOOGLE_MAP_API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(googlePlaceDetails,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject result) {
                        Log.i(TAG, "onResponse: Result= " + result.toString());
                        try {
                            JSONObject jsonObject = result.getJSONObject("result");

                            if (result.getString("status").equalsIgnoreCase("OK")) {

                                String mLat = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat");
                                double lat = Double.parseDouble(mLat);

                                String mLong = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lng");
                                double lng = Double.parseDouble(mLong);

                                String name = jsonObject.getString("name");

                                RestaurantMap(gMap,new LatLng(lat,lng),name);
                            } else if (result.getString("status").equalsIgnoreCase("ZERO_RESULTS")) {
                                Toast.makeText(getActivity(), "No Information found!!!",
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "parseLocationResult: Error=" + e.getMessage());
                            Toast.makeText(getActivity(), "Server Error!!!",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: Error= " + error);
                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
                    }
                });

        AppController.getInstance().addToRequestQueue(request);
    }

    private void RestaurantMap(final GoogleMap mMap,final LatLng latLng, String name){
        gMap = mMap;

        double srcLat = Double.valueOf(mParam2);
        double srcLng = Double.valueOf(mParam3);

        gMap.addMarker(
                new MarkerOptions().position(
                        new LatLng(lat,lng))
                        .title(mAddressOutput)
                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

        Marker desMarker = gMap.addMarker(new MarkerOptions().position(latLng).title(name));
        desMarker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        desMarker.showInfoWindow();

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(srcLat,srcLng), 17));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        gMap.animateCamera(CameraUpdateFactory.zoomTo(17));

        getRoutes(new LatLng(srcLat,srcLng),latLng);
    }

    private void getRoutes(LatLng origin, final LatLng destination){
        String apiMapUrl = "http://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + origin.latitude
                + "," + origin.longitude
                + "&destination=" + destination.latitude
                + "," + destination.longitude
                + "&sensor=false"
                + "&units=metric"
                + "&mode=walking";

        final List<List<HashMap<String, String>>> routes = new ArrayList<>() ;

        JsonObjectRequest request = new JsonObjectRequest(apiMapUrl,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject result) {
                        Log.i(TAG, "onResponse: Result= " + result.toString());
                        try {
                            if (result.getString(STATUS).equalsIgnoreCase(OK)) {
                                JSONArray jsonRoutes = result.getJSONArray(ROUTES);
                                for (int i=0; i<jsonRoutes.length(); i++){
                                    List path = new ArrayList<>();
                                    JSONObject overviewPolyline = jsonRoutes.getJSONObject(i).getJSONObject(OVERVIEW_POLYLINE);
                                    String poly = overviewPolyline.getString(POINTS);
                                    List<LatLng> list = decodePoly(poly);

                                    for (int l=0; l<list.size(); l++){
                                        HashMap<String, String> hm = new HashMap<>();
                                        hm.put("lat", Double.toString((list.get(l)).latitude) );
                                        hm.put("lng", Double.toString((list.get(l)).longitude) );
                                        path.add(hm);
                                    }
                                    routes.add(path);
                                }
                                // Traversing through all the routes
                                for (int i = 0; i < routes.size(); i++) {
                                    ArrayList<LatLng> points = new ArrayList<>();

                                    // Fetching i-th route
                                    List<HashMap<String, String>> path = routes.get(i);

                                    // Fetching all the points in i-th route
                                    for (int j = 0; j < path.size(); j++) {
                                        HashMap<String, String> point = path.get(j);

                                        double lat = Double.parseDouble(point.get("lat"));
                                        double lng = Double.parseDouble(point.get("lng"));
                                        LatLng position = new LatLng(lat, lng);

                                        points.add(position);
                                    }
                                    gMap.addPolyline(
                                            new PolylineOptions()
                                                    .geodesic(true)
                                                    .addAll(points)
                                                    .width(10).color(Color.RED).geodesic(true)
                                    );
                                }
                            } else if (result.getString(STATUS).equalsIgnoreCase(ZERO_RESULTS)) {
                                Toast.makeText(getActivity(), "No Information found!!!",
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "parseLocationResult: Error=" + e.getMessage());
                            Toast.makeText(getActivity(), "Server Error!!!",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: Error= " + error);
                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
                    }
                });

        AppController.getInstance().addToRequestQueue(request);
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
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

//        TextView snippet = (TextView) info.findViewById(R.id.date);
//        snippet.setText(marker.getSnippet());
//        snippet.setTypeface(ThemeFont);

        TextView roundTile = (TextView) info.findViewById(R.id.msg_thumb);
        try {
            roundTile.setText(String.valueOf(marker.getTitle().charAt(0)));
        } catch (StringIndexOutOfBoundsException ex){
            ex.printStackTrace();
        }
        roundTile.setTypeface(ThemeFont);

        return info;
    }
}
