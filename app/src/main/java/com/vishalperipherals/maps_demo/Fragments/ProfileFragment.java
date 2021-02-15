package com.vishalperipherals.maps_demo.Fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vishalperipherals.maps_demo.R;

public class ProfileFragment extends Fragment {

    Activity activity;
    View view;

    EditText etDepart,etDest;
    Button btnClick;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity)context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

         view =     inflater.inflate(R.layout.profile_fragment, container, false);

         initalize();
        return view;
    }

    private void initalize() {

        etDest  = view.findViewById(R.id.etDest);
        etDepart  = view.findViewById(R.id.etDepart);
        btnClick  = view.findViewById(R.id.btnClick);

        btnClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String dest = etDest.getText().toString().trim();
                String depart = etDepart.getText().toString().trim();

                if (dest.isEmpty()){

                    etDest.setError("Empty");
                    etDest.requestFocus();
                    return;
                }

                if (depart.isEmpty()){

                    etDepart.setError("Empty");
                    etDepart.requestFocus();
                    return;
                }

                openLocRoute(dest, depart);
            }
        });
    }

    private void openLocRoute(String dest, String depart) {

        try {
            Uri uri = Uri.parse("https://www.google.co.in/maps/dir"+ depart +"/"+ dest);
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.setPackage("com.google.android.apps.maps");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }catch (ActivityNotFoundException an)
        {


        Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }

    }


}
