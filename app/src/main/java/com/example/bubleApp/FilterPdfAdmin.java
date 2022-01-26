package com.example.bubleApp;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {

    // arraylist in which we want to search

    ArrayList<ModelPdf> filterList;
    // adapter in which filter need to be implemented

    AdapterPdfAdmin adapterPdfAdmin;

    // constructor

    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin= adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults result = new FilterResults();
        // value should not be null and empty

        if (constraint != null && constraint.length() > 0){
        // change to upper cae, of lower case to about case sentivility
        constraint = constraint.toString().toUpperCase().toLowerCase();
        ArrayList<ModelPdf> filteredModels = new ArrayList<>();

        for (int i = 0; i < filterList.size(); i++) {
            // validate
            if (filterList.get(i).getTitle().toUpperCase().contains(constraint)){
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
        adapterPdfAdmin.pdfArrayList = (ArrayList<ModelPdf>) filterResults.values;

        // notify change
        adapterPdfAdmin.notifyDataSetChanged();
    }
}
