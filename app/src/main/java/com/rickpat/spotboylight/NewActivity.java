package com.rickpat.spotboylight;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.rickpat.spotboylight.Utilities.Utilities;
import com.rickpat.spotboylight.spotboy_db.Spot;
import com.rickpat.spotboylight.spotboy_db.SpotBoyDBHelper;
import com.google.gson.Gson;

import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.rickpat.spotboylight.Utilities.Constants.*;

public class NewActivity extends AppCompatActivity implements View.OnClickListener {

    private String prefImagePath = "imagePath";
    private String cat = "cat";
    private String notes = "notes";

    private AlertDialog catDialog;
    private ImageView imageView;
    private GeoPoint geoPoint;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;


    private String log = "NewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_new);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException e){}
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey(COORDINATES)){
            geoPoint = new Gson().fromJson(bundle.getString(COORDINATES),GeoPoint.class);
            ((TextView)findViewById(R.id.new_spot_lat)).setText(String.valueOf(geoPoint.getLatitude()));
            ((TextView)findViewById(R.id.new_spot_lon)).setText(String.valueOf(geoPoint.getLongitude()));
        }
        findViewById(R.id.new_spot_cat_layout).setOnClickListener(this);
        findViewById(R.id.new_spot_fab_photo).setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.new_spot_imageView);
        createDialogs();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        String path = preferences.getString(prefImagePath, "zero");
        geoPoint = new Gson().fromJson(preferences.getString(COORDINATES,"zero"),GeoPoint.class);
        if (path != "zero") {
            fileUri = Uri.parse(path);
            Glide.with(this).load(fileUri.getEncodedPath()).into(imageView);
        }
        ((TextView) findViewById(R.id.new_spot_cat_textView)).setText(preferences.getString(cat, ""));
        ((EditText) findViewById(R.id.new_spot_notes_editText)).setText(preferences.getString(notes, ""));

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(cat, ((TextView) findViewById(R.id.new_spot_cat_textView)).getText().toString().trim());
        editor.putString(notes, ((EditText) findViewById(R.id.new_spot_notes_editText)).getText().toString().trim());
        editor.putString(COORDINATES, new Gson().toJson(geoPoint));
        if (fileUri != null) {
            editor.putString(prefImagePath, fileUri.getEncodedPath());
        } else {
            editor.remove(prefImagePath);
        }
        editor.apply();
    }

    private Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    private File getOutputMediaFile() {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "SBO_PHOTOS");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(log,"onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_new, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(log,"onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                // this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                // if this doesn't work as desired, another possibility is to call `finish()` here.
                onBackPressed();
                return true;
            case R.id.action_create:
                long result = createDBEntry();
                if (result > 0 ){
                    int resultCode = result>0?NEW_SPOT_CREATED:NEW_SPOT_CANCELED;
                    Intent returnIntent = new Intent();
                    setResult(resultCode, returnIntent);
                    finish();
                }else {
                    Toast.makeText(this,"something's missing",Toast.LENGTH_SHORT).show();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.new_spot_cat_layout:
                catDialog.show();
                break;
            case R.id.new_spot_fab_photo:
                fileUri = getOutputMediaFileUri();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                break;
        }
    }

    private long createDBEntry() {
        SpotBoyDBHelper db = new SpotBoyDBHelper(getApplicationContext(), null, null, 0);
        String cat = ((TextView)findViewById(R.id.new_spot_cat_textView)).getText().toString().trim();
        String lat = String.valueOf(geoPoint.getLatitude());
        String lon = String.valueOf(geoPoint.getLongitude());
        String notes = ((EditText)findViewById(R.id.new_spot_notes_editText)).getText().toString().trim();
        if (fileUri != null) {
            Spot spot = new Spot(cat, lat, lon, notes, fileUri.getEncodedPath(), new Date());
            return db.addSpot(spot);
        }else {
            Spot spot = new Spot(cat, lat, lon, notes, null, new Date());
            return db.addSpot(spot);
        }
    }

    private void createDialogs() {

        String[] spotTypes = Utilities.getSpotTypes();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        catDialog = builder.setSingleChoiceItems(spotTypes, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                catDialog.cancel();
            }
        }).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView listView = ((AlertDialog) dialog).getListView();
                Object object = listView.getAdapter().getItem(listView.getCheckedItemPosition());
                String selection = (String) object;
                ((TextView) findViewById(R.id.new_spot_cat_textView)).setText(selection);
            }
        }).create();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this,getString(R.string.error_message_img),Toast.LENGTH_LONG).show();
                if ( resultCode == NEW_SPOT_CREATED){
                    Toast.makeText(this,getString(R.string.spot_created_message),Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


}

