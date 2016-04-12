package com.rickpat.spotboylight.spotboy_db;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rickpat.spotboylight.Utilities.SpotType;
import com.rickpat.spotboylight.Utilities.Utilities;

import org.osmdroid.util.GeoPoint;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpotBoyDBHelper extends SQLiteOpenHelper {

    private SQLiteDatabase db;

    private String log = "SpotBoyDBHelper";

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "spotBoy_db_v06";
    private static final String TABLE_SPOTS = "spotBoy_db_table_v06";

    private static final String KEY_ID = "id";
    private static final String KEY_TYPE = "type";
    private static final String KEY_NOTES = "notes";
    private static final String KEY_URI = "uri";
    private static final String KEY_DATE = "date";
    private static final String KEY_GEO = "geo";

    public SpotBoyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SPOTS_TABLE = "CREATE TABLE " + TABLE_SPOTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TYPE + " TEXT,"
                + KEY_NOTES + " TEXT,"+ KEY_URI + " TEXT,"+ KEY_GEO + " TEXT," + KEY_DATE + ")";
        db.execSQL(CREATE_SPOTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPOTS);
        onCreate(db);
    }

    public long updateSpot(Spot local){
        ContentValues values = new ContentValues();
        values.put(KEY_TYPE, local.getSpotType().toString());
        values.put(KEY_NOTES, local.getNotes());
        values.put(KEY_URI, local.getUri());
        values.put(KEY_DATE, String.valueOf(local.getDate().getTime()));
        return db.update(TABLE_SPOTS, values, "id=" + local.getId(), null);
    }

    public long addSpot(Spot local) {
        ContentValues values = new ContentValues();
        values.put(KEY_TYPE, local.getSpotType().toString());
        values.put(KEY_NOTES, local.getNotes());
        values.put(KEY_URI, local.getUri());
        values.put(KEY_GEO, new Gson().toJson(local.getGeoPoint()));
        values.put(KEY_DATE, local.getDate().getTime());
        return db.insert(TABLE_SPOTS, null, values);
    }

    public List<Spot> getSpotList(){
        List<Spot> localList = new ArrayList<>();

        Cursor cursor = db.query(TABLE_SPOTS,null,null,null,null,null,null);
        cursor.moveToFirst();
        int count = cursor.getCount();
        Log.d(log, "count: " + count);
        for (int i = 0 ; i < count ; i++){
            String id = String.valueOf(cursor.getLong(0));
            SpotType spotType = Utilities.parseSpotTypeString(cursor.getString(1));
            String notes = cursor.getString(2);
            String uri = cursor.getString(3);
            GeoPoint geoPoint = new Gson().fromJson(cursor.getString(4), GeoPoint.class);
            Date date = new Date(cursor.getLong(5));
            SpotLocal local = new SpotLocal(id ,geoPoint, notes, uri, date, spotType);

            Log.d(log, "id " + id +
                    "\ncat " + spotType +
                    "\nnotes " + notes +
                    "\nuri " + uri +
                    "\ntime " + date);
            localList.add(local);
            cursor.moveToNext();
        }
        cursor.close();
        return localList;
    }

    public int deleteSpot( int id ){
        Log.d(log, "deleting spot with id " + id);
        return db.delete(TABLE_SPOTS,"id=" + id, null);
    }

    public long addSpotMultipleImages( SpotLocal spot ){
        ContentValues values = new ContentValues();
        values.put(KEY_TYPE, spot.getSpotType().toString());
        values.put(KEY_NOTES, spot.getNotes());
        values.put(KEY_URI, new Gson().toJson(spot.getFileStringList()));
        values.put(KEY_GEO, new Gson().toJson(spot.getGeoPoint()));
        values.put(KEY_DATE, spot.getDate().getTime());
        return db.insert(TABLE_SPOTS, null, values);
    }

    public List<SpotLocal> getSpotListMultipleImages(){
        List<SpotLocal> localList = new ArrayList<>();

        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();

        Cursor cursor = db.query(TABLE_SPOTS,null,null,null,null,null,null);
        cursor.moveToFirst();
        int count = cursor.getCount();
        Log.d(log, "count: " + count);
        for (int i = 0 ; i < count ; i++){
            String id = String.valueOf(cursor.getLong(0));
            SpotType spotType = Utilities.parseSpotTypeString(cursor.getString(1));
            String notes = cursor.getString(2);
            List<String> uriList = new Gson().fromJson(cursor.getString(3),listType);
            GeoPoint geoPoint = new Gson().fromJson(cursor.getString(4), GeoPoint.class);
            Date date = new Date(cursor.getLong(5));
            SpotLocal local = new SpotLocal(id ,geoPoint, notes, date, spotType, uriList);

            Log.d(log, "id " + id +
                    "\ncat " + spotType +
                    "\nnotes " + notes +
                    "\nuri " + uriList.size() +
                    "\ntime " + date);
            localList.add(local);
            cursor.moveToNext();
        }
        cursor.close();
        return localList;
    }

    public long updateSpotMultipleImages(SpotLocal spot){
        ContentValues values = new ContentValues();
        values.put(KEY_TYPE, spot.getSpotType().toString());
        values.put(KEY_NOTES, spot.getNotes());
        values.put(KEY_URI, new Gson().toJson(spot.getFileStringList()));
        values.put(KEY_DATE, String.valueOf(spot.getDate().getTime()));
        return db.update(TABLE_SPOTS, values, "id=" + spot.getId(), null);
    }
}

