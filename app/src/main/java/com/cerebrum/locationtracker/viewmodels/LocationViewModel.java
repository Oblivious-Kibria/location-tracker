package com.cerebrum.locationtracker.viewmodels;

import android.app.Application;
import android.content.Intent;
import android.location.Location;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.cerebrum.locationtracker.MyLocationService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;



/**
 * Created by User on 9/4/2019.
 */
public class LocationViewModel extends AndroidViewModel {


    private FusedLocationProviderClient fusedLocationClient;
    private MutableLiveData<Location> getLastLocation;




    public LocationViewModel(@NonNull Application application) {
        super(application);
        getLastLocation = new MutableLiveData<>();
    }




    public MutableLiveData<Location> findLastKnownLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication());
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                getLastLocation.setValue(location);
                            }
                        }
                );
        return getLastLocation;
    }




}
