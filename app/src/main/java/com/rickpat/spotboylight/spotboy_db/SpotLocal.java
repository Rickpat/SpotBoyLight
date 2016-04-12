package com.rickpat.spotboylight.spotboy_db;

import com.rickpat.spotboylight.Utilities.SpotType;

import org.osmdroid.util.GeoPoint;

import java.util.Date;
import java.util.List;

public class SpotLocal extends Spot {

    private List<String> fileStringList;

    public SpotLocal(String id, GeoPoint geoPoint, String notes, String uri, Date date, SpotType spotType) {
        super(id, geoPoint, notes, uri, date, spotType);
    }

    public SpotLocal(String id, GeoPoint geoPoint, String notes, Date date, SpotType spotType, List<String> fileUriList) {
        super(id, geoPoint, notes, "", date, spotType);
        this.fileStringList = fileUriList;
    }

    public List<String> getFileStringList() {
        return fileStringList;
    }

    public void setFileStringList(List<String> fileStringList) {
        this.fileStringList = fileStringList;
    }

    @Deprecated
    @Override
    public String getUri() {
        return super.getUri();
    }

    @Deprecated
    @Override
    public void setUri(String uri) {
        super.setUri(uri);
    }

    /*
     * SpotLocal describes an spot by its category and some notes that can be added
     * plus longitude and latitude.
     * It is stored locally
     * */

}