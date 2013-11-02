package edu.stolaf.psychsurveys;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

//Test cases: do we get broadcasts for rediscovering the same device over and over again?
//do we get broadcasts for devices that we've already bonded with and then come back to?

public class Bluetooth extends BroadcastReceiver implements Measurement {

	public static BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
	public static UUID uuid = new UUID(398754, 298436880);
	Server server = new Server();
	int phones;
	
	@Override
	public void start() {
		phones = 0;
		if (!server.isAlive())
			server.run();
		if (ba != null) {
			if (ba.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
				Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
				MainService.context.startActivity(discoverableIntent);
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
		String action = intent.getAction();
		if (BluetoothDevice.ACTION_FOUND.equals(action)) {
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
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			try {
				Method method = device.getClass().getMethod("createBond", (Class[]) null);
				method.invoke(device, (Object[]) null);
			} catch (Exception e) {
				Log.e("PsychSurveys", "", e);
			}
		} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
			if (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1) == BluetoothDevice.BOND_BONDED) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getAddress().compareTo(ba.getAddress()) > 0) {
					Log.i("PsychSurveys", "Waiting for connection from " + device.getAddress());
				} else {
					Log.i("PsychSurveys", "Connecting to " + device.getAddress());
					(new Client(device)).run();
				}
			}
		} else {
			Log.e("PsuchSurveys", "Unknown intent");
		}
	}
}

class Server extends Thread {
	public void run() {
		try {
			BluetoothServerSocket serverSocket = Bluetooth.ba.listenUsingInsecureRfcommWithServiceRecord("PsychSurveys", Bluetooth.uuid);
			while (true) {
				BluetoothSocket socket = serverSocket.accept();
				if (socket == null)
					Log.e("PsychSurveys", "Null server side socket");
				Log.i("PsychSurveys", "Connection accepted");
			}
		} catch (IOException e) {
			Log.e("PsychSurveys", "", e);
		} 		
	}
}

class Client extends Thread {
	BluetoothDevice device;
	
	public Client(BluetoothDevice d) {
		device = d;
	}
	
	public void run() {
		try {
			BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(Bluetooth.uuid);
			if (Bluetooth.ba.isDiscovering())
				Log.i("PsychSurveys", "Discovery cancelled");
			Bluetooth.ba.cancelDiscovery();
			socket.connect();
			Log.i("PsychSurveys", "Connection successful");
			socket.close();
		} catch (IOException e) {
			Log.e("PsychSurveys", "", e);
		} 		
	}
}