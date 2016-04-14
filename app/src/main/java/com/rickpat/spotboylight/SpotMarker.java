package com.rickpat.spotboylight;


import com.rickpat.spotboylight.spotboy_db.Spot;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;

/*
* gets spot data through constructor
* */

public class SpotMarker extends Marker {

    private Spot spot;

    public SpotMarker(MapView mapView, Spot spot) {
        super(mapView);
        this.spot = spot;
        this.setPosition(spot.getGeoPoint());
    }

    public Spot getSpot() {
        return spot;
    }
}
