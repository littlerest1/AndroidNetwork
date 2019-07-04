package com.example.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static BluetoothAdapter mBluetoothAdapter;
    private ArrayList<String> Devices = new ArrayList<String>();
    private ArrayList<BluetoothDevice> all = new ArrayList<BluetoothDevice>();
    public BluetoothServerSocket server;
    public BluetoothSocket mBTSocket;
    public BluetoothDevice mDevice;
    public static Handler mHandler;
    public String name = "";
    public static UUID myUUID = UUID.fromString("00001106-0000-1000-8000-00805F9B34FB");

    public ConnectedThread mConnectedThread;
    public ConnectThread mConnectThread;
    public AcceptThread mAcceptThread;
    private final String TAG = MainActivity.class.getSimpleName();
    public String message = "";
    public String received = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button check = (Button) findViewById(R.id.button);
        check.setText("Check BlueTooth Status");

        Button search = (Button) findViewById(R.id.button2);
        search.setText("Discover neighbourhood");
        search.setEnabled(false);

        Button send = (Button) findViewById(R.id.button3);
        send.setText("Send");
        send.setEnabled(false);

        EditText msg = (EditText) findViewById(R.id.editText);
        msg.setEnabled(false);


        mHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == 2){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                        System.out.println("Handler" + readMessage);
                       Toast.makeText(MainActivity.this,readMessage,Toast.LENGTH_LONG).show();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                //    mReadBuffer.setText(readMessage);
                }

                if(msg.what == 3){
                    if(msg.arg1 == 1)
                       System.out.println("Connected to Device: " + (String)(msg.obj));
                    else
                        System.out.println("Connection Failed");
                }
            }
        };

        check.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                }

                TextView field = (TextView) findViewById(R.id.textView);
                field.setText("BlueTooth is enabled");
                Button search = (Button) findViewById(R.id.button2);
                search.setEnabled(true);
            }
        });



        search.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText msg = (EditText) findViewById(R.id.editText);
                msg.setEnabled(true);
                Button send = (Button) findViewById(R.id.button3);
                send.setEnabled(true);
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
// If there are paired devices
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        //  mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                        System.out.println(device.getName() + " " + device.getAddress());
                    }
                }
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                try {
                    TimeUnit.SECONDS.sleep(8);

                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mReceiver, filter);
                    mBluetoothAdapter.startDiscovery();

                    //  ListView lv = (ListView) findViewById(R.id.listView);
                    //   lv.setAdapter(mArrayAdapter);

                    //  mBluetoothAdapter.getBondedDevices();
               }catch (Exception e){

                }
                System.out.println("Array in return " + Devices.isEmpty());

            }
        });

        System.out.println("Array" + Devices.toArray().toString());

        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent discoverableIntent = new
                        Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
                if(!Devices.isEmpty()) {
                    mBluetoothAdapter.cancelDiscovery();
                    String t = Devices.get(position);
                    System.out.println("Connect to " + t);

                    final String[] DName = t.split(" ");
                    System.out.println("Address: " + DName[0]);
                    mDevice = mBluetoothAdapter.getRemoteDevice(DName[0]);
                    name = mDevice.getName();
                    System.out.println(mDevice.getName());
                    mAcceptThread = new AcceptThread();
                    mAcceptThread.start();
                    mConnectThread = new ConnectThread(mDevice);
                    mConnectThread.start();
                }

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText msg = (EditText) findViewById(R.id.editText);
                message = msg.getText().toString();
             //   System.out.println(message);
                mConnectedThread.write(message);
                msg.setText("");
            }
        });

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, myUUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(myUUID);
    }



    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("AcceptThread",MainActivity.myUUID);
            } catch (IOException e) {
                System.out.println(e.getMessage().toString());
            }

            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                   /* ConnectedThread c = new ConnectedThread(socket);
                    c.start();*/

                   System.out.println("start manage");
                    mConnectedThread= new ConnectedThread(socket);
                    mConnectedThread.start();
                    try {
                        mmServerSocket.close();
                    }
                    catch (Exception e){
                        System.out.println("accept run " + e.getMessage().toString());
                    }
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final DataInputStream mmInStream;
        private final DataOutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = new DataInputStream(tmpIn);
            mmOutStream = new DataOutputStream(tmpOut);
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            System.out.println("Is in here");
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        buffer = new byte[1024];
                        bytes = mmInStream.read(buffer);
                        String readMessage = new String(buffer, 0, bytes);
                     /*   SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read*/
                        System.out.println(readMessage);
//                       Toast.makeText(MainActivity.this,readMessage,Toast.LENGTH_SHORT).show();
                        received = readMessage;
                        MainActivity.mHandler.obtainMessage(2, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity*/
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            System.out.println(input);
            byte[] bytes = input.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectThread extends Thread {
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
            } catch (IOException e) {
                System.out.println("Connet" + e.getMessage().toString());
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    System.out.println("Close" + closeException.getMessage().toString());
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
         //   manageConnectedSocket(mmSocket);
            mConnectedThread= new ConnectedThread(mmSocket);
            mConnectedThread.start();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device =
                            (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress();

                    // Add the name and address to an array adapter to show in a ListView
                    String whole = "";
                    System.out.println(deviceName + "   " + deviceHardwareAddress);
                    if (deviceName != null &&  !deviceName.isEmpty()) {
                        whole = deviceHardwareAddress + " " + deviceName;
                    } else {
                        whole = deviceHardwareAddress + " ";
                    }
                    System.out.println("Whole msg = " + whole);
                    if(!Devices.contains(whole)){
                        Devices.add(whole);
                        all.add(device);
                    }


            }

            System.out.println(Devices.isEmpty());
            if(!Devices.isEmpty()){
                mBluetoothAdapter.cancelDiscovery();
                System.out.println("Set list View");
                ListView lv = (ListView) findViewById(R.id.listView);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                        (MainActivity.this, android.R.layout.simple_list_item_1,Devices );
                lv.setAdapter(arrayAdapter);
                TextView result = (TextView) findViewById(R.id.textView);
                result.setText("Searching.. find " + Devices.size());
            }


        }
    };
    // Register the BroadcastReceiver

   // this.Context.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

}

