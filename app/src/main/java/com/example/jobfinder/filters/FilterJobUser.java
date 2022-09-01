package com.example.jobfinder.filters;

import android.widget.Filter;

import com.example.jobfinder.adapters.AdapterJobPostsRecruiter;
import com.example.jobfinder.adapters.AdapterJobUser;
import com.example.jobfinder.models.ModelJobPosts;

import java.util.ArrayList;

public class FilterJobUser extends Filter {

    ArrayList<ModelJobPosts> filterList;

    AdapterJobUser adapterJobUser;

    public FilterJobUser(ArrayList<ModelJobPosts> filterList, AdapterJobUser adapterJobUser) {
        this.filterList = filterList;
        this.adapterJobUser = adapterJobUser;
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
        adapterJobUser.jobPostsArrayList = (ArrayList<ModelJobPosts>) results.values;
        adapterJobUser.notifyDataSetChanged();

    }
}
