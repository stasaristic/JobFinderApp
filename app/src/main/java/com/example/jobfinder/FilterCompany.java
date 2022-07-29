package com.example.jobfinder;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.Locale;

public class FilterCompany extends Filter {

    // arraylist in which we want to search
    ArrayList<ModelCompany> filterList;
    //adapter in which the filter is implemented
    AdapterCompany adapterCompany;

    // constructor
    public FilterCompany(ArrayList<ModelCompany> filterList, AdapterCompany adapterCompany) {
        this.filterList = filterList;
        this.adapterCompany = adapterCompany;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        // value should not be null and empty
        if (constraint != null && constraint.length() > 0) {
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCompany> filteredModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {

                if (filterList.get(i).getCompany().toUpperCase().contains(constraint)) {
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
        adapterCompany.companyArrayList = (ArrayList<ModelCompany>) results.values;

        adapterCompany.notifyDataSetChanged();
    }
}
