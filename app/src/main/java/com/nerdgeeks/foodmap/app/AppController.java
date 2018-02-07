package com.nerdgeeks.foodmap.app;

/**
 * Created by hp on 11/18/2016.
 */

import android.app.Application;
import com.nerdgeeks.foodmap.helper.ConnectivityReceiver;



public class AppController extends Application {

    private static AppController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

    public void setPermissionListener(ConnectivityReceiver.GpsStatusReceiverListener listener) {
        ConnectivityReceiver.gpsStatusReceiver = listener;
    }
}