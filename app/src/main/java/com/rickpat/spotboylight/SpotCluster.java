package com.rickpat.spotboylight;

import android.content.Context;

import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;

/*
* A SpotCluster is a container for SpotMarker objects.
* */

public class SpotCluster extends RadiusMarkerClusterer {

    public SpotCluster(Context ctx) {
        super(ctx);
    }
}
