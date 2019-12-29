package org.systems.bluelink.plclink;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import org.systems.bluelink.plclink.NewSystemDialogFragment.NoticeDialogListener;

public class MainActivity extends AppCompatActivity{

    static final String DEVICE_NAME = "HC-05";

    //declaring global variables
    private static final String TAG = "MY_APP_DEBUG_TAG";
    public static Handler mainHandler;
    static BluetoothAdapter bluetoothAdapter;
    static boolean connectedToDevice = false;
    static StringBuilder stringBuilder;
    RecyclerView tagsListView;
    static TagsAdapter tagsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Handle passed extras
        DataKeeper.setCurrentSystemIndex(
                getIntent().getIntExtra("CURRENT SYSTEM",0));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set title to match system data
            setTitle(DataKeeper.getCurrentSystem().getSystemLocation() +" " +
                DataKeeper.getCurrentSystem().getSystemName());

        //initializing bluetooth adapter from framework
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        //finding views by id

        tagsListView = findViewById(R.id.list_view);

        //initialize globals
        DataKeeper.stripOldUpdateStatus();
        tagsAdapter = new TagsAdapter(this, DataKeeper.globalTagList);
        tagsListView.setAdapter(tagsAdapter);
        tagsListView.setLayoutManager(new GridLayoutManager(this,1));
        mainHandler = new MainHandler();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){
                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                        TagsAdapter.TagViewHolder mViewHolder = (TagsAdapter.TagViewHolder) viewHolder;
//                        DataKeeper.globalTagList.remove( mViewHolder.itemIndex);
//                        tagsAdapter.notifyItemRemoved( mViewHolder.itemIndex);
                        Intent intent = new Intent(MainActivity.this, TagEditActivity.class);
                        intent.putExtra("CURRENT TAG", mViewHolder.itemIndex);
                        startActivity(intent);
                    }

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                        return false;
                    }
                }
        );

        itemTouchHelper.attachToRecyclerView(tagsListView);
        //connecting to bluetooth device using helper method
        BluetoothDevice btDevice = BluetoothService.findBTdevice(DEVICE_NAME, this);
        if(btDevice != null){
        connectToDevice(btDevice);}


//        findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                String newTagAddress = ((EditText) findViewById(R.id.address_edit_text)).getText().toString();
//                String newTagName = ((EditText) findViewById(R.id.name_edit_text)).getText().toString();
//                String newTagType = ((EditText) findViewById(R.id.type_edit_text)).getText().toString();
//                boolean added = false;
//                switch (Integer.valueOf(newTagType)){
//                    case BaseTag.DATA_TYPE_DISCRETE:
//                        added = DataKeeper.addToGlobalTagList(new DiscreteTag(newTagAddress,newTagName, 16));
//                        break;
//                    case BaseTag.DATA_TYPE_ANALOG:
//                        added = DataKeeper.addToGlobalTagList(new AnalogTag(newTagAddress,newTagName));
//                        break;
//                }
//                if(added){
//                tagsAdapter.notifyDataSetChanged();
//                Toast.makeText(MainActivity.this, "tag added successfully", Toast.LENGTH_LONG).show();
//                }else{Toast.makeText(MainActivity.this, "tag already exists", Toast.LENGTH_LONG).show();}
//            }
//        });

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
        Toast.makeText(this, "Main destroyed",Toast.LENGTH_SHORT).show();
        BluetoothService.disconnectBT();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tagsAdapter.notifyDataSetChanged();
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
                    DataKeeper.buildReadTable();
                    Thread btListenerThread = new BtListenerThread();
                    btListenerThread.start();
                    break;
                case MessageType.DISCONNECTED:
                    connectedToDevice = false;
                    BluetoothService.disconnectBT();
                    break;
                case MessageType.WRITE:
                    stringBuilder.append("\n sent: "+ msg.obj);
                    break;

                case MessageType.READ:
                    String string= (String) msg.obj;
                    Log.d("received from BT: ", string);
                    DataKeeper.handleReceivedUpdate(string);
                    tagsAdapter.notifyDataSetChanged();
                    stringBuilder.append("\n received: "+ string);
                    break;

                case MessageType.TOAST:
                    String toast =  msg.getData().getString("toast");
                    Toast.makeText(MainActivity.this,toast,Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_projects, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            case R.id.action_add_project:
                Intent intent = new Intent(MainActivity.this, TagEditActivity.class);
                startActivity(intent);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

