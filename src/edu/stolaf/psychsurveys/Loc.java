package edu.stolaf.psychsurveys;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class Loc implements Measurement, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	LocationClient locationClient;
	LocationRequest locationRequest;
	
	@Override
	public void start() {
		locationClient = new LocationClient(Measurer.context, this, this);
		locationClient.connect();
		locationRequest = LocationRequest.create();
		locationRequest.setExpirationDuration(Globals.measureLength);
		locationRequest.setFastestInterval(Globals.reportFreq);
		locationRequest.setInterval(Globals.measureFreq);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		
		if(locationClient.isConnected()) {
			Handler handler = new Handler(Measurer.context.getMainLooper());
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
	public void stop() throws Exception {
		if(locationClient.isConnected()) {			
			Location location = locationClient.getLastLocation();
			if(location == null) {
				Measurer.info("No location.");
			} else {
				Measurer.info("Location available.");
				Measurer.toUpload.put("locTime", Globals.format.format(location.getTime()));
				Measurer.toUpload.put("lat", location.getLatitude());
				Measurer.toUpload.put("lon", location.getLongitude());
			}
		}
	}
	
	@Override
	public void onLocationChanged(Location location) {}
	
	@Override
	public void onConnected(Bundle arg0) {}

	@Override
	public void onDisconnected() {}
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {}

}
