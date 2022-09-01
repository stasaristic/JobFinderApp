package com.example.jobfinder.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.jobfinder.databinding.ActivityEditJobRecruiterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class EditJobRecruiterActivity extends AppCompatActivity {

    //view binding
    private ActivityEditJobRecruiterBinding binding;

    //jobId get form intent started from PostedJobsActivity
    private String jobId;

    //progress dialog
    private ProgressDialog progressDialog;

    private ArrayList<String> companyTitleArrayList, companyIdArrayList;
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;
    private ArrayList<String> typeTitleArrayList, typeIdArrayList;
    private ArrayList<String> seniorityTitleArrayList, seniorityIdArrayList;

    private static final String TAG = "JOB_EDIT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditJobRecruiterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        jobId = getIntent().getStringExtra("jobId");

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadCompany();
        loadCategory();
        loadType();
        loadSeniority();
        loadJobInfo();

        //handle click pick company
        binding.companyTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                companyDialog();
            }
        });
        //handle click pick category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialog();
            }
        });
        //handle click pick type
        binding.typeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeDialog();
            }
        });
        //handle click pick seniority
        binding.seniorityTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seniorityDialog();
            }
        });


        //handle click back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click begin upload
        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }

    private String title="", description="";
    private void validateData() {
        //get data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        //validate fata
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Enter title...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Enter description...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedCategoryId)) {
            Toast.makeText(this, "Pick category...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedCompanyId)) {
            Toast.makeText(this, "Pick company...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedTypeId)) {
            Toast.makeText(this, "Pick type...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedSeniorityId)) {
            Toast.makeText(this, "Pick seniority...", Toast.LENGTH_SHORT).show();
        }
        else {
            updateJob();
        }
    }

    private void updateJob() {
        Log.d(TAG, "updateJob: Starting update...");

        //show progress
        progressDialog.setMessage("Updating book info...");
        progressDialog.show();

        //setup data to update to db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("companyId", ""+selectedCompanyId);
        hashMap.put("categoryId", ""+selectedCategoryId);
        hashMap.put("seniorityId", ""+selectedSeniorityId);
        hashMap.put("typeId", ""+selectedTypeId);

        //start updating
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Jobs");
        ref.child(jobId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Successfull update");
                        progressDialog.dismiss();
                        Toast.makeText(EditJobRecruiterActivity.this, "Job info updated...", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update due to " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(EditJobRecruiterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadJobInfo() {
        Log.d(TAG, "loadJobInfo: Loading job info");

        DatabaseReference refJobs = FirebaseDatabase.getInstance().getReference("Jobs");
        refJobs.child(jobId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get job info
                        String title = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        selectedCategoryId = ""+snapshot.child("categoryId").getValue();
                        selectedCompanyId = ""+snapshot.child("companyId").getValue();
                        selectedTypeId = ""+snapshot.child("typeId").getValue();
                        selectedSeniorityId = ""+snapshot.child("seniorityId").getValue();
                        //set to views
                        binding.titleEt.setText(title);
                        binding.descriptionEt.setText(description);

                        Log.d(TAG, "onDataChange: Loading through ids");
                        DatabaseReference refJobCategory = FirebaseDatabase.getInstance().getReference("Categories");
                        refJobCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String category = "" + snapshot.child("category").getValue();

                                        binding.categoryTv.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                        DatabaseReference refJobCompany = FirebaseDatabase.getInstance().getReference("Companies");
                        refJobCompany.child(selectedCompanyId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String company = "" + snapshot.child("company").getValue();

                                        binding.companyTv.setText(company);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                        DatabaseReference refJobType = FirebaseDatabase.getInstance().getReference("Types");
                        refJobType.child(selectedTypeId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String type = "" + snapshot.child("type").getValue();

                                        binding.typeTv.setText(type);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                        DatabaseReference refJobSeniority= FirebaseDatabase.getInstance().getReference("Seniority");
                        refJobSeniority.child(selectedSeniorityId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String seniority = "" + snapshot.child("seniority").getValue();

                                        binding.seniorityTv.setText(seniority);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String selectedCompanyId = "", selectedCompanyTitle= "";
    private String selectedCategoryId= "", selectedCategoryTitle= "";
    private String selectedTypeId= "", selectedTypeTitle= "";
    private String selectedSeniorityId= "", selectedSeniorityTitle= "";

    private void companyDialog() {
        //make string array from arraylist of string
        String[] companiesArray = new String[companyTitleArrayList.size()];
        for (int i=0; i<companyTitleArrayList.size(); i++) {
            companiesArray[i] = companyTitleArrayList.get(i);
        }

        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Company")
                .setItems(companiesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCompanyId = companyIdArrayList.get(which);
                        selectedCompanyTitle = companyTitleArrayList.get(which);

                        //set textview
                        binding.companyTv.setText(selectedCompanyTitle);
                    }
                })
                .show();
    }

    private void categoryDialog() {
        //make string array from arraylist of string
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i=0; i<categoryTitleArrayList.size(); i++) {
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCategoryId = categoryIdArrayList.get(which);
                        selectedCategoryTitle = categoryTitleArrayList.get(which);

                        //set textview
                        binding.categoryTv.setText(selectedCategoryTitle);
                    }
                })
                .show();
    }

    private void typeDialog() {
        //make string array from arraylist of string
        String[] typesArray = new String[typeTitleArrayList.size()];
        for (int i=0; i<typeTitleArrayList.size(); i++) {
            typesArray[i] =typeTitleArrayList.get(i);
        }

        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Type")
                .setItems(typesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedTypeId = typeIdArrayList.get(which);
                        selectedTypeTitle = typeTitleArrayList.get(which);

                        //set textview
                        binding.typeTv.setText(selectedTypeTitle);
                    }
                })
                .show();
    }

    private void seniorityDialog() {
        //make string array from arraylist of string
        String[] seniorityArray = new String[seniorityTitleArrayList.size()];
        for (int i=0; i<seniorityTitleArrayList.size(); i++) {
            seniorityArray[i] =seniorityTitleArrayList.get(i);
        }

        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Seniority")
                .setItems(seniorityArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedSeniorityId = seniorityIdArrayList.get(which);
                        selectedSeniorityTitle = seniorityTitleArrayList.get(which);

                        //set textview
                        binding.seniorityTv.setText(selectedSeniorityTitle);
                    }
                })
                .show();
    }

    private void loadSeniority() {
        Log.d(TAG, "loadSeniority: Loading Seniority...");

        seniorityIdArrayList = new ArrayList<>();
        seniorityTitleArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Seniority");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                seniorityIdArrayList.clear();
                seniorityTitleArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = ""+ds.child("id").getValue();
                    String seniority = ""+ds.child("seniority").getValue();

                    seniorityIdArrayList.add(id);
                    seniorityTitleArrayList.add(seniority);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadType() {
        Log.d(TAG, "loadType: Loading Type...");

        typeIdArrayList = new ArrayList<>();
        typeTitleArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Types");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                typeIdArrayList.clear();
                typeTitleArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = ""+ds.child("id").getValue();
                    String type = ""+ds.child("type").getValue();

                    typeIdArrayList.add(id);
                    typeTitleArrayList.add(type);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCategory() {
        Log.d(TAG, "loadCategory: Loading Category...");

        categoryIdArrayList = new ArrayList<>();
        categoryTitleArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Category");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArrayList.clear();
                categoryTitleArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = ""+ds.child("id").getValue();
                    String category = ""+ds.child("category").getValue();

                    categoryIdArrayList.add(id);
                    categoryTitleArrayList.add(category);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCompany() {
        Log.d(TAG, "loadCompany: Loading Company...");

        companyIdArrayList = new ArrayList<>();
        companyTitleArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Company");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                companyIdArrayList.clear();
                companyTitleArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = ""+ds.child("id").getValue();
                    String company = ""+ds.child("company").getValue();

                    categoryIdArrayList.add(id);
                    categoryTitleArrayList.add(company);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}