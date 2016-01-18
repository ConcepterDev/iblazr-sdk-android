package com.concepter.iblazr2demo;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.concepter.ibllazrbluetoothlib.AbstractIBlazrDevice;
import com.concepter.ibllazrbluetoothlib.BLEIblazrDevice;
import com.concepter.ibllazrbluetoothlib.BLEManager;
import com.concepter.ibllazrbluetoothlib.OnIblazrDeviceDiscoverCallback;


public class MainActivity extends AppCompatActivity {
    final static String CONNECTED = "iBlazr Connected TRUE";
    final static String NOT_CONNECTED = "iBlazr does not connected";
    private TextView isIblazrConnect;
    private BLEManager bleManager;
    private BLEIblazrDevice bleIblazrDevice;
    private BluetoothGatt gattOfDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
        initBLEManager();
        verifyBLESupportable();
    }

    private void verifyBLESupportable() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_LONG).show();
        }
    }

    private void initBLEManager() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bleManager = BLEManager.getInstance(bluetoothManager.getAdapter(), this,
                new OnIblazrDeviceDiscoverCallback() {
                    @Override
                    public void onDeviceDiscovered(final BLEIblazrDevice device) {
                        bleManager.addBleIblazrDevice(device);
                        bleIblazrDevice = device;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isIblazrConnect.setText(CONNECTED);
                            }
                        });
                    }

                    @Override
                    public void onDeviceGATTConnectionError(int status) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                isIblazrConnect.setText(NOT_CONNECTED);
                            }
                        });
                    }

                    @Override
                    public void onServicesDiscoverError(int status) {

                    }
                });
    }

    private void setupViews() {
        Button connectIblazr = (Button) findViewById(R.id.buttonCnnect);
        Button checkIblzazr = (Button) findViewById(R.id.buttonCheck);
        Button checkColdLight = (Button) findViewById(R.id.buttonCheckColdLight);
        Button checkWarmLight = (Button) findViewById(R.id.buttonCheckWarmLight);
        Button checkFlash = (Button) findViewById(R.id.buttonCheckFlash);
        Button disconnect = (Button) findViewById(R.id.disconnect);
        isIblazrConnect = (TextView) findViewById(R.id.isiblazr_connect);
        connectIblazr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleManager.findDeviceFromConnectedDevices();
                bleManager.scanLeDevice(true);
            }
        });
        checkIblzazr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIblazr(bleIblazrDevice);
            }
        });
        checkColdLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkColdLight(bleIblazrDevice);
            }
        });
        checkWarmLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkWarmLight(bleIblazrDevice);
            }
        });
        checkFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIblazrFlash(bleIblazrDevice);
            }
        });
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });
    }

    private void checkIblazr(BLEIblazrDevice device) {
        if (device == null)
            return;
        device.light(BLEIblazrDevice.CHECK_LIGHT);
    }

    private void checkWarmLight(BLEIblazrDevice device) {
        if (device == null)
            return;
        bleIblazrDevice.setTemperature(0x00);
        bleIblazrDevice.light(AbstractIBlazrDevice.SHORT_LIGHT);
    }

    private void checkColdLight(BLEIblazrDevice device) {
        if (device == null)
            return;
        bleIblazrDevice.setTemperature(0x7D);
        bleIblazrDevice.light(AbstractIBlazrDevice.SHORT_LIGHT);
    }

    private void checkIblazrFlash(BLEIblazrDevice device) {
        if (device == null)
            return;
        device.setBrightness(0x3F);
        device.setTemperature(0xC);
        device.light(AbstractIBlazrDevice.SHORT_LIGHT);
    }

    private void disconnect() {
        if (gattOfDevice != null && bleIblazrDevice != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isIblazrConnect.setText(NOT_CONNECTED);
                    isIblazrConnect.setTextColor(Color.BLACK);
                }
            });
            gattOfDevice.disconnect();
            gattOfDevice = null;
            bleIblazrDevice = null;
        }
    }

}
