package org.systems.bluelink.plclink;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.systems.bluelink.plclink.data.tagItems.AnalogTag;
import org.systems.bluelink.plclink.data.tagItems.BaseTag;
import org.systems.bluelink.plclink.data.tagItems.BitTag;
import org.systems.bluelink.plclink.data.tagItems.DiscreteTag;

import java.util.ArrayList;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagViewHolder> {

    //declaring custom view holder class and sub-Classes
    class TagViewHolder extends RecyclerView.ViewHolder {

        TextView tagNameView;
        TextView tagAddressView;
        TextView tagValueView;
        TextView tagTypeView;
        int itemIndex;

        private TagViewHolder(View view) {
            super(view);
            tagNameView =  view.findViewById(R.id.text_tag_name);
            tagAddressView =  view.findViewById(R.id.text_tag_address);
            tagValueView =  view.findViewById(R.id.text_tag_value);
            tagTypeView =  view.findViewById(R.id.text_tag_type);


        }
    }

    private class DiscreteTagViewHolder extends TagViewHolder{
        LinearLayout bitsContainer;
        public DiscreteTagViewHolder(View view){
            super(view);
            view.findViewById(R.id.bits_container).setVisibility(View.VISIBLE);
            view.findViewById(R.id.analog_visualization).setVisibility(View.GONE);
            bitsContainer = view.findViewById(R.id.bits_container);
        }
    }

    private class AnalogTagViewHolder extends TagViewHolder{
        ProgressBar analogBar;
        public AnalogTagViewHolder(View view){
            super(view);
            view.findViewById(R.id.bits_container).setVisibility(View.GONE);
            view.findViewById(R.id.analog_visualization).setVisibility(View.VISIBLE);
            analogBar = view.findViewById(R.id.analog_bar);
        }
    }

    //adapter's member fields:
    private ArrayList<BaseTag> mTagsList;
    private Context context;
    //private LayoutInflater mInflater;

    //Adapter constructor:
    public TagsAdapter(Context context, ArrayList<BaseTag> tagsList){
        //mInflater = LayoutInflater.from(context);
        mTagsList = tagsList;
        this.context = context;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag,parent, false);
        switch (viewType){
            case BaseTag.DATA_TYPE_DISCRETE:
                return new DiscreteTagViewHolder(itemView);
            default:
                return new AnalogTagViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, final int i) {

        holder.tagNameView.setText(mTagsList.get(i).getTagName());
        holder.tagAddressView.setText(mTagsList.get(i).getTagAddress());
        String tagValueString = "****";
        holder.itemIndex = i;
        if (holder instanceof DiscreteTagViewHolder){

            holder.tagTypeView.setText("Discrete");
            if(mTagsList.get(i).doFoundOnPLC()){tagValueString = Integer
                    .toBinaryString(mTagsList.get(i).getTagRawValue());
            inflateBits(((DiscreteTagViewHolder) holder).bitsContainer,
                    ((DiscreteTag)mTagsList.get(i)).getChildes());}
        }
        else if (holder instanceof AnalogTagViewHolder){
            holder.tagTypeView.setText("Analog");
            if(mTagsList.get(i).doFoundOnPLC()){
                tagValueString = String.valueOf(mTagsList.get(i).getTagRawValue());
                ((AnalogTagViewHolder)holder).analogBar.setProgress(((AnalogTag) mTagsList.get(i)).getPercentage());
                ((AnalogTag) mTagsList.get(i)).getScaledValue();
            }else{((AnalogTagViewHolder)holder).analogBar.setProgress(0);}
        }
        holder.tagValueView.setText(tagValueString);

    }
 
    @Override
    public int getItemViewType(int position) {
        return mTagsList.get(position).getDataType();
    }

    @Override
    public int getItemCount() {
        return mTagsList.size();
    }

    /**
     * helper method that inflates Bits representing views inside discrete container
     */
    private void inflateBits(View v, ArrayList<BitTag> bitTags){
        int size = bitTags.size();
        LinearLayout bitsContainer = (LinearLayout) v; bitsContainer.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(32, 32);
        params.setMargins(8, 0, 8, 0);
        for (int i= (size-1); i >= 0 ; i--){
            TextView bitView = new TextView(context);
            bitView.setText(String.valueOf(bitTags.get(i).getElementIndex()));
            bitView.setLayoutParams(params);
            bitView.setTextSize(context.getResources().getDimension(R.dimen.bitView_text_size));
            bitView.setGravity(Gravity.CENTER);
            boolean value = bitTags.get(i).getValue();
            if(value == BitTag.LOGIC_HIGH) {
                bitView.setBackgroundColor(context.getResources().getColor(R.color.colorLogicHigh));
            }else if(value == BitTag.LOGIC_LOW){
                    bitView.setBackgroundColor(context.getResources().getColor(R.color.colorLogicLow));
            }
            bitsContainer.addView(bitView);
        }
        
    }
}
