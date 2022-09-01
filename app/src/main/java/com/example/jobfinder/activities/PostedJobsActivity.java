package com.example.jobfinder.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.example.jobfinder.adapters.AdapterJobPostsRecruiter;
import com.example.jobfinder.databinding.ActivityPostedJobsBinding;
import com.example.jobfinder.models.ModelJobPosts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PostedJobsActivity extends AppCompatActivity {

    // view binding
    private ActivityPostedJobsBinding binding;

    // firebase auth
    private FirebaseAuth firebaseAuth;

    // ArrayList to store job posts
    private ArrayList<ModelJobPosts> jobPostsArrayList;

    // adapter
    private AdapterJobPostsRecruiter adapterJobPostsRecruiter;

    private static final String TAG = "PDF_LIST_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostedJobsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        
        loadJobPostingList();

        // search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // search as and when user type anny letter
                try {
                    adapterJobPostsRecruiter.getFilter().filter(s);
                }
                catch (Exception e) {

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // handle click, go to previous activity
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadJobPostingList() {
        // init arraylist
        jobPostsArrayList = new ArrayList<>();
        // get all job posts for firebase > Jobs
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Jobs");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // clear arraylist before adding data into it
                jobPostsArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    // get data
                    ModelJobPosts model = ds.getValue(ModelJobPosts.class);

                    // add to arraylist
                    jobPostsArrayList.add(model);
                }
                // set up adapter
                adapterJobPostsRecruiter = new AdapterJobPostsRecruiter(PostedJobsActivity.this, jobPostsArrayList);
                // set adapter to recycleview
                binding.jobsRv.setAdapter(adapterJobPostsRecruiter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}