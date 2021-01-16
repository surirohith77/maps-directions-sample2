package com.vishalperipherals.maps_demo.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.vishalperipherals.maps_demo.Internet.NetworkConnection;
import com.vishalperipherals.maps_demo.LoUtils.MyClusterManagerRender;
import com.vishalperipherals.maps_demo.Network.ApplicationRequest;
import com.vishalperipherals.maps_demo.PlacePickerActivity;
import com.vishalperipherals.maps_demo.R;
import com.vishalperipherals.maps_demo.Services.LocationService;
import com.vishalperipherals.maps_demo.models.ClusterMarker;
import com.vishalperipherals.maps_demo.models.PolylineData;
import com.vishalperipherals.maps_demo.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.ACTIVITY_SERVICE;
import static com.vishalperipherals.maps_demo.LoUtils.Constants.MAPVIEW_BUNDLE_KEY;

public class StoreFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnPolylineClickListener /*, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener*/ {

    Activity activity;
    View view;

    private static final String TAG = "UserListFragment";
    private static final int LOCATION_UPDATE_INTERVAL = 3000;

    private static final int MAP_LAYOUT_STATE_CONTRACTED = 0;
    private static final int MAP_LAYOUT_STATE_EXPANDED = 1;
    private int mMapLayoutState = 0;

    //widgets
    private RecyclerView mUserListRecyclerView;
    private MapView mMapView;
    private RelativeLayout mMapContainer;


    //vars
    private ArrayList<User> mUserList = new ArrayList<>();

    private GoogleMap mGoogleMap;
    private LatLngBounds mMapBoundary;


    //  private ClusterManager<ClusterMarker> mClusterManager;
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRender mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private GeoApiContext mGeoApiContext;
    ///  private ArrayList<PolylineData> mPolyLinesData = new ArrayList<>();
    private Marker mSelectedMarker = null;
    private ArrayList<Marker> mTripMarkers = new ArrayList<>();


    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;
    List<Address> addresses;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";


    //Define a request code to send to Google Play services
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;
    private ArrayList<PolylineData> mPolyLinesData = new ArrayList<>();

