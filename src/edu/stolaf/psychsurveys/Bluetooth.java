package edu.stolaf.psychsurveys;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//TODO: don't connect to devices that are already peers
public class Bluetooth extends BroadcastReceiver implements Measurement {

	public static BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
	public static UUID uuid = new UUID(398754, 298436880);
	public static Set<String> nonPeers = new HashSet<String>();
	Server server = new Server();
	int phones;
	
	@Override
	public void start() {
		phones = 0;
		if (ba != null) {
			if (ba.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE && !server.isAlive()) {
				Measurer.info("Starting server");
				server.start();
			}
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
				
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() == BluetoothDevice.BOND_BONDED)
					makeConnection(device);
				else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
					try {
						Method method = device.getClass().getMethod("createBond", (Class[]) null);
						Boolean bondFailed = ! (Boolean) method.invoke(device, (Object[]) null);
						if (bondFailed)
							Measurer.error("Bond failed!");
					} catch (Exception e) {
						Measurer.error("", e);
					}
				}
			} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1); 
				if (bondState == BluetoothDevice.BOND_BONDED) {
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					makeConnection(device);
				}
			} else {
				Measurer.error("Unknown intent");
			}
		} catch (Exception e) {
			Measurer.dragnet(e);
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
	
	void makeConnection(BluetoothDevice device) {
		if (!nonPeers.contains(device.getAddress())) {
			if (device.getAddress().compareTo(ba.getAddress()) > 0) {
				Measurer.info("Waiting for connection from " + device.getAddress());
			} else {
				Measurer.info("Connecting to " + device.getAddress());
				(new Client(device)).start();
			}
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
					Measurer.error("Null server side socket");
				Measurer.info("Connection accepted");
			}
		} catch (IOException e) {
			Measurer.error("", e);
		} catch (Exception e) {
			Measurer.dragnet(e);
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
				Measurer.info("Discovery cancelled");
			Bluetooth.ba.cancelDiscovery();
			socket.connect();
			Measurer.info("Connection successful");
			socket.close();
		} catch (IOException e) {
			Measurer.error("", e);
			Bluetooth.nonPeers.add(device.getAddress());
		}  catch (Exception e) {
			Measurer.dragnet(e);
		} 		
	}
}