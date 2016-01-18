package com.concepter.ibllazrbluetoothlib;

/**
 * Created by pavel on 17.01.16.
 */
public interface IBlazrConnectionListener {
    public void notifyIblazrRemoved(BLEIblazrDevice device);
    public void notifyIblazrConnected(BLEIblazrDevice device);
}
