package com.example.jobfinder.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.jobfinder.MyApplication;
import com.example.jobfinder.R;
import com.example.jobfinder.databinding.ActivityJobDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JobDetailActivity extends AppCompatActivity {

    //view binding
    private ActivityJobDetailBinding binding;

    boolean isInMyInterested = false;

    private FirebaseAuth firebaseAuth;

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

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser()!= null) {
            checkIsInterested();
        }

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

        //handle click, add/remove interested
        binding.addInterestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(JobDetailActivity.this, "You're not logged in", Toast.LENGTH_SHORT).show();
                }
                else {
                    checkUserTypeForInterest();
                }
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

    private void checkUserTypeForInterest() {
        // get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        //check in db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        // get user type
                        String userType = "" + snapshot.child("userType").getValue();
                        // check user type
                        if (userType.equals("user"))
                        {
                            //can add to interested
                            if(isInMyInterested) {
                                MyApplication.removeFromInterested(JobDetailActivity.this, jobId);
                            }
                            else {
                                MyApplication.addToInterested(JobDetailActivity.this, jobId);
                            }
                        }
                        else if (userType.equals("recruiter"))
                        {
                            // cant add
                            Toast.makeText(JobDetailActivity.this, "Recruiter doesn't have favourites", Toast.LENGTH_SHORT).show();
                        }
                        else if (userType.equals("admin"))
                        {
                            Toast.makeText(JobDetailActivity.this, "Admin doesn't have favourites", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

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

    private void checkIsInterested() {
        //logged in check
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Interested").child(jobId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyInterested = snapshot.exists(); // true if exists
                        if (isInMyInterested) {
                            binding.addInterestBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white,0,0);
                            binding.addInterestBtn.setText("Remove Interest");
                        }
                        else {
                            binding.addInterestBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white,0,0);
                            binding.addInterestBtn.setText("Add Interest");

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}