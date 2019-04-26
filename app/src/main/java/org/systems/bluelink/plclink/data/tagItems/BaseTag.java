package org.systems.bluelink.plclink.data.tagItems;

public class BaseTag {

    //declaring constants
    public static final int DATA_TYPE_DISCRETE = 0;
    public static final int DATA_TYPE_ANALOG = 1;

    //declaring fields
     String mTagAddress;
     String mTagName;
     private int mRawDataValue;
     int mDataType;
     int mDataSize;
     private boolean mIsAlarm;
     private boolean mIsEvent;
     boolean mAddedToReadTable;
     boolean mFoundOnPLC;
     private long lastSendTime; // the last time this tag was sent to scheduled read table
     private static long globalLastSendTime = 0; // the last time a tag was sent to scheduled read table
     private int numberOfSend = 0;

    //declaring setters and getters
    public String getTagAddress(){return mTagAddress;}
    public String getTagName(){return mTagName;}
    public int getTagRawValue(){return mRawDataValue;}
    public int getDataType(){return mDataType;}
    public boolean getAlarmStatus(){return mIsAlarm;}
    public boolean getEventStatus(){return mIsEvent;}
    public void addedToReadTable(){mAddedToReadTable = true;}
    public void removedFromReadTable(){mAddedToReadTable = false;}
    public  boolean doFoundOnPLC() {return mFoundOnPLC;}
    public  long getLastSendTime() {return lastSendTime;}
    public  long getGlobalLastSendTime() {return globalLastSendTime;}
    public  int getNumberOfSend() {return numberOfSend;}

    public void setValue(int newValue){mRawDataValue = newValue; mFoundOnPLC = true;}
    public void sentNow(){globalLastSendTime = lastSendTime = System.nanoTime(); numberOfSend++;}

}
