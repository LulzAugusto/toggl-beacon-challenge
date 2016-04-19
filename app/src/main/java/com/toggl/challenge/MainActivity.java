package com.toggl.challenge;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.configuration.scan.EddystoneScanContext;
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
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ProximityManager.ProximityListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ProximityManagerContract proximityManager;
    private ScanContext scanContext;

    private TextView trackingTextView;
    private TextView t1;
    private TextView t2;
    private TextView t3;

    private String trackingText = "";

    private static final String BATHROOM = "0AvC";
    private static final String KITCHEN = "3xDF";
    private static final String LIVINGROOM = "jGeF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        KontaktSDK.initialize("BbYSubZhSaaJMAogJNQwRGlykAkpClCZ");
        proximityManager = new KontaktProximityManager(this);

        t1 = (TextView) findViewById(R.id.text1);
        t2 = (TextView) findViewById(R.id.text2);
        t3 = (TextView) findViewById(R.id.text3);
        trackingTextView = (TextView) findViewById(R.id.tracking);
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

        if (deviceList.size() > 0 && bluetoothDeviceEvent.getEventType() == EventType.DEVICES_UPDATE) {
            for (Iterator<? extends RemoteBluetoothDevice> i = deviceList.iterator(); i.hasNext();) {
                RemoteBluetoothDevice device = i.next();
                TextView t = getTextViewForDeviceId(device.getUniqueId());

                if (t != null) {
                    double distance = device.getDistance() * 10;
                    t.setText(device.getUniqueId() + " " + String.valueOf(distance));
                    if (distance < 10) {
                        switch (device.getUniqueId()) {
                            case BATHROOM:
                                trackingText = "Tracking showe";
                                break;
                            case KITCHEN:
                                trackingText = "Tracking cooking";
                                break;
                            case LIVINGROOM:
                                trackingText = "Tracking relaxing";
                                break;
                        }
                    }
                }
            }

            if (trackingText.length() == 0) {
                trackingText = "";
            }

            if (trackingText.length() > 0) {
                trackingTextView.setText(trackingText);
            } else {
                trackingTextView.setText("Tracking something else");
            }
        }
    }

    @Override
    public void onScanStart() {
        Log.d(TAG, "scan started");
    }

    @Override
    public void onScanStop() {
        Log.d(TAG, "scan stopped");

    }


    private ScanContext getScanContext() {
        List<IBeaconUniqueIdFilter> filters = Arrays.asList(
                IBeaconFilters.newUniqueIdFilter("3xDF"),
                IBeaconFilters.newUniqueIdFilter("0AvC"),
                IBeaconFilters.newUniqueIdFilter("jGeF")
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

    private TextView getTextViewForDeviceId (String id) {
        TextView t;
        switch (id) {
            case BATHROOM:
                t = t1;
                break;
            case KITCHEN:
                t = t2;
                break;
            case LIVINGROOM:
                t = t3;
                break;
            default:
                t = null;
                break;
        }

        return t;
    }
}
