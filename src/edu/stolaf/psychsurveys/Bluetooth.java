package edu.stolaf.psychsurveys;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//TODO: don't connect to devices that are already peers
public class Bluetooth extends BroadcastReceiver implements Measurement {

	public static BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
	int phones;
	
	@Override
	public void start() {
		phones = 0;
		if (ba != null) {			
			if (ba.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
				Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Measurer.context.startActivity(intent);
			}
			if (!ba.startDiscovery())
				ba.enable();
		}		
	}

	@Override
	public String stop() {
		if (ba == null)
			return "BLUETOOTH: Device does not support bluetooth.";
		else
			return "BLUETOOTH: PHONE_COUNT: " + Integer.toString(phones); //Assuming no rediscovery, this is wrong
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				cacheDeviceInfo(intent);
			} else {
				Measurer.error("Unknown intent");
			}
		} catch (Exception e) {
			Measurer.dragnet(e, context);
		}
	}
	
	void cacheDeviceInfo(Intent intent) {
		StringBuilder stringBuilder = new StringBuilder();
		
		BluetoothClass btClass = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);
		stringBuilder.append("BLUETOOTH: CLASS: " + btClass.toString() +"\n");
		if (btClass.getMajorDeviceClass() == BluetoothClass.Device.Major.PHONE)
			phones += 1;
		
		String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
		if (name != null)
			stringBuilder.append("BLUETOOTH: NAME: " + name + "\n");
		
		short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
		if (rssi != Short.MIN_VALUE)
			stringBuilder.append("BLUETOOTH: RSSI: " + Integer.toString(rssi) + "\n");
		
		Measurer.appendToCache(stringBuilder.toString());
	}
}