    Double userLatitude, userLongitude;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.store_fragment, container, false);

        mMapView = (MapView) view.findViewById(R.id.user_list_map);

        initGoogleMap(savedInstanceState);

        Button btnClick = view.findViewById(R.id.btnClick);

       /* if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);

*/
        //getLocation();

        btnClick.setOnClickListener(v -> startActivity(new Intent(activity, PlacePickerActivity.class)));


        startLocationService();

        getUserLocationhttpRequest();


        return view;
    }



 /*   private void getLocation() {

        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


    }
*/

    private void initGoogleMap(Bundle savedInstanceState) {

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);

        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_api_key))
                    .build();
        }
    }

    private void addMapMarkers() {

        if (mGoogleMap != null) {

            resetMap();

            if (mClusterManager == null) {
                //mClusterManager = new ClusterManager<ClusterMarker>(getActivity().getApplicationContext(), mGoogleMap);
                mClusterManager = new ClusterManager<ClusterMarker>(activity, mGoogleMap);
            }

            if (mClusterManagerRenderer == null) {

                mClusterManagerRenderer = new MyClusterManagerRender(
                        activity,
                        mGoogleMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);

            }
            int avatar = R.drawable.delivery_boy;

            //   if (userLatitude!=null && userLongitude!=null){

            ClusterMarker newClusterMarker = new ClusterMarker(
                    // new LatLng(17.3615636, 78.4724758),
                    new LatLng(userLatitude, userLongitude),
                    "Rohith",
                    "this is you",
                    avatar

            );
            mClusterManager.addItem(newClusterMarker);
            mClusterMarkers.add(newClusterMarker);

            mClusterManager.cluster();

//              setCameraView();
            //}

        }
    }

    private void resetSelectedMarker() {
        if (mSelectedMarker != null) {
            mSelectedMarker.setVisible(true);
            mSelectedMarker = null;
            removeTripMarkers();
        }
    }

    private void removeTripMarkers() {
        for (Marker marker : mTripMarkers) {
            marker.remove();
        }
    }

    private void resetMap() {
        if (mGoogleMap != null) {
            mGoogleMap.clear();

            if (mClusterManager != null) {
                mClusterManager.clearItems();
            }

            if (mClusterMarkers.size() > 0) {
                mClusterMarkers.clear();
                mClusterMarkers = new ArrayList<>();
            }
            //setCameraView();
            if (mPolyLinesData.size() > 0) {
                mPolyLinesData.clear();
                mPolyLinesData = new ArrayList<>();
            }
        }
    }

    private void setCameraView() {

        // Overall mapview window 0.2 * 0.2 = 0.04

        double bottomBoundary = currentLatitude - .1;
        double leftBoundary = currentLongitude - .1;
        double topBoundary = currentLatitude + .1;
        double rightBoundary = currentLongitude + .1;

        mMapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary));

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        //startUserLocationsRunnable();


        //  mGoogleApiClient.connect();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();

        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        /*if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

*/
    }

    @Override
    public void onMapReady(GoogleMap map) {
         //  map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        //

        mGoogleMap = map;
        // setCameraView();

        mGoogleMap.setOnInfoWindowClickListener(this);
        mGoogleMap.setOnPolylineClickListener(this);

        //   mGoogleMap.setInfoWindowAdapter(this);

      //  addMapMarkers();
   /* if (userLatitude!=null && userLongitude!=null){

        addMapMarkers();
    }*/


    }



    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
        //  stopLocationUpdates();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    @Override
    public void onInfoWindowClick(final Marker marker) {

        if (marker.getTitle().contains("Trip #")) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Open Google Maps?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            String latitude = String.valueOf(marker.getPosition().latitude);
                            String longitude = String.valueOf(marker.getPosition().longitude);
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");

                            try {
                                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                    startActivity(mapIntent);
                                }
                            } catch (NullPointerException e) {
                                Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.getMessage());
                                Toast.makeText(getActivity(), "Couldn't open map", Toast.LENGTH_SHORT).show();
                            }

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        } else {


            Toast.makeText(getActivity(), "Hello", Toast.LENGTH_SHORT).show();

            if (marker.getSnippet().equals("This is you")) {
                marker.hideInfoWindow();
            } else {

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(marker.getSnippet())
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.dismiss();
                                  resetSelectedMarker();
                                //mSelectedMarker = marker;
                                // calculateDirections(marker);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }

        }

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void
    onPolylineClick(Polyline polyline) {

        int index = 0;
        for(PolylineData polylineData: mPolyLinesData){
            index++;
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(ContextCompat.getColor(activity, R.color.blue1));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lng
                );

            /*    Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(endLocation)
                        .title("Trip #" + index)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).draggable(true).visible(true)
                        .snippet("Duration: " + polylineData.getLeg().duration
                        ));
*/

                Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(endLocation)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.home2))
                        .anchor(0.5f, 0.5f)
                        .flat(true)
                        .draggable(true)
                        .visible(true)
                        .position(endLocation)
                        .title("Trip #" + index)
                        .snippet("Duration: " + polylineData.getLeg().duration)
                );


                marker.showInfoWindow();

                mTripMarkers.add(marker);
            }
            else{
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.darkGrey));
                polylineData.getPolyline().setZIndex(0);
            }
        }

    }

    private void calculateDirections() {
        Log.d(TAG, "calculateDirections: calculating directions.");

     /*   com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );*/

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                17.433004,
                78.4221764
        );

        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);

        /*directions.origin(
                new com.google.maps.model.LatLng(
                        mUserPosition.getGeoPoint().getLatitude(),
                        mUserPosition.getGeoPoint().getLongitude()
                )
        );*/

        directions.origin(
                new com.google.maps.model.LatLng(
                        userLatitude,userLongitude
                )
        );

