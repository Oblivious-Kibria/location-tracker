package com.cerebrum.locationtracker;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import com.cerebrum.locationtracker.preference.AppPreference;
import com.cerebrum.locationtracker.preference.PrefKey;
import com.cerebrum.locationtracker.viewmodels.LocationViewModel;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;



public class MainActivity extends BaseActivity implements View.OnClickListener {


    private LocationViewModel viewModel;
    private AppCompatButton btnLocationUpdate;
    private GoogleMap googleMapHomeFrag;
    private AppPreference appPreference;
    private SupportMapFragment mSupportMapFragment;


    private MyLocationService mService = null;
    boolean mServiceConnected = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLocationUpdate = findViewById(R.id.btn_location_update);
        mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        btnLocationUpdate.setOnClickListener(this);


        viewModel = ViewModelProviders.of(this).get(LocationViewModel.class);
        viewModel.findLastKnownLocation();
        appPreference = AppPreference.getInstance(getApplicationContext());
        initMap();


        setLocationUpdateStatus();
    }




    private void initMap() {
        mSupportMapFragment.getMapAsync(googleMap -> {
            if (googleMap != null) {
                googleMapHomeFrag = googleMap;
                googleMapHomeFrag.getUiSettings().setAllGesturesEnabled(true);
                googleMapHomeFrag.getUiSettings().setScrollGesturesEnabled(true);
                googleMapHomeFrag.getUiSettings().setCompassEnabled(true);
                googleMapHomeFrag.getUiSettings().setMapToolbarEnabled(false);

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                else {
                    googleMapHomeFrag.setMyLocationEnabled(true);
                    googleMapHomeFrag.getUiSettings().setMyLocationButtonEnabled(true);
                }

                viewModel.getLastKnownLocation().observe(this, location -> {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16);
                    googleMapHomeFrag.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                    googleMapHomeFrag.animateCamera(cameraUpdate);
                });

            }
        });
    }




    private void setLocationUpdateStatus() {
        if (appPreference.getBoolean(PrefKey.IS_LOCATION_UPDATING)) {
            btnLocationUpdate.setText("STOP LOCATION UPDATES");
            startLocationUpdates();
        }
        else {
            btnLocationUpdate.setText("START UPDATING LOCATION");
        }
    }




    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_location_update:
//                if (appPreference.getBoolean(PrefKey.IS_LOCATION_UPDATING)) {
//                    // Location is updating, so start stop updating location;
//                    btnLocationUpdate.setText("START UPDATING LOCATION");
//                    appPreference.setBoolean(PrefKey.IS_LOCATION_UPDATING, false);
//                    stopLocationUpdates();
//                }
//                else {
//                    // Location not updating, so start updating location;
                    startLocationUpdates();
                    btnLocationUpdate.setText("STARTING LOCATION UPDATES ...");
              //  }

                break;
        }
    }




    private void startLocationUpdates() {
        askLocationPermissions(() ->
                checkLocationSettings(() -> {
                    Intent serviceIntent = new Intent(this, MyLocationService.class);
                    serviceIntent.putExtra("ACTION_TYPE", "START_LOCATION_TRACKING");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent);
                        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
                    }
                    else {
                        startService(serviceIntent);
                        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
                    }
                })
        );
    }




    private void stopLocationUpdates() {
        Toast.makeText(this, "Location updates stopped.", Toast.LENGTH_SHORT).show();
        viewModel.stopLocationUpdates();
    }




    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override

        public void onServiceConnected(ComponentName className, IBinder binder) {

            Log.d("LocationTest", "Connected to service.");
            mService = ((MyLocationService.LocalBinder) binder).getService();
            mServiceConnected = true;
            receiveUpdates();
        }




        @Override

        public void onServiceDisconnected(ComponentName className) {

            Log.d("LocationTest", "Disconnected from service.");
            mService = null;
            mServiceConnected = false;

        }

    };




    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mServiceConnected = false;
    }




    public void receiveUpdates() {
        mService.getLocationUpdates().observe(this, location -> {
            Log.d("LocationTest", "receiveUpdates:  " + location.getLatitude() + " " + location.getLongitude());
        });
    }


}
