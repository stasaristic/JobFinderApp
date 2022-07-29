package com.example.jobfinder;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterType extends Filter {

    ArrayList<ModelType> filterList;
    AdapterType adapterType;

    public FilterType(ArrayList<ModelType> filterList, AdapterType adapterType) {
        this.filterList = filterList;
        this.adapterType = adapterType;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if (constraint != null && constraint.length() > 0) {
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelType> filteredModels = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {

                if (filterList.get(i).getType().toUpperCase().contains(constraint)) {
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
        adapterType.typeArrayList = (ArrayList<ModelType>) results.values;

        adapterType.notifyDataSetChanged();
    }
}
