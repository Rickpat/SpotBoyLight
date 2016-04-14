package com.rickpat.spotboylight;

import android.content.Context;
import android.location.Location;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

/*
* to display user position on the map
* */

public class MyPositionOverlay extends MyLocationNewOverlay {
    public MyPositionOverlay(Context context, MapView mapView) {
        super(context, mapView);
    }

    /*
    * called when the device receives new gps data
    * */
    @Override
    public void onLocationChanged(Location location, IMyLocationProvider source) {
        super.onLocationChanged(location, source);
    }
}
