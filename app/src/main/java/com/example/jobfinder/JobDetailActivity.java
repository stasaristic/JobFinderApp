package com.example.jobfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

import com.example.jobfinder.databinding.ActivityJobDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JobDetailActivity extends AppCompatActivity {

    //view binding
    private ActivityJobDetailBinding binding;

    String jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        jobId = intent.getStringExtra("jobId");

        lookJobDetails();

        //handle click back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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
                        String title =""+ snapshot.child("title").getValue();
                        String description = ""+ snapshot.child("description").getValue();
                        String categoryId  = ""+ snapshot.child("categoryId").getValue();
                        String companyId = ""+ snapshot.child("companyId").getValue();
                        String seniorityId = ""+ snapshot.child("seniorityId").getValue();
                        String typeId = ""+ snapshot.child("typeId").getValue();
                        String url = ""+ snapshot.child("url").getValue();

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