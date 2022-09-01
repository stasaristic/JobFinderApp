package com.example.jobfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.example.jobfinder.adapters.AdapterType;
import com.example.jobfinder.databinding.ActivityJobTypeAddBinding;
import com.example.jobfinder.models.ModelType;
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

public class JobTypeAddActivity extends AppCompatActivity {

    // view binding
    private ActivityJobTypeAddBinding binding;

    // firebase auth
    private FirebaseAuth firebaseAuth;

    // progress dialog
    private ProgressDialog progressDialog;

    // ArrayList to store categories
    private ArrayList<ModelType> typeArrayList;

    // adapter
    private AdapterType adapterType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobTypeAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadCategories();

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
                    adapterType.getFilter().filter(s);
                }
                catch (Exception ex) {

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadCategories() {
        // init arraylist
        typeArrayList = new ArrayList<>();
        // get all companies from firebase > Companies
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Types");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // clear arraylist before adding data into it
                typeArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    // get data
                    ModelType model = ds.getValue(ModelType.class);

                    // add to arraylist
                    typeArrayList.add(model);
                }
                // set up adapter
                adapterType = new AdapterType(JobTypeAddActivity.this, typeArrayList);
                // set adapter to recycleview
                binding.typeRv.setAdapter(adapterType);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String jobType = "";

    private void validateData() {
        // get data
        jobType = binding.jobTypeEt.getText().toString().trim();
        // validate if not empty
        if (TextUtils.isEmpty(jobType)) {
            Toast.makeText(this, "Please enter category...!", Toast.LENGTH_SHORT).show();
        }
        else {
            addJobTypeFirebase();
        }
    }

    private void addJobTypeFirebase() {

        // show progress
        progressDialog.setMessage("Adding type...");
        progressDialog.show();

        // get timestamp
        long timestamp = System.currentTimeMillis();

        // setup info to add in firebase db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("type", ""+jobType);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", ""+firebaseAuth.getUid());

        // add to firebase db ... Database Root > Types > typeId > type info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Types");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // category add success
                        progressDialog.dismiss();
                        Toast.makeText(JobTypeAddActivity.this, "Job type added successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // category add failure
                        progressDialog.dismiss();
                        Toast.makeText(JobTypeAddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}