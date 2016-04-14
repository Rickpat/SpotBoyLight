package com.rickpat.spotboylight.Utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.rickpat.spotboylight.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Utilities {
    private Utilities(){}

    /*
    * loads items from enum to string array
    * */
    public static String[] getLibrariesStringArray(){
        String[] librariesArray = new String[Library.values().length];
        int help = 0;
        for (Library lib : Library.values()){
            librariesArray[help] = lib.toString();
            help++;
        }
        Arrays.sort(librariesArray);
        return librariesArray;
    }

    /*
    * loads items from enum to string array
    * */
    public static String[] getSpotTypes(){
        String[] items = new String[SpotType.values().length];
        int i = 0;
        for( SpotType category : SpotType.values()){
            items[i] = category.toString().toUpperCase();
            i++;
        }
        Arrays.sort(items);
        return items;
    }

    /*
    * loads Bitmap from resources by given spotType for SpotCluster class
    * */
    public static Bitmap getClusterIcon(Context applicationContext, SpotType spotType) {
        Bitmap icon;
        int resourceId = 0;
        switch (spotType){
            case dirt:
                resourceId = R.drawable.ic_dirtcluster;
                break;
            case park:
                resourceId = R.drawable.ic_parkcluster;
                break;
            case street:
                resourceId = R.drawable.ic_streetcluster;
                break;
            case flat:
                resourceId = R.drawable.ic_flatcluster;
        }
        icon = BitmapFactory.decodeResource(applicationContext.getResources(), resourceId);
        return icon;
    }

    /*
    * loads Drawable from resources by given spotType for SpotMarker class
    * */
    public static Drawable getMarkerIcon(Context applicationContext, SpotType spotType) {
        Drawable icon;
        int resourceId = 0;
        switch (spotType){
            case dirt:
                resourceId = R.drawable.ic_dirtmarker;
                break;
            case park:
                resourceId = R.drawable.ic_parkmarker;
                break;
            case street:
                resourceId = R.drawable.ic_streetmarker;
                break;
            case flat:
                resourceId = R.drawable.ic_flatmarker;
        }

        icon = ResourcesCompat.getDrawable(applicationContext.getResources(), resourceId, null);
        return icon;
    }

    /*
    * takes spot type string and returns SpotType
    * */
    public static SpotType parseSpotTypeString(String spotType){
        SpotType type = null;
        for (SpotType item : SpotType.values()){
            if (spotType.equalsIgnoreCase(item.toString())){
                type = item;
            }
        }
        return type;
    }

}
