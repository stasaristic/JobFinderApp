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

import com.example.jobfinder.adapters.AdapterCategory;
import com.example.jobfinder.databinding.ActivityCategoryAddBinding;
import com.example.jobfinder.models.ModelCategory;
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

public class CategoryAddActivity extends AppCompatActivity {

    // view binding
    private ActivityCategoryAddBinding binding;

    // firebase auth
    private FirebaseAuth firebaseAuth;

    // progress dialog
    private ProgressDialog progressDialog;

    // ArrayList to store categories
    private ArrayList<ModelCategory> categoryArrayList;

    // adapter
    private AdapterCategory adapterCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadCategories();

        // configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // handle click, begin upload category
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        // handle click, go back
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
                    adapterCategory.getFilter().filter(s);
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
        categoryArrayList = new ArrayList<>();
        // get all companies from firebase > Companies
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // clear arraylist before adding data into it
                categoryArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    // get data
                    ModelCategory model = ds.getValue(ModelCategory.class);

                    // add to arraylist
                    categoryArrayList.add(model);
                }
                // set up adapter
                adapterCategory = new AdapterCategory(CategoryAddActivity.this, categoryArrayList);
                // set adapter to recycleview
                binding.categoryRv.setAdapter(adapterCategory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String category = "";

    private void validateData(){
        /* Before adding validate data */

        // get data
        category = binding.categoryEt.getText().toString().trim();
        // validate if not empty
        if (TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Please enter category...!", Toast.LENGTH_SHORT).show();
        }
        else {
            addCategoryFirebase();
        }
    }

    private void addCategoryFirebase() {
        // show progress
        progressDialog.setMessage("Adding category...");
        progressDialog.show();

        // get timestamp
        long timestamp = System.currentTimeMillis();

        // setup info to add in firebase db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("category", ""+category);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", ""+firebaseAuth.getUid());

        // add to firebase db ... Database Root > Categories > categoryId > category info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // category add success
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, "Category added successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // category add failure
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
