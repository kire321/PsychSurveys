package edu.stolaf.psychsurveys;

import java.util.Set;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.ParcelUuid;

public class Bluetooth implements Measurement {

	BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
	
	@Override
	public void start() {
		if (ba != null)
			if (!ba.startDiscovery())
				ba.enable();
		
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
	@Override
	public String stop() {
		if (ba == null)
			return "BLUETOOTH: Device does not support bluetooth.";
		Set<BluetoothDevice> devices = ba.getBondedDevices();
		if (devices.size() == 0)
			return "BLUETOOTH: No devices found.";
		StringBuilder stringBuilder = new StringBuilder();
		int phones = 0;
		Boolean apiOK = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
		if (!apiOK)
			stringBuilder.append("BLUETOOTH: UUID unsupported");
		for (BluetoothDevice device : devices) {
			stringBuilder.append("BLUETOOTH: NAME: " + device.getName() + "\n");
			stringBuilder.append("BLUETOOTH: CLASS: " + device.getBluetoothClass().toString() + "\n");
			if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.PHONE)
				phones += 1;
			if (apiOK) {
				ParcelUuid[] uuids = device.getUuids();
				if (uuids == null)
					stringBuilder.append("BLUETOOTH: UUID: NULL\n");
				else
					for (ParcelUuid uuid : uuids) {
						stringBuilder.append("BLUETOOTH: UUID: " + uuid.toString() + "\n");
					}		
			}
		}
		stringBuilder.append("BLUETOOTH: PHONE_COUNT: " + Integer.toString(phones));
		return stringBuilder.toString();
	}
}
