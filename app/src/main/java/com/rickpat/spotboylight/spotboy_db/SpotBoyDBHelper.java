package com.rickpat.spotboylight.spotboy_db;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpotBoyDBHelper extends SQLiteOpenHelper {

    private SQLiteDatabase db;

    private String log = "SpotBoyDBHelper";

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "spotBoy_db_v01";
    private static final String TABLE_SPOTS = "spotBoy_db_table_v01";

    private static final String KEY_ID = "id";
    private static final String KEY_CAT = "cat";
    private static final String KEY_NOTES = "notes";
    private static final String KEY_URI = "uri";
    private static final String KEY_LON = "lon";
    private static final String KEY_LAT = "lat";
    private static final String KEY_DATE = "date";

    public SpotBoyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SPOTS_TABLE = "CREATE TABLE " + TABLE_SPOTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_CAT + " TEXT,"
                 + KEY_NOTES + " TEXT,"+ KEY_URI + " TEXT,"+ KEY_LON +
                " TEXT," + KEY_LAT + " TEXT," + KEY_DATE + " DATE)";
        db.execSQL(CREATE_SPOTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPOTS);
        onCreate(db);
    }

    public long updateSpot(Spot spot){
        ContentValues values = new ContentValues();
        values.put(KEY_CAT, spot.getCategory());
        values.put(KEY_NOTES, spot.getNotes());
        values.put(KEY_URI, spot.getUri());
        values.put(KEY_LON, spot.getLongitude());
        values.put(KEY_LAT, spot.getLatitude());
        values.put(KEY_DATE, spot.getDate().getTime());
        return db.update(TABLE_SPOTS,values,"id="+spot.getId(),null);
    }

    public long addSpot(Spot spot) {
        ContentValues values = new ContentValues();
        values.put(KEY_CAT, spot.getCategory());
        values.put(KEY_NOTES, spot.getNotes());
        values.put(KEY_URI, spot.getUri());
        values.put(KEY_LON, spot.getLongitude());
        values.put(KEY_LAT, spot.getLatitude());
        values.put(KEY_DATE, spot.getDate().getTime());
        return db.insert(TABLE_SPOTS, null, values);
    }

    public void readAll(){
        Cursor cursor = db.query(TABLE_SPOTS,null,null,null,null,null,null);
        cursor.moveToFirst();
        int count = cursor.getCount();
        Log.d(log, "count: " + count);
        for (int i = 0 ; i < count ; i++){
            long id = cursor.getLong(0);
            String cat = cursor.getString(1);
            String notes = cursor.getString(2);
            String uri = cursor.getString(3);
            String lon = cursor.getString(4);
            String lat = cursor.getString(5);
            Date time = new Date(cursor.getLong(6));
            Log.d(log, "id " + id +
                    "\ncat " + cat +
                    "\nnotes " + notes +
                    "\nuri " + uri +
                    "\nlon " + lon +
                    "\nlat " + lat +
                    "\ntime " + time);
            cursor.moveToNext();
        }
        cursor.close();
    }

    public List<Spot> getSpotList(){
        List<Spot> spotList = new ArrayList<>();

        Cursor cursor = db.query(TABLE_SPOTS,null,null,null,null,null,null);
        cursor.moveToFirst();
        int count = cursor.getCount();
        Log.d(log, "count: " + count);
        for (int i = 0 ; i < count ; i++){
            int id = (int)cursor.getLong(0);
            String cat = cursor.getString(1);
            String notes = cursor.getString(2);
            String uri = cursor.getString(3);
            String lon = cursor.getString(4);
            String lat = cursor.getString(5);
            Date date = new Date(cursor.getLong(6));
            Spot spot = new Spot(cat,lon,lat,notes,uri,date);
            spot.setId(id);
            Log.d(log, "id " + id +
                    "\ncat " + cat +
                    "\nnotes " + notes +
                    "\nuri " + uri +
                    "\nlon " + lon +
                    "\nlat " + lat +
                    "\ntime " + date);
            spotList.add(spot);
            cursor.moveToNext();
        }
        cursor.close();
        return spotList;
    }

    public int deleteSpot( int id ){
        Log.d(log,"deleting spot with id " + id);
        return db.delete(TABLE_SPOTS,"id=" + id, null);
    }
}

