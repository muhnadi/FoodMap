package com.nerdgeeks.foodmap.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nerdgeeks.foodmap.app.AppController;
import com.nerdgeeks.foodmap.view.OnItemClickListener;
import com.nerdgeeks.foodmap.R;
import com.nerdgeeks.foodmap.activities.FullImageActivity;
import com.nerdgeeks.foodmap.adapter.PhotosAdapter;
import com.nerdgeeks.foodmap.model.PhotoModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.nerdgeeks.foodmap.app.AppConfig.GOOGLE_MAP_API_KEY;
import static com.nerdgeeks.foodmap.app.AppConfig.TAG;

public class PhotosFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView recyclerView;
    private ArrayList<PhotoModel> arrayPhotos = new ArrayList<>();
    private PhotosAdapter photoAdapter;
    private TextView mStatusText;

    public PhotosFragment() {
        // Required empty public constructor
    }

    public static PhotosFragment newInstance(String param1, String param2) {
        PhotosFragment fragment = new PhotosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_photos, container, false);

        if(!isAdded()) {
            return rootView;
        }

        recyclerView = (RecyclerView) rootView.findViewById(R.id.rView);
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
        mStatusText = (TextView) rootView.findViewById(R.id.notifier);
        mStatusText.setVisibility(View.INVISIBLE);
        LoadInformation(mParam1);
        return rootView;
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
                        String ref;
                        try {
                            JSONObject jsonObject = result.getJSONObject("result");
                            JSONArray jsonArray = jsonObject.getJSONArray("photos");

                            if (result.getString("status").equalsIgnoreCase("OK")) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject obj = jsonArray.getJSONObject(i);
                                    PhotoModel model = new PhotoModel();
                                    if (!obj.isNull("photo_reference")) {
                                        ref = obj.getString("photo_reference");
                                        model.setPhotoReference(ref);
                                        mStatusText.setVisibility(View.INVISIBLE);
                                        arrayPhotos.add(model);
                                    }
                                }
                                photoAdapter = new PhotosAdapter(arrayPhotos, getActivity());
                                photoAdapter.notifyDataSetChanged();

                                if (photoAdapter != null) {
                                    photoAdapter.setOnItemClickListener(recyclerRowClickListener);
                                    recyclerView.setAdapter(photoAdapter);
                                    recyclerView.setHasFixedSize(true);
                                } else {
                                    mStatusText.setText("No Photo Available");
                                    mStatusText.setVisibility(View.VISIBLE);
                                }
                            } else if (result.getString("status").equalsIgnoreCase("ZERO_RESULTS")) {
                                Toast.makeText(getActivity(), "No Information found!!!",
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "parseLocationResult: Error=" + e.getMessage());
                            mStatusText.setText("No Photo Available");
                            mStatusText.setVisibility(View.VISIBLE);
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

    private OnItemClickListener recyclerRowClickListener = new OnItemClickListener() {

        @Override
        public void onClick(View v, int position) {
            Intent detailIntent = new Intent(getActivity(), FullImageActivity.class);
            detailIntent.putExtra("pos", position);
            detailIntent.putExtra("arrayList",arrayPhotos);
            startActivity(detailIntent);
            getActivity().overridePendingTransition(R.anim.anim_enter, R.anim.anim_leave);

        }
    };
}
