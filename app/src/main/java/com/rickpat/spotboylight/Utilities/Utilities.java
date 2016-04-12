package com.rickpat.spotboylight.Utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.rickpat.spotboylight.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Utilities {
    private Utilities(){}

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

    public static String getTimeString(){
        DateFormat df = new SimpleDateFormat(Constants.TIME_FORMAT);
        return df.format(new Date());
    }

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



    public static Bitmap decodeSampledBitmapFromResource(Resources res, String fileName,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(fileName, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            icon = applicationContext.getResources().getDrawable(resourceId,applicationContext.getTheme());
        }else{
            icon = applicationContext.getResources().getDrawable(resourceId);
        }
        return icon;
    }

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
