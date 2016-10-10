/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 *
 * This is the activity created when the app boots up
 */
public class DeviceScanActivity extends ListActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;  // 10 seconds.
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean isScanning;
    private Handler mHandler;

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.title_devices);

        // When you create a new Handler, it is bound to the thread / message
        // queue of the thread that is creating it. There are two main uses
        // for a Handler: (1) to schedule messages and runnables to be executed
        // as some point in the future; and (2) to enqueue an action to be
        // performed on a different thread than your own.
        mHandler = new Handler();

        // Visually, this makes the app seem to shutdown before launching if the device
        // does not have bluetooth.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            // Call this when your activity is done and should be closed.
            // Since this activity has no parent (see AndroidManifest.xml),
            // we exit the app and return back to the Android Desktop.
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            // Call this when your activity is done and should be closed.
            finish();
        }
    }

    /**
     * I think this get's called whether app is first booting or rebooting
     */
    @Override
    protected void onResume() {
        super.onResume();

        // We know bluetooth EXISTS on the device. However, if Bluetooth is
        // not currently ENABLED, display a dialog asking the user to grant
        // permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        /* I guess we end up here if the user clicked "enable"? */

        // Initialize list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter(getLayoutInflater());
        setListAdapter(mLeDeviceListAdapter);
        startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
        mLeDeviceListAdapter.clear();
    }

    // This is NORMALLY only called once, the first time the options menu is displayed.
    // However, we call "invalidateOptionsMenu()" below, so this does get re-used.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // A `MenuInflater` is used to instantiate menu XML files into Menu objects.
        getMenuInflater().inflate(R.menu.main, menu);
        if (!isScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        // if you return false the menu will not be shown.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                startScan();
                break;
            case R.id.menu_stop:
                stopScan();
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startScan() {
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, SCAN_PERIOD);

        isScanning = true;
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        invalidateOptionsMenu();
    }

    private void stopScan() {
        isScanning = false;
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        invalidateOptionsMenu();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        final Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        if (isScanning) stopScan();
        // You will not receive any info when the activity exits.
        startActivity(intent);
    }

}