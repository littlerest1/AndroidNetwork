package com.example.lab2_wifi;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.net.wifi.*;
import kotlinx.android.extensions.ContainerOptions;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class Main2Activity extends AppCompatActivity {
    private String wifiName = "";
    private  String type = "";
    private WifiManager wifiManager;
    private static final int count = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        TextView pw = findViewById(R.id.textView2);
        pw.setText("Password: ");



        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);


        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        wifiName = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        type = intent.getStringExtra(MainActivity.Type);
        System.out.println(wifiName + "     " + type);
        if (mWifi.isConnected()) {
            WifiInfo info = wifiManager.getConnectionInfo ();
            String ssid = info.getSSID();
            String temp =  "\"" + wifiName + "\"";
            System.out.println("Connected to wifi " + temp);
            if(ssid.equals(temp)){
                System.out.println("Same as the connected wifi");
                Toast.makeText(Main2Activity.this, "You already connected to this wifi.", Toast.LENGTH_LONG).show();
                finish();
            }
        }





        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView);
      //  textView.setText("Connect to wifi: " + wifiName + " ------------------- " + type);
        textView.setText("Identity ");

        Button Cancel = (Button) findViewById(R.id.button1);
        Cancel.setText("Cancel");
        Cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Back(v);
            }
        });

        Button Connent = (Button) findViewById(R.id.button2);
        Connent.setText("Connect");
        Connent.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Connect(v);
            }
        });


        if(type.contains("EAP")){
            System.out.println("Connect to EAP network");
            TextView uN = (TextView) findViewById(R.id.textView1);
            uN.setText("Identity: ");
        }
        if(type.contains("WPA")){
            HorizontalScrollView x = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
            EditText username = findViewById(R.id.editText2);
            x.removeView(username);
        }
    }

    private void Back(View v){
       finish();
    }

    private void  Connect(View v){
     //   wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        EditText editText = findViewById(R.id.editText);
        String password = editText.getText().toString();
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + wifiName + "\"";
        if(password.isEmpty()){
            Toast.makeText(Main2Activity.this, "Please provide your passwords.", Toast.LENGTH_LONG).show();
        }
        else if(type.contains("WPA")) {
            System.out.println("wifi type is WPA");
            System.out.println("wifi password " + password);

            config.preSharedKey = "\"" + password + "\"";
            //  config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.status=WifiConfiguration.Status.ENABLED;
            int netId= wifiManager.addNetwork(config);
            System.out.println("net id is " +String.valueOf(netId));
            wifiManager.saveConfiguration();
            wifiManager.reconnect();
            finish();
        }
        else if(type.contains("EAP")){
            System.out.println("wifi type is EAP");
            System.out.println("wifi password " + password);
            EditText editText1 = findViewById(R.id.editText2);
            String username = editText1.getText().toString();
            System.out.println("wifi identity " +  username);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            config.status=WifiConfiguration.Status.ENABLED;
            WifiEnterpriseConfig enterpriseCon = new WifiEnterpriseConfig();
            enterpriseCon.setIdentity(username);
            enterpriseCon.setPassword(password);
            enterpriseCon.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);

            config.enterpriseConfig = enterpriseCon;

            int netid = wifiManager.addNetwork(config);
            wifiManager.enableNetwork(netid,wifiManager.reconnect());
            System.out.println("net id is " +String.valueOf(netid) + " reconnect " + wifiManager.reconnect());
            wifiManager.saveConfiguration();
            wifiManager.reconnect();
            if(netid > 0) {
                finish();
            }


        }
        else{

            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.status=WifiConfiguration.Status.ENABLED;
            int netId= wifiManager.addNetwork(config);
            System.out.println("net id is " +String.valueOf(netId));
            wifiManager.saveConfiguration();
            wifiManager.reconnect();
            finish();
        }

    }
}
