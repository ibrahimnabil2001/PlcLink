package org.systems.bluelink.plclink.data.tagItems;

import android.bluetooth.BluetoothAssignedNumbers;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class System {

    @SerializedName("systemName")
    private String mSystemName;

    @SerializedName("systemLocation")
    private  String mSystemLocation;

    @SerializedName("tagList")
    private ArrayList<BaseTag> mTagList;

    public System(String systemName, String systemLocation){

        mSystemName = systemName;
        mSystemLocation = systemLocation;
        mTagList = new ArrayList<>();
    }

    public void setTagList(ArrayList<BaseTag> tagList){mTagList = tagList;}
    public void setmSystemName(String systemName){mSystemName = systemName;}
    public void setmSystemLocation(String systemLocation){mSystemLocation = systemLocation;}

    public String getSystemName() {
        return mSystemName;
    }

    public String getSystemLocation() {
        return mSystemLocation;
    }

    public ArrayList<BaseTag> getTagList(){return mTagList;}
}
