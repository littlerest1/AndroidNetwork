package com.example.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.example.bluetooth.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import static com.example.bluetooth.MainActivity.myUUID;

public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    public ConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code

         tmp = device.createRfcommSocketToServiceRecord(myUUID);
       //   tmp = (BluetoothSocket) (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(mmDevice, 1);
        } catch (Exception e) {

        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        MainActivity.mBluetoothAdapter.cancelDiscovery();
        BluetoothSocket tmp = mmSocket;
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            System.out.println("try connect");
            mmSocket.connect();
            System.out.println(mmSocket.isConnected());
            if(mmSocket.isConnected()){
                System.out.println("Connected");
            }
            ConnectedThread mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.run();
        } catch (IOException connectException) {
            System.out.println("Enable to connect " + connectException.getMessage());
            try {
                mmSocket.close();


            } catch (IOException closeException) {
                System.out.println("Socket close fail");
            }
            return;
        }

        // Do work to manage the connection (in a separate thread)

    }


    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}

