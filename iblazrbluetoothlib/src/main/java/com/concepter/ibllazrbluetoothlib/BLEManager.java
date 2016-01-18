package com.concepter.ibllazrbluetoothlib;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * Created by pavel on 17.01.16.
 */
public class BLEManager {

    /**
     * UUID for
     *
     * @see android.bluetooth.BluetoothGattCharacteristic
     * responsable for changing temperature and brightness of iblazr device
     */
    public static final String WRITABLE_FLASH_CHARACTERISTIC =
            "0000faf1-0000-1000-8000-00805f9b34fb";
    /**
     * UUID for
     *
     * @see android.bluetooth.BluetoothGattCharacteristic
     */
    public static final String READABLE_FLASH_CHARACTERISTIC =
            "0000faf2-0000-1000-8000-00805f9b34fb";

    /**
     * UUID for
     *
     * @see android.bluetooth.BluetoothGattService
     * that contains WRITABLE_FLASH_CHARACTERISTIC and READABLE_FLASH_CHARACTERISTIC
     */
    public static final String FLASH_CHARACTERISTICS_SERVICE =
            "0000fafa-000-1000-8000-00805f9b34fb";
    /**
     * Service for updating iblazr by OTA
     */
    public static final String OTA_UPDATE_SERVICE = "f000ffc0-0451-4000-b000-000000000000";
    /**
     * Service for updating iblazr by OTA with callback
     */
    public static final String OTA_UPDATE_WITH_RESPONSE_CHARACTERISTIC =
            "f000ffc1-0451-4000-b000-000000000000";
    /**
     * Service for updating iblazr by OTA without callback
     */
    public static final String OTA_UPDATE_NO_RESPONSE_CHARACTERISTIC = "f000ffc2-0451-4000-b000-000000000000";
    /**
     * Service for geting battery from iblazr
     */
    public static final String BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb";
    /**
     * Characteristik for get battary
     */
    public static final String BATTARY_CHARACTERISTIC = "00002a19-0000-1000-8000-00805f9b34fb";
    /**
     * Service for geting information from iblazr
     */
    public static final String DEVICE__INFORMATION_SERVICE = "0000180a-0000-1000-8000-00805f9b34fb";
    /**
     * Characteristik for get firmware version
     */
    public static final String FIRMWARE_REVISION_CHARACTERISTIC = "00002a26-0000-1000-8000-00805f9b34fb";
    /**
     * Characteristik for get firmware version
     */
    public static final String HARDWARE_REVISION_CHARACTERISTIC = "00002a27-0000-1000-8000-00805f9b34fb";
    /**
     * Name of device, used for filtering during ble scanning
     */
    public static final String DEVICE_NAME = "iblazr";

