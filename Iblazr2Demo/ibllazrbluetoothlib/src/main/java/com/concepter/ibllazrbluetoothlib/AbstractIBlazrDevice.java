package com.concepter.ibllazrbluetoothlib;

/**
 * Created by pavel on 17.01.16.
 */
public interface AbstractIBlazrDevice {
    final public static int SHORT_LIGHT = 0;
    final public static int LONG_LIGHT = 1;
    final public static int CHECK_LIGHT = 2;
    public void setBrightness(int value);

    public void setBrightnessNoLight(int value);
    public void setTemperature (int value);

    public int getBrightness();
    public int getTemperature ();
    public void light(int timeMode);
    public void stop();
    public void startLongLight();
    public void stopLongLight();
}
