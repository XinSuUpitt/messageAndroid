package com.messagecube.messaging.ui.mediapicker;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.InflateException;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.messagecube.messaging.R;
import com.messagecube.messaging.ui.mediapicker.location.MapsActivity;

/**
 * Created by suxin on 10/1/17.
 */

public class LocationMediaChooser extends MediaChooser implements OnMapReadyCallback{
    /**
     * Initializes a new instance of the Chooser class
     *
     * @param mediaPicker The media picker that the chooser is hosted in
     */

    private View view;
    private View mMissingPermissionView;
    private GoogleMap mGoogleMap;
    private MapsActivity mapsActivity;
    private double Latitude;
    private double Longitude;
    private String currAddress;

    LocationMediaChooser(MediaPicker mediaPicker) {
        super(mediaPicker);
    }

    @Override
    protected View createView(ViewGroup container) {

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        try {
            view = (View) getLayoutInflater().inflate(R.layout.mediapicker_location_chooser, container, false);
//            Intent intent = new Intent(getContext(), MapsActivity.class);
//            getContext().startActivity(intent);
//            destroyView();
            MapsActivity mapsActivity = new MapsActivity();
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.map_fragment, mapsActivity).commit();



        } catch (InflateException e) {

        }

//        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
//        mapFragment.getMapAsync(this);
        //mapsActivity = new MapsActivity();


        return view;
    }



    @Override
    public int getSupportedMediaTypes() {
        return MediaPicker.MEDIA_TYPE_LOCATION;
    }

    @Override
    int getIconResource() {
        return R.drawable.ic_location_on_white_18dp;
    }

    @Override
    int getIconDescriptionResource() {
        return R.string.mediapicker_locationDescription;
    }

    @Override
    int getActionBarTitleResId() {
        return 0;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        LatLng sydney = new LatLng(-34, 151);
        mGoogleMap.addMarker(new MarkerOptions().position(sydney).title("Mark in Sydney"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
