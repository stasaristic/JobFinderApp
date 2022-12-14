package com.example.jobfinder.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.jobfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    // firebase auth
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // start main screen after 2sec
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                checkUser();
            }
        }, 2000); // 2000 means 2 seconds


    }

    private void checkUser() {
        String TAG = "CHECK_USER_TAG";
        // get current user, if logged in
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null)
        {
            // start main activity
            Log.d(TAG, "checkUser: Postoji user");
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish(); // finish this activity
        }
        else
        {
            // user is logged in so it's important to check the type of the current user
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
                                Log.d(TAG, "onDataChange: user");
                                // open user dashboard
                                startActivity(new Intent(SplashActivity.this, DashboardUserActivity.class));
                                finish();

                            }
                            else if (userType.equals("recruiter"))
                            {
                                Log.d(TAG, "onDataChange: recruiter");
                                // open recruiter dashboard
                                startActivity(new Intent(SplashActivity.this, DashboardRecruiterActivity.class));
                                finish();
                            }
                            else if (userType.equals("admin"))
                            {
                                Log.d(TAG, "onDataChange: admin");
                                // open admin dashboard
                                startActivity(new Intent(SplashActivity.this, DashboardAdminActivity.class));
                                finish();
                            }
                            else {
                                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                Log.d(TAG, "onDataChange: niko");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {

                        }
                    });
        }
    }
}