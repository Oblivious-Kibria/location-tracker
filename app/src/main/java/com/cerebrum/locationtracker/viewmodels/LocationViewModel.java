package com.cerebrum.locationtracker.viewmodels;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;



/**
 * Created by User on 9/4/2019.
 */
public class LocationViewModel extends AndroidViewModel {


    private FusedLocationProviderClient fusedLocationClient;
    private MutableLiveData<Location> lastLocationMutableLiveData;
    private MutableLiveData<Location> locationMutableLiveData;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;




    public LocationViewModel(@NonNull Application application) {
        super(application);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication().getApplicationContext());
        locationMutableLiveData = new MutableLiveData<>();
        lastLocationMutableLiveData = new MutableLiveData<>();
        initLocationCallBack();
        createLocationRequest();
    }




    public void findLastKnownLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        Log.d("LocationTest", location.getLatitude() + " " + location.getLongitude());
                        lastLocationMutableLiveData.setValue(location);
                    }
                });
    }



    public MutableLiveData<Location> getLastKnownLocation(){
        return lastLocationMutableLiveData;
    }




    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }




    public void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }




    private void initLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data;
                    if (location != null) {
                        Log.d("LocationTest", "Updating location: " + location.getLatitude() + " " + location.getLongitude());
                        locationMutableLiveData.setValue(location);
                    }
                }
            }
        };

    }




    public MutableLiveData<Location> getLocation() {
        return locationMutableLiveData;
    }




    @Override
    protected void onCleared() {
        super.onCleared();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }




    public void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }


}
