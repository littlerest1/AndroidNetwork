package com.example.p2p;

import android.Manifest;
import android.app.LauncherActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    private  WifiP2pManager Pmanager;
    private WifiP2pManager.Channel ch;
    private WifiP2pManager.ChannelListener listener;
    private  WifiP2pManager.ActionListener actionListener;
    private  WifiP2pManager.ConnectionInfoListener info;
 //   private WifiP2pManager.ActionListener aL;
    private static final int RC_LOCATION = 1;
    private ArrayAdapter<LauncherActivity.ListItem> adapter;
    private ArrayList<String> devices = null;
    private BroadcastReceiver receiver = null;
    private WifiP2pManager.PeerListListener peerListListener;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        setContentView(R.layout.activity_main);
        TextView text = (TextView) findViewById(R.id.TextView1);
        text.setText("Hello world");
        Button btn = (Button) findViewById(R.id.button);
        btn.setText("Peer discovery");

        WifiP2pManager WD_Manager = (WifiP2pManager)
                getSystemService(Context.WIFI_P2P_SERVICE);
        if (WD_Manager.WIFI_P2P_STATE_ENABLED != 2) {
            System.out.println("Not support");
            text.setText("Not support Wifi direct");
        }
        else{
            text.setText("Supports Wifi direct");
        }
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myClick(v);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(Pmanager, ch, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void myClick(View v) {
        WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // text.setText(String.valueOf(mWifiManager.isWifiEnabled()));
        System.out.println(mWifiManager.isWifiEnabled());
        if (!mWifiManager.isWifiEnabled()) {
            if (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
                mWifiManager.setWifiEnabled(true);
            }
        }

        String location = android.Manifest.permission.ACCESS_COARSE_LOCATION;
        if (ActivityCompat.checkSelfPermission(this, location) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("here");
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    RC_LOCATION  );
        }

        final ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(null);
        Pmanager = null;
        Pmanager = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        ch = Pmanager.initialize(this, getMainLooper(),listener);
        Pmanager.discoverPeers(ch, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                System.out.println("Discovered");


                ShowDevice();
            }

            public void onFailure(int reasonCode) {
               System.out.println("Discovery errorcode:" + String.valueOf(reasonCode));
            }
        });

    }




    public void ShowDevice(){
        Pmanager.requestPeers(ch, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                final ListView lv = (ListView) findViewById(R.id.listView);
                final List<String> List = new ArrayList<String>();
                ArrayList<String> list = getDeviceList(wifiP2pDeviceList.getDeviceList());

                Object[] arr = list.toArray();
                final String[] str = Arrays.copyOf(arr,
                        arr.length,
                        String[].class);

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                        (MainActivity.this, android.R.layout.simple_list_item_1 , list);
                lv.setAdapter(arrayAdapter);

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                            long id) {
                        System.out.println("Click " + position);
                        String t = devices.get(position);
                        System.out.println("Connect to " + t);

                        final String[] DName = t.split(" -------MAC: ");
                        System.out.println("Try to connect to " + DName[1]);
                        WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress = DName[1];


                        Pmanager.connect(ch,config, new WifiP2pManager.ActionListener() {
                            public void onSuccess() {
                                System.out.println("Connecting ");


                            try {
                                TimeUnit.SECONDS.sleep(7);
                            }catch (Exception e){

                            }
                               Pmanager.requestConnectionInfo(ch, new WifiP2pManager.ConnectionInfoListener() {
                                   @Override
                                   public void onConnectionInfoAvailable(WifiP2pInfo info) {
                                         if (info.groupFormed) {
                                             // The device now act as the slave device,
                                             // and the other connected device is group owner
                                             Toast.makeText(MainActivity.this, "Connected to " + DName[0], Toast.LENGTH_LONG).show();
                                         }
                                         else{
                                             Toast.makeText(MainActivity.this, "Failed due to unexpected error " + DName[0], Toast.LENGTH_LONG).show();
                                         }
                                   }
                               });

                            }

                            public void onFailure(int reasonCode) {
                                System.out.println("Connect fail error code:" + String.valueOf(reasonCode));
                            }
                        });

                    }
                });

            }
        });
    }

    private ArrayList<String> getDeviceList(Collection<WifiP2pDevice> list){
        ArrayList<String> device = new ArrayList<String>();
        for(WifiP2pDevice x : list){
            System.out.println(x.deviceName.toString() + " MAC " + x.deviceAddress);
            device.add(x.deviceName.toString() + " -------MAC: " +  x.deviceAddress);
        }
        this.devices = device;
        return device;
    }


    /**
     * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
     */
    public static class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

        private WifiP2pManager mManager;
        private WifiP2pManager.Channel mChannel;
        private MainActivity mActivity;

        public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                           MainActivity activity) {
            super();
            this.mManager = manager;
            this.mChannel = channel;
            this.mActivity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
            }
        }
    }
}
