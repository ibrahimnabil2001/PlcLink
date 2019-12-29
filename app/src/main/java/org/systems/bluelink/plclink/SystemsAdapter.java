package org.systems.bluelink.plclink;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.systems.bluelink.plclink.data.tagItems.AnalogTag;
import org.systems.bluelink.plclink.data.tagItems.BaseTag;
import org.systems.bluelink.plclink.data.tagItems.BitTag;
import org.systems.bluelink.plclink.data.tagItems.DiscreteTag;
import org.systems.bluelink.plclink.data.tagItems.System;

import java.util.ArrayList;
import java.util.List;

public class SystemsAdapter extends ArrayAdapter<System> {

    public SystemsAdapter(Context context, List<System> systems){
        super (context, 0, systems);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        System system = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_system, parent, false);
        }
        // Lookup view for data population
        TextView systemName =  convertView.findViewById(R.id.system_name_text);
        TextView systemLocation = convertView.findViewById(R.id.system_location_text);
        // Populate the data into the template view using the data object
        systemName.setText(system.getSystemName());
        systemLocation.setText(system.getSystemLocation());
        // Return the completed view to render on screen

        convertView.findViewById(R.id.system_delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataKeeper.globalSystemList.remove(position);
                HomeActivity.systemsAdapter.notifyDataSetChanged();
            }
        });
        return convertView;
    }

}
