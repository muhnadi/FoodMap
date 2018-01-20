package com.nerdgeeks.foodmap.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.nerdgeeks.foodmap.app.AppController;
import com.nerdgeeks.foodmap.R;
import com.nerdgeeks.foodmap.adapter.ReviewsAdapter;
import com.nerdgeeks.foodmap.model.ReviewsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

import static com.nerdgeeks.foodmap.app.AppConfig.GOOGLE_MAP_API_KEY;
import static com.nerdgeeks.foodmap.app.AppConfig.TAG;

public class ReviewsFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ArrayList<ReviewsModel> arrayReviews = new ArrayList<>();
    private ReviewsAdapter reviewsAdapter;
    private RecyclerView mRecyclerView;
    private TextView mTextView;

    public ReviewsFragment() {
        // Required empty public constructor
    }

    public static ReviewsFragment newInstance(String param1, String param2) {
        ReviewsFragment fragment = new ReviewsFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_reviews, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rView);
        mTextView = (TextView) rootView.findViewById(R.id.notifier);
        mTextView.setVisibility(View.INVISIBLE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new SlideInUpAnimator());
        mRecyclerView.setHasFixedSize(true);
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
                        try {
                            JSONObject jsonObject = result.getJSONObject("result");
                            JSONArray jsonArray = jsonObject.getJSONArray("reviews");

                            if (result.getString("status").equalsIgnoreCase("OK")) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject obj = jsonArray.getJSONObject(i);
                                    ReviewsModel model = new ReviewsModel();
                                    if (!obj.isNull("author_name")) {
                                        String Name = obj.getString("author_name");
                                        model.setAuthorName(Name);
                                    }
                                    if (!obj.isNull("profile_photo_url")) {
                                        String picUrl = obj.getString("profile_photo_url");
                                        model.setPhotoUrl(picUrl);
                                    }
                                    if (!obj.isNull("rating")) {
                                        String rating = obj.getString("rating");
                                        model.setRatings(rating);
                                    }
                                    if (!obj.isNull("text")) {
                                        String text = obj.getString("text");
                                        model.setAuthorText(text);
                                    }
                                    if (!obj.isNull("relative_time_description")) {
                                        String text = obj.getString("relative_time_description");
                                        model.setTime(text);
                                    }
                                    arrayReviews.add(model);
                                }

                                reviewsAdapter = new ReviewsAdapter(arrayReviews, getActivity());
                                reviewsAdapter.notifyDataSetChanged();

                                if (reviewsAdapter != null) {
                                    mRecyclerView.setAdapter(reviewsAdapter);
                                }
                            } else if (result.getString("status").equalsIgnoreCase("ZERO_RESULTS")) {
                                Toast.makeText(getActivity(), "No Information found!!!",
                                        Toast.LENGTH_LONG).show();
                                mTextView.setVisibility(View.VISIBLE);
                                mTextView.setText("No Reviews Found");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mTextView.setVisibility(View.VISIBLE);
                            mTextView.setText("No Reviews Found");
                            Log.e(TAG, "parseLocationResult: Error=" + e.getMessage());
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
}
