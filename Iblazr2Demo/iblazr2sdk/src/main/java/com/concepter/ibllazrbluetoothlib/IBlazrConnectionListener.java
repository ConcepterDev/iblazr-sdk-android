package com.concepter.ibllazrbluetoothlib;

/**
 * Created by pavel on 17.01.16.
 */
public interface IBlazrConnectionListener {

    void notifyIblazrRemoved(BLEIblazrDevice device);

    void notifyIblazrConnected(BLEIblazrDevice device);

}
