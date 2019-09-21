package com.cerebrum.locationtracker.viewmodels;

import android.app.Application;
import android.content.Intent;
import android.location.Location;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.cerebrum.locationtracker.MyLocationService;



/**
 * Created by User on 9/4/2019.
 */
public class LocationViewModel extends AndroidViewModel {


    private MutableLiveData<Location> lastLocationMutableLiveData;
    private MutableLiveData<Location> locationMutableLiveData;




    public LocationViewModel(@NonNull Application application) {
        super(application);
        locationMutableLiveData = new MutableLiveData<>();
        lastLocationMutableLiveData = new MutableLiveData<>();
    }




    public MutableLiveData<Location> getLastKnownLocation() {
        return lastLocationMutableLiveData;
    }




    public MutableLiveData<Location> getLocation() {
        return locationMutableLiveData;
    }




    public void findLastKnownLocation() {

    }




    public void startLocationUpdates() {
        Intent serviceIntent = new Intent(getApplication(), MyLocationService.class);
        serviceIntent.putExtra("ACTION_TYPE", "START_LOCATION_TRACKING");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication().startForegroundService(serviceIntent);
        }
        else {
            getApplication().startService(serviceIntent);
        }
    }




    public void stopLocationUpdates() {

    }


}
