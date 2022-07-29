package com.example.jobfinder;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterSeniority extends Filter {

    ArrayList<ModelSeniority> filterList;
    AdapterSeniority adapterSeniority;

    public FilterSeniority(ArrayList<ModelSeniority> filterList, AdapterSeniority adapterSeniority) {
        this.filterList = filterList;
        this.adapterSeniority = adapterSeniority;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if (constraint != null && constraint.length() > 0) {
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelSeniority> filteredModels = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {

                if (filterList.get(i).getSeniority().toUpperCase().contains(constraint)) {
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
        adapterSeniority.seniorityArrayList = (ArrayList<ModelSeniority>) results.values;

        adapterSeniority.notifyDataSetChanged();
    }
}
