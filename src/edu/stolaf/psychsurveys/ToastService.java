package edu.stolaf.psychsurveys;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.hardware.*;
import android.location.Location;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.lang.Process;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.*;
import com.google.android.gms.location.*;

class AccelListener implements SensorEventListener {
	Float sumSquaredDeviations;
	Integer count;
	
    public AccelListener() {
    	sumSquaredDeviations = (float) 0;
    	count = 0;
    }
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		final Float x = event.values[0];
		final Float y = event.values[1];
		final Float z = event.values[2];
		final Float deviation = (float) (Math.sqrt(x*x + y*y + z*z) - 9.8); 
		sumSquaredDeviations += deviation*deviation;
		count += 1;
	}
}

class StartMeasurements extends TimerTask {
	public ToastService toastService;
	
	public StartMeasurements(ToastService ts) {
		toastService = ts;
	}
	
	@Override
	public void run() {
		Log.i("PsychSurveys", "Revision Number: " + Integer.toString(toastService.revisionNumber));
		toastService.accelListener = new AccelListener();
		toastService.sensorManager.registerListener(toastService.accelListener, toastService.accel, 1*1000000);
				
		toastService.recorder = new MediaRecorder();
		toastService.recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		toastService.recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		toastService.recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		toastService.recorder.setOutputFile("/dev/null");
		try {
			toastService.recorder.prepare();
			toastService.recorder.start();	
			toastService.recorder.getMaxAmplitude();
		} catch (IOException e) {
			toastService.recorder = null;
			Log.e("PsychSurveys", "", e);
		} catch (RuntimeException e) {
			toastService.recorder = null;
			Log.e("PsychSurveys", "", e);
		}
		
		if(toastService.locationClient.isConnected()) {
			Handler handler = new Handler(toastService.getApplicationContext().getMainLooper());
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					toastService.locationClient.requestLocationUpdates(toastService.locationRequest, toastService);
				}
			};
			handler.post(runnable);
		}
		
		toastService.timer.schedule(new Write(toastService), toastService.measureLength);
	}
}

class Write extends TimerTask {
	public ToastService toastService;
	
	public Write(ToastService ts) {
		toastService = ts;
	}
	
	@Override
	public void run() {
		toastService.sensorManager.unregisterListener(toastService.accelListener);
		String accel = "ACCEL: " + Float.toString(toastService.accelListener.sumSquaredDeviations/toastService.accelListener.count) + "\n";

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
		String time = "TIME: " + format.format(System.currentTimeMillis()) + "\n";
		String rev = "REV: " + Integer.toString(toastService.revisionNumber) + "\n";
		String sound;
		if(toastService.recorder == null) {
			sound = "Error recording sound.\n";
		} else {
			sound = "SOUND: " + Integer.toString(toastService.recorder.getMaxAmplitude()) + "\n";
			toastService.recorder.stop();
			toastService.recorder.release();
		}
		
		String lat = "";
		String lon = "";
		String loctime = "";
		if(toastService.locationClient.isConnected()) {			
			Location location = toastService.locationClient.getLastLocation();
			if(location == null) {
				Log.i("PsychSurveys", "No location.");
			} else {
				Log.i("PsychSurveys", "Location available.");				
				loctime = "LOCTIME: " + format.format(location.getTime()) + "\n";
				lat = "LAT: " + Double.toString(location.getLatitude()) + "\n";
				lon = "LON: " + Double.toString(location.getLongitude()) + "\n";				
			}
		} else {
			Log.i("PsychSurveys", "Service not connected.");
		}		
		
		toastService.append(time + rev + sound + accel + loctime + lat + lon);
	}
}

class Report extends TimerTask {
	public ToastService toastService;
	
	public Report(ToastService ts) {
		toastService = ts;
	}
	
	@Override
	public void run() {
		InputStream inputStream;
		try {
			inputStream = toastService.openFileInput(toastService.cache);
			if(inputStream == null) {
				Log.e("PsychSurveys", "file is null");
				return;
			}
		} catch (FileNotFoundException e) {
			Log.e("PsychSurveys", "", e);
			return;
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		String receiveString = "";
        StringBuilder stringBuilder = new StringBuilder();

        try {
	        while ( (receiveString = in.readLine()) != null ) {
	            stringBuilder.append(receiveString + "\n");
	        }	
	        inputStream.close();
        } catch (IOException e) {
			Log.e("PsychSurveys", "", e);
			return;
		}
        try {
        	toastService.send(stringBuilder.toString());
		} catch (UnknownHostException e) {
			Log.e("PsychSurveys", "", e);
			return;
		} catch (IOException e) {
			Log.e("PsychSurveys", "", e);
			return;
		}
        if( ! toastService.deleteFile(toastService.cache)) {
        	Log.e("PsychSurveys", "Could not delete cache");
        }
	}
}

class Updater extends TimerTask {
	public ToastService toastService;
	
