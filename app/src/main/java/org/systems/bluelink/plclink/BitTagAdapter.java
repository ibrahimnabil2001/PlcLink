package org.systems.bluelink.plclink;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnFocusChangeListener;
import org.systems.bluelink.plclink.data.tagItems.BitTag;
import org.systems.bluelink.plclink.data.tagItems.System;

import java.util.ArrayList;
import java.util.List;

public class BitTagAdapter extends ArrayAdapter<BitTag> {

    private List<BitTag> BitTagList;

    public BitTagAdapter(Context context, List<BitTag> BitTagList){
        super (context, 0, BitTagList);
        this.BitTagList = BitTagList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BitTag tag = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_bit_tag, parent, false);
        }

        // Lookup view for data population
        EditText tagNameView =  convertView.findViewById(R.id.bit_name_editText);
        TextView tagIndexView = convertView.findViewById(R.id.bit_index_text);

        // Populate the data into the template view using the data object
        tagNameView.setText(tag.getTagName());
        tagIndexView.setText(String.valueOf(tag.getElementIndex()));

        tagNameView.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    EditText tagNameView =  v.findViewById(R.id.bit_name_editText);
                    getItem(position).setName(tagNameView.getText().toString());
                    BitTagList.get(position).setName(tagNameView.getText().toString());
                }
            }
        });
        // Return the completed view to render on screen
        return convertView;
    }

    public List<BitTag> getUpdatedList(){return BitTagList;}

}
