package com.cerebrum.locationtracker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.cerebrum.locationtracker.listeners.LocationPermissionListeners;
import com.cerebrum.locationtracker.listeners.LocationSettingsListeners;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;



/**
 * Created by User on 9/4/2019.
 */
public class BaseActivity extends AppCompatActivity {


    private String[] locationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int LOCATION_PERMISSION_SETTING_REQUEST_CODE = 103;
    private LocationPermissionListeners locationPermissionListeners;




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }




    /*** Location getting functionality ***/
    public void askLocationPermissions(LocationPermissionListeners locationPermissionListeners) {
        this.locationPermissionListeners = locationPermissionListeners;

        if(Build.VERSION.SDK_INT >=28){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                // Permission not found;
                requestLocationPermission();
            }
            else {
                // Permission found;
                locationPermissionListeners.onPermissionFound();
            }
        }
        else if(Build.VERSION.SDK_INT >=23 && Build.VERSION.SDK_INT <=28){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Permission not found;
                requestLocationPermission();
            }
            else {
                // Permission found;
                locationPermissionListeners.onPermissionFound();
            }
        }

    }




    private void requestLocationPermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        locationPermissions[0],
                        locationPermissions[1])
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            locationPermissionListeners.onPermissionFound();
                            //Log.d(TAG, "PermissionTesting  onPermissionGranted");
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            //Log.d(TAG, "PermissionTesting:  isAnyPermissionPermanentlyDenied");
                            // permission is denied permanently, navigate user to app settings
                            showLocationSettingsDialog();
                        }
                    }




                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        //Log.d(TAG, "PermissionTesting  onPermissionRationaleShouldBeShown");
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(error -> Toast.makeText(BaseActivity.this, "Permission request error!", Toast.LENGTH_SHORT).show())
                .onSameThread()
                .check();
    }




    private void showLocationSettingsDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Location Permissions");
        builder.setMessage("This app needs location permission to know your whereabouts. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", (DialogInterface dialog, int which) -> {
            dialog.cancel();
            openLocationSettings();
        });
        builder.setNegativeButton("Cancel", (DialogInterface dialog, int which) ->
                dialog.cancel()
        );
        builder.show();
    }




    private void openLocationSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, LOCATION_PERMISSION_SETTING_REQUEST_CODE);
    }




    public void checkLocationSettings(LocationSettingsListeners locationSettingsListeners) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        SettingsClient client = LocationServices.getSettingsClient(getApplication());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            locationSettingsListeners.onLocationSettingsSatisfied();
        });

        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(BaseActivity.this,
                            LOCATION_PERMISSION_SETTING_REQUEST_CODE);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }


}
