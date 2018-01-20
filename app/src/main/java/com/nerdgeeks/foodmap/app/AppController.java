package com.nerdgeeks.foodmap.app;

/**
 * Created by hp on 11/18/2016.
 */

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.nerdgeeks.foodmap.cache.BitmapLruCache;
import com.nerdgeeks.foodmap.helper.ConnectivityReceiver;
import com.nerdgeeks.foodmap.service.MapService;


import static com.nerdgeeks.foodmap.app.AppConfig.TAG;

public class AppController extends Application {

    private RequestQueue mRequestQueue;
    private static AppController mInstance;
    private ImageLoader mImageLoader;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        startService(new Intent(this, MapService.class));
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    BitmapLruCache.getInstance());
        }
        return this.mImageLoader;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

    public void setPermissionListener(ConnectivityReceiver.GpsStatusReceiverListener listener) {
        ConnectivityReceiver.gpsStatusReceiver = listener;
    }
}