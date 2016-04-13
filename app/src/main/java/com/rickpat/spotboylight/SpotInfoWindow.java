package com.rickpat.spotboylight;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rickpat.spotboylight.spotboy_db.Spot;

import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.views.MapView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class SpotInfoWindow extends InfoWindow {

    private InfoCallback infoCallback;
    private Activity activity;

    public interface InfoCallback{
        void infoCallback(Spot spot);
    }

    public SpotInfoWindow(int layoutResId, MapView mapView , Activity activity) {
        super(layoutResId, mapView);
        this.infoCallback = (InfoCallback)activity;
        this.activity = activity;
    }

    @Override
    public void onOpen(Object item) {
        final SpotMarker myMarker = (SpotMarker)item;
        Button btnMoreInfo = (Button) mView.findViewById(R.id.infoWindow_moreButton);
        ImageView imageView = (ImageView) mView.findViewById(R.id.infoWin_image);
        TextView catTextView = (TextView) mView.findViewById(R.id.infoWin_cat);
        TextView notesTextView = (TextView) mView.findViewById(R.id.infoWin_notes);
        TextView dateTextView = (TextView) mView.findViewById(R.id.infoWin_time);

        DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.GERMAN);

        if ( myMarker.getSpot().getUrlList() != null){
            if (myMarker.getSpot().getUrlList().size() > 0 ){
                Glide.with(activity).load(myMarker.getSpot().getUrlList().get(0)).override(300,200).into(imageView);
            }
        }else {
            Log.d("SPOT_INFO_WINDOW", "list is null");
        }

        catTextView.setText(myMarker.getSpot().getSpotType().toString());
        notesTextView.setText(myMarker.getSpot().getNotes());
        dateTextView.setText(df.format(myMarker.getSpot().getDate()));

        btnMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoCallback.infoCallback(myMarker.getSpot());
            }
        });
    }

    @Override
    public void onClose() {

    }
}
