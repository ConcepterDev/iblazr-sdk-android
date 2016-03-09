package com.concepter.ibllazrbluetoothlib;

/**
 * Created by pavel on 17.01.16.
 */
public interface OnIblazrDeviceDiscoverCallback {

    void onDeviceDiscovered(BLEIblazrDevice device);

    void onDeviceGATTConnectionError(int status);

    void onServicesDiscoverError(int status);

}
