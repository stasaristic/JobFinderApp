package com.example.jobfinder.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.jobfinder.databinding.ActivityDashboardRecruiterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardRecruiterActivity extends AppCompatActivity {

    // view binding
    private ActivityDashboardRecruiterBinding binding;

    // firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardRecruiterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        // handle check, logout
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

        // handle check, post new job click
        binding.postNewJobBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardRecruiterActivity.this, JobPostActivity.class));
            }
        });

        // handle check, post new job click
        binding.viewPostedJobsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardRecruiterActivity.this, PostedJobsActivity.class));
            }
        });


    }

    private void checkUser() {
        // get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null)
        {
            // not logged in
            startActivity(new Intent(DashboardRecruiterActivity.this, MainActivity.class));
            finish();
        }
        else
        {
            // logged in
            String email = firebaseUser.getEmail();
            // set in textview of toolbar
            binding.subtitleTv.setText(email);
        }
    }
}