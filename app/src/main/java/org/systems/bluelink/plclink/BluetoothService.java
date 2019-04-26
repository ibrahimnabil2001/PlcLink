package org.systems.bluelink.plclink;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Set;

public class BluetoothService {

    private static final String TAG = "MY_APP_DEBUG_TAG";
    private static Handler handler; // handler that handles data between bluetooth service class and UI-thread
    private static BluetoothSocket mmSocket;
    private static OutputStream mmOutStream = null; //to be used within send method

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    public interface MessageType {
        int READ = 0;
        int WRITE = 1;
        int TOAST = 2;
        int CONNECTED = 3;
        int DISCONNECTED = 4;
    }

//-----------------------------------Thread--------------------------------------------------------
//-------------------------------------------------------------------------------------------------
    public static class ConnectToBT extends Thread {


        private final BluetoothDevice mmDevice;
        private final String TAG = this.getName();

        //initialize bluetoothAdapter
        private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        ConnectToBT(BluetoothDevice device) { //pass a device to the constructor to connect to

            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket mSocket = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                mSocket = mmDevice.createRfcommSocketToServiceRecord(mmDevice.getUuids()[0].getUuid());
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = mSocket;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try { //wait 2sec for screen rotation
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                Log.e(TAG, "Could not connect to the client socket", connectException);
                try {
                    bluetoothAdapter.startDiscovery();
                    mmSocket.close();
                    handler.sendEmptyMessage(MessageType.DISCONNECTED);
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            // By sending message to Ui thread handler containing Connected socket.
            Message connectedMessage = new Message();
            connectedMessage.what = MessageType.CONNECTED;
            connectedMessage.obj = mmSocket;
            handler.sendMessage(connectedMessage);
        }
    }
//-----------------------------------Thread--------------------------------------------------------
//-------------------------------------------------------------------------------------------------
    static class BtListenerThread extends Thread {

        private static InputStream mmInStream = null;


        BtListenerThread(BluetoothSocket socket) {

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                mmInStream = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                mmOutStream = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

        }

        @Override
        public void run() {

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    if (mmInStream.available() > 0) {
                        StringBuilder inputStringBuilder = new StringBuilder();

                        // Read from the InputStream.
                        InputStreamReader inputStreamReader = new InputStreamReader(mmInStream,
                                Charset.forName("UTF-8"));
                        BufferedReader reader = new BufferedReader(inputStreamReader);
                        String line = reader.readLine();
                        inputStringBuilder.append(line); //The single message is one line only

                        // feed back the obtained bytes to the UI activity.
                        Message readMsg = handler.obtainMessage(MessageType.READ,
                                inputStringBuilder.toString().trim());
                        readMsg.sendToTarget();
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }
    }
//------------------------------------UTILITY------------------------------------------------------
//-------------------------------------------------------------------------------------------------

    // Call this from the main activity to send data to the remote device.
    public static void send(String outString) {

        try {
            mmOutStream.write(outString.getBytes());

            // Share the sent message with the UI activity.
            Message writtenMsg = handler.obtainMessage(MessageType.WRITE, outString);
            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);

            // Send a failure message back to the activity.
            Message writeErrorMsg = handler.obtainMessage(MessageType.TOAST);
            Bundle bundle = new Bundle();
            bundle.putString("toast", "Couldn't send data to the other device");
            writeErrorMsg.setData(bundle);
            handler.sendMessage(writeErrorMsg);
        }
    }

    //find the passed device name
     static public BluetoothDevice findBTdevice(String deviceName, Activity activity){

        // get reference to Main thread Handler
         handler = MainActivity.mainHandler;

        //initializing bluetooth adapter from framework class
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Message writeErrorMsg = handler.obtainMessage(MessageType.TOAST);
        Bundle bundle = new Bundle();

        if (bluetoothAdapter == null) { // check if framework returned any adapter
            // Send a failure message back to the activity.
            bundle.putString("toast", "Bluetooth is not supported");
            writeErrorMsg.setData(bundle);
            handler.sendMessage(writeErrorMsg);
            return null;

        }else if (!bluetoothAdapter.isEnabled()) { //if bluetooth is not enabled ask the user to enable it
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableAdapter, 0); }

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

        if(bondedDevices.isEmpty()) { // check if any device is paired
            bundle.putString("toast", "Please Pair the Device first");
            writeErrorMsg.setData(bundle);
            handler.sendMessage(writeErrorMsg);
            return null;
        } else {
            for (BluetoothDevice iterator : bondedDevices) {

                String iteratorDeviceName = iterator.getName(); //getDevice Nmae
                if(iteratorDeviceName.equals(deviceName)){
                    return iterator;
                }
            } }

            return null;
    }
    // Closes the client socket.
    static public void disconnectBT() {
        //initialize bluetoothAdapter
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.startDiscovery(); //placed before try/catch to make sure it
        // will be executed because leaving BT
        // DISCOVERY DISABLED causes the next
        // socket.connect to throw an exception
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}