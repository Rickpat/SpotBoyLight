package com.rickpat.spotboylight;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.rickpat.spotboylight.Utilities.SpotType;
import com.rickpat.spotboylight.Utilities.Utilities;
import com.rickpat.spotboylight.spotboy_db.SpotLocal;
import com.rickpat.spotboylight.spotboy_db.SpotBoyDBHelper;
import com.google.gson.Gson;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static com.rickpat.spotboylight.Utilities.Constants.*;

public class MainActivity extends AppCompatActivity implements MapEventsReceiver, SpotInfoWindow.InfoCallback {

    private String log = "MainActivity_LOG";

    private MyPositionOverlay myPositionOverlay;
    private MapView map;
    private AlertDialog markerDialog;
    private AlertDialog newMarkerDialog;
    private File kmlFile;
    private HashMap<SpotType,SpotCluster> clusterHashMap;
    private FolderOverlay kmlOverlay;

    @Override   //after onPause
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(log, "onSaveInstanceState");
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if ( kmlFile != null ) {
            editor.putString(KML_FILE, new Gson().toJson(kmlFile));
        } else {
            if (preferences.contains(KML_FILE)){
                editor.remove(KML_FILE);
            }
        }
        editor.putString(GEOPOINT, new Gson().toJson(map.getMapCenter()));
        editor.putInt(ZOOM_LEVEL, map.getZoomLevel());
        editor.apply();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(log, "onRestoreInstanceState");
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        if (preferences.contains(KML_FILE)){
            kmlFile = new Gson().fromJson(preferences.getString(KML_FILE,""),File.class);
        }
        map.getController().setCenter(new Gson().fromJson(preferences.getString(GEOPOINT, ""), GeoPoint.class));
        map.getController().setZoom(preferences.getInt(ZOOM_LEVEL, 18));
    }

    @Override   //called by action
    protected void onPause() {
        super.onPause();
        Log.d(log, "onPause");
        myPositionOverlay.disableMyLocation();
        removeMarkerCluster();
        removeKMLOverlay();
    }

    @Override   //first call
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(log, "onCreate");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        setMap();
        setMarkerDialog();
        setFab();
        //next onStart
    }

    @Override   //after onCreate
    protected void onStart() {
        super.onStart();
        Log.d(log, "onStart");
        // next onRestoreInstanceState after e.g. screen orientation changed
        // otherwise onResume
    }

    @Override   //after onStart or onRestoreInstanceState
    protected void onResume() {
        super.onResume();
        Log.d(log, "onResume");
        myPositionOverlay.enableMyLocation();
        setMarkerCluster();
        createKMLOverlay();
        //now activity is running till onPause
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(log, "onActivityResult");
        if ( requestCode == HUB_REQUEST && resultCode == HUB_SHOW_ON_MAP){
            Bundle bundle = data.getExtras();
            if (bundle.containsKey(SPOT)){
                SpotLocal spot = new Gson().fromJson(bundle.getString(SPOT),SpotLocal.class);
                Log.d(log,"received spot with id: " + spot.getId());
                map.getController().animateTo(spot.getGeoPoint());
            }
        }
        if ( requestCode == INFO_ACTIVITY_REQUEST){
            switch (resultCode){
                case INFO_ACTIVITY_SPOT_MODIFIED:
                    Log.d(log,"spot modified");
                    break;
                case INFO_ACTIVITY_SPOT_DELETED:
                    Log.d(log,"spot deleted");
            }
        }

        if ( requestCode == KML_REQUEST ){
            switch (resultCode){
                case KML_LOAD:
                    Log.d(log,"KML_LOAD");
                    Bundle bundle = data.getExtras();
                    if (bundle != null){
                        if (bundle.containsKey(KML_FILE)){
                            kmlFile = new Gson().fromJson(bundle.getString(KML_FILE,""),File.class);
                            Log.d(log,kmlFile.getName() + " parsed to KmlDocument");
                        }
                    }
                    break;
                case KML_REMOVE:
                    Log.d(log,"KML_REMOVE");
                    removeKMLOverlay();
                    kmlFile = null;
                    break;
            }
        }
    }

    private void setMarkerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] items = Utilities.getSpotTypes();
        boolean[] selectedItems = new boolean[items.length];
        for (int i = 0 ; i < selectedItems.length ; i++){
            selectedItems[i] = true;
        }
        markerDialog = builder
                .setMultiChoiceItems(items, selectedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        SpotType spotTypeSelection = Utilities.parseSpotTypeString(items[indexSelected]);
                        if (isChecked) {
                            Log.d(log,"checked item id: " + indexSelected + " value: " + items[indexSelected]);
                            addCluster( spotTypeSelection );
                        } else {
                            removeCluster( spotTypeSelection );
                        }
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        markerDialog.cancel();
                    }
                }).create();
    }

    private void removeCluster(SpotType spotTypeSelection) {
        if( map.getOverlays().contains(clusterHashMap.get(spotTypeSelection)) ){
            map.getOverlays().remove(clusterHashMap.get(spotTypeSelection));
            map.invalidate();
        }
    }

    private void addCluster(SpotType spotTypeSelection) {
        if (!map.getOverlays().contains(clusterHashMap.get(spotTypeSelection))){
            map.getOverlays().add(clusterHashMap.get(spotTypeSelection));
            map.invalidate();
        }
    }

    private void removeMarkerCluster(){
        for (SpotCluster spotCluster : clusterHashMap.values()){
            map.getOverlays().remove(spotCluster);
        }
        clusterHashMap.clear();
    }

    private void setMarkerCluster() {
        SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(this, null, null, 1);
        List<SpotLocal> spotList = spotBoyDBHelper.getSpotListMultipleImages();
        if ( spotList.size() > 0 ){
            map.getController().animateTo(spotList.get(spotList.size()-1).getGeoPoint());
        }
        clusterHashMap = new HashMap<>();
        for (SpotType spotType : SpotType.values()){
            SpotCluster spotCluster = new SpotCluster(this);
            spotCluster.setName(spotType + "Cluster");
            spotCluster.setIcon(Utilities.getClusterIcon(getApplicationContext(), spotType));
            clusterHashMap.put(spotType, spotCluster);
        }
        for (SpotLocal spot : spotList){
            Log.d(log, "SpotLocal id: " + spot.getId() + " type: " + spot.getSpotType());
            if (spot.getSpotType() == null){
                spotBoyDBHelper.deleteSpot(Integer.valueOf(spot.getId()));
            }
            SpotMarker spotMarker = new SpotMarker(map, spot);
            spotMarker.setIcon(Utilities.getMarkerIcon(getApplicationContext(),spot.getSpotType()));
            spotMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            spotMarker.setInfoWindow(new SpotInfoWindow(R.layout.info_window,map,this));
            clusterHashMap.get(spot.getSpotType()).add(spotMarker);
        }
        for (SpotCluster spotCluster : clusterHashMap.values()){
            Log.d(log, "cluster: " + spotCluster.getName() + " items: " + spotCluster.getItems().size());
            map.getOverlays().add(spotCluster);
        }
        map.invalidate();
    }

    private void createKMLOverlay() {
        if ( kmlFile != null ) {
            KmlDocument kmlDocument = new KmlDocument();
            kmlDocument.parseKMLFile(kmlFile);
            kmlOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(map, null, null, kmlDocument);
            map.getOverlays().add(kmlOverlay);
            map.invalidate();
            Log.d(log,"kml overlay created");
        }
    }

    private void removeKMLOverlay() {
        if ( kmlFile != null ){
            if (map.getOverlays().contains(kmlOverlay)){
                map.getOverlays().remove(kmlOverlay);
                map.invalidate();
                Log.d(log,"kml overlay removed");
            }
        }
    }

    private void setFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final GeoPoint geoPoint = myPositionOverlay.getMyLocation();
                if (geoPoint != null){
                    map.getController().animateTo(geoPoint);
                }else{
                    CoordinatorLayout coordinatorLayout = (CoordinatorLayout)findViewById(R.id.main_coordinator);
                    Snackbar.make(coordinatorLayout,getString(R.string.waiting_for_gps_signal_message),Snackbar.LENGTH_SHORT)
                            .show();
                    myPositionOverlay.runOnFirstFix(new Runnable() {
                        @Override
                        public void run() {
                            map.getController().animateTo(myPositionOverlay.getMyLocation());
                        }
                    });
                }
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                markerDialog.show();
                return false;
            }
        });
    }

    private void setMap() {
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15);
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
        map.getOverlays().add(0, mapEventsOverlay);


        myPositionOverlay = new MyPositionOverlay(this, map);
        myPositionOverlay.enableMyLocation();
        myPositionOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                try {
                    map.getController().animateTo(myPositionOverlay.getMyLocation());
                } catch (Exception e) {
                    Log.d(log, e.toString());
                }
            }
        });
        map.getOverlays().add(myPositionOverlay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_about:
                Intent intent = new Intent(this,AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.action_hub:
                Intent hubIntent = new Intent(this, HubActivity.class);
                startActivityForResult(hubIntent,HUB_REQUEST);
                break;
            case R.id.action_new:
                GeoPoint geoPoint = myPositionOverlay.getMyLocation();
                if ( geoPoint != null) {
                    Log.d(log,"starting NewActivity with geoPoint: " + geoPoint);
                    Intent newSpotIntent = new Intent(this, NewActivity.class);
                    newSpotIntent.putExtra(GEOPOINT, new Gson().toJson(geoPoint));
                    startActivityForResult(newSpotIntent, NEW_SPOT_REQUEST);
                }
                break;
            case R.id.action_kml:
                Intent kmlIntent = new Intent(this,KMLActivity.class);
                startActivityForResult(kmlIntent,KML_REQUEST );
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        closeAllInfoWindows();
        return false;
    }

    private void closeAllInfoWindows() {
        for (SpotCluster spotCluster : clusterHashMap.values()){
            for (Marker spotMarker : spotCluster.getItems()){
                spotMarker.closeInfoWindow();
            }
        }
    }

    @Override
    public boolean longPressHelper(final GeoPoint p) {
        closeAllInfoWindows();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        newMarkerDialog = builder
                .setTitle(getString(R.string.new_marker_alert_message))
                .setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        newMarkerDialog.cancel();
                    }
                }).setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (p != null) {
                            Intent newSpotIntent = new Intent(MainActivity.this, NewActivity.class);
                            newSpotIntent.putExtra(GEOPOINT, new Gson().toJson(p));
                            startActivityForResult(newSpotIntent, NEW_SPOT_REQUEST);
                        }
                    }
                }).create();
        newMarkerDialog.show();
        return false;
    }

    @Override
    public void infoCallback(SpotLocal spot) {
        closeAllInfoWindows();
        Intent infoIntent = new Intent(this,InfoActivity.class);
        infoIntent.putExtra(SPOT,new Gson().toJson(spot));
        startActivityForResult(infoIntent,INFO_ACTIVITY_REQUEST);
    }
}
