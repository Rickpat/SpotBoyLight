package com.rickpat.spotboylight.Utilities;

public class Constants {
    private Constants() {
    }

    public static final String SPOT_TYPE = "SPOT_TYPE";
    public static final String NOTES = "NOTES";
    public static final String IMG_PATH = "IMG_PATH";
    public static final String URI_SET = "URI_SET";
    public static final String SPOT = "SPOT";

    public static String PREFERENCES = "PREFERENCES";
    public static String GEOPOINT = "GEOPOINT";
    public static String ZOOM_LEVEL = "ZOOM_LEVEL";
    public static String MODIFIED = "MODIFIED";

    public static final String TIME_FORMAT = "HH:mm";
    public static final String KML_FILE = "KML_FILE";

    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    public static final int NEW_SPOT_REQUEST = 101;
    public static final int NEW_SPOT_CREATED = 102;
    public static final int NEW_SPOT_CANCELED = 103;

    public static final int INFO_ACTIVITY_REQUEST = 201;
    public static final int INFO_ACTIVITY_SPOT_DELETED = 202;
    public static final int INFO_ACTIVITY_SPOT_MODIFIED = 203;

    public static final int HUB_REQUEST = 301;
    public static final int HUB_SHOW_ON_MAP = 302;

    public static final int KML_REQUEST = 401;
    public static final int KML_LOAD = 402;
    public static final int KML_REMOVE = 403;

    public static final int VIEW_PAGER_MAX_FRAGMENTS = 3;
}
