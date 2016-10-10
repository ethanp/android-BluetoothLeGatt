package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

// Adapter for holding devices found through scanning.
class LeDeviceListAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> mLeDevices;

    // The layout inflater is not some custom subclass, it just get's passed-in
    // because it has access to the UI Context
    private LayoutInflater mInflater;

    LeDeviceListAdapter(LayoutInflater inflater) {
        super();
        mLeDevices = new ArrayList<>();
        mInflater = inflater;
    }

    void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    // Get a View that displays the data at the specified position in the data set.
    // You can either create a View manually or inflate it from an XML layout file.
    // If `view` passed in is non-null, it is an old view that we can reuse (we do).
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        DeviceAttrs deviceAttrs;
        // General ListView optimization code.
        if (view == null) {
            // I'm not sure why we don't need to pass in a parent view
            view = mInflater.inflate(R.layout.listitem_device, null);
            deviceAttrs = new DeviceAttrs();
            deviceAttrs.name = (TextView) view.findViewById(R.id.device_name);
            deviceAttrs.addr = (TextView) view.findViewById(R.id.device_address);

            // A tag can be used to mark a view in its hierarchy and does not
            // have to be unique within the hierarchy. Tags can also be used
            // to store data within a view without resorting to another data
            // structure.
            view.setTag(deviceAttrs);
        } else {
            deviceAttrs = (DeviceAttrs) view.getTag();
        }
        BluetoothDevice device = mLeDevices.get(i);
        final String deviceName = device.getName();
        boolean hasName = deviceName != null && !deviceName.isEmpty();
        if (hasName) {
            deviceAttrs.name.setText(deviceName);
        } else {
            deviceAttrs.name.setText(R.string.unknown_device);
        }
        deviceAttrs.addr.setText(device.getAddress());
        return view;
    }

    private static class DeviceAttrs {
        TextView name;
        TextView addr;
    }
}
