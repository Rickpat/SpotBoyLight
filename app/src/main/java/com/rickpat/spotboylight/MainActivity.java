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
import com.rickpat.spotboylight.spotboy_db.Spot;
import com.rickpat.spotboylight.spotboy_db.SpotBoyDBHelper;
import com.google.gson.Gson;

import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import java.util.HashMap;
import java.util.List;

import static com.rickpat.spotboylight.Utilities.Constants.*;

public class MainActivity extends AppCompatActivity implements MapEventsReceiver, SpotInfoWindow.InfoCallback {

    private String log = "MainActivity_LOG";

    private MyPositionOverlay myPositionOverlay;
    private MapView map;
    private AlertDialog markerDialog;
    private AlertDialog newMarkerDialog;

    private HashMap<SpotType,SpotCluster> clusterHashMap;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(GEO_POINT, new Gson().toJson(map.getMapCenter()));
        editor.putInt(ZOOM_LEVEL, map.getZoomLevel());
        editor.apply();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        map.getController().setCenter(new Gson().fromJson(preferences.getString(GEO_POINT, ""), GeoPoint.class));
        map.getController().setZoom(preferences.getInt(ZOOM_LEVEL, 18));
    }

    @Override
    protected void onPause() {
        super.onPause();
        myPositionOverlay.disableMyLocation();
        removeMarkerCluster();
    }

    @Override
    protected void onResume() {
        super.onResume();
        myPositionOverlay.enableMyLocation();
        setMarkerCluster();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        setMap();
        setMarkerDialog();
        setFab();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == HUB_REQUEST && resultCode == HUB_SHOW_ON_MAP){
            Bundle bundle = data.getExtras();
            if (bundle.containsKey(SPOT)){
                Spot spot = new Gson().fromJson(bundle.getString(SPOT),Spot.class);
                Log.d(log,"received spot with id: " + spot.getId());
                map.getController().animateTo(spot.getGeoPoint());
            }
        }
        if ( requestCode == INFO_ACTIVITY_REQUEST && resultCode == INFO_ACTIVITY_SPOT_DELETED){
            Log.d(log,"spot deleted");
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
        List<Spot> spotList = spotBoyDBHelper.getSpotList();
        if (spotList.size()>0){
            map.getController().animateTo(spotList.get(spotList.size()-1).getGeoPoint());
        }
        clusterHashMap = new HashMap<>();
        for (SpotType spotType : SpotType.values()){
            SpotCluster spotCluster = new SpotCluster(this);
            spotCluster.setName(spotType + "Cluster");
            spotCluster.setIcon(Utilities.getClusterIcon(getApplicationContext(), spotType));
            clusterHashMap.put(spotType, spotCluster);
        }
        for (Spot spot : spotList){
            Log.d(log, "Spot id: " + spot.getId() + " type: " + spot.getSpotType());
            if (spot.getSpotType() == null){
                spotBoyDBHelper.deleteSpot(spot.getId());
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
                if (myPositionOverlay.getMyLocation() != null) {
                    Intent newSpotIntent = new Intent(this, NewActivity.class);
                    newSpotIntent.putExtra(COORDINATES, new Gson().toJson(myPositionOverlay.getMyLocation()));
                    startActivityForResult(newSpotIntent, NEW_SPOT_REQUEST);
                }
                break;
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
                            newSpotIntent.putExtra(COORDINATES, new Gson().toJson(p));
                            startActivityForResult(newSpotIntent, NEW_SPOT_REQUEST);
                        }
                    }
                }).create();
        newMarkerDialog.show();
        return false;
    }

    @Override
    public void infoCallback(Spot spot) {
        closeAllInfoWindows();
        Intent infoIntent = new Intent(this,InfoActivity.class);
        infoIntent.putExtra(SPOT,new Gson().toJson(spot));
        startActivityForResult(infoIntent,INFO_ACTIVITY_REQUEST);
    }
}
