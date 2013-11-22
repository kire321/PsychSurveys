package edu.stolaf.psychsurveys;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

class Accel implements SensorEventListener, Measurement {
	Float sumSquaredDeviations;
	Integer count;
	SensorManager sensorManager;
	Sensor accelerometer;
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
		final Float x = event.values[0];
		final Float y = event.values[1];
		final Float z = event.values[2];
		final Float deviation = (float) (Math.sqrt(x*x + y*y + z*z) - 9.8); 
		sumSquaredDeviations += deviation*deviation;
		count += 1;
	}

	@Override
	public void start() {
		sensorManager = (SensorManager) Measurer.context.getSystemService(Context.SENSOR_SERVICE);
    	accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sumSquaredDeviations = (float) 0;
    	count = 0;
    	sensorManager.registerListener(this, accelerometer, 1*1000000);
	}

	@Override
	public String stop() {
		sensorManager.unregisterListener(this);
		return "ACCEL: " + Float.toString(sumSquaredDeviations/count);
	}
}
