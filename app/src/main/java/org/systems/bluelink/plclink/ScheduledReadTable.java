package org.systems.bluelink.plclink;

import org.systems.bluelink.plclink.data.tagItems.BaseTag;

import java.util.ArrayList;
import java.util.List;


public class ScheduledReadTable {
    private static final long UPDATING_TIME = 50000000000L;  // 50s - time for each run operation

    //    --------------------------------------------Updater Thread-------------------------------------------
    public static class ReadTableUpdater extends Thread {

        private static final long UPDATE_PACE = 1000000000L; // 1s - time between two add commands
        private static final long RESEND_PACE = 15000000000L; // 15s - time between two add commands for the same tags
        private static final int NUMBER_OF_RESEND = 10; //RESEND Ten TIMES

        private long lastRuntime;
        private List<BaseTag> mTagList;

    ReadTableUpdater(List<BaseTag> tagList) {

            mTagList = new ArrayList<> (tagList) ;
        }

        public void run() {

            lastRuntime = System.nanoTime();
            while (System.nanoTime() < lastRuntime + UPDATING_TIME) { //keep updating for UPDATING_TIME period

                //keep sending add commands for those tags that are still not found on
                // PLC every UPDATE_PACE and resend each non found tag every RESEND_PACE

                for (BaseTag tag : mTagList) {
                    //iterating over arrayList of tags and send add for only nun added tags
                    if (!tag.doFoundOnPLC() && System.nanoTime() > tag.getGlobalLastSendTime() + UPDATE_PACE
                            && System.nanoTime() > tag.getLastSendTime() + RESEND_PACE
                            && tag.getNumberOfSend() < NUMBER_OF_RESEND && BluetoothService.isConnected) {

                        BluetoothService.send("*add|" + tag.getTagAddress() + "#");
                        tag.sentNow();
                    }
                }
            }
        }
    }

    //    --------------------------------------------Builder Thread-------------------------------------------
    public static class ReadTableBuilder extends Thread {

        private List<BaseTag> mTagList;

        ReadTableBuilder(List<BaseTag> tagList) {
            mTagList = tagList;
        }

        public void run() {

                StringBuilder buildCommand = new StringBuilder("*build|");
                //iterating over arrayList of tags and send build command for all tags tags
                for (BaseTag tag : mTagList) {
                    buildCommand.append(tag.getTagAddress() + "&");
                }
                buildCommand.append("#");
                BluetoothService.send(buildCommand.toString());
            }
        }
//----------------------------------------------------------------------------------------------------------------
    public static void clearReadTable() {
        BluetoothService.send("*clear#");
    }
}
