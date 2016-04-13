package com.rickpat.spotboylight;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rickpat.spotboylight.Utilities.ScreenSlidePagerAdapter;
import com.rickpat.spotboylight.Utilities.Utilities;
import com.rickpat.spotboylight.fragments.GalleryItemFragment;
import com.rickpat.spotboylight.spotboy_db.SpotLocal;
import com.rickpat.spotboylight.spotboy_db.SpotBoyDBHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import static com.rickpat.spotboylight.Utilities.Constants.*;

public class InfoActivity extends AppCompatActivity {

    private SpotLocal spot;

    private String log="InfoActivity";
    private boolean modified = false;

    private AlertDialog catAlertDialog;
    private AlertDialog notesAlertDialog;

    private ViewPager mPager;

    @Override   //after onPause
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(log, "onSaveInstanceState");
        SharedPreferences.Editor editor = getSharedPreferences(PREFERENCES,MODE_PRIVATE).edit();
        editor.putBoolean(MODIFIED,modified);
        editor.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(log, "onPause");
    }

    @Override   //first call
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Log.d(log, "onCreate");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_info);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){}
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();

        if (bundle.containsKey(SPOT)){
            spot = new Gson().fromJson(bundle.getString(SPOT), SpotLocal.class);
        }else {
            finish();
        }
        mPager = (ViewPager) findViewById(R.id.info_viewPager);
    }

    @Override   //after onCreate or onRestoreInstanceState
    protected void onStart() {
        super.onStart();
        Log.d(log, "onStart");
        //next onRestoreInstance... or onResume
    }

    @Override   //after onCreate if screen orientation changed
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(log, "onRestoreInstanceState");
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        modified = preferences.getBoolean(MODIFIED,false);
        if (modified){
            SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(InfoActivity.this, null, null, 1);
            spot = spotBoyDBHelper.getMultipleImagesSpot(spot.getId());
            Log.d(log, "spot refreshed spotType: " + spot.getSpotType());
        }

        //next onResume
    }

    @Override   //after onStart or onRestoreInstance...
    protected void onResume() {
        super.onResume();
        Log.d(log, "onResume");
        setContent();
        setDialogs();
        setViewPagerContent();
    }

    private void setViewPagerContent() {
        List<Fragment> viewPagerFragments = new Vector<>(VIEW_PAGER_MAX_FRAGMENTS);
        for ( String url : spot.getFileStringList() ){
            Bundle page = new Bundle();
            page.putString(IMG_PATH, url);
            Log.d(log,"adding fragment for img: " + url);
            viewPagerFragments.add(Fragment.instantiate(this, GalleryItemFragment.class.getName(), page));
        }

        //after adding all the fragments write the below lines

        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), viewPagerFragments);

        mPager.setAdapter(mPagerAdapter);
    }

    private void setContent() {
        Log.d(log,"setContent: spot spotType " + spot.getSpotType() );
        ((TextView) findViewById(R.id.info_catTextView)).setText(spot.getSpotType().toString());
        ((TextView)findViewById(R.id.info_notesTextView)).setText(spot.getNotes());

        DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.GERMAN);
        ((TextView)findViewById(R.id.info_dateTextView)).setText(df.format(spot.getDate()));

        findViewById(R.id.info_cat_fab_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(log, "info_cat_fab_edit).setOnClickListener(new View.OnClickListener() ");
                catAlertDialog.show();
            }
        });

        findViewById(R.id.info_notes_fab_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(log, "info_notes_fab_edit).setOnClickListener(new View.OnClickListener()  ");
                notesAlertDialog.show();
            }
        });

    }

    private void setDialogs() {
        AlertDialog.Builder catBuilder = new AlertDialog.Builder(this);
        final String[] catItems = Utilities.getSpotTypes();
        int selectedItem = getSelection(catItems);
        catAlertDialog = catBuilder
                .setTitle(getString(R.string.new_cat_alert_title))
                .setSingleChoiceItems(catItems, selectedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(log, "selection: " + catItems[which]);
                    }
                }).setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        catAlertDialog.cancel();
                    }
                }).setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView listView = ((AlertDialog) dialog).getListView();
                        Object object = listView.getAdapter().getItem(listView.getCheckedItemPosition());
                        String selection = (String) object;
                        spot.setSpotType(Utilities.parseSpotTypeString(selection));
                        SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(InfoActivity.this, null, null, 1);
                        long dbResult = spotBoyDBHelper.updateSpotMultipleImages(spot);
                        if (dbResult > 0) {
                            ((TextView) findViewById(R.id.info_catTextView)).setText(selection);
                            modified = true;
                        }
                    }
                }).create();

        AlertDialog.Builder notesBuilder = new AlertDialog.Builder(this);
        View notesContent = getLayoutInflater().inflate(R.layout.dialog_input,null);
        final EditText editText = (EditText)notesContent.findViewById(R.id.dialog_editText);
        editText.setText(spot.getNotes());
        notesAlertDialog = notesBuilder
                .setTitle(getString(R.string.new_notes_dialog_title))
                .setView(notesContent)
                .setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String notes = editText.getText().toString().trim();
                        Log.d(log,"new notes: " + notes);
                        spot.setNotes(notes);
                        SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(InfoActivity.this, null, null, 1);
                        long dbResult = spotBoyDBHelper.updateSpotMultipleImages(spot);
                        if (dbResult > 0) {
                            ((TextView) findViewById(R.id.info_notesTextView)).setText(notes);
                            modified = true;
                        }
                    }
                }).setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        notesAlertDialog.cancel();
                    }
                })
                .create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                // if this doesn't work as desired, another possibility is to call `finish()` here.
                if (modified){
                    setResult(INFO_ACTIVITY_SPOT_MODIFIED);
                    finish();
                }

                onBackPressed();
                return true;
            case R.id.action_delete:
                SpotBoyDBHelper spotBoyDBHelper = new SpotBoyDBHelper(InfoActivity.this, null, null, 1);
                long dbResult = spotBoyDBHelper.deleteSpot(Integer.valueOf(spot.getId()));
                if (dbResult > 0){
                    Toast.makeText(this,getString(R.string.spot_deleted_message),Toast.LENGTH_SHORT).show();
                    setResult(INFO_ACTIVITY_SPOT_DELETED);
                    finish();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public int getSelection(String[] catItems) {
        int help=0;
        for (String str : catItems){
            if (str.equalsIgnoreCase(spot.getSpotType().toString())){
                return help;
            }
            help++;
        }
        return help;
    }
}
