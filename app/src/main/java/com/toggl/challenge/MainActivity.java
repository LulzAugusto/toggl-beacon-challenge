package com.toggl.challenge;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;
import com.kontakt.sdk.android.manager.KontaktProximityManager;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements ProximityManager.ProximityListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ProximityManagerContract proximityManager;
    private ScanContext scanContext;

    private String timeEntryID = "";
    private boolean startDeviceDetected = false;
    private boolean timerRunnning = false;
    private static final String START = "jGeF";
    private static final String FINISH = "3xDF";
    private Timer localTimer = null;
    public Date timerStart = null;
    private BeaconEmitter beaconEmitter = new BeaconEmitter(new BeaconListener() {
        @Override
        public void onScanStart() {
            onBeaconScanStart();
        }

        @Override
        public void onScanStop() {
            onBeaconScanStop();
        }
        @Override
        public void onEvent(BluetoothDeviceEvent bluetoothDeviceEvent) {
            onBeaconEvent(bluetoothDeviceEvent);
        }
    });

    @Override
    public void onScanStart() { // Proxy to beaconEmitter
        Log.d(TAG, "Proxy scan start");
        this.beaconEmitter.onScanStart();
    }

    @Override
    public void onScanStop() { // Proxy to beaconEmitter
        Log.d(TAG, "Proxy scan stop");
        this.beaconEmitter.onScanStop();
    }

    @Override
    public void onEvent(BluetoothDeviceEvent bluetoothDeviceEvent) { // Proxy to beaconEmitter
        Log.d(TAG, "Proxy scan event");
        this.beaconEmitter.onEvent(bluetoothDeviceEvent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        KontaktSDK.initialize("BbYSubZhSaaJMAogJNQwRGlykAkpClCZ");
        proximityManager = new KontaktProximityManager(this);
    }

    public void onClickTest(View view) {
        if(this.localTimer == null) {
            this.swapTrackingImage(true);
            this.startLocalTimer();
        } else {
            this.swapTrackingImage(false);
            this.stopLocalTimer();
        }
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

    public void onBeaconEvent(BluetoothDeviceEvent bluetoothDeviceEvent) {
        Log.d("TEID", timeEntryID != null ? timeEntryID : "nothing to see here");
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

            if (startDevice != null) {
                Log.d("DISTANCE Start", String.valueOf(startDevice.getDistance()) + " / " + startDevice.getProximity().name());
            }

            if (finishDevice != null) {
                Log.d("DISTANCE Finish", String.valueOf(finishDevice.getDistance()) + " / " + finishDevice.getProximity().name());
            }

            if (!timerRunnning &&
                    startDeviceDetected &&
                    (startDevice != null) &&
                    (startDevice.getDistance() >= 2)) {
                // START TIMER
                new StartTimerTask().execute(this);
                timerRunnning = true;
                Log.d(TAG, "start timer");
            }


            if (timerRunnning &&
                    (finishDevice != null) &&
                    (finishDevice.getDistance() <= 0.5)) {
                // STOP TIMER
                if (timeEntryID.length() > 0) {
                    Log.d(TAG, "stop timer");
                    new StopTimerTask().execute(timeEntryID);
                    timerRunnning = false;
                    startDeviceDetected = false;
                }

            }
        }
    }

    public void onBeaconScanStart() {
//        Log.d(TAG, "scan started");
    }

    public void onBeaconScanStop() {
//        Log.d(TAG, "scan stopped");
    }

    private ScanContext getScanContext() {
        List<IBeaconUniqueIdFilter> filters = Arrays.asList(
                IBeaconFilters.newUniqueIdFilter(START),
                IBeaconFilters.newUniqueIdFilter(FINISH)
        );

        IBeaconScanContext ibeaconScanContext = new IBeaconScanContext.Builder()
                .setIBeaconFilters(filters)
                .setDevicesUpdateCallbackInterval(1000)
                .build();
        if (scanContext == null) {
            scanContext = new ScanContext.Builder()
                    .setScanPeriod(ScanPeriod.RANGING) // or for monitoring for 15 seconds scan and 10 seconds waiting:
//                    .setScanPeriod(new ScanPeriod(TimeUnit.SECONDS.toMillis(3), TimeUnit.SECONDS.toMillis(2)))
                    .setScanMode(ProximityManager.SCAN_MODE_LOW_LATENCY)
                    .setActivityCheckConfiguration(ActivityCheckConfiguration.MINIMAL)
                    .setForceScanConfiguration(ForceScanConfiguration.MINIMAL)
                    .setIBeaconScanContext(ibeaconScanContext)
                    .build();
        }
        return scanContext;
    }

    private RemoteBluetoothDevice getDeviceById (List<? extends RemoteBluetoothDevice> list, String id) {
        RemoteBluetoothDevice result = null;

        for (RemoteBluetoothDevice device : list) {
            if (device.getUniqueId().equals(id)) {
                result = device;
            }
        }

        return result;
    }

    public void setTimeEntryID (String id) {
        timeEntryID = id;
    }

    protected void startLocalTimer() {
        this.timerStart = new Date();
        if(this.localTimer != null) {
            this.stopLocalTimer();
        }
        this.localTimer = new Timer();
        this.localTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timerTickProxy();
            }
        }, 0, 40);
    }

    private void stopLocalTimer() {
        if(this.localTimer != null) {
            this.localTimer.cancel();
            this.localTimer = null;
        }
    }

    private void timerTickProxy() {
        this.runOnUiThread(TimerTick);
    }

    private Runnable TimerTick = new Runnable() {
        public void run() {
            TextView view = (TextView) findViewById(R.id.text_duration);
            TextView view2 = (TextView) findViewById(R.id.text_duration2);
            long duration = new Date().getTime() - MainActivity.this.timerStart.getTime();
            int minutes =  (int) Math.floor(duration / 1000d / 60d);
            int seconds = (int) Math.floor(duration / 1000d);
            int milliseconds = (int) ((duration - (minutes * 1000d * 60d) - (seconds * 1000d)) / 10d);
            String text = (minutes > 0d ? String.format("%02d", minutes) + ":" : "")
                    + String.format("%02d", seconds) + ":"
                    + String.format("%02d", milliseconds);
            view.setText(text);
            view2.setText(text);
        }
    };

    String getDescription() {
        EditText textView = (EditText) findViewById(R.id.input_description);
        return textView.getText().toString();
    }

    void swapTrackingImage(Boolean isTracking) {
        ImageView imageView = (ImageView) findViewById(R.id.background_logo);
        imageView.setImageResource(isTracking ? R.drawable.toggl_logo : R.drawable.toggl_logo_dark);
    }
}
