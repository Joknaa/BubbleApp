package com.example.bubleApp;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterCategory extends Filter {
    // arraylist in which we want to search

    ArrayList<ModelCategory> filterList;
    // adapter in which filter need to be implemented

    AdapterCategory adapterCategory;

    // constructor

    public FilterCategory(ArrayList<ModelCategory> filterList, AdapterCategory adapterCategory) {
        this.filterList = filterList;
         this.adapterCategory = adapterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults result = new FilterResults();
        // value should not be null and empty

        if (constraint != null && constraint.length() > 0){
            // change to upper cae, of lower case to about case sentivility
            constraint = constraint.toString().toUpperCase().toLowerCase();
            ArrayList<ModelCategory> filteredModels = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                // validate
                if (filterList.get(i).getCategory().toUpperCase().contains(constraint)){
                    // add to filter
                    filteredModels.add(filterList.get(i));
                }
            }

            result.count = filteredModels.size();
            result.values = filteredModels;
        } else {
            result.count = filterList.size();
            result.values = filterList;
        }
        return result; // Dont miss this !!
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        // apply the filter change
        adapterCategory.categoryArrayList = (ArrayList<ModelCategory>) filterResults.values;

        // notify change
        adapterCategory.notifyDataSetChanged();
    }
}
