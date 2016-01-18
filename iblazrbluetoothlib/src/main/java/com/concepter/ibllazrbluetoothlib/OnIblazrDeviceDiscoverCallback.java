package com.concepter.ibllazrbluetoothlib;

/**
 * Created by pavel on 17.01.16.
 */
public interface OnIblazrDeviceDiscoverCallback {

    public void onDeviceDiscovered(BLEIblazrDevice device);

    public void onDeviceGATTConnectionError(int status);

    public void onServicesDiscoverError(int status);

}
