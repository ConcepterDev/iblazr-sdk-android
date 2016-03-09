package com.concepter.ibllazrbluetoothlib;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by pavel on 17.01.16.
 */
public class BLEIblazrDevice implements AbstractIBlazrDevice {

    public static volatile boolean mBusy = false; // Write/read pending response
    private static final byte LONG_LIGHT_CHARACTERISTIC = 0x16;
    private static final byte SHORT_LIGHT_CHARACTERISTIC = 0x10;
    private static final byte CHECK_LIGHT_CHARACTERISTIC = 0x11;
    private Queue<BluetoothGattCharacteristic> readableCharacteristic;
    private int batteryLevel = 100;
    private String firmwareVersion = "";
    private String hardwareVersion = "";
    private boolean sendingValues = false;
    private byte functionInWritableCharacteristic = 0x10;



    /**
     * Reference to gatt server on device
     */
    private BluetoothGatt gatt;
    /**
     * Current temperature value
     */
    private byte temperature = 0x10;
    /**
     * Current brightness value
     */
    private byte brightness = 0x10;
    /**
     * Map of characteristics, needed to update temperature, brightness and firmware
     * Keys for characteristics (UUIDs of them is used as keys):
     *
     * @see BLEManager
     */
    private HashMap<String, BluetoothGattCharacteristic> characteristics;

    public BLEIblazrDevice(BluetoothGatt gatt,
                           HashMap<String, BluetoothGattCharacteristic> characteristics) {
        this.gatt = gatt;
        this.characteristics = characteristics;
        getReadableCharactValues();
    }

    /**
     * Set brightness to device and light flash
     *
     * @param value
     */



    /**
     * Set brightness to device without lighting flash
     *
     * @param value
     */
    @Override
    public void setBrightnessNoLight(int value) {
        this.brightness = (byte) value;
    }

    /**
     * Send byte array of flash characters to device
     *
     * @param values is the byte array for desired characters of flash
     * @throws NullPointerException
     */
    private void sendValuesToGATT(byte[] values) throws NullPointerException {
        if (!sendingValues || values[0] == LONG_LIGHT_CHARACTERISTIC) {
            setSendingValues(true);
            BluetoothGattCharacteristic writableCharacteristic = characteristics.get
                    (BLEManager.WRITABLE_FLASH_CHARACTERISTIC);

            if (writableCharacteristic != null) {
                if (((writableCharacteristic.getProperties()
                        & BluetoothGattCharacteristic.PROPERTY_WRITE) |
                        (writableCharacteristic.getProperties()
                                & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0) {

                    // writing characteristic functions
                    writableCharacteristic.setValue(values);
                    gatt.writeCharacteristic(writableCharacteristic);
                }
            } else {
                throw new NullPointerException("Characteristic for writing is null");
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!(values[0] == 22))
                setSendingValues(false);
        }
    }

    /**
     * Set temperature to flash and light flash
     *
     * @param value - value of desired temperature
     */
    @Override
    public void setTemperature(int value, int lightMode) {
        this.temperature = (byte) value;
        light(lightMode);
    }

    @Override
    public int getBrightness() {
        return brightness;
    }

    @Override
    public void setBrightness(int value, int lightMode) {
        this.brightness = (byte) value;
        light(lightMode);
    }

    @Override
    public int getTemperature() {
        return temperature;
    }

    /**
     * Light flash with current characteristics
     *
     * @throws NullPointerException
     */
    @Override
    public void light(int timeMode) throws NullPointerException {
        switch (timeMode) {
            case SHORT_LIGHT:
                functionInWritableCharacteristic = SHORT_LIGHT_CHARACTERISTIC;
                sendValuesToGATT(new byte[]{functionInWritableCharacteristic, 0x00, 0x79, temperature, brightness});
                break;
            case LONG_LIGHT:
                functionInWritableCharacteristic = LONG_LIGHT_CHARACTERISTIC;
                sendValuesToGATT(new byte[]{functionInWritableCharacteristic, 0x02, 0, temperature, brightness});
                break;
            case CHECK_LIGHT:
                functionInWritableCharacteristic = CHECK_LIGHT_CHARACTERISTIC;
                sendValuesToGATT(new byte[]{CHECK_LIGHT_CHARACTERISTIC, 0x00, 0x78, 0x3E, 0x08});
                break;
        }
    }

    public void showFlash() {
        sendValuesToGATT(new byte[]{0x11, 0x00, 0x78, 0x3E, 0x08});
    }

    /**
     * Stop lighting
     *
     * @throws NullPointerException
     */
    @Override
    public void stop() throws NullPointerException {
        stopLongLight();
    }

    @Override
    public void startLongLight() {
        sendValuesToGATT(new byte[]{LONG_LIGHT_CHARACTERISTIC, 0x00, 0x00, temperature, brightness});
    }

    @Override
    public void stopLongLight() {
        setSendingValues(false);
        sendValuesToGATT(new byte[]{SHORT_LIGHT_CHARACTERISTIC, 0x00, 0x00, temperature, brightness});
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setSendingValues(boolean sendingValues) {
        this.sendingValues = sendingValues;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }

    private boolean getReadableCharactValues() {
        Comparator<BluetoothGattCharacteristic> comparator = new Comparator<BluetoothGattCharacteristic>() {

            @Override
            public int compare(BluetoothGattCharacteristic lhs, BluetoothGattCharacteristic rhs) {
                if (lhs.hashCode() < rhs.hashCode())
                    return 1;
                if (rhs.hashCode() < lhs.hashCode())
                    return -1;
                return 0;
            }

        };
        readableCharacteristic = new PriorityQueue<>(10, comparator);
        BluetoothGattCharacteristic batteryCharacteristic = characteristics.get(BLEManager.BATTARY_CHARACTERISTIC);
        BluetoothGattCharacteristic firmwareCharacteristic = characteristics.get(BLEManager.FIRMWARE_REVISION_CHARACTERISTIC);
        BluetoothGattCharacteristic hardwareCharacteristic = characteristics.get(BLEManager.HARDWARE_REVISION_CHARACTERISTIC);
        readableCharacteristic.add(firmwareCharacteristic);
        readableCharacteristic.add(hardwareCharacteristic);
        gatt.readCharacteristic(batteryCharacteristic);
        return true;
    }

    public Queue<BluetoothGattCharacteristic> getReadableCharacteristic() {
        return readableCharacteristic;
    }

    @Override
    public int hashCode() {
        return gatt.getDevice().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BLEIblazrDevice)) return false;

        BLEIblazrDevice that = (BLEIblazrDevice) o;
        return !(gatt != null ? !gatt.getDevice().equals(that.gatt.getDevice()) : that.gatt != null);

    }

}
