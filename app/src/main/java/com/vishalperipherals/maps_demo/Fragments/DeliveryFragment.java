package com.vishalperipherals.maps_demo.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vishalperipherals.maps_demo.Adapter.RvAdapter;
import com.vishalperipherals.maps_demo.R;
import com.vishalperipherals.maps_demo.models.Fhandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeliveryFragment extends Fragment {

    Activity activity;
    View view;
    TextView tvImageUpload;
    Bitmap bitmap;
    ImageView ivCamera;
    String img;
    Button btnSave;
    Uri imageUri;

    private static final int GALLERY_CODE = 12;
    private static final int CAMERA_CODE = 13;

    private static final int STORAGE_PERMISSION_CODE = 132;
    private static final int CAMERA_PERMISSION_CODE = 133;
    private static final int REQUEST_NO_NETWORK = 1321;

    DatabaseReference databaseReference;
    StorageReference storageReference;
    String downloadurl;

    RecyclerView recyclerView;
    List<Fhandler> fhandlerList;
    RvAdapter rvAdapter;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity)context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

         view =     inflater.inflate(R.layout.delivery_fragment, container, false);

        tvImageUpload = view.findViewById(R.id.tvImageUpload);
        ivCamera = view.findViewById(R.id.ivCamera);
        btnSave = view.findViewById(R.id.btnSave);

        databaseReference = FirebaseDatabase.getInstance().getReference("Hello");
        storageReference = FirebaseStorage.getInstance().getReference("Hello");

        initRv();


        tvImageUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooseAlert();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadtoStorage();
            }
        });

        getDataFromFBase();

        return view;
    }

    private void initRv() {

        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    }


    private void openImageChooseAlert() {

        ViewGroup viewGroup = view.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.choose_image_type_dialog, viewGroup, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity,R.style.CustomAlertDialog);
        builder.setView(dialogView);
        builder.setCancelable(true);
        AlertDialog alertDialog = builder.create();

        //  WindowManager.LayoutParams wmlp = alertDialog.getWindow().getAttributes();
        // wmlp.gravity = Gravity.TOP;
        alertDialog.show();


        TextView tvCamera, tvGallery;

        tvCamera = dialogView.findViewById(R.id.tvCamera);
        tvGallery = dialogView.findViewById(R.id.tvGallery);

        tvGallery.setOnClickListener(v -> {

            alertDialog.dismiss();
            openGalleryPermissionCheck();
        });

        tvCamera.setOnClickListener(v -> {

            alertDialog.dismiss();

            openCAMERAPermissionCheck();
        });

    }

    private void openGalleryPermissionCheck() {

        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    /*Toast.makeText(activity, "You have already granted this permission!",
                            Toast.LENGTH_SHORT).show();*/

            openGallery();

            // showFileChooser();
        } else {
            requestStoragePermission();
        }
    }


    private void openGallery() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),GALLERY_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), data.getData());

                         imageUri = data.getData();
                         ivCamera.setImageURI(imageUri);
                         ivCamera.setVisibility(View.VISIBLE);
                        //   String    img = BitMapToString(bitmap);
                     //   setImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        else  if (requestCode == CAMERA_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                bitmap = (Bitmap) data.getExtras().get("data");
                setImage();
            }
        }
    }

    private void setImage(){

        ivCamera.setImageBitmap(bitmap);
        ivCamera.setVisibility(View.VISIBLE);
       // img = BitMapToString(bitmap);
       // counter++;
        //  img = "data:image/"+getMimeType(this,profileUri)+";base64,"+img;
    }



    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new android.app.AlertDialog.Builder(activity)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to access gallery")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);

                            openGallery();
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
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                openGallery();
            } else {
                Toast.makeText(activity, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }

        else if (requestCode == CAMERA_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                openCamera();
            } else {
                Toast.makeText(activity, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void openCAMERAPermissionCheck() {

        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    /*Toast.makeText(activity, "You have already granted this permission!",
                            Toast.LENGTH_SHORT).show();*/

            openCamera();

            // showFileChooser();
        } else {
            requestCameraPermission();
        }
    }

    private void openCamera(){
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_CODE);
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.CAMERA)) {

            new android.app.AlertDialog.Builder(activity)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to access Camera")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity,
                                    new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);

                            openCamera();
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
                    new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }


    private void uploadtoStorage(){


       // final StorageReference filepath = storageReference.child(String.valueOf(System.currentTimeMillis()));
        final StorageReference filepath = storageReference.child("Category");

        final UploadTask uploadTask = filepath.putFile(imageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Toast.makeText(getContext(), "Uploaded Successfully", Toast.LENGTH_SHORT).show();

                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                        if (!task.isSuccessful()){

                            throw task.getException();
                        }else {

                            downloadurl = filepath.getDownloadUrl().toString();
                        }

                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()){

                            downloadurl = task.getResult().toString();
                            Toast.makeText(getContext(), "Data Saved Successfully", Toast.LENGTH_SHORT).show();

                            savetodatabase();

                      /*  Intent intent = new Intent(getContext(),ViewallFragment.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);*/
                        }
                    }
                });
            }
        });
    }


    public void savetodatabase(){

        String uniqueid = databaseReference.push().getKey();

        Fhandler fh = new Fhandler(downloadurl,downloadurl+"  rohith");

        databaseReference.child(uniqueid).setValue(fh);

        Toast.makeText(getContext(), "Data Saved", Toast.LENGTH_SHORT).show();

        ivCamera.setImageResource(R.drawable.ic_baseline_camera_250);

    }

    private void getDataFromFBase(){

        fhandlerList = new ArrayList<>();
      //  databaseReference = FirebaseDatabase.getInstance().getReference("Hello");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                fhandlerList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                    Fhandler fh = snapshot.getValue(Fhandler.class);
                    fhandlerList.add(fh);
                }

                rvAdapter = new RvAdapter(fhandlerList);
                recyclerView.setAdapter(rvAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(),databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
