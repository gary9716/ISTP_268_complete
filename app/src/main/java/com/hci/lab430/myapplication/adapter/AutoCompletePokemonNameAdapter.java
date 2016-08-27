package com.hci.lab430.myapplication.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KTChou on 2016/8/27.
 */
public class AutoCompletePokemonNameAdapter extends ArrayAdapter<String> {

    List<String> allData;

    public AutoCompletePokemonNameAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        allData = new ArrayList<>(objects);
    }

    private Filter myFilter = new Filter() { //Anonymous Filter class object

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if(constraint == null || constraint.length() == 0) {
                results.count = 0;
                results.values = null;
            }
            else {
                ArrayList<String> filteredNames = new ArrayList<>();
                String constraintStrInLowerCase = constraint.toString().toLowerCase();
                for(String name : allData) {
                    if(name.toLowerCase().contains(constraintStrInLowerCase)) {
                        filteredNames.add(name);
                    }
                }
                results.count = filteredNames.size();
                results.values = filteredNames;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            AutoCompletePokemonNameAdapter.this.clear();
            if(results.count != 0) {
                AutoCompletePokemonNameAdapter.this.addAll((List<String>) results.values);
            }
            AutoCompletePokemonNameAdapter.this.notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() {
        return myFilter;
    }
}
