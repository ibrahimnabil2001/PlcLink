package org.systems.bluelink.plclink.data.tagItems;

public class BitTag extends DiscreteTag {


    public static final boolean LOGIC_HIGH = true;
    public static final boolean LOGIC_LOW = false;

    private boolean mAlarmLevel;
    private boolean mEventLogLevel;

    private boolean mValue;
    private int mElementIndex;

    public BitTag(String tagName, int index){

        mTagName = tagName;
        mElementIndex = index;
    }

    public void setValue(boolean newValue){mValue = newValue;}
    public void setName(String newName){mTagName = newName;}
    public void setAlarmLevel(boolean newLevel){mAlarmLevel = newLevel;}
    public void setEventLogLevel(boolean newLevel){mEventLogLevel = newLevel;}

    public boolean getValue(){return  mValue;}
    public int getElementIndex(){return  mElementIndex;}


}
