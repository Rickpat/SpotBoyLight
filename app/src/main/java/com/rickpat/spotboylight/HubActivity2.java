package com.rickpat.spotboylight;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.rickpat.spotboylight.spotboy_db.Spot;
import com.rickpat.spotboylight.spotboy_db.SpotLocal;
import com.rickpat.spotboylight.spotboy_db.SpotBoyDBHelper;

import static com.rickpat.spotboylight.Utilities.Constants.HUB_SHOW_ON_MAP;
import static com.rickpat.spotboylight.Utilities.Constants.INFO_ACTIVITY_REQUEST;
import static com.rickpat.spotboylight.Utilities.Constants.INFO_ACTIVITY_SPOT_DELETED;
import static com.rickpat.spotboylight.Utilities.Constants.INFO_ACTIVITY_SPOT_MODIFIED;
import static com.rickpat.spotboylight.Utilities.Constants.SPOT;

public class HubActivity2 extends AppCompatActivity implements SpotHubAdapter2.IHubAdapter {

    private SpotHubAdapter2 mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_hub);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){}
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(this, null, null, 1);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.hub_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new SpotHubAdapter2(spotBoyDBHelper.getSpotListMultipleImages(), this);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                // if this doesn't work as desired, another possibility is to call `finish()` here.
                onBackPressed();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void moreButtonCallback(SpotLocal spot) {
        Intent infoIntent = new Intent(this,InfoActivity2.class);
        infoIntent.putExtra(SPOT,new Gson().toJson(spot));
        startActivityForResult(infoIntent, INFO_ACTIVITY_REQUEST);

    }

    @Override
    public void markerButtonCallback(SpotLocal spot) {
        Intent showMarkerIntent = new Intent();
        showMarkerIntent.putExtra(SPOT,new Gson().toJson(spot));
        setResult(HUB_SHOW_ON_MAP, showMarkerIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean update = false;
        if ( requestCode == INFO_ACTIVITY_REQUEST ) {
            switch (resultCode){
                case INFO_ACTIVITY_SPOT_DELETED:
                    update = true;
                    break;
                case INFO_ACTIVITY_SPOT_MODIFIED:
                    update = true;
                    break;
            }
        }
        if (update){
            SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(this,null,null,1);
            mAdapter.updateList(spotBoyDBHelper.getSpotListMultipleImages());
            mAdapter.notifyDataSetChanged();
        }
    }
}
