package com.vishalperipherals.maps_demo.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.vishalperipherals.maps_demo.Internet.NetworkConnection;
import com.vishalperipherals.maps_demo.LoUtils.GPSTracker;
import com.vishalperipherals.maps_demo.Network.ApplicationRequest;
import com.vishalperipherals.maps_demo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PlacePickerActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

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
    public static Double longitude;
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
    TextView tvSendLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_picker);

        Bundle bundle = getIntent().getExtras();
        if (bundle!=null){

            latAutoSearch =     bundle.getDouble("lat");
            lngAutoSearch =     bundle.getDouble("lng");
        }

        getDone_btn =  findViewById(R.id.getDone_btn);
        tapthemap =  findViewById(R.id.tapthemap);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }


        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager()
                .findFragmentById(R.id.loc_maps);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);



        if (ContextCompat.checkSelfPermission(PlacePickerActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
          /*  Toast.makeText(PlacrPickerActivity.this, "You have already granted this permission!",
                    Toast.LENGTH_SHORT).show();*/
            getDeviceLocation();

        } else {
            requestLocationPermission();
        }



        getDone_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Toast.makeText(PlacrPickerActivity.this, ""+addresses, Toast.LENGTH_LONG).show();
                //tapthemap.setText(""+addresses);

                if (address!=null){

                    //setPreferences();

                    startActivity(new Intent(PlacePickerActivity.this, MainActivity.class));
                    finish();
                }else {

                    requestLocationPermission();
                }
            }
        });

        RelativeLayout realtiveSendLocation = findViewById(R.id.realtiveSendLocation);
        tvSendLocation = findViewById(R.id.tvSendLocation);

        realtiveSendLocation.setOnClickListener(v -> {

            sendhttprequest();
        });

        /*tvSendLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
*//*
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345"));
                startActivity(intent);*//*

                String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=loc:%f,%f", latitude,longitude);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);

            }
        });*/


    }


   /* private void setCameraView(){
        // Overall mapview window 0.2 * 0.2 = 0.04

        double bottomBoundary = mUserPosition.getGeoPoint().getLatitude() - .1;
        double leftBoundary = mUserPosition.getGeoPoint().getLongitude() - .1;
        double topBoundary = mUserPosition.getGeoPoint().getLatitude() + .1;
        double rightBoundary = mUserPosition.getGeoPoint().getLongitude() + .1;

        mMapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary));

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary,0));
    }
*/

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // markerOptions = new MarkerOptions();
        gps = new GPSTracker(this);
        // place = new LatLng(PlaceDetails.lat, PlaceDetails.log);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());


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


        if (ActivityCompat.checkSelfPermission(this
                , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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


                getDone_btn.setVisibility(View.VISIBLE);

                latitude = position.latitude;
               longitude = position.longitude;
/*
                StoredObjects.shop_lat=Double.toString(latitude);
                StoredObjects.shop_long =Double.toString(longitude);

                StoredObjects.latitude=Double.toString(latitude);
                StoredObjects.longitude =Double.toString(longitude);


                StoredObjects.LogMethod("","shop_loc:--"+StoredObjects.shop_lat+"**"+StoredObjects.shop_long+"**"+position+"**"+gps.Addressconverstion(getActivity(),latitude,longitude));*/
                Toast.makeText(getApplicationContext(),"latlag:-- "+position,Toast.LENGTH_LONG).show();

                checkPositoin = position;

                Geocoder geocoder = new Geocoder(PlacePickerActivity.this, Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);

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

                    // getAdminArea means state name
                    String state = addresses.get(0).getAdminArea();

                    // getPostalCode means pincode or postal code of area
                    String zip = addresses.get(0).getPostalCode();

                    // getCountryName means country name
                    String country = addresses.get(0).getCountryName();

                    // Toast.makeText(MainActivity.this, ""+city+state+"\n"+zip+"\n"+country, Toast.LENGTH_LONG).show();
                    Toast.makeText(PlacePickerActivity.this, ""+address, Toast.LENGTH_LONG).show();

                    tvSendLocation.setText("Send this location"+"\n"+address);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        });
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


 /*   @Override
    public void onDirectionFinderStart() {

    }

    @Override
    public void onDirectionFinderSuccess(List<Route> route) {

    }
*/

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
            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.


                        if (latAutoSearch != null && lngAutoSearch != null){

                            currentLocationLATLNG = new LatLng(latAutoSearch, lngAutoSearch);
                            marker =  mMap.addMarker(new MarkerOptions().position(currentLocationLATLNG).title("Custom location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).draggable(true).visible(true));

                            Geocoder geocoder = new Geocoder(PlacePickerActivity.this, Locale.getDefault());
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

                           /* latitude2 =   latAutoSearch;
                            longitude2 =    lngAutoSearch;
*/
                            latitude =   latAutoSearch;
                            longitude =    lngAutoSearch;

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
                            marker = mMap.addMarker(new MarkerOptions().position(currentLocationLATLNG).title("Custom location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).draggable(true).visible(true));

                            Geocoder geocoder = new Geocoder(PlacePickerActivity.this, Locale.getDefault());
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

                          /*  latitude2 =   mLastKnownLocation.getLatitude();
                            longitude2 =    mLastKnownLocation.getLongitude();*/

                            latitude =   mLastKnownLocation.getLatitude();
                            longitude =    mLastKnownLocation.getLongitude();


                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                                //Toast.makeText(PlacrPickerActivity.this, ""+mLastKnownLocation.getLatitude()+"\n"+mLastKnownLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                            }
                        }



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
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

   /* private void setPreferences(){

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        sharedPref.edit().putString(getString(R.string.subLocality), subLocality).apply();
        sharedPref.edit().putString(getString(R.string.address), address).apply();

    }*/

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("We need this permission to show maps to you")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(PlacePickerActivity.this,
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
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();

                getDeviceLocation();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }




    private void sendhttprequest() {


        if (!NetworkConnection.isConnected(this)){
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = getString(R.string.dom);


        StringRequest jsonRequest = new StringRequest(Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                try {
                    JSONObject jsonObject  = new JSONObject(response);

                    JSONArray jsonArray = jsonObject.getJSONArray("customer");

                    int length = jsonArray.length();

                    for (int index = 0; index < length; index++) {


                        JSONObject object = jsonArray.getJSONObject(index);

                        //  User item = new User();

                      /*  item.setCustomer_id(object.getString("customer_id"));
                        item.setCustomer_name(object.getString("customer_name"));
                        item.setCustomer_email(object.getString("customer_email"));
                        item.setCustomer_telephone(object.getString("customer_telephone"));

*/
                        String success = object.getString("customer_id");

                       // if (success.equals("3")) {
                            if (success!=null) {

                           // String customer_id = object.getString("customer_id");

                            //Toast.makeText(LoginActivity.this, ""+name+"\n"+email+"\n"+password, Toast.LENGTH_SHORT).show();

                            Toast.makeText(PlacePickerActivity.this, "Location Saved", Toast.LENGTH_SHORT).show();

                        }
                        else {

                            Toast.makeText(PlacePickerActivity.this, "Server return invalid response", Toast.LENGTH_SHORT).show();


                        }

                        // iconSubCategoriesList.add(item);

                    }


                }

                catch (JSONException e) {
                    e.printStackTrace();


                    Toast.makeText(PlacePickerActivity.this, "Invalid response from the server", Toast.LENGTH_SHORT).show();

                }
            }

        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                Toast.makeText(PlacePickerActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }

        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding the parameters to the request
                params.put("action", "createcustomer");
                params.put("customername", "Rohith ");
                params.put("customermobile", "7680002077");

                params.put("customeraddress", address);
                params.put("customerlatitude", String.valueOf(latitude));
                params.put("customerlongitude", String.valueOf(longitude));

                return params;
            }

        };

        int socketTimeout = 30000; //30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(
                socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);


        jsonRequest.setRetryPolicy(policy);

        ApplicationRequest.getInstance(this).addToRequestQueue(jsonRequest);

    }


    public void showinmaps(View view) {

        String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=loc:%f,%f", latitude,longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }
}