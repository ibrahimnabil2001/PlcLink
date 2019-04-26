package org.systems.bluelink.plclink.data.tagItems;


import java.util.ArrayList;

public class DiscreteTag extends BaseTag {

    private ArrayList<BitTag> mChildTags;

    //declaring parameter-less constructor to make inheriting classes be able to declare there own constructors
    public DiscreteTag(){}

    //default constructor for DiscreteWord object
    public DiscreteTag(String tagAddress, String tagName, int dataSize){

        mTagAddress = tagAddress;
        mTagName = tagName;
        mAddedToReadTable = false;
        mFoundOnPLC = false;

        mDataType = BaseTag.DATA_TYPE_DISCRETE;
        mDataSize = dataSize;
        mChildTags = createChildes(dataSize);

    }

    private ArrayList<BitTag> createChildes(int size){
        ArrayList<BitTag> childes = new ArrayList<>();
        for(int i = 0; i < size ; i++){
            childes.add(new BitTag("", i));
        }
        return  childes;
    }

    //declaring setters and getters
    @Override
    public void setValue (int newValue){
        super.setValue(newValue);
        updateChildesValues(newValue);

    }

    public void setChildName (int childIndex, String childName){
        mChildTags.get(childIndex).setName(childName);
    }

    public boolean getChildValue(int childIndex){
        if (childIndex < mDataSize){
            return mChildTags.get(childIndex).getValue();
        }
        return false;
    }

    public BitTag getChild(int index){
        if (index < mDataSize){
            return mChildTags.get(index);
        }
        return null;
    }

    public ArrayList<BitTag> getChildes(){
            return mChildTags;
        }


    /**
     * Helper method that convert integer to array of boolean with custom size
     * @param value integer value to convert
     */
    private void updateChildesValues(int value){

            for (int i = 0; i < mDataSize; i++) {

                if(((value >> i) & 1) == 1){
                mChildTags.get(i).setValue(true);}
                else {mChildTags.get(i).setValue(false);}
            }
        }
    }

