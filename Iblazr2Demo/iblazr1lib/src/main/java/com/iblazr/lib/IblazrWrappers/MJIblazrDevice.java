package com.iblazr.lib.IblazrWrappers;

import android.content.Context;
import android.widget.Toast;

import com.iblazr.lib.constantlight.GeneratorConstThread;

/**
 * Created by pavel on 09.03.16.
 */
public class MJIblazrDevice implements AbstractIblazrDevice {
    private int brightness = 2000;
    private GeneratorConstThread generator;
    private Context context;

    public MJIblazrDevice(Context context) {
        this.context = context;
        this.brightness = 15750;
    }

    public void stopGenerate() {
        if (generator != null && generator.isRunning()) {
            generator.finish();
            generator = null;
        }
    }

    public void stopPlaying() {
        if (generator != null && generator.isRunning()) {
            generator.finish();
        }
    }

    public void setConstLight(final int frequency) {
        if (generator == null)
            generator = new GeneratorConstThread(context, frequency);
        generator.setFrequency(frequency);
        this.brightness = frequency;
    }

    @Override
    public void light(int timeMode) {
        setConstLight(this.brightness);
    }

    @Override
    public void setBrightnessNoLight(int value) {
        this.brightness = value;
    }

    @Override
    public int getBrightness() {
        return brightness;
    }

    @Override
    public void setBrightness(int value, int lightMode) {
        setConstLight(value);
    }

    @Override
    public void stop() {
        if (generator != null) {
            generator.setFrequency(500);
        }
        if (generator != null && generator.isRunning()) {
            generator.finish();
            generator.interrupt();
            generator = null;
        }
    }

    @Override
    public void startLongLight() {
        light(SHORT_LIGHT);
    }

    @Override
    public void stopLongLight() {
        stop();
    }

    @Override
    public int getTemperature() {
        return 0;
    }

    @Override
    public void setTemperature(int value, int lightMode) {
        Toast.makeText(context, "This iblazr doesn't support temperature settings",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return false;
    }
}
