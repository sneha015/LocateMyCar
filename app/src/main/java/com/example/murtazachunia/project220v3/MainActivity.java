package com.example.murtazachunia.project220v3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
/*
This is the class which runs when the app is first launched.
It starts scanning for the ble devices and stores the data of
those ble devices whose identifier is equal to "12345".
When one these ble devices is recognized by the app it initializes
a sensor object,which is an object of the class SensorClass. It monitors
the walking activity of the user. Once the user starts to walk it stores
the data of that ble device which has the strongest signal strength. Strong
signal strength implies that to which ble device the user is closest to and hence
user must have parked the car in the zone of that ble deice. Ble device works as
a simple broadcaster device and it broadcasts the place, floor and zone information of the
spot it corresponds to.
 */

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter btAdapter;
    IntentFilter filter;
    BroadcastReceiver receiver;
    Button start;
    Button stop;
    Button mycar;
    Button pair;
    BackgroundTask backgroundTask;
    UpdataDatabaseTask updateDatabaseTask;
    // UpdataDatabaseTask updateDatabaseTask2;
    String data_final;
    String device_addr = new String();
    String method;
    String name;
    String address;
    String floor;
    String floorMap;
    String zone;
    String ux;
    String uy;
    String lx;
    String ly;
    String lat;
    String lng;

    SensorClass mySensor;
    private BluetoothAdapter mBluetoothAdapter;
    Handler mHandler = new Handler();
    SparseArray<BluetoothDevice> mDevices;
    HashMap signalStrength = new HashMap();
    BluetoothAdapter.LeScanCallback mLeScanCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        device_addr = "hello";
        setContentView(R.layout.activity_main);
        start = (Button) findViewById(R.id.btnBlu);
        stop = (Button) findViewById(R.id.btnStop);
        stop.setBackgroundColor(Color.TRANSPARENT);
        mycar = (Button) findViewById(R.id.btnMyCar);
        pair = (Button) findViewById(R.id.btnPair);
        pair.setBackgroundColor(Color.TRANSPARENT);


        mycar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callGet();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setBackgroundColor(Color.TRANSPARENT);
                init_ble();

            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothDevice finalDevice = null;
                int strength = -1000;
                int currentSignalStrength;
                for (int i=0;i<mDevices.size();i++) {
                    Toast.makeText(getApplicationContext(), mDevices.valueAt(i).getAddress().toString()+" "+mDevices.valueAt(i).getName(), Toast.LENGTH_SHORT).show();
                    currentSignalStrength = (int)signalStrength.get(mDevices.valueAt(i).getAddress());
                    if( currentSignalStrength > strength){
                        finalDevice = mDevices.valueAt(i);
                    }
                }
                Toast.makeText(getApplicationContext(), "BLE SCAN SUCCESSFULLY STOPPED FINAL DEVICE IS : "+finalDevice.getName(), Toast.LENGTH_SHORT).show();

                if(finalDevice != null) {
                    String uuid = finalDevice.getName();
                    initializeData(uuid);
                    address = finalDevice.getAddress();

                    updateDatabaseTask = new UpdataDatabaseTask(MainActivity.this);
                    updateDatabaseTask.setUpdateListener(new UpdataDatabaseTask.OnUpdateListener() {
                        @Override
                        public void onUpdate(String[] obj) {
                            Toast.makeText(getApplicationContext(), obj[0], Toast.LENGTH_SHORT).show();
                            //  unregisterReceiver(receiver);
                            //finish();
                            //System.exit(0);
                        }
                    });
                    updateDatabaseTask.execute("update", name, zone, address, floor);
                }
            }
        });


    }

    protected void onActivityResult(int requestcode,int resultcode,Intent intent){
        super.onActivityResult(requestcode, resultcode, intent);
        if (resultcode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(),"Bluetooth must be enabled",Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void startdiscovery() {
        // btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }

    protected void onPause(){
        super.onPause();
        //  unregisterReceiver(receiver);
    }



    private void init_ble(){
        mySensor = new SensorClass(MainActivity.this);
        mySensor.setStepListener(new SensorClass.stepListener() {
            @Override
            public void onSensorUpdate(String[] obj) {
                BluetoothDevice finalDevice = null;
                int strength = -1000;
                int currentSignalStrength;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                for (int i=0;i<mDevices.size();i++) {
                    Toast.makeText(getApplicationContext(), mDevices.valueAt(i).getAddress().toString()+" "+mDevices.valueAt(i).getName(), Toast.LENGTH_SHORT).show();
                    currentSignalStrength = (int)signalStrength.get(mDevices.valueAt(i).getAddress());
                    if( currentSignalStrength > strength){
                        finalDevice = mDevices.valueAt(i);
                    }
                }
                Toast.makeText(getApplicationContext(), "BLE SCAN SUCCESSFULLY STOPPED FINAL DEVICE IS : "+finalDevice.getName(), Toast.LENGTH_SHORT).show();

                if(finalDevice != null) {
                    String uuid = finalDevice.getName();
                    initializeData(uuid);
                    address = finalDevice.getAddress();

                    updateDatabaseTask = new UpdataDatabaseTask(MainActivity.this);
                    updateDatabaseTask.setUpdateListener(new UpdataDatabaseTask.OnUpdateListener() {
                        @Override
                        public void onUpdate(String[] obj) {
                            Toast.makeText(getApplicationContext(), obj[0], Toast.LENGTH_SHORT).show();

                        }
                    });
                    updateDatabaseTask.execute("update", name, zone, address, floor);
                }
                else Toast.makeText(getApplicationContext(), "NO DEVICE TO UPDATE", Toast.LENGTH_SHORT).show();
            }
        });
        mySensor.execute("stepble");

        mDevices = new SparseArray<BluetoothDevice>();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE NOT SUPPORTED", Toast.LENGTH_SHORT).show();
            finish();
        }else {
            Toast.makeText(this, "BLE SUPPORTED", Toast.LENGTH_SHORT).show();
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    // Toast.makeText(MainActivity.this,device.getAddress().toString(),Toast.LENGTH_LONG).show();
                    String UU_ID = device.getName().substring(0,5);
                    if(UU_ID.equals("12345")) {
                        mDevices.put(device.getName().hashCode(), device);
                        signalStrength.put(device.getAddress(),rssi);
                      //  mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
                }
            };

            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }

    }

    private void initializeData(String uuid) {
        int length;
        int start;
        int end;

        start = 0;
        end = 5;

        //  System.out.println("IDENTIFIER : "+uuid.substring(start,end));

        start = end;
        end = end + 2;
        length = Integer.parseInt(uuid.substring(start,end));
        start = end;
        end = length + end;
        name = uuid.substring(start, end);

        start = end;
        end = end + 2;
        length = Integer.parseInt(uuid.substring(start,end));
        start = end;
        end = length + end;
        zone = uuid.substring(start, end);

        start = end;
        end = end + 2;
        length = Integer.parseInt(uuid.substring(start,end));
        start = end;
        end = length + end;
        floor = uuid.substring(start, end);


    }

    public void onResume(){
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

    }



    private void callGet(){
        updateDatabaseTask = new UpdataDatabaseTask(MainActivity.this);
        updateDatabaseTask.setUpdateListener(new UpdataDatabaseTask.OnUpdateListener() {
            @Override
            public void onUpdate(String[] obj) {
                final String[] car = obj;
                String sysid1String = Arrays.toString(obj);

                if(!(car[0].equals("No data found on getdata")) || !(car[0] == null)) {
                    backgroundTask = new BackgroundTask(MainActivity.this);
                    backgroundTask.setUpdateListener(new BackgroundTask.OnUpdateListener() {
                        @Override
                        public void onUpdateBackground(String data) {
                            floorMap = data;

                            if (!floorMap.equals("error")) {
                                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                                intent.putExtra("MAP",floorMap);
                                intent.putExtra("NAME",car[1]);
                                intent.putExtra("FLOOR",car[4]);
                                intent.putExtra("ZONE",car[2]);
                                startActivity(intent);

                            } else {
                                //  setContentView(R.layout.activity_draw);
                                Toast.makeText(getApplicationContext(), "Error getting floor Map", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    //backgroundTask.execute("Floor Plan","sjsu","floor1");
                    backgroundTask.execute("Floor Plan",car[1],car[4]);
                }
                else Toast.makeText(getApplicationContext(), "Error on get data", Toast.LENGTH_LONG).show();
            }
        });
        updateDatabaseTask.execute("getdata");
    }

}
