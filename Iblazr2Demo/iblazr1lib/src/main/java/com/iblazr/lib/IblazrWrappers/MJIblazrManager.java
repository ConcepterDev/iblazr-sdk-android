package com.iblazr.lib.IblazrWrappers;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.iblazr.lib.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pavel on 09.03.16.
 */
public class MJIblazrManager extends BroadcastReceiver {
    private static MJIblazrManager instance;
    public boolean isMJIblazrTurned;
    List<AbstractIblazrDevice> devices;
    private MJIblazrDevice device;
    private boolean creatingDialog = false;
    private List<MjIblazrConnectionListener> listWithListeners;

    private MJIblazrManager() {
        listWithListeners = new ArrayList<MjIblazrConnectionListener>();
    }

    public static MJIblazrManager getInstance() {
        if (instance == null) {
            instance = new MJIblazrManager();
        }
        return instance;
    }

    public MJIblazrDevice getDevice() {
        return device;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.hasExtra("state")) {
            if (isMJIblazrTurned && intent.getIntExtra("state", 0) == 0) {
                isMJIblazrTurned = false;
                if (device != null) {
                    removeIblazr();
                }
                Toast.makeText(context, "Iblazr disconnected", Toast.LENGTH_SHORT).show();
            } else if (!isMJIblazrTurned && intent.getIntExtra("state", 0) == 1 && !creatingDialog) {
                creatingDialog = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater li = LayoutInflater.from(context);
                View theView = li.inflate(R.layout.alert_dialog_iblazr1_check, null);
                builder.setView(theView);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        isMJIblazrTurned = true;
                        if (device == null) {
                            device = new MJIblazrDevice(context);
                            device.light(AbstractIblazrDevice.SHORT_LIGHT);
                            device.stopGenerate();
                            notifyListenersMjIblazrConnected();
                            creatingDialog = false;
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        creatingDialog = false;
                    }
                });
                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                Toast.makeText(context, "Iblazr connected", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void addMjIblazrConnectionListener(MjIblazrConnectionListener listener) {
        if (listener != null)
            listWithListeners.add(listener);
    }

    public void removeMjIblazrConnection(MjIblazrConnectionListener listener) {
        if (listWithListeners != null && listWithListeners.contains(listener))
            listWithListeners.remove(listener);
    }

    public void notifyListenersMjIblazrConnected(){
        for (MjIblazrConnectionListener listener:listWithListeners){
            listener.notifyIblazrConnected();
        }
    }

    public void notifyListenersMjIblazrDisconnected(){
        for (MjIblazrConnectionListener listener:listWithListeners){
            listener.notifyIblazrDisconnected();
        }
    }

    public void removeIblazr() {
        device.stopPlaying();
        notifyListenersMjIblazrDisconnected();
        devices = null;
        device = null;
        isMJIblazrTurned = false;
    }


    @Override
    public int hashCode() {
        return (isMJIblazrTurned ? 1 : 0);
    }

    public List<AbstractIblazrDevice> getListWithDevices() {
        if (device != null) {
            devices = new ArrayList<AbstractIblazrDevice>();
            devices.add(device);
            return devices;
        }
        return null;
    }
}
