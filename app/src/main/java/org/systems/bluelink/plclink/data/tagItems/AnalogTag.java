package org.systems.bluelink.plclink.data.tagItems;

public class AnalogTag extends BaseTag {

    private int mRawScaleMin = 0;
    private int mRawScaleMax = 4095;
    private int mEngScaleMin = 0;
    private int mEngScaleMax = 10;

    private float mAlarmLL;
    private float mAlarmL;
    private float mAlarmH;
    private float mAlarmHH;
    private float mScaledValue;
    private int mPercentage;
    private String mEngUnit;

    //default constructor for DiscreteWord object
    public AnalogTag(String tagAddress, String tagName){

        mTagAddress = tagAddress;
        mTagName = tagName;
        mAddedToReadTable = false;
        mFoundOnPLC = false;

        mDataType = BaseTag.DATA_TYPE_ANALOG;
    }

    //declare field access methods

    public void setScale(int rawScaleMin, int rawScaleMax, int engScaleMi, int engScaleMax){
        mRawScaleMin = rawScaleMin;
        mRawScaleMax = rawScaleMax;
        mEngScaleMin = engScaleMi;
        mEngScaleMax = engScaleMax;
    }

    public void setAlarmLevels(float alarmLL, float alarmL, float alarmH, float alarmHH){
        mAlarmLL = alarmLL;
        mAlarmL = alarmL;
        mAlarmH = alarmH;
        mAlarmHH = alarmHH;
    }

 @Override
    public void setValue(int newValue){
        super.setValue(newValue);
        mScaledValue = (newValue/(mRawScaleMax-mRawScaleMin)*(mEngScaleMax-mEngScaleMin))
                                            +mEngScaleMin;
        mPercentage = Math.round((newValue/Float.valueOf(mRawScaleMax-mRawScaleMin)*100f));
    }

    public void setUnit(String unit){mEngUnit = unit;}


    public int getPercentage(){return mPercentage;}
    public float getScaledValue(){return mScaledValue;}
    public String getEngUnit(){return mEngUnit;}
}
