package com.example.jobfinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.jobfinder.databinding.ActivityDashboardRecruiterBinding;
import com.example.jobfinder.databinding.ActivityJobPostBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class JobPostActivity extends AppCompatActivity {

    // setup view binding
    private ActivityJobPostBinding binding;

    // auth
    private FirebaseAuth firebaseAuth;

    // progress Dialog
    private ProgressDialog progressDialog;

    // arraylist for companies, categories, types and seniority
    private ArrayList<ModelCompany> companyArrayList;
    private ArrayList<ModelCategory> categoryArrayList;
    private ArrayList<ModelType> typeArrayList;
    private ArrayList<ModelSeniority> seniorityArrayList;

    // uri of picked pdf
    private Uri pdfUri = null;

    // tag for debugging
    private static final String TAG = "ADD_PDF_TAG";

    private static final int PDF_PICK_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadItems();

        // setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        // handle click, back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // handle click, attach pdf
        binding.attachPdfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });

        // handle click, pick company
        binding.companyTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemPickDialog("Companies");
            }
        });

        // handle click, pick category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemPickDialog("Categories");
            }
        });

        // handle click, pick type
        binding.typeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemPickDialog("Types");
            }
        });

        // handle click, pick seniority
        binding.seniorityTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemPickDialog("Seniority");
            }
        });

        // handle click, submit and post
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // validate data
                validateData();
            }
        });
    }

    private String title = "", description = "", company = "", category = "", type = "", seniority = "";

    private void validateData() {
        // Step 1: Validate data
        Log.d(TAG, "validateData: validating data...");

        // get data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        company = binding.companyTv.getText().toString().trim();
        category = binding.categoryTv.getText().toString().trim();
        type = binding.typeTv.getText().toString().trim();
        seniority = binding.seniorityTv.getText().toString().trim();

        // validate data
        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(company)) {
            Toast.makeText(this, "Enter Company...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Enter Category...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(type)) {
            Toast.makeText(this, "Enter Job Type...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(seniority)) {
            Toast.makeText(this, "Enter Seniority...", Toast.LENGTH_SHORT).show();
        }
        else if (pdfUri == null) {
            Toast.makeText(this, "Pick Pdf...", Toast.LENGTH_SHORT).show();
        }
        else {
            // all data is valid, can upload now
            Log.d(TAG, "validateData: data is valid...");
            uploadJobToStorage();
        }

    }

    private void uploadJobToStorage() {
        // Step 2: Upload data to firebase storage
        Log.d(TAG, "uploadJobToStorage: uploading data to storage...");

        // show progress
        progressDialog.setMessage("Posting Job...");
        progressDialog.show();

        // timestamp
        long timestamp = System.currentTimeMillis();

        // path of pdf to firebase storage
        String filePathAndName = "Jobs/" + timestamp;

        // storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: PDF upload to storage...");
                        Log.d(TAG, "onSuccess: getting PDF url...");

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl = "" + uriTask.getResult();

                        // upload to firebase db
                        uploadPdfInfo(uploadedPdfUrl, timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: PDF upload failed due to " + e.getMessage());
                        Toast.makeText(JobPostActivity.this, "PDF upload failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadPdfInfo(String uploadedPdfUrl, long timestamp) {
        Log.d(TAG, "uploadPdfInfo: uploading PDF info to firebase db...");

        progressDialog.setMessage("Uploading pdf info...");

        String uid = firebaseAuth.getUid();

        // setup data to upload
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("company", ""+company);
        hashMap.put("category", ""+category);
        hashMap.put("type", ""+type);
        hashMap.put("seniority", ""+seniority);
        hashMap.put("url", ""+uploadedPdfUrl);
        hashMap.put("timestamp", timestamp);

        // db reference: DB > Jobs
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Jobs");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: successfully uploaded...");
                        Toast.makeText(JobPostActivity.this, "Successfully uploaded...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Failed to upload to db due to " + e.getMessage());
                        Toast.makeText(JobPostActivity.this, "Failed to upload to db due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadItems() {
        String typeItems[] = {"Companies", "Categories", "Types", "Seniority"};
        DatabaseReference ref;
        for(int i=0; i < typeItems.length; i++){
            Log.d(TAG, "loading items: " + typeItems[i]);
            if (typeItems[i]=="Companies"){
                companyArrayList = new ArrayList<>();

                // db reference to lead items... db > ItemType
                ref = FirebaseDatabase.getInstance().getReference(typeItems[i]);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        companyArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            ModelCompany model = ds.getValue(ModelCompany.class);
                            companyArrayList.add(model);

                            Log.d(TAG, "onDataChange: " + model.getCompany());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            else if (typeItems[i]=="Categories"){
                categoryArrayList = new ArrayList<>();

                // db reference to lead items... db > ItemType
                ref = FirebaseDatabase.getInstance().getReference(typeItems[i]);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        categoryArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            ModelCategory model = ds.getValue(ModelCategory.class);
                            categoryArrayList.add(model);

                            Log.d(TAG, "onDataChange: " + model.getCategory());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            else if (typeItems[i]=="Types"){
                typeArrayList = new ArrayList<>();

                // db reference to lead items... db > ItemType
                ref = FirebaseDatabase.getInstance().getReference(typeItems[i]);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        typeArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            ModelType model = ds.getValue(ModelType.class);
                            typeArrayList.add(model);

                            Log.d(TAG, "onDataChange: " + model.getType());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            else if (typeItems[i]=="Seniority"){
                seniorityArrayList = new ArrayList<>();

                // db reference to lead items... db > ItemType
                ref = FirebaseDatabase.getInstance().getReference(typeItems[i]);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        seniorityArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            ModelSeniority model = ds.getValue(ModelSeniority.class);
                            seniorityArrayList.add(model);

                            Log.d(TAG, "onDataChange: " + model.getSeniority());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }

    }

    private void itemPickDialog(String items) {
        Log.d(TAG, "itemPickDialog: showing " + items + " pick dialog...");

        // get string array of items from arraylist
        if (items == "Companies") {
            String[] companyArray = new String[companyArrayList.size()];

            for (int i=0; i<companyArrayList.size(); i++) {
                companyArray[i] = companyArrayList.get(i).getCompany();
            }

            // alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pick Company")
                    .setItems(companyArray, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // handle item click
                            // get clicked item from list
                            String company = companyArray[which];
                            // set to company textview
                            binding.companyTv.setText(company);

                            Log.d(TAG, "onClick: Company chosen: " + company);
                        }
                    })
                    .show();
        }
        else if (items == "Categories") {
            String[] categoryArray = new String[categoryArrayList.size()];

            for (int i=0; i<categoryArrayList.size(); i++) {
                categoryArray[i] = categoryArrayList.get(i).getCategory();
            }

            // alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pick Category")
                    .setItems(categoryArray, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // handle item click
                            // get clicked item from list
                            String category = categoryArray[which];
                            // set to company textview
                            binding.categoryTv.setText(category);

                            Log.d(TAG, "onClick: Category chosen: " + category);
                        }
                    })
                    .show();
        }
        else if (items == "Types") {
            String[] typesArray = new String[typeArrayList.size()];

            for (int i=0; i<typeArrayList.size(); i++) {
                typesArray[i] = typeArrayList.get(i).getType();
            }

            // alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pick Type")
                    .setItems(typesArray, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // handle item click
                            // get clicked item from list
                            String type = typesArray[which];
                            // set to company textview
                            binding.typeTv.setText(type);

                            Log.d(TAG, "onClick: Type chosen: " + type);
                        }
                    })
                    .show();
        }
        else if (items == "Seniority") {
            String[] seniorityArray = new String[seniorityArrayList.size()];

            for (int i=0; i<seniorityArrayList.size(); i++) {
                seniorityArray[i] = seniorityArrayList.get(i).getSeniority();
            }

            // alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pick Seniority")
                    .setItems(seniorityArray, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // handle item click
                            // get clicked item from list
                            String seniority = seniorityArray[which];
                            // set to company textview
                            binding.seniorityTv.setText(seniority);

                            Log.d(TAG, "onClick: Seniority chosen: " + seniority);
                        }
                    })
                    .show();
        }
        else {
            Log.d(TAG, "itemPickDialog: cancelled");
        }


    }

    private void pdfPickIntent() {

        Log.d(TAG, "pdfPickIntent: starting pdf pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == PDF_PICK_CODE){
                Log.d(TAG, "onActivityResult: PDF Picked");

                pdfUri = data.getData();

                Log.d(TAG, "onActivityResult: URI: " + pdfUri);
            }
        }else {
            Log.d(TAG, "onActivityResult: cancelled picking PDF");
            Toast.makeText(this, "Cancelled picking PDF...", Toast.LENGTH_SHORT).show();
        }
    }
}