package com.cerebrum.locationtracker;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.cerebrum.locationtracker.viewmodels.LocationViewModel;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;



public class MainActivity extends BaseActivity implements View.OnClickListener {


    private AppCompatButton btnLocationUpdate;
    private GoogleMap googleMapHomeFrag;
    private SupportMapFragment mSupportMapFragment;


    private MyLocationService mService = null;
    private boolean mServiceConnected = false;
    private LocationViewModel viewModel;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLocationUpdate = findViewById(R.id.btn_location_update);
        mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        btnLocationUpdate.setOnClickListener(this);

        viewModel = ViewModelProviders.of(this).get(LocationViewModel.class);

        initMap();
    }




    private void initMap() {
        mSupportMapFragment.getMapAsync(googleMap -> {
            if (googleMap != null) {
                googleMapHomeFrag = googleMap;
                googleMapHomeFrag.getUiSettings().setAllGesturesEnabled(true);
                googleMapHomeFrag.getUiSettings().setScrollGesturesEnabled(true);
                googleMapHomeFrag.getUiSettings().setCompassEnabled(true);
                googleMapHomeFrag.getUiSettings().setMapToolbarEnabled(false);


                askLocationPermissions(() ->
                        checkLocationSettings(() ->
                                {
                                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        return;
                                    }
                                    else {
                                        googleMapHomeFrag.setMyLocationEnabled(true);
                                        googleMapHomeFrag.getUiSettings().setMyLocationButtonEnabled(true);
                                        viewModel.findLastKnownLocation().observe(this, location ->
                                                updateLocationUI(location)
                                        );
                                    }
                                }
                        )
                );

            }
        });
    }




    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_location_update:
                if (isMyServiceRunning(MyLocationService.class) && mService != null) {
                    if (mService.isLocationUpdating()) {
                        stopLocationUpdates();
                    }
                    else {
                        startLocationUpdates();
                    }
                }
                else {
                    // Start location updated after checking location permission;
                    checkLocationPermission();
                }
                break;
        }
    }




    @Override
    protected void onResume() {
        super.onResume();
        setLocationUpdateStatus();
        getServiceStatus();
    }




    private void getServiceStatus() {
        if (isMyServiceRunning(MyLocationService.class)) {
            Intent intent = new Intent(MainActivity.this, MyLocationService.class);
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }




    private void checkLocationPermission() {
        askLocationPermissions(() ->
                checkLocationSettings(() ->
                        startLocationService())
        );
    }




    // To start service and stating location updates;
    private void startLocationService() {
        Intent serviceIntent = new Intent(this, MyLocationService.class);
        serviceIntent.putExtra("ACTION_TYPE", "START_LOCATION_TRACKING");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        }
        else {
            startService(serviceIntent);
        }
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        btnLocationUpdate.setText("Stop Location Updates");
    }




    private void startLocationUpdates() {
        if (mService != null) {
            btnLocationUpdate.setText("Stop Location Updates");
            mService.startLocationTracking();
        }
    }




    private void stopLocationUpdates() {
        Toast.makeText(this, "Location updates stopped.", Toast.LENGTH_SHORT).show();
        mServiceConnected = false;
        btnLocationUpdate.setText("Start Location Updates");

        Intent serviceIntent = new Intent(this, MyLocationService.class);
        mService.stopForeground(true);
        unbindService(mServiceConnection);
        mService.stopService(serviceIntent);
    }




    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override

        public void onServiceConnected(ComponentName className, IBinder binder) {

            Log.d("LocationTest", "onServiceConnected");
            mService = ((MyLocationService.LocalBinder) binder).getService();
            mServiceConnected = true;
            if (!mService.isLocationUpdating()) {
                mService.startLocationTracking();
            }

            receiveUpdates();
            setLocationUpdateStatus();
        }




        @Override

        public void onServiceDisconnected(ComponentName className) {

            mService = null;
            mServiceConnected = false;
            Log.d("LocationTest", "onServiceDisconnected");

        }




        @Override
        public void onBindingDied(ComponentName name) {
            Log.d("LocationTest", "onBindingDied");
        }
    };




    private void setLocationUpdateStatus() {
        if (mService != null) {
            if (mService.isLocationUpdating()) {
                btnLocationUpdate.setText("Stop Location Updates");
            }
            else {
                btnLocationUpdate.setText("Start Location Updates");
            }
        }
        else {
            btnLocationUpdate.setText("Start Location Updates");
        }
    }




    public void receiveUpdates() {
        mService.getLocationUpdates().observe(this, location -> {
            Toast.makeText(mService, location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_SHORT).show();
            Log.d("LocationUpdates", "receiveUpdates:  " + location.getLatitude() + " " + location.getLongitude());
            updateLocationUI(location);
        });
    }




    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }




    private void updateLocationUI(Location location) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18);
        googleMapHomeFrag.moveCamera(cameraUpdate);
        googleMapHomeFrag.animateCamera(cameraUpdate);
    }




    @Override
    protected void onStop() {
        super.onStop();
        if (mService != null) {
            mService.getLocationUpdates().removeObservers(this);
        }
    }


}
