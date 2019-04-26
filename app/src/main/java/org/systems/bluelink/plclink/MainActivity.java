package org.systems.bluelink.plclink;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.systems.bluelink.plclink.BluetoothService.MessageType;
import org.systems.bluelink.plclink.BluetoothService.ConnectToBT;
import org.systems.bluelink.plclink.BluetoothService.BtListenerThread;
import org.systems.bluelink.plclink.data.tagItems.AnalogTag;
import org.systems.bluelink.plclink.data.tagItems.BaseTag;
import org.systems.bluelink.plclink.data.tagItems.DiscreteTag;


public class MainActivity extends AppCompatActivity {

    static final String DEVICE_NAME = "HC-05";

    //declaring global variables
    private static final String TAG = "MY_APP_DEBUG_TAG";
    public static Handler mainHandler;
    static BluetoothAdapter bluetoothAdapter;
    static TextView mainText;
    static boolean connectedToDevice = false;
    static BluetoothSocket mmSocket;
    static StringBuilder stringBuilder;
    static EditText inputTextField;
    RecyclerView tagsListView;
    TagsAdapter tagsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing bluetooth adapter from framework
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        //finding views by id
        mainText = findViewById(R.id.main_text);
        inputTextField = findViewById(R.id.main_edit_text);
        tagsListView = findViewById(R.id.list_view);

        //initialize globals
        tagsAdapter = new TagsAdapter(this, DataKeeper.globalTagList);
        tagsListView.setAdapter(tagsAdapter);
        tagsListView.setLayoutManager(new LinearLayoutManager(this));
        mainHandler = new MainHandler();

        //connecting to bluetooth device using helper method
        BluetoothDevice btDevice = BluetoothService.findBTdevice(DEVICE_NAME, this);
        if(btDevice != null){
        connectToDevice(btDevice);}

        //setting on click listeners
        findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String inputText = inputTextField.getText().toString();
                BluetoothService.send(inputText);
            }
        });


        findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String newTagAddress = ((EditText) findViewById(R.id.address_edit_text)).getText().toString();
                String newTagName = ((EditText) findViewById(R.id.name_edit_text)).getText().toString();
                String newTagType = ((EditText) findViewById(R.id.type_edit_text)).getText().toString();
                switch (Integer.valueOf(newTagType)){
                    case BaseTag.DATA_TYPE_DISCRETE:
                        DataKeeper.addToGlobalTagList(new DiscreteTag(newTagAddress,newTagName, 16));
                        break;
                    case BaseTag.DATA_TYPE_ANALOG:
                        DataKeeper.addToGlobalTagList(new AnalogTag(newTagAddress,newTagName));
                        break;
                }
                tagsAdapter.notifyDataSetChanged();
            }
        });
        findViewById(R.id.refresh_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new ReadTableUpdater(DataKeeper.globalTagList).start();

            }
        });
    }

    /**
     * helper method that uses background thread to connect to passed BT device
     * @param device BluetoothDevice
     */
    private void connectToDevice(BluetoothDevice device){
        Thread connectThread = new ConnectToBT(device);
        connectThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //free up bluetooth to be able to use next time and by other apps
        BluetoothService.disconnectBT();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

     class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case MessageType.CONNECTED:
                    connectedToDevice = true;
                    stringBuilder = new StringBuilder("Connected \n");
                    mainText.setText(stringBuilder.toString());
                    mmSocket = (BluetoothSocket) msg.obj;
                    Thread connectedThread = new BtListenerThread(mmSocket);
                    connectedThread.start();

                    break;
                case MessageType.DISCONNECTED:
                    connectedToDevice = true;
                    mainText.setText("Disconnected");
                    BluetoothService.disconnectBT();
                    break;
                case MessageType.WRITE:
                    stringBuilder.append("\n sent: "+ msg.obj);
                    mainText.setText(stringBuilder.toString());
                    inputTextField.setText("");
                    break;

                case MessageType.READ:
                    String string= (String) msg.obj;
                    Log.d("received from BT: ", string);
                    DataKeeper.handleReceivedUpdate(string, tagsAdapter);
                    tagsAdapter.notifyDataSetChanged();
                    stringBuilder.append("\n received: "+ string);
                    mainText.setText(stringBuilder.toString());
                    break;

                case MessageType.TOAST:
                    String toast =  msg.getData().getString("toast");
                    Toast.makeText(MainActivity.this,toast,Toast.LENGTH_SHORT).show();
            }
        }
    }
}