/*
        directions.origin(
                new com.google.maps.model.LatLng(
                        17.4844388,             17.4844382, 78.4187157
                        78.4186269
                )
        );*/

        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "onResult: routes: " + result.routes[0].toString());
                Log.d(TAG, "onResult: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());

                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "onFailure: " + e.getMessage());

            }
        });
    }


    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                // Removes the old polylines
                if(mPolyLinesData.size() > 0){
                    for(PolylineData polylineData: mPolyLinesData){
                        polylineData.getPolyline().remove();
                    }
                    mPolyLinesData.clear();
                    mPolyLinesData = new ArrayList<>();
                }

                double duration = 999999999;

                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(activity, R.color.darkGrey));
                    polyline.setClickable(true);

                    mPolyLinesData.add(new PolylineData(polyline, route.legs[0]));

                    //  onPolylineClick(polyline);

                    // highlight the fastest route and adjust camera
                    double tempDuration = route.legs[0].duration.inSeconds;
                    if(tempDuration < duration){
                        duration = tempDuration;
                        onPolylineClick(polyline);
                        zoomRoute(polyline.getPoints());
                    }

                    // mSelectedMarker.setVisible(false);

                }
            }
        });
    }




    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (mGoogleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mGoogleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }







    /*  private void getDeviceLocation() {
     *//*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     *//*
        try {
            *//*    if (mLocationPermissionGranted) {*//*
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.



                            mLastKnownLocation = task.getResult();
                            currentLocationLATLNG = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            marker = mMap.addMarker(new MarkerOptions().position(currentLocationLATLNG).title("Custom location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).draggable(true).visible(true));

                            Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
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

                          *//*  latitude2 =   mLastKnownLocation.getLatitude();
                            longitude2 =    mLastKnownLocation.getLongitude();*//*

                            latitude =   mLastKnownLocation.getLatitude();
                            longitude =    mLastKnownLocation.getLongitude();


                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                                //Toast.makeText(PlacrPickerActivity.this, ""+mLastKnownLocation.getLatitude()+"\n"+mLastKnownLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                            }




                    } else {
                          *//*  Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());*//*
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
    }*/




    /**
     * If connected get lat and long
     *
     */
   /* @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            //If everything went fine lets get latitude and longitude
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();

            Toast.makeText(activity, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        *//*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         *//*
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(activity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                *//*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 *//*
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            *//*
             * If no resolution is available, display a dialog to the
             * user with the error.
             *//*
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    *//**
     * If locationChanges change lat and long
     *
     *
     * @param location
     *//*
    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        Toast.makeText(activity, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
    }

*/
    private void startLocationService(){

        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(activity, LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

               activity.startForegroundService(serviceIntent);
            } else {

                activity.startService(serviceIntent);

            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.vishalperipherals.maps_demo.Services".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private void getUserLocationhttpRequest() {

        if (!NetworkConnection.isConnected(activity)){
            Toast.makeText(activity, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://vishalperipherals.in/delivery/api/update_cus.php?";


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
                        Integer success = object.getInt("success");

                        if (success==1) {

                           userLatitude = Double.valueOf(object.getString("customer_latitude_update"));
                          userLongitude = Double.valueOf(object.getString("customer_longitude_update"));

                            calculateDirections();
                            addMapMarkers();
                        }
                        else {

                            Toast.makeText(activity, "Server return invalid response ", Toast.LENGTH_SHORT).show();
                        }
                        // iconSubCategoriesList.add(item);
                    }
                }

                catch (JSONException e) {
                    e.printStackTrace();

                    Toast.makeText(activity, "Invalid response from the server ", Toast.LENGTH_SHORT).show();

                }
            }

        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                Toast.makeText(activity, "Something went wrong ", Toast.LENGTH_SHORT).show();

            }

        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding the parameters to the request
                params.put("action", "trackorder");
                params.put("cid", "3");

                return params;
            }

        };

        int socketTimeout = 30000; //30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(
                socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);


        jsonRequest.setRetryPolicy(policy);

        ApplicationRequest.getInstance(activity).addToRequestQueue(jsonRequest);


    }

}
