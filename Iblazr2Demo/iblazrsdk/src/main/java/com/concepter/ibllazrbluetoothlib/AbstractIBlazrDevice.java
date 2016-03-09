package com.concepter.ibllazrbluetoothlib;

/**
 * Created by pavel on 17.01.16.
 */
public interface AbstractIBlazrDevice {

    final public static int SHORT_LIGHT = 0;
    final public static int LONG_LIGHT = 1;
    final public static int CHECK_LIGHT = 2;

    void setBrightnessNoLight(int value);

    int getBrightness();

    void setBrightness(int value, int lightMode);

    int getTemperature();

    void setTemperature(int value, int lightMode);

    void light(int timeMode);

    void stop();

    void startLongLight();

    void stopLongLight();

}
