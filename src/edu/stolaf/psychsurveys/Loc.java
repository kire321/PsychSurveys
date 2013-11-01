package edu.stolaf.psychsurveys;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class Loc implements Measurement, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	LocationClient locationClient;
	LocationRequest locationRequest;
	
	public Loc() {
		locationClient = new LocationClient(MainService.context, this, this);
		locationClient.connect();
		locationRequest = LocationRequest.create();
		locationRequest.setExpirationDuration(MainService.measureLength);
		locationRequest.setFastestInterval(MainService.reportFreq);
		locationRequest.setInterval(MainService.measureFreq);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	
	@Override
	public void start() {
		if(locationClient.isConnected()) {
			Handler handler = new Handler(MainService.context.getMainLooper());
			final Loc self = this;
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					locationClient.requestLocationUpdates(locationRequest, self);
				}
			};
			handler.post(runnable);
		}
	}

	@Override
	public String stop() {
		if(locationClient.isConnected()) {			
			Location location = locationClient.getLastLocation();
			if(location == null) {
				Log.i("PsychSurveys", "No location.");
				return "LOC: no location available.";
			} else {
				Log.i("PsychSurveys", "Location available.");				
				String loctime = "LOCTIME: " + Measurer.format.format(location.getTime()) + "\n";
				String lat = "LAT: " + Double.toString(location.getLatitude()) + "\n";
				String lon = "LON: " + Double.toString(location.getLongitude());	
				return loctime + lat + lon;
			}
		} else {
			Log.i("PsychSurveys", "Service not connected.");
			return "LOC: Service not connected.";
		}
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

}
