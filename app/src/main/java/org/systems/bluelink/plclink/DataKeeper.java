package org.systems.bluelink.plclink;

import android.util.Log;

import org.systems.bluelink.plclink.data.tagItems.BaseTag;

import java.util.ArrayList;

public class DataKeeper {

    protected static ArrayList<BaseTag> globalTagList = new ArrayList<BaseTag>();
    private static ReadTableUpdater tableUpdater;

    static void syncReadTable() {

        tableUpdater = new ReadTableUpdater(globalTagList);
        tableUpdater.start();
    }

    static public void addToGlobalTagList(BaseTag newTag){
        globalTagList.add(newTag);
        syncReadTable();
    }

//    static public ArrayList<BaseTag> getGlobalTagList() {
//        return globalTagList;
//    }

    public static boolean handleReceivedUpdate(String command, TagsAdapter adapter){

        if (command.startsWith("*update")){
            String receivedTagAddress = command.substring(command.indexOf("|")+1, command.indexOf("="));
            int receivedNewValue = Integer.valueOf(command.substring(command.indexOf("=")+1, command.indexOf("#")));

            for (BaseTag tag : globalTagList) {
                if(tag.getTagAddress().equals(receivedTagAddress)){
                    tag.setValue(receivedNewValue);
                    int currentTagIndex = globalTagList.indexOf(tag);
                    adapter.notifyItemChanged(currentTagIndex);
                    Log.d("found add to update: ", receivedTagAddress +" with value = " + receivedNewValue);
                    return true;
                }
            }
        }

        return false;
    }
}
