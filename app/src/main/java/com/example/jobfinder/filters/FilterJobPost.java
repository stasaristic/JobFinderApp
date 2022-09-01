package com.example.jobfinder.filters;

import android.widget.Filter;

import com.example.jobfinder.adapters.AdapterJobPostsRecruiter;
import com.example.jobfinder.models.ModelJobPosts;

import java.util.ArrayList;
import java.util.Locale;

public class FilterJobPost extends Filter {

    ArrayList<ModelJobPosts> filterList;

    AdapterJobPostsRecruiter adapterJobPostsRecruiter;

    public FilterJobPost(ArrayList<ModelJobPosts> filterList, AdapterJobPostsRecruiter adapterJobPostsRecruiter) {
        this.filterList = filterList;
        this.adapterJobPostsRecruiter = adapterJobPostsRecruiter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if (constraint != null && constraint.length() > 0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelJobPosts> filteredModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {
                if (filterList.get(i).getTitle().toUpperCase().contains(constraint)) {
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else {
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapterJobPostsRecruiter.jobPostsArrayList = (ArrayList<ModelJobPosts>) results.values;

        adapterJobPostsRecruiter.notifyDataSetChanged();
    }
}
