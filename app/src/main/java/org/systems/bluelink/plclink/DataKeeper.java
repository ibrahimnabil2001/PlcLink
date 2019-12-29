package org.systems.bluelink.plclink;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.systems.bluelink.plclink.data.tagItems.AnalogTag;
import org.systems.bluelink.plclink.data.tagItems.BaseTag;
import org.systems.bluelink.plclink.data.tagItems.DiscreteTag;
import org.systems.bluelink.plclink.data.tagItems.System;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.systems.bluelink.plclink.ScheduledReadTable.ReadTableUpdater;
import org.systems.bluelink.plclink.ScheduledReadTable.ReadTableBuilder;

public class DataKeeper {

    protected static ArrayList<BaseTag> globalTagList;
    protected static List<System> globalSystemList;

    private static int currentSystemIndex;

    static void syncReadTable() {

        ReadTableUpdater tableUpdater = new ReadTableUpdater(globalTagList);
        tableUpdater.start();
    }

    static void buildReadTable() {

        ReadTableBuilder tableBuilder = new ReadTableBuilder(globalTagList);
        tableBuilder.start();
    }

    static public boolean addToGlobalTagList(BaseTag newTag){
        if(addressExists(newTag.getTagAddress())){return false;}
        globalTagList.add(newTag);
        syncReadTable();
        globalSystemList.get(currentSystemIndex).setTagList(globalTagList);
        return true;
    }

    static private boolean addressExists(String tagAddress){
        for (BaseTag tag : globalTagList){
            if (tag.getTagAddress().equals(tagAddress)){return true;}
        }
        return false;
    }

    static public void addToGlobalSystemList(System newSystem){
        globalSystemList.add(newSystem);

    }

    static public void setGlobalTagList(ArrayList<BaseTag> newList){
        globalTagList = newList;
        ScheduledReadTable.clearReadTable();
        syncReadTable();
    }

    static public void setCurrentSystemIndex(int i){
        currentSystemIndex = i;
        globalTagList = globalSystemList.get(i).getTagList();
        buildReadTable();
    }

    static public System getCurrentSystem(){
        return globalSystemList.get(currentSystemIndex);
    }

    public static void pullFromFile(Context context, String fileName){

        globalSystemList  = new ArrayList<>();

        if (dataFileExists(context, fileName)){
            try {
                FileInputStream fis = context.openFileInput(fileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader bufferedReader = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                globalSystemList = constructFromJson(sb.toString()) ;
                stripOldUpdateStatus();
            } catch (FileNotFoundException fileNotFound) {
                Log.d("DataKeeper:", "pullFromFile" +fileName + "file Not Found");
            } catch (IOException ioException) {
                Log.d("DataKeeper:", fileName + "pullFromFile ioException");
            }
        }

    }

    private static List<System> constructFromJson(String jsonString){
        Gson gson = new Gson();
        List<System> systemList= new ArrayList<>();
        try {
            JSONArray jsonSystems = new JSONArray(jsonString);
            for (int i=0; i < jsonSystems.length(); i++ ) {
                JSONObject newJsonSystem = jsonSystems.getJSONObject(i);
                String systemName = newJsonSystem.getString("systemName");
                String systemLocation = newJsonSystem.getString("systemLocation");
                System newSystem = new System(systemName, systemLocation);

                ArrayList<BaseTag> tagList = new ArrayList<>();
                JSONArray jsonTagList = newJsonSystem.getJSONArray("tagList");
                for (int j=0; j < jsonTagList.length(); j++ ) {
                    JSONObject jsonTag =  jsonTagList.getJSONObject(j);
                    int thisTagDataType = jsonTag.getInt("mDataType");
                    BaseTag newTag = new BaseTag();
                    switch (thisTagDataType){
                        case BaseTag.DATA_TYPE_DISCRETE:
                             newTag = gson.fromJson(jsonTag.toString(), DiscreteTag.class);
                             break;
                        case BaseTag.DATA_TYPE_ANALOG:
                             newTag = gson.fromJson(jsonTag.toString(), AnalogTag.class);
                             break;
                    }
                    tagList.add(newTag);
                }
                newSystem.setTagList(tagList);
                systemList.add(newSystem);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("DataKeeper", "Problem parsing the JSON String", e);
        }
        return systemList;
    }

    public static void saveToFile(Context context, String fileName){

        Gson gson = new Gson();
        String jsonString = gson.toJson(globalSystemList);

        try {
            FileOutputStream fos = context.openFileOutput(fileName,Context.MODE_PRIVATE);

            if (jsonString != null) {
                fos.write(jsonString.getBytes());
            }

            fos.close();

        } catch (FileNotFoundException fileNotFound) {
            Log.d("DataKeeper:", "saveToFile" +fileName + "file Not Found");
        } catch (IOException ioException) {
            Log.d("DataKeeper:", "saveToFile" +fileName + "Io exception");
        }

    }


    private static boolean dataFileExists(Context context, String fileName) {
        String currentDataPath = // get app file directory and navigate to database folder
                context.getFilesDir().getAbsolutePath()  + "/" + fileName;
        File dataFile = new File(currentDataPath);
        return dataFile.exists();
    }

    public static boolean handleReceivedUpdate(String command){

        if (command.startsWith("*update")){
            String receivedTagAddress = command.substring(command.indexOf("|")+1, command.indexOf("="));
            int receivedNewValue = Integer.valueOf(command.substring(command.indexOf("=")+1, command.indexOf("#")));

            for (BaseTag tag : globalTagList) {
                if(tag.getTagAddress().equals(receivedTagAddress)){
                    tag.setValue(receivedNewValue);
                    int currentTagIndex = globalTagList.indexOf(tag);
                    //MainActivity.tagsAdapter.notifyItemChanged(currentTagIndex);
                    Log.d("found add to update: ", receivedTagAddress +" with value = " + receivedNewValue);
                    return true;
                }
            }
        }

        return false;
    }

    public static void stripOldUpdateStatus(){
        for (System system: globalSystemList){
            ArrayList<BaseTag> tagList =  system.getTagList();
            for (BaseTag tag : tagList){
                tag.clearUpdateStatus();
            }
        }
    }
}
