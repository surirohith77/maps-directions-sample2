package com.vishalperipherals.maps_demo.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vishalperipherals.maps_demo.Fragments.StoreFragment;
import com.vishalperipherals.maps_demo.Internet.NetworkConnection;
import com.vishalperipherals.maps_demo.R;
import com.vishalperipherals.maps_demo.Services.LocationService;
import com.vishalperipherals.maps_demo.models.Customer_LatLng;

public class RouteActivity extends AppCompatActivity {

    private static final String TAG = "loc_service";
    Double desti_latitude, desti_longitude, userLatitude, userLongitude;
    DatabaseReference drl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        drl = FirebaseDatabase.getInstance().getReference("loc");

        Bundle bundle = getIntent().getExtras();
        if (bundle!=null){
            desti_latitude = bundle.getDouble("lat");
            desti_longitude = bundle.getDouble("lng");
            Toast.makeText(this, ""+desti_latitude+"\n"+desti_longitude, Toast.LENGTH_SHORT).show();
        }

        initTool();

        startLocationService();

        getLocFromFbase();



    }

    private void initTool() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Directions ");
        setSupportActionBar(toolbar);
    }


    private void getLocFromFbase(){


        if (!NetworkConnection.isConnected(this)){
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }

        drl.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {


                    Customer_LatLng customer_latLng = postSnapshot.getValue(Customer_LatLng.class);

                    assert customer_latLng != null;
                    userLatitude = customer_latLng.getLat();
                    userLongitude =  customer_latLng.getLng();



                    //calculateDirections();

                }




                //    updateViews("No customers till now", R.drawable.monkey_confusion);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //progressDialog.dismiss();
                //  updateViews("Something went wrong", R.drawable.monkey_anger3);
                Toast.makeText(RouteActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

            }





        });
    }


    private void inflateDirFragment() {

        StoreFragment fragment = StoreFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putDouble("user_latitude", userLatitude);
        bundle.putDouble("user_longitude", userLongitude);
        bundle.putDouble("desti_latitude", desti_latitude);
        bundle.putDouble("desti_longitude", desti_longitude);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //  transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.replace(R.id.fragment_container, fragment, getString(R.string.fragment_user_list));
        transaction.addToBackStack(getString(R.string.fragment_user_list));
        transaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calculate_directions,menu);
        return true;
    }

    public void showDir(MenuItem item) {

        inflateDirFragment();
    }


    private void startLocationService(){

        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(RouteActivity.this, LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                RouteActivity.this.startForegroundService(serviceIntent);

            } else {

                RouteActivity.this.startService(serviceIntent);

            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager)RouteActivity.this.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.vishalperipherals.maps_demo.Services".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

}