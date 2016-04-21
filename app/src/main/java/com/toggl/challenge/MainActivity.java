package com.toggl.challenge;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.internal.util.Predicate;
import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.EventType;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilters;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconUniqueIdFilter;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerContract;
import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.Proximity;
import com.kontakt.sdk.android.common.profile.DeviceProfile;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;
import com.kontakt.sdk.android.manager.KontaktProximityManager;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ProximityManager.ProximityListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ProximityManagerContract proximityManager;
    private ScanContext scanContext;

    private boolean startDeviceDetected = false;
    private boolean timerRunnning = false;
    private static final String START = "0AvC";
    private static final String FINISH = "3xDF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        KontaktSDK.initialize("BbYSubZhSaaJMAogJNQwRGlykAkpClCZ");
        proximityManager = new KontaktProximityManager(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        proximityManager.initializeScan(getScanContext(), new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.attachListener(MainActivity.this);
            }

            @Override
            public void onConnectionFailure() {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        proximityManager.detachListener(this);
        proximityManager.disconnect();
    }

    @Override
    public void onEvent(BluetoothDeviceEvent bluetoothDeviceEvent) {
        List<? extends RemoteBluetoothDevice> deviceList = bluetoothDeviceEvent.getDeviceList();

        if (bluetoothDeviceEvent.getEventType() == EventType.DEVICE_DISCOVERED &&
                !startDeviceDetected &&
                !timerRunnning) {
            RemoteBluetoothDevice device = getDeviceById(deviceList, START);

            if (device != null) {
                startDeviceDetected = true;
                Log.d(TAG, "start detected");
            }
        }

        if (bluetoothDeviceEvent.getEventType() == EventType.DEVICES_UPDATE) {
            RemoteBluetoothDevice startDevice = getDeviceById(deviceList, START);
            RemoteBluetoothDevice finishDevice = getDeviceById(deviceList, FINISH);

            if (!timerRunnning &&
                    startDeviceDetected &&
                    (startDevice != null) &&
                    (startDevice.getProximity() == Proximity.FAR)) {
                // START TIMER
                new TimerService().execute("");
                timerRunnning = true;
                Log.d(TAG, "start timer");
            }


            if (timerRunnning &&
                    (finishDevice != null) &&
                    (finishDevice.getProximity() == Proximity.IMMEDIATE)) {
                Log.d(TAG, "stop timer");
                timerRunnning = false;
            }
        }
    }

    @Override
    public void onScanStart() {
//        Log.d(TAG, "scan started");
    }

    @Override
    public void onScanStop() {
//        Log.d(TAG, "scan stopped");
    }

    private ScanContext getScanContext() {
        List<IBeaconUniqueIdFilter> filters = Arrays.asList(
                IBeaconFilters.newUniqueIdFilter("3xDF"),
                IBeaconFilters.newUniqueIdFilter("0AvC")
        );

        IBeaconScanContext ibeaconScanContext = new IBeaconScanContext.Builder()
                .setIBeaconFilters(filters)
                .setDevicesUpdateCallbackInterval(1000)
                .build();
        if (scanContext == null) {
            scanContext = new ScanContext.Builder()
                    .setScanPeriod(ScanPeriod.RANGING) // or for monitoring for 15 seconds scan and 10 seconds waiting:
                    //.setScanPeriod(new ScanPeriod(TimeUnit.SECONDS.toMillis(15), TimeUnit.SECONDS.toMillis(10)))
                    .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                    .setActivityCheckConfiguration(ActivityCheckConfiguration.MINIMAL)
                    .setForceScanConfiguration(ForceScanConfiguration.MINIMAL)
                    .setIBeaconScanContext(ibeaconScanContext)
                    .build();
        }
        return scanContext;
    }

    RemoteBluetoothDevice getDeviceById (List<? extends RemoteBluetoothDevice> list, String id) {
        RemoteBluetoothDevice result = null;

        for (RemoteBluetoothDevice device : list) {
            if (device.getUniqueId().equals(id)) {
                result = device;
            }
        }

        return result;
    }
}
