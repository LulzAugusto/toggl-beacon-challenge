package com.toggl.challenge;

import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.manager.ProximityManager;

import java.util.ArrayList;

/**
 * Created by andrin on 21/04/16.
 */
public class BeaconEmitter implements ProximityManager.ProximityListener {

    private ArrayList<BeaconListener> eventHandlers = new ArrayList();

    public BeaconEmitter(BeaconListener handler) {
        this.eventHandlers.add(handler);
    }

    @Override
    public void onEvent(BluetoothDeviceEvent bluetoothDeviceEvent) {
        for (int i = 0; i < this.eventHandlers.size() ; i++) {
            this.eventHandlers.get(i).onEvent(bluetoothDeviceEvent);
        }
    }

    @Override
    public void onScanStart() {
//        Log.d(TAG, "scan started");
        for (int i = 0; i < this.eventHandlers.size() ; i++) {
            this.eventHandlers.get(i).onScanStart();
        }
    }

    @Override
    public void onScanStop() {
//        Log.d(TAG, "scan stopped");
        for (int i = 0; i < this.eventHandlers.size() ; i++) {
            this.eventHandlers.get(i).onScanStop();
        }
    }
}
