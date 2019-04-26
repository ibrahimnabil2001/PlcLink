package org.systems.bluelink.plclink;

import org.systems.bluelink.plclink.data.tagItems.BaseTag;

import java.util.ArrayList;

public class ReadTableUpdater extends Thread {


    private static final long UPDATE_PACE = 50000000L; // 50 ms - time between two add commands
    private static final long RESEND_PACE = 5000000000L; // 5s - time between two add commands for the same tags
    private static final int NUMBER_OF_RESEND = 10; //RESEND THREE TIMES

    private ArrayList<BaseTag> mTagList;

    ReadTableUpdater(ArrayList<BaseTag> tagList) {

        //clearReadTable();
        mTagList = tagList;
    }

    public void run() {

        //keep sending add commands for those tags that are still not found on
        // PLC every UPDATE_PACE and resend each non found tag every RESEND_PACE

                for (BaseTag tag : mTagList) { //iterating over arrayList of tags and send add
                    // for only nun added tags
                    if (!tag.doFoundOnPLC() && System.nanoTime() > tag.getGlobalLastSendTime() + UPDATE_PACE
                            && System.nanoTime() > tag.getLastSendTime() + RESEND_PACE
                            && tag.getNumberOfSend() < NUMBER_OF_RESEND) {
                        BluetoothService.send("*add|" + tag.getTagAddress() + "#");

                        tag.sentNow();

            }
    }

}

    public void clearReadTable(){BluetoothService.send("*clear#");}



}
