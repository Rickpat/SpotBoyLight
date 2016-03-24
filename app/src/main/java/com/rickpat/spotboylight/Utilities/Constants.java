package com.rickpat.spotboylight.Utilities;

public class Constants {
    private Constants() {
    }

    public static String PREFERENCES = "PREFERENCES";
    public static String GEO_POINT = "GEO_POINT";
    public static String ZOOM_LEVEL = "ZOOM_LEVEL";

    public static final String COORDINATES = "COORDINATES";
    public static final String NO_GPS = "NO_GPS";
    public static final String SPOT = "SPOT";
    public static final String TIME_FORMAT = "HH:mm";

    public static final int NEW_SPOT_REQUEST = 101;
    public static final int NEW_SPOT_CREATED = 102;
    public static final int NEW_SPOT_CANCELED = 103;

    public static final int INFO_ACTIVITY_REQUEST = 201;
    public static final int INFO_ACTIVITY_SPOT_DELETED = 202;

    public static final int HUB_REQUEST = 301;
    public static final int HUB_SHOW_ON_MAP = 302;
}
