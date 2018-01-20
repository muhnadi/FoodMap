package com.nerdgeeks.foodmap.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.nerdgeeks.foodmap.model.PlaceDeatilsModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hp on 2/6/2017.
 */

public class PrefManager {
    public static final String TAG = PrefManager.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    private Context mContext;

    private static final String PREF_NAME = "foodmap";

    private static final String MAP_ARRAY = "maplist";

    public PrefManager(Context mContext) {
        this.mContext = mContext;
        int PRIVATE_MODE = 0;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
    }

    public void storeData(ArrayList<PlaceDeatilsModel> arrayList){
        Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        editor.putString(MAP_ARRAY,gson.toJson(arrayList));
        editor.apply();
        Log.e(TAG,"Inserted");
    }

    public ArrayList<PlaceDeatilsModel> readData(){
        List<PlaceDeatilsModel> modelArrayList;

        if(sharedPreferences.contains(MAP_ARRAY)){
            Gson gson = new Gson();
            String json = sharedPreferences.getString(MAP_ARRAY,null);
            PlaceDeatilsModel [] placeDeatilsModels = gson.fromJson(json,PlaceDeatilsModel[].class);

            modelArrayList = Arrays.asList(placeDeatilsModels);
            modelArrayList = new ArrayList<>(modelArrayList);
        } else {
            return null;
        }
        return (ArrayList<PlaceDeatilsModel>) modelArrayList;
    }

    public boolean isPrefAvailable(){
        return sharedPreferences.contains(MAP_ARRAY);
    }
}
