package com.example.lab2_wifi;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.app.Activity;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.*;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends Activity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    public static final String Type  = "com.example.myfirstapp.Type";
    private WifiManager wifiManager;
    private static final int RC_LOCATION = 1;
    private String connectTo = "";
    private String securityType = "";
    PopupWindow popUp;
    LinearLayout layout;
    TextView tv;
    ViewGroup.LayoutParams params;
    LinearLayout mainLayout;
  //  private static final int WPA = 1;
  //  private static final int open = 2;
    private List<ScanResult> results = null;
    private ArrayList<String> wifis = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//	set	default	counter	for	the	text	box
        Button clickMeBtn = (Button) findViewById(R.id.button1);
        clickMeBtn.setText("Scan for wifi");

        TextView text = (TextView) findViewById(R.id.textView1);
        text.setText("Results ");

        clickMeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myClick(v);
            }
        });
    }

        public void myClick(View v) {

            WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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

            BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent intent) {
                    boolean success = intent.getBooleanExtra(
                            WifiManager.EXTRA_RESULTS_UPDATED, false);

                    if (success) {
                        scanSuccess();
                    } else {
                        // scan failure handling
                        scanFailure();
                    }
                }
            };
         //   Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
         //   startActivity(myIntent);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);

            boolean success = wifiManager.startScan();
            if (!success) {
                // scan failure handling
                scanFailure();
            }

        }

        public String getConnectTo(){
            return this.connectTo;
        }

        public void setConnectTo(String SSID){
            this.connectTo = SSID;
        }

        public String getSecurityType(){
            return this.securityType;
        }

        public void setSecurityType(String type){
            this.securityType = type;
        }

        private void scanSuccess(){
            results = wifiManager.getScanResults();
            System.out.println(results.isEmpty());
            if(!results.isEmpty()){
                System.out.println(results.toString());
            }
            String name = "";
            final ListView lv = (ListView) findViewById(R.id.ListView);
            final List<String> List = new ArrayList<String>();
            ArrayList<String> list = getWifiList(results);

            Object[] arr = list.toArray();
            final String[] str = Arrays.copyOf(arr,
                    arr.length,
                    String[].class);

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                    (this, android.R.layout.simple_list_item_1,list );
            lv.setAdapter(arrayAdapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    System.out.println("Click " + position);
                    String t = wifis.get(position);
                    System.out.println("Connect to " + t);

                    String[] wifiName = t.split(" ");
                    System.out.println("Try to connect to " + wifiName[1]);
                    setConnectTo(wifiName[1]);
                    setSecurityType(wifiName[0]);
                    //openActivity2();

                    if(wifiName[0].contains("EAP") || wifiName[0].contains("WPA")){
                        openActivity2();
                    }
                    else{
                        WifiConfiguration config = new WifiConfiguration();
                        config.SSID = "\"" + connectTo + "\"";
                        config.status=WifiConfiguration.Status.ENABLED;
                        int netId= wifiManager.addNetwork(config);
                        System.out.println("net id is " +String.valueOf(netId));
                        wifiManager.saveConfiguration();
                        wifiManager.reconnect();
                    }

                   // openActivity2();
                }
            });



       //     ListView layout= (ListView) findViewById(R.id.ListView);
        //    ListView lyt =  (ListView) findViewById(R.id.ListView);
          //  lyt.removeAllViews();
      //      int count = 3;
            //filter the duplicate wifi
            //List<ScanResult> filtered = filter(results);
         /*  for(ScanResult x : results){

            //    name+= x.SSID + "\n";
                name = x.SSID;
                int level = -1;

                level = wifiManager.calculateSignalLevel(x.level, 100);
                System.out.println(level);
                TextView txt = new TextView(this );
            //    txt.setText(x.SSID + "  -----------   " +  String.valueOf(level));
                txt.setGravity(Gravity.LEFT);
                txt.setId(count);
                System.out.println(x.capabilities);
                if(x.capabilities.contains("WPA") && x.capabilities.contains("PSK")){
                    txt.setText( "WPA/WPA2-PSK "+ x.SSID + "  -----------   " +  String.valueOf(level));
                }
                else if(x.capabilities.contains("WPA") && x.capabilities.contains("EAP")){
                    txt.setText( "EAP "+ x.SSID + "  -----------   " +  String.valueOf(level));
                }
                else{
                    txt.setText("OPEN " + x.SSID + "  -----------   " +  String.valueOf(level));
                }

                Button button = new Button(this);
                button.setText("Connect ");

                button.setId(count);
                button.setGravity(Gravity.CENTER);
                button.setWidth(10);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {



                        TextView t = (TextView) findViewById(v.getId());
                        String[] wifiName = t.getText().toString().split(" ");
                        System.out.println("Try to connect to " + wifiName[1]);
                        setConnectTo(wifiName[1]);
                        setSecurityType(wifiName[0]);
                        //openActivity2();

                        if(wifiName[0].contains("EAP") || wifiName[0].contains("WPA")){
                            openActivity2();
                        }
                        else{
                            WifiConfiguration config = new WifiConfiguration();
                            config.SSID = "\"" + connectTo + "\"";
                            config.status=WifiConfiguration.Status.ENABLED;
                            int netId= wifiManager.addNetwork(config);
                            System.out.println("net id is " +String.valueOf(netId));
                            wifiManager.saveConfiguration();
                            wifiManager.reconnect();
                        }*/
