package com.example.jobfinder.adapters;

import static com.example.jobfinder.Constants.MAX_BYTES_PDF;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinder.EditJobRecruiterActivity;
import com.example.jobfinder.JobDetailActivity;
import com.example.jobfinder.MyApplication;
import com.example.jobfinder.databinding.RowJobsRecruiterBinding;
import com.example.jobfinder.filters.FilterJobPost;
import com.example.jobfinder.models.ModelJobPosts;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class AdapterJobPostsRecruiter extends RecyclerView.Adapter<AdapterJobPostsRecruiter.HolderJobsRecruiter> implements Filterable {

    // context
    private Context context;
    // arraylist to hold list of data of type ModelJobPosts
    public ArrayList<ModelJobPosts> jobPostsArrayList, filterList;

    // view binding row_jobs_recruiter.xml
    private RowJobsRecruiterBinding binding;

    private FilterJobPost filter;

    private static final String TAG = "PDF_ADAPTER_TAG";

    // progress
    private ProgressDialog progressDialog;

    // constructor
    public AdapterJobPostsRecruiter(Context context, ArrayList<ModelJobPosts> jobPostsArrayList) {
        this.context = context;
        this.jobPostsArrayList = jobPostsArrayList;
        this.filterList = jobPostsArrayList;

        // init progress dialog
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderJobsRecruiter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // bind layout using view binding
        binding = RowJobsRecruiterBinding.inflate(LayoutInflater.from(context), parent, false);

        return new AdapterJobPostsRecruiter.HolderJobsRecruiter(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderJobsRecruiter holder, int position) {
        /* Get data, Set data, handle clicks...etc. */
        // init arraylist
        ModelJobPosts model = jobPostsArrayList.get(position);

        String jobId = model.getId();
        String title = model.getTitle();
        String description = model.getDescription();
        String categoryId = model.getCategoryId();
        String companyId = model.getCompanyId();
        String typeId = model.getTypeId();
        String seniorityId = model.getSeniorityId();
        String pdfUrl = model.getUrl();
        //long timestamp = model.getTimestamp();

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);

        MyApplication.loadCategory(""+categoryId, holder.categoryTv);
        MyApplication.loadCompany(""+companyId, holder.companyTv);
        MyApplication.loadType(""+typeId, holder.typeTv);
        MyApplication.loadSeniority(""+seniorityId, holder.seniorityTv);
        MyApplication.loadPdfFromUrl(""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar
        );

        // handle click, show dialog with options: 1) Edit, 2) Delete
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsDialog(model, holder);
            }
        });

        //handle job/pdf click, open pdf detals page, pass pdf/job id to get details of it
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, JobDetailActivity.class);
                intent.putExtra("jobId", jobId);
                context.startActivity(intent);
            }
        });

    }

    private void moreOptionsDialog(ModelJobPosts model, HolderJobsRecruiter holder) {

        String jobId = model.getId();
        String jobUrl =  model.getUrl();
        String jobTitle = model.getTitle();

        // options to show in dialog
        String[] options = {"Edit", "Delete"};

        // alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // handel dialog option click
                        if (which == 0) {
                            // Edit clicked
                            Intent intent = new Intent(context, EditJobRecruiterActivity.class);
                            intent.putExtra("jobId", jobId);
                            context.startActivity(intent);
                        }
                        else if (which == 1) {
                            // Delete Clicked
                            MyApplication.deleteJob(context,
                                    ""+jobId,
                                    ""+jobUrl,
                                    ""+jobTitle
                            );

                        }
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return jobPostsArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterJobPost(filterList, this);
        }
        return filter;
    }

    /* View Holder class for row_jobs_recruiter */
    class HolderJobsRecruiter extends RecyclerView.ViewHolder{

        // UI Views of row_posted_jobs_recruiter.xml
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, companyTv, typeTv, seniorityTv;
        ImageButton moreBtn;

        public HolderJobsRecruiter(@NonNull View itemView) {
            super(itemView);

            // init ui views
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            companyTv = binding.companyTv;
            typeTv = binding.typeTv;
            seniorityTv = binding.seniorityTv;
            moreBtn = binding.moreBtn;

        }
    }
}
