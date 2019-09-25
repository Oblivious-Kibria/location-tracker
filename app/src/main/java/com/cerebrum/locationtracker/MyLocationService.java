package com.cerebrum.locationtracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.cerebrum.locationtracker.preference.AppPreference;
import com.cerebrum.locationtracker.preference.PrefKey;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;



/**
 * Created by User on 9/12/2019.
 */
public class MyLocationService extends Service {


    private PowerManager.WakeLock powerManagerWakeLock;


    private static FusedLocationProviderClient fusedLocationClient;
    private static LocationCallback locationCallback;
    private static LocationRequest locationRequest;
    private static MutableLiveData<Location> locationMutableLiveData;
    private static AppPreference appPreference;




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }




    public class LocalBinder extends Binder {


        /**
         * Return enclosing BinderService instance
         */
        MyLocationService getService() {

            return MyLocationService.this;

        }


    }


    private final IBinder mBinder = new LocalBinder();




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LocationTest", "onStartCommand: ");
        //startLocationTracking();
        return START_STICKY;
    }




    public void startLocationTracking() {
        Log.d("LocationTest", "startLocationTracking: ");
        appPreference = AppPreference.getInstance(getApplicationContext());
        appPreference.setBoolean(PrefKey.IS_LOCATION_UPDATING, true);

        startFusedLocationProvider();
    }




    public void stopLocationTracking() {
        if (powerManagerWakeLock.isHeld()) {
            powerManagerWakeLock.release();
        }
        stopForeground(true);
        stopSelf();

        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.d("LocationTest", "Location Service is stopped 1");
        }
        appPreference = AppPreference.getInstance(getApplicationContext());
        appPreference.setBoolean(PrefKey.IS_LOCATION_UPDATING, false);
        Log.d("LocationTest", "Location Service is stopped 2");
    }




    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("LocationTest", "onCreate: ");
        locationMutableLiveData = new MutableLiveData<>();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        powerManagerWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");
        powerManagerWakeLock.acquire();
        startForeground(1, createNotification());
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("LocationTest", "OnDestroy");
    }




    private Notification createNotification() {

        String notificationChannelId = "LOCATION_TRACKER_SERVICE_CHANNEL";

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, "LOCATION_TRACKER_CHANNEL", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setDescription("Location Tracker");
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            long[] array = {100, 200, 300, 400, 500, 400, 300, 200, 400};
            notificationChannel.setVibrationPattern(array);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification.Builder notificationBuilder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(this, notificationChannelId);
        }
        else {
            notificationBuilder = new Notification.Builder(this);
        }


        return notificationBuilder
                .setContentTitle("Employee Location Tracker")
                .setContentText("Location tracker service working")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("location is updating")
                .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
                .build();
    }




    private void startFusedLocationProvider() {
        Log.d("LocationTest", "startFusedLocationProvider: ");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication().getApplicationContext());
        initLocationCallBack();
        createLocationRequest();

        startLocationUpdates();
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
                        locationMutableLiveData.postValue(location);
                    }
                }
            }
        };

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




    public MutableLiveData<Location> getLocationUpdates() {
        return locationMutableLiveData;
    }


}
