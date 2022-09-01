package com.example.jobfinder.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.example.jobfinder.adapters.AdapterSeniority;
import com.example.jobfinder.databinding.ActivityJobSeniorityBinding;
import com.example.jobfinder.models.ModelSeniority;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class JobSeniorityActivity extends AppCompatActivity {

    // view binding
    private ActivityJobSeniorityBinding binding;

    // firebase auth
    private FirebaseAuth firebaseAuth;

    // progress dialog
    private ProgressDialog progressDialog;

    // ArrayList to store seniority
    private ArrayList<ModelSeniority> seniorityArrayList;

    // adapter
    private AdapterSeniority adapterSeniority;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobSeniorityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadSeniority();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, begin upload category
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // edit text change listen, search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterSeniority.getFilter().filter(s);
                }
                catch (Exception ex) {

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadSeniority() {
        // init arraylist
        seniorityArrayList = new ArrayList<>();
        // get all seniority from firebase > Seniority
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Seniority");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // clear arraylist before adding data into it
                seniorityArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    // get data
                    ModelSeniority model = ds.getValue(ModelSeniority.class);

                    // add to arraylist
                    seniorityArrayList.add(model);
                }
                // set up adapter
                adapterSeniority = new AdapterSeniority(JobSeniorityActivity.this, seniorityArrayList);
                // set adapter to recycleview
                binding.seniorityRv.setAdapter(adapterSeniority);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String jobSeniority = "";

    private void validateData() {

        // get data
        jobSeniority = binding.jobSeniorityEt.getText().toString().trim();
        // validate if not empty
        if (TextUtils.isEmpty(jobSeniority)) {
            Toast.makeText(this, "Please enter category...!", Toast.LENGTH_SHORT).show();
        }
        else {
            addJobSeniorityFirebase();
        }
    }

    private void addJobSeniorityFirebase() {

        // show progress
        progressDialog.setMessage("Adding seniority...");
        progressDialog.show();

        // get timestamp
        long timestamp = System.currentTimeMillis();

        // setup info to add in firebase db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("seniority", ""+jobSeniority);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", ""+firebaseAuth.getUid());

        // add to firebase db ... Database Root > Seniority > seniorityId > seniority info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Seniority");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // category add success
                        progressDialog.dismiss();
                        Toast.makeText(JobSeniorityActivity.this, "Job seniority added successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // category add failure
                        progressDialog.dismiss();
                        Toast.makeText(JobSeniorityActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}