    private final static int REQUEST_ENABLE_BT = 1;
    /**
     * BluetoothAdapter for scanning BLE devices
     * Can be obtained from
     *
     * @see android.bluetooth.BluetoothManager
     * More info:
     * @see android.bluetooth.BluetoothAdapter
     */
    /**
     * Stop scan if device wasn't detected in 10 seconds
     */
    private static final long SCAN_PERIOD = 50000;
    private static BLEManager instance;
    private BluetoothAdapter bluetoothAdapter;
    private List<AbstractIBlazrDevice> bleIblazrDevices;
    private boolean isScanning;
    private Activity context;
    private OnIblazrDeviceDiscoverCallback deviceDiscoverCallback;
    private boolean keepLooking;
    private Set<IBlazrConnectionListener> iBlazrConnectionListeners;
    private HashMap<String, BluetoothGattCharacteristic> characteristics;
    /**
     * Scan callback for discovering iblazr devices, its services and characteristics
     */
    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device != null && device.getName() != null &&
                    device.getName().toLowerCase().startsWith(DEVICE_NAME)) {
                if (!containtsBluetoothDevice(device, bleIblazrDevices)) {
                    device.connectGatt(context, false, btg);
                    bluetoothAdapter.stopLeScan(scanCallback);
                }

            }
        }
    };
    private BluetoothGattCallback btg = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_CONNECTING) {

            } else {
                removeFromIblazrDevice(gatt);
                gatt.close();
                if (deviceDiscoverCallback != null)
                    deviceDiscoverCallback.onDeviceGATTConnectionError(newState);
                BLEIblazrDevice.mBusy = true;
            }

            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == 0) {
                BLEIblazrDevice.mBusy = false;
            }
        }

        @Override
        public synchronized void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Queue<BluetoothGattCharacteristic> readableCharact = null;
            BLEIblazrDevice dev = null;
            for (AbstractIBlazrDevice bledevice : bleIblazrDevices) {
                if (((BLEIblazrDevice) bledevice).getGatt().equals(gatt)) {
                    dev = (BLEIblazrDevice) bledevice;
                    readableCharact = dev.getReadableCharacteristic();
                }
            }
            if (dev != null) {
                if (characteristic.equals(characteristics.get(BATTARY_CHARACTERISTIC)))
                    dev.setBatteryLevel(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
                if (characteristic.equals(characteristics.get(FIRMWARE_REVISION_CHARACTERISTIC)))
                    dev.setFirmwareVersion(characteristic.getStringValue(0));
                if (characteristic.equals(characteristics.get(HARDWARE_REVISION_CHARACTERISTIC)))
                    dev.setHardwareVersion(characteristic.getStringValue(0));
            }

            if (readableCharact != null && !readableCharact.isEmpty())
                gatt.readCharacteristic(readableCharact.poll());
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                characteristics = getCharacteristicsFromGatt(gatt);
                BLEIblazrDevice iblazrDevice = new BLEIblazrDevice(gatt,
                        getCharacteristicsFromGatt(gatt));

                if (deviceDiscoverCallback != null)
                    deviceDiscoverCallback.onDeviceDiscovered(iblazrDevice);


            } else {
                if (deviceDiscoverCallback != null)
                    deviceDiscoverCallback.onServicesDiscoverError(status);
            }

            super.onServicesDiscovered(gatt, status);
        }
    };

    private BLEManager() {
    }

    private BLEManager(BluetoothAdapter adapter, Activity context, OnIblazrDeviceDiscoverCallback callback) {
        this.context = context;
        bluetoothAdapter = adapter;
        deviceDiscoverCallback = callback;
        iBlazrConnectionListeners = new HashSet<>();
    }

    public static BLEManager getInstance(BluetoothAdapter adapter, Activity context, OnIblazrDeviceDiscoverCallback callback) {
        if (instance == null) {
            instance = new BLEManager(adapter, context, callback);
        }
        return instance;
    }

    public void addOnIblazrConnectionListener(IBlazrConnectionListener listener) {
        iBlazrConnectionListeners.add(listener);
    }

    public void removeOnIblazrConnectionListener(IBlazrConnectionListener listener) {
        iBlazrConnectionListeners.remove(listener);
    }

    public void addBleIblazrDevice(BLEIblazrDevice device) {
        if (bleIblazrDevices == null) {
            bleIblazrDevices = new ArrayList<>();
            if (!bleIblazrDevices.contains(device)) {
                bleIblazrDevices.add(device);
            } else {
                bleIblazrDevices.set(bleIblazrDevices.indexOf(device), device);
            }

        } else {
            if (!bleIblazrDevices.contains(device)) {
                bleIblazrDevices.add(device);
            }
        }
        notifyIblazrConnected(device);
    }

    public void removeFromIblazrDevice(BluetoothGatt gatt) {
        if (bleIblazrDevices == null || gatt == null || bleIblazrDevices.isEmpty())
            return;
        for (Iterator<AbstractIBlazrDevice> iterator = bleIblazrDevices.listIterator(); iterator.hasNext(); ) {
            BLEIblazrDevice deviceFromlist = (BLEIblazrDevice) iterator.next();
            if (gatt.equals(deviceFromlist.getGatt())) {
                iterator.remove();
                notifyIblazrRemoved(deviceFromlist);
            }
        }
    }

    private void notifyIblazrConnected(BLEIblazrDevice device) {
        for (IBlazrConnectionListener listener : iBlazrConnectionListeners) {
            if (listener != null)
                listener.notifyIblazrConnected(device);
        }
    }

    private void notifyIblazrRemoved(BLEIblazrDevice deviceFromlist) {
        for (IBlazrConnectionListener listener : iBlazrConnectionListeners) {
            if (listener != null)
                listener.notifyIblazrRemoved(deviceFromlist);
        }
    }

    /**
     * Method for scanning iblazr devices nearby
     *
     * @param enable -value true starts scanning
     */
    public void scanLeDevice(final boolean enable) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }
        if (enable && !isScanning) {

            // Stops scanning after a pre-defined scan period.
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    bluetoothAdapter.stopLeScan(scanCallback);
                }
            }, SCAN_PERIOD);

            isScanning = true;
            bluetoothAdapter.startLeScan(scanCallback);

        } else {
            isScanning = false;
        }
    }


    private HashMap<String, BluetoothGattCharacteristic> getCharacteristicsFromGatt(BluetoothGatt gatt) {
        HashMap<String, BluetoothGattCharacteristic> characteristics =
                new HashMap<>(8);
        BluetoothGattService service =
                gatt.getService(UUID.fromString
                        (BLEManager.FLASH_CHARACTERISTICS_SERVICE));
        characteristics.put(BLEManager.WRITABLE_FLASH_CHARACTERISTIC,
                service.getCharacteristic(
                        UUID.fromString(BLEManager.WRITABLE_FLASH_CHARACTERISTIC)));
        characteristics.put(BLEManager.READABLE_FLASH_CHARACTERISTIC,
                service.getCharacteristic(
                        UUID.fromString(BLEManager.READABLE_FLASH_CHARACTERISTIC)));
        service = gatt.getService(UUID.fromString
                (BLEManager.OTA_UPDATE_SERVICE));
        characteristics.put(BLEManager.OTA_UPDATE_NO_RESPONSE_CHARACTERISTIC,
                service.getCharacteristic(
                        UUID.fromString(BLEManager.OTA_UPDATE_NO_RESPONSE_CHARACTERISTIC)));
        characteristics.put(BLEManager.OTA_UPDATE_WITH_RESPONSE_CHARACTERISTIC,
                service.getCharacteristic(
                        UUID.fromString(BLEManager.OTA_UPDATE_WITH_RESPONSE_CHARACTERISTIC)));
        service = gatt.getService(UUID.fromString
                (BLEManager.BATTERY_SERVICE));
        characteristics.put(BLEManager.BATTARY_CHARACTERISTIC,
                service.getCharacteristic(
                        UUID.fromString(BLEManager.BATTARY_CHARACTERISTIC)));
        service = gatt.getService(UUID.fromString
                (BLEManager.DEVICE__INFORMATION_SERVICE));
        characteristics.put(BLEManager.FIRMWARE_REVISION_CHARACTERISTIC,
                service.getCharacteristic(
                        UUID.fromString(BLEManager.FIRMWARE_REVISION_CHARACTERISTIC)));
        characteristics.put(BLEManager.HARDWARE_REVISION_CHARACTERISTIC,
                service.getCharacteristic(
                        UUID.fromString(BLEManager.HARDWARE_REVISION_CHARACTERISTIC)));
        return characteristics;
    }

    public List<AbstractIBlazrDevice> getListWithDevices() {
        if (bleIblazrDevices != null) {
            return bleIblazrDevices;
        }
        return null;
    }

    public void findDeviceFromConnectedDevices() {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        keepLooking = true;
        for (BluetoothDevice device : devices) {
            if (device != null && device.getName() != null && keepLooking &&
                     device.getName().toLowerCase().startsWith(DEVICE_NAME)) {
                device.connectGatt(context, false, btg);
            }
        }

    }

    private boolean containtsBluetoothDevice(BluetoothDevice bluetoothDevice, List<AbstractIBlazrDevice> devices) {
        boolean b = false;
        Set<BluetoothDevice> bluetoothDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (bluetoothDevices.contains(bluetoothDevice))
            b = true;
        return b;
    }

    public static void destroyBLEManager() {
        instance = null;
    }
}
