package com.vishalperipherals.maps_demo.Fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.vishalperipherals.maps_demo.Activities.RouteActivity;
import com.vishalperipherals.maps_demo.LoUtils.GPSTracker;
import com.vishalperipherals.maps_demo.LoUtils.PlaceAutoSuggestAdapter;
import com.vishalperipherals.maps_demo.R;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DestinationFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    View view;
    Activity activity;

    AutoCompleteTextView search_locations;

    GoogleMap mMap;
    LatLng place;
    String dlocation;
    Marker marker;
    GPSTracker gps;
    //TextView tvdis, tvdur;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    LatLng dloct;
    private String provider;
    private boolean started = false;
    MarkerOptions markerOptions;
    //TextView getdirection_btn;
    public static Double longitute;
    public static Double latitude;
    TextView getDone_btn;

    Integer pos=0;
    String classname="",final_amnt,final_qtty;
    LatLng currentLocationLATLNG;

    //  private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_CODE = 213;
    Double latitude2, longitude2;
    LatLng checkPositoin ;
    List<Address> addresses;
    TextView tapthemap;
    String address, area, state, subLocality;
    Double latAutoSearch, lngAutoSearch;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.destination_fragment, container, false);


        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }


        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.loc_maps);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        initialize();
        initilizeAutoSuggestLoc();


        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
          /*  Toast.makeText(PlacrPickerActivity.this, "You have already granted this permission!",
                    Toast.LENGTH_SHORT).show();*/
            getDeviceLocation();

        } else {
            requestLocationPermission();
        }



      /*  getDone_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Toast.makeText(PlacrPickerActivity.this, ""+addresses, Toast.LENGTH_LONG).show();
                //tapthemap.setText(""+addresses);

                if (address!=null){

                    setPreferences();

                    startActivity(new Intent(activity,MainActivity3.class));
                    finish();
                }else {

                    requestLocationPermission();
                }
            }
        });*/

        return view;
    }

    private void initialize() {
        TextView getDone_btn = view.findViewById(R.id.getDone_btn);
        getDone_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, RouteActivity.class);
                intent.putExtra("lat",latitude2);
                intent.putExtra("lng",longitude2);
                startActivity(intent);
            }
        });
    }

    private void initilizeAutoSuggestLoc() {

        search_locations = (AutoCompleteTextView) view.findViewById(R.id.etSearch);


        search_locations.setAdapter(new PlaceAutoSuggestAdapter(activity,R.layout.address_display));
        search_locations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Address : ",search_locations.getText().toString());
                LatLng latLng = getLatLngFromAddress(search_locations.getText().toString());
                if(latLng!=null) {
                    Log.d("Lat Lng : ", " " + latLng.latitude + " " + latLng.longitude);
                    //  Toast.makeText(LocationPreferenceActivities.this, " " + latLng.latitude + " " + latLng.longitude, Toast.LENGTH_SHORT).show();

                   /* Intent intent = new Intent(LocationPreferenceActivities.this,PlacrPickerActivity.class);
                    intent.putExtra("lat",latLng.latitude);
                    intent.putExtra("lng",latLng.longitude);
                    startActivity(intent);*/

                 //   Toast.makeText(activity, ""+latLng.latitude+"\n"+latLng.longitude, Toast.LENGTH_SHORT).show();

                    latAutoSearch  = latLng.latitude;
                    lngAutoSearch  = latLng.longitude ;


                    getDeviceLocation();

                    // if want addres from latlng can use below method;
                    /* Address address=getAddressFromLatLng(latLng);
                    if(address!=null) {
                        Log.d("Address : ", "" + address.toString());
                        Log.d("Address Line : ",""+address.getAddressLine(0));
                        Log.d("Phone : ",""+address.getPhone());
                        Log.d("Pin Code : ",""+address.getPostalCode());
                        Log.d("Feature : ",""+address.getFeatureName());
                        Log.d("More : ",""+address.getLocality());
                    }
                    else {
                        Log.d("Adddress","Address Not Found");
                    }*/
                }
                else {
                    Log.d("Lat Lng","Lat Lng Not Found");
                }

            }
        });

    }

    private LatLng getLatLngFromAddress(String address){

        Geocoder geocoder=new Geocoder(activity);
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(address, 1);
            if(addressList!=null){
                Address singleaddress=addressList.get(0);
                LatLng latLng=new LatLng(singleaddress.getLatitude(),singleaddress.getLongitude());
                return latLng;
            }
            else{
                return null;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }


    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(activity)
                    .setTitle("Permission needed")
                    .setMessage("We need this permission to show maps to you")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);

                            getDeviceLocation();
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity, "Permission GRANTED", Toast.LENGTH_SHORT).show();

                getDeviceLocation();
            } else {
                Toast.makeText(activity, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            /*    if (mLocationPermissionGranted) {*/
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {

                    // Set the map's camera position to the current location of the device.

                        if (latAutoSearch != null && lngAutoSearch != null){

                            currentLocationLATLNG = new LatLng(latAutoSearch, lngAutoSearch);

                            if (marker != null) {
                                marker.remove();
                            }

                            marker =  mMap.addMarker(new MarkerOptions().position(currentLocationLATLNG).title("Custom location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).draggable(true).visible(true));

                            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                            try {
                                addresses = geocoder.getFromLocation(latAutoSearch, lngAutoSearch, 1);
                                // getAddressLine means full address
                                address = addresses.get(0).getAddressLine(0);

                                // getFeatureName means door number
                                String featureName = addresses.get(0).getFeatureName();

                                // getSubAdminArea means district Name
                                String subAdminArea =  addresses.get(0).getSubAdminArea();

                                // getSubLocality means area Name
                                subLocality = addresses.get(0).getSubLocality();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            latitude2 =   latAutoSearch;
                            longitude2 =    lngAutoSearch;

                            if (currentLocationLATLNG != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(latAutoSearch,
                                                lngAutoSearch), DEFAULT_ZOOM));

                                //Toast.makeText(PlacrPickerActivity.this, ""+mLastKnownLocation.getLatitude()+"\n"+mLastKnownLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                            }

                        }
                        else {
                            mLastKnownLocation = task.getResult();
                            currentLocationLATLNG = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());

                            if (marker != null) {
                                marker.remove();
                            }

                            marker = mMap.addMarker(new MarkerOptions().position(currentLocationLATLNG).title("Custom location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).draggable(true).visible(true));


                       /*     mMap.addCircle(new CircleOptions()
                                    .center(currentLocationLATLNG)
                                    .radius(100)
                                    .strokeWidth(0f)
                                    .fillColor(0x550000FF));*/




                            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                            try {
                                addresses = geocoder.getFromLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), 1);
                                // getAddressLine means full address
                                address = addresses.get(0).getAddressLine(0);

                                // getFeatureName means door number
                                String featureName = addresses.get(0).getFeatureName();

                                // getSubAdminArea means district Name
                                String subAdminArea =  addresses.get(0).getSubAdminArea();

                                // getSubLocality means area Name
                                subLocality = addresses.get(0).getSubLocality();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            latitude2 =   mLastKnownLocation.getLatitude();
                            longitude2 =    mLastKnownLocation.getLongitude();

                            // Toast.makeText(activity, ""+latitude2+"\n"+longitude2, Toast.LENGTH_SHORT).show();


                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                                //Toast.makeText(PlacrPickerActivity.this, ""+mLastKnownLocation.getLatitude()+"\n"+mLastKnownLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                            }

                        }

                        getGEOCODE();

                    } else {
                          /*  Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());*/
                        LatLng mDefaultLocation = new LatLng(latitude2, longitude2);
                        mMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                }
            });
            //  }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // markerOptions = new MarkerOptions();
        gps = new GPSTracker(activity);
        // place = new LatLng(PlaceDetails.lat, PlaceDetails.log);
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());


        String time = DateFormat.getTimeInstance().format(new Date());
        try {
            List<Address> list = geocoder.getFromLocation(gps.getLatitude(), gps.getLongitude(), 1);
            //    String caddress = list.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            // dlocation = caddress; //This is the complete address.
        } catch (IOException e) {
            e.printStackTrace();
        }

       /* marker = mMap.addMarker(new MarkerOptions().position(place)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_address))
                .anchor(0.5f, 0.5f)
                .flat(true)
                .snippet(dlocation)
                .title(PlaceDetails.plname)
                .draggable(true)
                .visible(true));
       */
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(new LatLng(gps.getLatitude(), gps.getLongitude()))
//                .target(new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude()))
                .zoom(12)
                .bearing(0)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                2000, null);



        //LatLng currentLocation = gps.getLatitude(), gps.getLongitude();


        if (ActivityCompat.checkSelfPermission(activity
                , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //LatLng currentLocation = new LatLng(gps.getLatitude(), gps.getLongitude());
        // marker =  mMap.addMarker(new MarkerOptions().position(currentLocationLATLNG).title("Current location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).draggable(true).visible(true));//enter code here


//     mMap.addMarker(new MarkerOptions().position(currentLocationLATLNG));
        //   marker =  mMap.addMarker(new MarkerOptions().position(currentLocationLATLNG).title("Custom location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).draggable(true).visible(true));

        //  mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng position) {

                if (marker != null) {
                    marker.remove();
                }
                marker =  mMap.addMarker(new MarkerOptions().position(position).title("Custom location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).draggable(true).visible(true));//enter code here


                //getDone_btn.setVisibility(View.VISIBLE);

              latitude2 = position.latitude;
              longitude2 = position.longitude;
/*
                StoredObjects.shop_lat=Double.toString(latitude);
                StoredObjects.shop_long =Double.toString(longitude);

                StoredObjects.latitude=Double.toString(latitude);
                StoredObjects.longitude =Double.toString(longitude);


                StoredObjects.LogMethod("","shop_loc:--"+StoredObjects.shop_lat+"**"+StoredObjects.shop_long+"**"+position+"**"+gps.Addressconverstion(getActivity(),latitude,longitude));*/

                getGEOCODE();

               // Toast.makeText(activity,"latlag:-- "+position,Toast.LENGTH_LONG).show();

                checkPositoin = position;


            }

        });


    }




    private void getGEOCODE(){
        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude2, longitude2, 1);

            // getAddressLine means full address
            address = addresses.get(0).getAddressLine(0);

            // getFeatureName means door number
            String featureName = addresses.get(0).getFeatureName();

            // getSubAdminArea means district Name
            String subAdminArea =  addresses.get(0).getSubAdminArea();

            // getSubLocality means area Name
            subLocality = addresses.get(0).getSubLocality();

            // getLocality means city name
            String city = addresses.get(0).getLocality();

            //getAdminArea means state name
            String state = addresses.get(0).getAdminArea();

            //getPostalCode means pincode or postal code of area
            String zip = addresses.get(0).getPostalCode();

            //getCountryName means country name
            String country = addresses.get(0).getCountryName();

            // Toast.makeText(MainActivity.this, ""+city+state+"\n"+zip+"\n"+country, Toast.LENGTH_LONG).show();
            Toast.makeText(activity, "area "+subLocality+"\n city "+city+"\n dist"+subAdminArea, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void onLocationChanged(Location location) {

        if (marker != null) {
            marker.remove();

        }

        // TextView tvLocation = (TextView) findViewById(R.id.tv_location);

        // Getting latitude of the current location
        double latitude = location.getLatitude();

        // Getting longitude of the current location
        double longitude = location.getLongitude();

        // Creating a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);
        //   marker =  mMap.addMarker(new MarkerOptions().position(position).title("Custom location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).draggable(true).visible(true));//enter code here

        //  marker = mMap.addMarker(new MarkerOptions().position(new LatLng(gps.getLatitude(), gps.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).draggable(true).visible(true));
        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(gps.getLatitude(), gps.getLongitude())));
        // Showing the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        // Zoom in the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onPause() {
        super.onPause();


    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }

}