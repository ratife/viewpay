package com.markelys.viewpay;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;


import java.util.Timer;
import java.util.TimerTask;

import androidx.core.content.ContextCompat;


/**
 * Created by Herbert TOMBO on 02/02/2018.
 */

class ViewPayLocationManager {

    protected ViewPayDataManager data;
    private Context ctx;

    private Timer timer;
    private LocationManager lm;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;

    public ViewPayLocationManager(Context context) {
        ctx = context;
        data = ViewPayDataManager.getInstance();

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        try{
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (gps_enabled)
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                        locationListenerGps);
            if (network_enabled)
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                        locationListenerNetwork);
            timer=new Timer();
            timer.schedule(new GetLastLocation(), 10);
        }
        catch (Exception e){

        }
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer.cancel();
            setLocation(location);
            lm.removeUpdates(this);
            lm.removeUpdates(locationListenerNetwork);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer.cancel();
            setLocation(location);
            lm.removeUpdates(this);
            lm.removeUpdates(locationListenerGps);

        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private void setLocation(Location loc){
        double lon = (double) (loc.getLongitude());
        double lat = (double) (loc.getLatitude());

        data.setLatitude(lat + "");
        data.setLongitude(lon + "");
    }

    class GetLastLocation extends TimerTask {

        @Override
        public void run() {
            lm.removeUpdates(locationListenerGps);
            lm.removeUpdates(locationListenerNetwork);

            Location net_loc=null, gps_loc=null;
            if(gps_enabled)
                gps_loc=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(network_enabled)
                net_loc=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if(gps_loc!=null && net_loc!=null){
                if(gps_loc.getTime()>net_loc.getTime())
                {
                    setLocation(gps_loc);
                }
                else{
                    setLocation(net_loc);
                }
            }

            if(gps_loc!=null){
                setLocation(gps_loc);
            }
            if(net_loc!=null){
                setLocation(net_loc);
            }
        }
    }
}