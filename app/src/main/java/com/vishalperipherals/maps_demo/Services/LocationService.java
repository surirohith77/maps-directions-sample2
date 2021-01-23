package com.vishalperipherals.maps_demo.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vishalperipherals.maps_demo.Internet.NetworkConnection;
import com.vishalperipherals.maps_demo.Network.ApplicationRequest;
import com.vishalperipherals.maps_demo.models.Customer_LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LocationService  extends Service {

    private static final String TAG = "LocationService";

    private FusedLocationProviderClient mFusedLocationClient;
    private final static long UPDATE_INTERVAL = 4 * 1000;  /* 4 secs */
    private final static long FASTEST_INTERVAL = 2000; /* 2 sec */

    DatabaseReference drl;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {

        drl = FirebaseDatabase.getInstance().getReference("loc");

        super.onCreate();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "maps_demo_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("OffO order confirmed")
                    .setContentText("Your Delivery is on the way").build();

            startForeground(1, notification);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");
        getLocation();
        return START_NOT_STICKY;
    }


    private void getLocation() {

        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);


        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: stopping the location service.");
            stopSelf();
            return;
        }
        Log.d(TAG, "getLocation: getting location information.");
        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {

                        Log.d(TAG, "onLocationResult: got location result.");

                        Location location = locationResult.getLastLocation();

                       /* if (location != null) {
                            User user = ((UserClient)(getApplicationContext())).getUser();
                            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                            UserLocation userLocation = new UserLocation(geoPoint,null,user);
                            saveUserLocation(userLocation);
                        }*/
                        if (location != null){

                          //  Toast.makeText(LocationService.this, ""+location.getLatitude()+"\n"+location.getLongitude(), Toast.LENGTH_SHORT).show();
                            sendhttprequest(location.getLatitude(),location.getLongitude());
                            savetoFbase(location.getLatitude(),location.getLongitude());
                        }
                    }
                },
                Looper.myLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed
    }



    private void sendhttprequest(double latitude, double longitude) {

        if (!NetworkConnection.isConnected(this)){
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://vishalperipherals.in/delivery/api/customer_update.php?";


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

                        }
                        else {

                            Toast.makeText(LocationService.this, "Server return invalid response ", Toast.LENGTH_SHORT).show();


                        }

                        // iconSubCategoriesList.add(item);

                    }


                }

                catch (JSONException e) {
                    e.printStackTrace();
                    ;
                    Toast.makeText(LocationService.this, "Invalid response from the server ", Toast.LENGTH_SHORT).show();


                }
            }

        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                Toast.makeText(LocationService.this, "Something went wrong ", Toast.LENGTH_SHORT).show();

            }

        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding the parameters to the request
                params.put("action", "trackupdate");
                params.put("clongitude", String.valueOf(longitude));
                params.put("clatitude", String.valueOf(latitude));
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

        ApplicationRequest.getInstance(this).addToRequestQueue(jsonRequest);



    }


    private void savetoFbase(double latitude, double longitude) {


        Customer_LatLng customer_latLng = new Customer_LatLng(latitude,longitude);
        String id = drl.push().getKey();
        drl.child("rohith").setValue(customer_latLng);
    }



}