	public Updater(ToastService ts) {
		toastService = ts;
	}
	
	public void exec(String[] cmd) throws IOException, InterruptedException {
		Process process = Runtime.getRuntime().exec(cmd);
		int status = process.waitFor();
		if(status != 0) {
			Log.e("PsychSurveys", "Download updates gave exit status" + Integer.toString(status));
			return;
		}
	}
	
	@Override
	public void run() {
		Socket socket;
		try {
			socket = new Socket(toastService.ip, toastService.port);
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out.write("update? revNo=\"" + Integer.toString(toastService.revisionNumber) + "\"\n");
			out.flush();
			String reply = in.readLine();
			in.close();
			out.close();
			if(reply.equals("No update.")) {
				Log.i("PsychSurveys", "No update.");				
			} else if(reply.equals("Update.")) {
				Log.i("PsychSurveys", "Updating.");
				String[] downloadCMD = {
						"sh",
						"-c",
						"echo \"update.\" | nc " + toastService.ip + " " + Integer.toString(toastService.port) + " > " + toastService.updateFileName
				};
				String[] permissionCMD = {"chmod", "666", toastService.updateFileName};
				exec(downloadCMD);
				exec(permissionCMD);
				
				Intent intent = new Intent();
				String path = "file://" + toastService.updateFileName;
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse(path), "application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				toastService.startActivity(intent);
			} else {
				Log.e("PyschSurveys", "Unknown reply from server");
			}
			
		} catch (UnknownHostException e) {
			Log.e("PsychSurveys", "", e);
		} catch (IOException e) {
			Log.e("PsychSurveys", "", e);
		} catch (InterruptedException e) {
			Log.e("PsychSurveys", "", e);
		}
	}
}

public class ToastService extends Service implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	final int revisionNumber = 2;
	final String cache = "PsychSurveys.cache";
	final int measureLength = 10 * 1000;
	final int measureFreq = 30 * 1000;
	final int reportFreq = 60 * 1000;
	final String ip = "192.168.1.5";
	final int port = 8000;
	String updateFileName = ""; //initialized in onStartCommand
	final int updateFreq = 5 * 60 * 1000;
	
	SensorManager sensorManager;
	Sensor accel;
	Timer timer;
	MediaRecorder recorder;
	AccelListener accelListener;
	LocationClient locationClient;
	LocationRequest locationRequest;
	
	public void append(String str) {
		try {
			OutputStreamWriter out = new OutputStreamWriter(openFileOutput(cache, MODE_APPEND));
			out.write(str);
			out.close();
		} catch (FileNotFoundException e) {
			Log.e("PsychSurveys", "", e);
		} catch (IOException e) {
			Log.e("PsychSurveys", "", e);
		}
	}
	
	public void send(String str) throws UnknownHostException, IOException {
		Socket socket;
		socket = new Socket(ip, port);
		OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
		out.write(str);
		out.close();	
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		updateFileName = getFilesDir().getAbsolutePath() + "/PsychSurveys.apk";
		
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    	accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	timer = new Timer();
		timer.scheduleAtFixedRate(new StartMeasurements(this), 0, measureFreq);
		timer.scheduleAtFixedRate(new Report(this), 0, reportFreq);
		timer.scheduleAtFixedRate(new Updater(this), 0, updateFreq);
		
		locationClient = new LocationClient(getApplicationContext(), this, this);
		locationClient.connect();
		locationRequest = LocationRequest.create();
		locationRequest.setExpirationDuration(measureLength);
		locationRequest.setFastestInterval(reportFreq);
		locationRequest.setInterval(measureFreq);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		
        return START_STICKY;
    }

	@Override
	public void onLocationChanged(Location location) {}
	
	@Override
	public void onConnected(Bundle arg0) {
		Log.i("LOC", "Service connected");
	}

	@Override
	public void onDisconnected() {
		Log.i("LOC", "Service disconnected");
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Log.i("LOC", "Connection to service failed");
	}
	
	public class ToastBinder extends Binder {
        ToastService getService() {
            return ToastService.this;
        }
    }
	
	@Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new ToastBinder();
}
