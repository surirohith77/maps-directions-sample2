package com.vishalperipherals.maps_demo.lo_medium;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.vishalperipherals.maps_demo.R;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback,  OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener {

    private static final int BOUNDS_PADDING = 5;
    private GoogleMap mMap;
    private static final LatLng BOUND1 = new LatLng(-35.595209, 138.585857);
    private static final LatLng BOUND2 = new LatLng(-35.494644, 138.805927);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        moveCameraToWantedArea();
    }

    private void moveCameraToWantedArea() {
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                // Set up the bounds coordinates for the area we want the user's viewpoint to be.
                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(BOUND1)
                        .include(BOUND2)
                        .build();
                // Move the camera now.
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, BOUNDS_PADDING));
            }
        });
    }


    /**
     * Method to draw all poly lines. This will manually draw polylines one by one on the map by calling
     * addPolyline(PolylineOptions) on a map instance. The parameter passed in is a new PolylineOptions
     * object which can be configured with details such as line color, line width, clickability, and
     * a list of coordinates values.
     */
  /*  private void drawAllPolyLines() {
        // Add a blue Polyline.
        mMap.addPolyline(new PolylineOptions()
                .color(getResources().getColor(R.color.colorPolyLineBlue)) // Line color.
                .width(POLYLINE_WIDTH) // Line width.
                .clickable(false) // Able to click or not.
                .addAll(readEncodedPolyLinePointsFromCSV(this, LINE_BLUE))); // all the whole list of lat lng value pairs which is retrieved by calling helper method readEncodedPolyLinePointsFromCSV.
        // Add a violet Polyline.
        mMap.addPolyline(new PolylineOptions()
                .color(getResources().getColor(R.color.colorPolyLineViolet))
                .width(POLYLINE_WIDTH)
                .clickable(false)
                .addAll(readEncodedPolyLinePointsFromCSV(this, LINE_VIOLET)));
        // Add an orange Polyline.
        mMap.addPolyline(new PolylineOptions()
                .color(getResources().getColor(R.color.colorPolyLineOrange))
                .width(POLYLINE_WIDTH)
                .clickable(false)
                .addAll(readEncodedPolyLinePointsFromCSV(this, LINE_ORANGE)));
        // Add a green Polyline.
        mMap.addPolyline(new PolylineOptions()
                .color(getResources().getColor(R.color.colorPolyLineGreen))
                .width(POLYLINE_WIDTH)
                .clickable(false)
                .addAll(readEncodedPolyLinePointsFromCSV(this, LINE_GREEN)));
        // Add a pink Polyline.
        mMap.addPolyline(new PolylineOptions()
                .color(getResources().getColor(R.color.colorPolyLinePink))
                .width(POLYLINE_WIDTH)
                .clickable(false)
                .addAll(readEncodedPolyLinePointsFromCSV(this, LINE_PINK)));
    }*/
}