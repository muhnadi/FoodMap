package com.nerdgeeks.foodmap.Api;

import com.nerdgeeks.foodmap.app.AppConfig;
import com.nerdgeeks.foodmap.model.PlaceModelCall;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by TAOHID on 1/21/2018.
 */

public interface ApiInterface {

    String key = AppConfig.GOOGLE_MAP_API_KEY;

//    @GET
//    Call<PlaceModelCall> getPlaceModel(@Url String mUrl);

    @GET("api/place/nearbysearch/json?sensor=false&key="+key)
    Call<PlaceModelCall> getNearbyPlaces(@Query("type") String type, @Query("location") String location, @Query("radius") int radius);
}