/*
                       if(wifiName[0].equals("WPA")) {
                           System.out.println("wifi type is WPA");
                            String pass = "83882989";

                            config.preSharedKey = "\"" + pass + "\"";
                            //  config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        }
                        else{
                            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        }
                        config.status=WifiConfiguration.Status.ENABLED;
                        int netId= wifiManager.addNetwork(config);
                        System.out.println("net id is " +String.valueOf(netId));
                        wifiManager.saveConfiguration();
                        wifiManager.reconnect();*/
              /*      }
                });
                System.out.println(x.toString());
                count ++;
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.FILL_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                layout.addView(txt, p);
                layout.addView(button);
            }
            */

      //      TextView text = (TextView) findViewById(R.id.textView2);
        //    text.setText(name);
        }

        private void scanFailure(){
            System.out.println("Scan fail");
        }

        public void openActivity2(){
         //   Intent intent = new Intent(this, Activity2.class);

           // startActivity(intent);
            Intent intent = new Intent(this, Main2Activity.class);

            String message = connectTo;
            String Stype = securityType;
            intent.putExtra(EXTRA_MESSAGE, message);
            intent.putExtra(Type,Stype);
            startActivity(intent);
        }

        public ArrayList<String> getWifiList(List<ScanResult> list){
            ArrayList<String> wifi = new ArrayList<String>();
           int level = -1;
            Collections.sort(list, new LevelComparator());

            for(ScanResult x : list){
                level = wifiManager.calculateSignalLevel(x.level, 100);
                System.out.println(String.valueOf(level));
                if(x.SSID.isEmpty()){

                    continue;
                }
                else if(x.capabilities.contains("WPA") && x.capabilities.contains("PSK")){
                    wifi.add( "WPA/WPA2-PSK "+ x.SSID + "  -----------   " +  x.level);
                }
                else if(x.capabilities.contains("WPA") && x.capabilities.contains("EAP")){
                   wifi.add( "EAP "+ x.SSID + "  -----------   " +  x.level);
                }
                else{
                    wifi.add("OPEN " + x.SSID + "  -----------   " +  x.level);
                }
            }

            this.wifis = wifi;
            return wifi;
        }

         class LevelComparator implements Comparator<ScanResult> {
            @Override
            public int compare(ScanResult a, ScanResult b) {
              //  int levelA = wifiManager.calculateSignalLevel(a.level, 100);
           //     int levelB = wifiManager.calculateSignalLevel(b.level, 100);
                return a.level > b.level ? -1 : a.level == b.level ? 0 : 1;
            }
        }
    }
