package com.example.jobfinder.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobfinder.MyApplication;
import com.example.jobfinder.activities.JobDetailActivity;
import com.example.jobfinder.databinding.RowJobsUserBinding;
import com.example.jobfinder.filters.FilterJobPost;
import com.example.jobfinder.filters.FilterJobUser;
import com.example.jobfinder.models.ModelJobPosts;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterJobUser extends RecyclerView.Adapter<AdapterJobUser.HolderJobUser> implements Filterable {

    private Context context;
    public ArrayList<ModelJobPosts> jobPostsArrayList, filterList;

    private RowJobsUserBinding binding;

    private FilterJobUser filter;

    private static final String TAG = "ADAPTER_JOB_USER_TAG";

    public AdapterJobUser(Context context, ArrayList<ModelJobPosts> jobPostsArrayList) {
        this.context = context;
        this.jobPostsArrayList = jobPostsArrayList;
        this.filterList = jobPostsArrayList;
    }

    @NonNull
    @Override
    public HolderJobUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind the view
        binding = RowJobsUserBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderJobUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderJobUser holder, int position) {
        /*Get data, set data, handle click etc*/

        // get data
        ModelJobPosts model = jobPostsArrayList.get(position);
        String jobId = model.getId();
        String title = model.getTitle();;
        String description = model.getDescription();
        String url = model.getUrl();
        String categoryId = model.getCategoryId();
        String companyId = model.getCompanyId();
        String typeId = model.getTypeId();
        String seniorityId = model.getSeniorityId();

        //set data
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);

        MyApplication.loadPdfFromUrl(""+url,""+title, holder.pdfView, holder.progressBar);
        MyApplication.loadCategory(""+categoryId, holder.categoryTv);
        MyApplication.loadSeniority(""+seniorityId, holder.seniorityTv);
        MyApplication.loadType(""+typeId, holder.typeTv);
        MyApplication.loadCompany(""+companyId, holder.companyTv);

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

    @Override
    public int getItemCount() {
        return jobPostsArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterJobUser(filterList, this);
        }
        return filter;
    }

    class HolderJobUser extends RecyclerView.ViewHolder {
        TextView titleTv, descriptionTv, categoryTv, seniorityTv, companyTv, typeTv;
        PDFView pdfView;
        ProgressBar progressBar;

        public HolderJobUser(View itemView) {
            super(itemView);

            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            companyTv = binding.companyTv;
            seniorityTv = binding.seniorityTv;
            typeTv = binding.typeTv;
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
        }
    }
}
