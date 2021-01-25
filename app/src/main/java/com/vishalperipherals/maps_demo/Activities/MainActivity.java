package com.vishalperipherals.maps_demo.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.vishalperipherals.maps_demo.Fragments.DeliveryFragment;
import com.vishalperipherals.maps_demo.Fragments.DestinationFragment;
import com.vishalperipherals.maps_demo.Fragments.ProfileFragment;
import com.vishalperipherals.maps_demo.Fragments.ShareFragment;
import com.vishalperipherals.maps_demo.Fragments.StoreFragment;
import com.vishalperipherals.maps_demo.R;

public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new DestinationFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_store);
        }

        /*if (savedInstanceState == null) {

            StoreFragment fragment = StoreFragment.newInstance();
            Bundle bundle = new Bundle();
            bundle.putDouble(getString(R.string.lat), 1);
            bundle.putDouble(getString(R.string.lng), 2);
            fragment.setArguments(bundle);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            //  transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
            transaction.replace(R.id.fragment_container, fragment, getString(R.string.fragment_user_list));
            transaction.addToBackStack(getString(R.string.fragment_user_list));
            transaction.commit();
        }
*/
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.nav_store:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new DestinationFragment()).commit();
                break;

            case R.id.nav_delivery:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new DeliveryFragment()).commit();
                break;

            case R.id.nav_profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
                break;

            case R.id.nav_share:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ShareFragment()).commit();
                break;
            case R.id.nav_send:
                Toast.makeText(this, "Send", Toast.LENGTH_SHORT).show();
                break;

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}