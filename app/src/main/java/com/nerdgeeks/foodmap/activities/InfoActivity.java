package com.nerdgeeks.foodmap.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.model.LatLng;
import com.nerdgeeks.foodmap.app.AppController;
import com.nerdgeeks.foodmap.R;
import com.nerdgeeks.foodmap.adapter.TabsAdapter;
import com.nerdgeeks.foodmap.fragments.MapFragment;
import com.nerdgeeks.foodmap.fragments.PhotosFragment;
import com.nerdgeeks.foodmap.fragments.ReviewsFragment;
import com.nerdgeeks.foodmap.model.TabsItem;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.nerdgeeks.foodmap.app.AppConfig.*;

public class InfoActivity extends AppCompatActivity implements MaterialTabListener {

    private ProgressDialog progressDialog;
    private List<TabsItem> mTabs = new ArrayList<>();
    private int pos;
    private TextView mPhone, mWeb, mName, mVicnity, mOpen, mRate, mTime, mDistance;
    private String web, phone, mapUrl, iconUrl, placeId, photoUrl, photoReference;
    private MaterialTabHost tabHost;
    private ViewPager viewPager;
    private ImageView mThumbs;
    private RatingBar ratingBar;
    private String resName;
    private TabsAdapter tabAdapter;
    private String Latitude, Longitude;
    double latitude, longitude;
    private int[] icon = {R.drawable.ic_map, R.drawable.ic_landscape, R.drawable.ic_feedback};

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/HelveticaNeue.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        setContentView(R.layout.activity_info);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

        pos = getIntent().getIntExtra("position", 0);
        placeId = getIntent().getStringExtra("placeId");
        latitude = getIntent().getDoubleExtra("lat",0);
        longitude = getIntent().getDoubleExtra("lng",0);

        Latitude = String.valueOf(latitude);
        Longitude = String.valueOf(longitude);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");

        photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="
                + photoReference
                + "&key="
                + "AIzaSyAbNDLy8J2oefyHeY-47pFrtU8EQl1Q04g";

        mThumbs = (ImageView) findViewById(R.id.imgThumb);

        mName = (TextView) findViewById(R.id.nName);
        mVicnity = (TextView) findViewById(R.id.nVicnity);
        mOpen = (TextView) findViewById(R.id.nOpen);
        mRate = (TextView) findViewById(R.id.nRate);
        mPhone = (TextView) findViewById(R.id.nPhone);
        mWeb = (TextView) findViewById(R.id.nWeb);
        mTime = (TextView) findViewById(R.id.nTime);
        mDistance = (TextView) findViewById(R.id.nDistance);

        ratingBar = (RatingBar) findViewById(R.id.rateBar);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        LoadInformation(placeId);
        createTabsItem();

