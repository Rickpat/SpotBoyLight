package com.rickpat.spotboylight.spotboy_db;

import com.rickpat.spotboylight.Utilities.SpotType;

import org.osmdroid.util.GeoPoint;

import java.util.Date;

public class Spot {
    private int id;
    private String category;
    private String longitude;
    private String latitude;
    private String notes;
    private String uri;
    private Date date;
    private GeoPoint geoPoint;
    private SpotType spotType;

    public Spot( String category, String longitude, String latitude, String notes, String uri, Date date) {
        this.category = category;
        this.longitude = longitude;
        this.latitude = latitude;
        this.notes = notes;
        this.uri = uri;
        this.date = date;
        this.id = -1;
        float lon = Float.valueOf(longitude);
        float lat = Float.valueOf(latitude);
        this.geoPoint = new GeoPoint(lon,lat);
        setSpotType();
    }

    private void setSpotType() {
        for (SpotType spotType : SpotType.values()){
            if (spotType.toString().equalsIgnoreCase(category)){
                this.spotType = spotType;
                break;
            }
        }
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public Date getDate() {
        return date;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getCategory() {
        return category;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getNotes() {
        return notes;
    }

    public String getUri() {
        return uri;
    }

    public int getId() {
        return id;
    }

    public void setCategory(String category) {
        this.category = category;
        setSpotType();
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }


    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public void setSpotType(SpotType spotType) {
        this.spotType = spotType;
    }
}
