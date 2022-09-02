package com.example.jobfinder;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jobfinder.adapters.AdapterJobUser;
import com.example.jobfinder.databinding.FragmentJobUserBinding;
import com.example.jobfinder.models.ModelJobPosts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JobUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JobUserFragment extends Fragment {

    private String categoryId, category, uid;

    private ArrayList<ModelJobPosts> jobPostsArrayList;
    private AdapterJobUser adapterJobUser;

    //view binding
    private FragmentJobUserBinding binding;

    private FirebaseAuth firebaseAuth;

    private static final String TAG = "JOBS_USER_TAG";

    public JobUserFragment() {
        // Required empty public constructor
    }

    public static JobUserFragment newInstance(String categoryId, String category, String uid) {
        JobUserFragment fragment = new JobUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate/bind the layout for this fragment
        binding = FragmentJobUserBinding.inflate(LayoutInflater.from(getContext()), container, false);

        if (category.equals("All")) {

            loadAllJobs();

        }
        else if(category.equals("Interested")) {
            if (firebaseAuth != null) {
                loadInterestingJobs();
            }
        }
        else {
            loadCategorizedJobs();
        }

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterJobUser.getFilter().filter(s);

                }catch (Exception e) {
                    Log.d(TAG, "onTextChanged: " + e.getMessage());
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return binding.getRoot();
    }

    private void loadInterestingJobs() {
        jobPostsArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Interested")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // clear list before adding data
                        jobPostsArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            String jobId = ""+ds.child("jobId").getValue();
                            Log.d(TAG, "onDataChange: " + jobId);

                            //set id to model
                            //ModelJobPosts model = new ModelJobPosts();
                            //model.setId(jobId);


                            DatabaseReference refJob = FirebaseDatabase.getInstance().getReference("Jobs");
                            refJob.child(jobId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    String title = ""+snapshot.child("title").getValue();
                                    String description = ""+snapshot.child("description").getValue();
                                    String categoryId = ""+snapshot.child("categoryId").getValue();
                                    String typeId = ""+snapshot.child("typeId").getValue();
                                    String companyId = ""+snapshot.child("companyId").getValue();
                                    String seniorityId = ""+snapshot.child("seniorityId").getValue();
                                    long timestamp = (long) snapshot.child("timestamp").getValue();
                                    String url = ""+snapshot.child("url").getValue();
                                    String uid = ""+snapshot.child("uid").getValue();

                                    ModelJobPosts modelJobPosts = new ModelJobPosts(uid, jobId, title, description,
                                            companyId, categoryId, typeId,
                                            seniorityId, url, timestamp, true);

                                    jobPostsArrayList.add(modelJobPosts);

                                    adapterJobUser = new AdapterJobUser(getContext(), jobPostsArrayList);

                                    binding.jobsRv.setAdapter(adapterJobUser);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadCategorizedJobs() {
        jobPostsArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Jobs");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                jobPostsArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()) {
                    ModelJobPosts model = ds.getValue(ModelJobPosts.class);

                    jobPostsArrayList.add(model);

                }
                adapterJobUser = new AdapterJobUser(getContext(), jobPostsArrayList);

                binding.jobsRv.setAdapter(adapterJobUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadAllJobs() {
        jobPostsArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Jobs");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                jobPostsArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()) {
                    ModelJobPosts model = ds.getValue(ModelJobPosts.class);

                    jobPostsArrayList.add(model);

                }
                adapterJobUser = new AdapterJobUser(getContext(), jobPostsArrayList);

                binding.jobsRv.setAdapter(adapterJobUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}