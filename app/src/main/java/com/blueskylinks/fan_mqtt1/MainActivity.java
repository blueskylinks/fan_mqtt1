package com.blueskylinks.fan_mqtt1;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    byte sc1[];
    int st=-1;
    int ms1;
    int ms2;
    int ms3;
    TextView tv;
    MqttClient sampleClient;
    MqttMessage Mmessage1;
    MqttMessage Mmessage2;
    MqttMessage Mmessage3;
    int count=0;
    int lr[]=new int[6];
    private BluetoothGatt mGatt;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner scanner;
    private BluetoothDevice ble_device;
    ScanRecord scan_rec;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},200);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},200);
       // initialize();
       // mqtt_connect();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume(){
        super.onResume();
       // startscand();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        Log.i("BleScanning:", "initilizing.......");
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.i("BleScanning:", "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.i("BleScanning:", "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            mBluetoothAdapter.enable();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    //================  Start BLE Scanning  ===============
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startscand() {
        Log.i("BLE------", "Start Scanning");
        //final ParcelUuid UID_SERVICE =
        ParcelUuid.fromString("000000f1-0000-1000-8000-00805f9b34fb");
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        ScanFilter beaconFilter = new ScanFilter.Builder() // this filter will be used to get only specific device based on service UUID
                //.setServiceUuid(UID_SERVICE)
                .build();
        ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(beaconFilter);
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        scanner.startScan(filters, settings, mcallback);

    }



    // =============== BLE Callback =======================
    // This callback method will be automatically called every time the scanner get the device adv data
    public ScanCallback mcallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            int rssi;
            rssi = result.getRssi();
            scan_rec = result.getScanRecord();
            Log.i("Scan result",String.valueOf(rssi));
           // Log.i("record",scan_rec.toString());
            sc1=scan_rec.getManufacturerSpecificData(0);
            tv=findViewById(R.id.tv);
            for (int i=0;i<sc1.length; i++){
                Log.i("Data-----:", String.valueOf(sc1[i]));
                lr[i]=sc1[i];
            }
           try {
                //  Thread.sleep(10000);
               if(String.valueOf(ms1)!=String.valueOf(lr[0]) || String.valueOf(ms1)!=String.valueOf(lr[0]) || String.valueOf(ms1)!=String.valueOf(lr[0]))
               {msg_pub();tv.setText(String.valueOf(lr[0]));}
            } catch (MqttException e) {
                e.printStackTrace();
            }
            ms1=lr[0];
            ms2=lr[1];
            ms3=lr[2];
            ble_device = result.getDevice();
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopscand() {
        Log.i("BLE-----", "Stop Scanning");
        scanner.stopScan(mcallback);
    }


    public void mqtt_connect(){
        String broker       = "tcp://13.126.9.228:1883";
        String clientId     = "4";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            Log.i("Connecting to broker: ", broker);
            sampleClient.connect(connOpts);
            Log.i("Connected", "C");
            //sampleClient.subscribe("home");


        } catch(MqttException me) {
            Log.i("reason ",String.valueOf(me.getReasonCode()));
            Log.i("msg ",String.valueOf(me.getMessage()));
            Log.i("loc ",String.valueOf(me.getLocalizedMessage()));
            Log.i("cause ",String.valueOf(me.getCause()));
            Log.i("excep ",String.valueOf(me));
            me.printStackTrace();
        }

    }

    public void msg_pub()throws MqttException {

        String topic = "home";
        Mmessage1 = new MqttMessage();
        Mmessage2 = new MqttMessage();
        Mmessage3 = new MqttMessage();
        Mmessage1.setPayload( String.valueOf(lr[0]).getBytes());
        sampleClient.publish(topic, Mmessage1);
        Log.i("message sending ",String.valueOf(Mmessage1));


          Mmessage2.setPayload( String.valueOf(lr[1]).getBytes());
        sampleClient.publish(topic, Mmessage2);
        Log.i("message sending ",String.valueOf(Mmessage2));

      Mmessage3.setPayload( String.valueOf(lr[2]).getBytes());
        sampleClient.publish(topic, Mmessage3);
        Log.i("message sending ",String.valueOf(Mmessage3));
    }

    public void stratService(View view){
        Intent intent=new Intent(this,mqttservice.class);
        startService(intent);
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
    }
    public void stopService(View view){
        Intent intent=new Intent(this,mqttservice.class);
        stopService(intent);
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
    }
}