        /**
         *Set an Adapter for the View Pager
         */
        tabAdapter = new TabsAdapter(getSupportFragmentManager(), mTabs);
        viewPager.setOffscreenPageLimit(mTabs.size());
        viewPager.setAdapter(tabAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // when user do a swipe the selected tab change
                tabHost.setSelectedNavigationItem(position);
            }
        });

        //Adding TabHost
        tabHost = (MaterialTabHost) findViewById(R.id.materialTabHost);

        // insert all tabs from pagerAdapter data
        for (int i = 0; i < tabAdapter.getCount(); i++) {
            tabHost.addTab(
                    tabHost.newTab()
                            .setIcon(getResources().getDrawable(icon[i]))
                            .setTabListener(InfoActivity.this)
            );
        }

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        finish();
                        startActivity(intent);
                    }
                }, 700);

            }
        });

        //final CoordinatorLayout layout = findViewById(R.id.activity_info);

        View llBottomSheet = findViewById(R.id.card);

        // init the bottom sheet behavior
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                // this part hides the button immediately and waits bottom sheet
                // to collapse to show
                if (BottomSheetBehavior.STATE_DRAGGING == newState) {
                    fab.animate().scaleX(0).scaleY(0).setDuration(300).start();
                } else if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                    fab.animate().scaleX(1).scaleY(1).setDuration(300).start();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                viewPager.animate().translationY(1 - slideOffset).setDuration(0).start();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        finish();
        return true;
    }

    private void createTabsItem() {
        mTabs.add(new TabsItem(MapFragment.newInstance(placeId,Latitude,Longitude)));
        mTabs.add(new TabsItem(PhotosFragment.newInstance(placeId, "")));
        mTabs.add(new TabsItem(ReviewsFragment.newInstance(placeId, "")));
    }

    private void LoadInformation(String placeId) {
        progressDialog.show();
        String googlePlaceDetails = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" +
                placeId +
                "&sensor=false" +
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

                                if (!jsonObject.isNull("formatted_address")) {
                                    String vicnity = jsonObject.getString("formatted_address");
                                    mVicnity.setText(vicnity);
                                } else {
                                    mVicnity.setText("N/A");
                                }

                                if (!jsonObject.isNull("international_phone_number")) {
                                    phone = jsonObject.getString("international_phone_number");
                                    mPhone.setText("Phone : " + phone);
                                    mPhone.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startCall();
                                        }
                                    });
                                } else {
                                    mPhone.setText("N/A");
                                }

                                if (!jsonObject.isNull("icon")) {
                                    iconUrl = jsonObject.getString("icon");
                                    Picasso.with(InfoActivity.this)
                                            .load(iconUrl)
                                            .into(mThumbs);
                                }

                                if (!jsonObject.isNull("name")) {
                                    String name = jsonObject.getString("name");
                                    mName.setText(name);
                                }

                                String open = jsonObject.getJSONObject("opening_hours").getString("open_now");
                                Boolean OPEN = Boolean.parseBoolean(open);

                                if (OPEN) {
                                    mOpen.setText("Opened Now");
                                }

                                if (!OPEN) {
                                    mOpen.setText("Closed Now");
                                }

                                if (!jsonObject.isNull("website")) {
                                    web = jsonObject.getString("website");
                                    mWeb.setText("Website : " + web);
                                    mWeb.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent urlIntent = new Intent(InfoActivity.this, WebActivity.class);
                                            urlIntent.putExtra("url", web);
                                            startActivity(urlIntent);
                                        }
                                    });
                                } else {
                                    mWeb.setText("N/A");
                                }

                                if (!jsonObject.isNull("url")) {
                                    mapUrl = jsonObject.getString("url");
                                }

                                if (!jsonObject.isNull("rating")) {
                                    String rate = jsonObject.getString("rating");
                                    mRate.setText(rate);
                                    ratingBar.setRating(Float.parseFloat(rate));
                                }else {
                                    mRate.setText("N/A");
                                }

                                String mLat = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat");
                                double lat = Double.parseDouble(mLat);

                                String mLong = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lng");
                                double lng = Double.parseDouble(mLong);

                                if (!jsonObject.isNull("name")) {
                                    resName = jsonObject.getString("name");
                                } else {
                                    resName = "";
                                }
                                LoadDistanceTime(new LatLng(latitude,longitude),new LatLng(lat,lng));
                                progressDialog.dismiss();

                            } else if (result.getString("status").equalsIgnoreCase("ZERO_RESULTS")) {
                                Toast.makeText(getBaseContext(), "No Information found!!!",
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "parseLocationResult: Error=" + e.getMessage());
                            progressDialog.dismiss();
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

    private void LoadDistanceTime(LatLng origin, LatLng destination){
        String URL = "http://maps.googleapis.com/maps/api/distancematrix/json?"
                + "origins=" + origin.latitude
                + "," + origin.longitude
                + "&destinations=" + destination.latitude
                + "," + destination.longitude
                + "&mode=walking"
                + "&sensor=false";

        JsonObjectRequest request = new JsonObjectRequest(URL,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject result) {
                        Log.i(TAG, "onResponse: Result= " + result.toString());
                        try {
                            if (result.getString("status").equalsIgnoreCase("OK")) {

                                JSONArray jsonRows = result.getJSONArray("rows");
                                for (int i=0; i<jsonRows.length(); i++){
                                    JSONArray jsonElements = ( (JSONObject)jsonRows.get(i)).getJSONArray("elements");
                                    for (int j=0; j<jsonElements.length(); j++){
                                        JSONObject jsonDistance = jsonElements.getJSONObject(j).getJSONObject("distance");
                                        JSONObject jsonDuration = jsonElements.getJSONObject(j).getJSONObject("duration");

                                        String distance = jsonDistance.getString("text");
                                        mDistance.setText(distance);

                                        String duration = jsonDuration.getString("text");
                                        mTime.setText(duration);
                                    }
                                }

                            } else if (result.getString("status").equalsIgnoreCase("ZERO_RESULTS")) {
                                Toast.makeText(getBaseContext(), "No Information found!!!",
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "parseLocationResult: Error=" + e.getMessage());
                            progressDialog.dismiss();
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

    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.info_option, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.phone:
                startCall();
                return true;
            case R.id.web:
                startWeb(web);
                return true;
            case R.id.explore:
                exploreMap(mapUrl);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void exploreMap(String url){
        if(url!=null){
            Intent mapUrlIntent = new Intent(InfoActivity.this, WebActivity.class);
            mapUrlIntent.putExtra("url", url);
            startActivity(mapUrlIntent);
        }else {
            new AlertDialog.Builder(InfoActivity.this)
                    .setIcon(R.drawable.ic_map_grey)
                    .setTitle("Unavailable")
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }

    public void startCall() {
        if (phone != null) {
            new AlertDialog.Builder(InfoActivity.this)
                    .setIcon(R.drawable.ic_phone_grey)
                    .setTitle("Call this number?")
                    .setMessage(phone)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    try {
                                        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                                        phoneIntent.setData(Uri.parse("tel:" + phone));
                                        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                            return;
                                        }
                                        startActivity(phoneIntent);
                                    } catch (SecurityException e) {
                                        Toast.makeText(InfoActivity.this,
                                                "Call failed, please try again later!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                    .show();
        } else {
            new AlertDialog.Builder(InfoActivity.this)
                    .setIcon(R.drawable.ic_phone_grey)
                    .setTitle("Phone Number Unavailable")
                    .setMessage(phone)
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }

    public void startWeb(String webUrl) {
        if (webUrl != null) {
            Intent urlIntent = new Intent(InfoActivity.this, WebActivity.class);
            urlIntent.putExtra("url", webUrl);
            startActivity(urlIntent);
        } else {
            new AlertDialog.Builder(InfoActivity.this)
                    .setIcon(R.drawable.ic_web_grey)
                    .setTitle("Website Unavailable")
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }

    @Override
    public void onTabSelected(MaterialTab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(MaterialTab tab) {

    }

    @Override
    public void onTabUnselected(MaterialTab tab) {

    }
}
