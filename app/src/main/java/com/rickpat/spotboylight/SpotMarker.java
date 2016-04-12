package com.rickpat.spotboylight;


import com.rickpat.spotboylight.spotboy_db.SpotLocal;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;

public class SpotMarker extends Marker {

    private SpotLocal spot;

    public SpotMarker(MapView mapView, SpotLocal spot) {
        super(mapView);
        this.spot = spot;
        this.setPosition(spot.getGeoPoint());
    }

    public SpotLocal getSpot() {
        return spot;
    }
}
