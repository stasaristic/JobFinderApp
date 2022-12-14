package com.example.jobfinder;

import static com.example.jobfinder.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinder.adapters.AdapterJobPostsRecruiter;
import com.example.jobfinder.models.ModelJobPosts;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

//application class runs before your launcher activity
public class MyApplication extends Application {
    @Override
    public void onCreate() {super.onCreate();}

    public static void deleteJob(Context context, String jobId, String jobUrl, String jobTitle) {
        String TAG = "DELETE_JOB_TAG";

        Log.d(TAG, "deleteJob: Deleting...");
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Deleting " + jobTitle);
        progressDialog.show();

        Log.d(TAG, "deleteJob: Deleting from storage");
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(jobUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Deleted from storage");
                        Log.d(TAG, "onSuccess: Now deleting info from db");

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Jobs");
                        reference.child(jobId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Deleted from database");
                                        progressDialog.dismiss();;
                                        Toast.makeText(context, "Job Deleted Successfully", Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to delete from db due to " + e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to delete from storage due to " + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void loadPdfFromUrl(String pdfUrl, String title, PDFView pdfView, ProgressBar progressBar) {
        // using url we can get file and its metadata from firebase storage
        String TAG = "PDF_LOAD_FROM_URL";

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: "+ title + " successfully got the file");

                        //set to pdfView
                        pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: " + t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: " + t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: pdf loaded");
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG, "onFailure: failed getting file from url due to" + e.getMessage());
                    }
                });

    }

    public static void loadCategory(String categoryId, TextView categoryTv) {
        String TAG = "LOAD_CATEGORY_TAG";

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        Log.d(TAG, "loadCategory: "+categoryId);
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = ""+snapshot.child("category").getValue();
                        categoryTv.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void loadCompany(String companyId, TextView companyTv) {
        String TAG = "LOAD_COMPANY_TAG";

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Companies");
        ref.child(companyId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String company = ""+snapshot.child("company").getValue();
                        companyTv.setText(company);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void loadType(String typeId, TextView typeTv) {
        String TAG = "LOAD_TYPE_TAG";

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Types");
        ref.child(typeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String type = ""+snapshot.child("type").getValue();
                        typeTv.setText(type);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void loadSeniority(String seniorityId, TextView seniorityTv) {
        String TAG = "LOAD_SENIORITY_TAG";

        Log.d(TAG, "loadSeniority: " + seniorityId);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Seniority");
        ref.child(seniorityId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String seniority = ""+snapshot.child("seniority").getValue();
                        seniorityTv.setText(seniority);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void downloadJobSpec(Context context, String jobId, String title, String url) {
        String TAG = "DOWNLOAD_TAG";
        Log.d(TAG, "downloadJobSpec: downloading job spec...");
        String nameWithExtension = title + ".pdf";

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Downloading..." + nameWithExtension);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        storageReference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: Job specification downloaded");
                        saveDownloadedJobSpec(context, progressDialog, bytes, nameWithExtension, jobId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to download due to "+ e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        
                    }
                });
    }

    public static void saveDownloadedJobSpec(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String jobId) {
        String TAG = "SAVING_TAG";
        Log.d(TAG, "saveDownloadedJobSpec: Saving Job spec");
        try{
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdirs();

            String filePath = downloadsFolder.getPath() + "/" + nameWithExtension;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();
            
            Toast.makeText(context, "Saved to Download Folder", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "saveDownloadedJobSpec: Saved to DownloadFolder");
            progressDialog.dismiss();
        }
        catch (Exception e) {
            Toast.makeText(context, "Failed to save due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Failed to save due to "+ e.getMessage());
        }
    }



    public static void addToInterested(Context context, String jobId) {
        // interest can be added only if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(context, "You are not logged in", Toast.LENGTH_SHORT).show();
        }
        else {
            long timestamp = System.currentTimeMillis();

            //setup data to add in firebase db of current user for interested job
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("jobId", ""+jobId);
            hashMap.put("timestamp", ""+timestamp);

            //save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Interested").child(jobId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Added to interested jobs list", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to add interested jobs list due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public static void removeFromInterested(Context context, String jobId) {
        // interest can be removed only if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(context, "You are not logged in", Toast.LENGTH_SHORT).show();
        }
        else {
            //remove from db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Interested").child(jobId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Removed from interested jobs list", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Failed to remove from interested jobs list due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}
