package com.example.jobfinder.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.jobfinder.MyApplication;
import com.example.jobfinder.databinding.ActivityJobDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JobDetailActivity extends AppCompatActivity {

    //view binding
    private ActivityJobDetailBinding binding;

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    String jobId, title, url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        jobId = intent.getStringExtra("jobId");

        binding.downloadBtn.setVisibility(View.GONE);

        lookJobDetails();

        //handle click back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click read more
        binding.readJobSpecificationsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(JobDetailActivity.this, ReadMoreActivity.class);
                intent1.putExtra("jobId", jobId);
                startActivity(intent1);
            }
        });

        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted){
                        Log.d(TAG_DOWNLOAD, "Permission granted");
                        MyApplication.downloadJobSpec(this, ""+jobId, ""+title, ""+url);
                    }
                    else {
                        Log.d(TAG_DOWNLOAD, "Permission denied");
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                });


        //handle click, download pdf
        binding.downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG_DOWNLOAD, "Checking permission");
                if (ContextCompat.checkSelfPermission(JobDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG_DOWNLOAD, "onClick: Permission already granted");
                    MyApplication.downloadJobSpec(JobDetailActivity.this, ""+jobId, ""+title, ""+url);
                }
                else {
                    Log.d(TAG_DOWNLOAD, "onClick: Permission not granted");
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });


    }

    private void lookJobDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Jobs");
        ref.child(jobId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        title =""+ snapshot.child("title").getValue();
                        String description = ""+ snapshot.child("description").getValue();
                        String categoryId  = ""+ snapshot.child("categoryId").getValue();
                        String companyId = ""+ snapshot.child("companyId").getValue();
                        String seniorityId = ""+ snapshot.child("seniorityId").getValue();
                        String typeId = ""+ snapshot.child("typeId").getValue();
                        url = ""+ snapshot.child("url").getValue();

                        binding.downloadBtn.setVisibility(View.VISIBLE);

                        MyApplication.loadCategory(""+categoryId, binding.categoryTv);
                        MyApplication.loadCompany(""+companyId, binding.companyTv);
                        MyApplication.loadType(""+typeId, binding.typeTv);
                        MyApplication.loadSeniority(""+seniorityId, binding.seniorityTv);
                        MyApplication.loadPdfFromUrl(""+url, ""+title, binding.pdfView, binding.progressBar);

                        //set data
                        binding.titleTv.setText(title);
                        binding.descriptionTv.setText(description);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}