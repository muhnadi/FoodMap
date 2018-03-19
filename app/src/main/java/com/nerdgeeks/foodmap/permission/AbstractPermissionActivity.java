package com.nerdgeeks.foodmap.permission;

/**
 * Created by TAOHID on 3/19/2018.
 */

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import java.util.ArrayList;

abstract public class AbstractPermissionActivity extends AppCompatActivity {

    abstract protected String[] getDesiredPermissions();
    abstract protected void onPermissionDenied();
    abstract protected void onReady();

    private static final int REQUEST_PERMISSION=61125;
    private static final String STATE_IN_PERMISSION="inPermission";
    private boolean isInPermission=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState!=null) {
            isInPermission=
                    savedInstanceState.getBoolean(STATE_IN_PERMISSION, false);
        }

        if (hasAllPermissions(getDesiredPermissions())) {
            onReady();
        }

        else if (!isInPermission) {
            isInPermission=true;

            ActivityCompat.requestPermissions(this, netPermissions(getDesiredPermissions()), REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        isInPermission=false;

        if (requestCode==REQUEST_PERMISSION) {
            if (hasAllPermissions(getDesiredPermissions())) {
                onReady();
            }
            else {
                onPermissionDenied();
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_IN_PERMISSION, isInPermission);
    }

    private boolean hasAllPermissions(String[] perms) {
        for (String perm : perms) {
            if (!hasPermission(perm)) {
                return(false);
            }
        }
        return(true);
    }

    private boolean hasPermission(String perm) {
        return(ContextCompat.checkSelfPermission(this, perm)==
                PackageManager.PERMISSION_GRANTED);
    }

    private String[] netPermissions(String[] wanted) {
        ArrayList<String> result=new ArrayList<>();
        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return(result.toArray(new String[result.size()]));
    }
}
