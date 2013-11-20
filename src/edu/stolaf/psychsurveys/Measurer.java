package edu.stolaf.psychsurveys;

import android.util.Log;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

class Stop extends TimerTask {
	
	public void run() {
		StringBuffer toWrite = new StringBuffer();								
		for (Measurement measurement : Measurer.measurements) {
			String result = measurement.stop();
			if (result != null)
				toWrite.append(result + "\n");
		}
		Measurer.appendToCache(new String(toWrite));
		if (!Measurer.wakeLock.isHeld()) {
			Log.e("PsychSurveys", "Unheld wakelock in stop");
		} else {
			Measurer.wakeLock.release();
		}
	}			
};

public class Measurer extends MinimalService {

	public static Vector<Measurement> measurements = new Vector<Measurement>();
	public static WakeLock wakeLock;
	
	@SuppressLint("Wakelock")
	public void run() {		
		Log.i("PsychSurveys", "Starting Measurements");
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE); 
    	wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MainService");
        wakeLock.acquire();
        if (!Measurer.wakeLock.isHeld()) {
			Log.e("PsychSurveys", "Unheld wakelock in run");
		}
		
		measurements.add(new Bluetooth());
    	measurements.add(new Accel());
    	measurements.add(new Sound());
    	measurements.add(new Loc());
    	
        Log.i("PsychSurveys", "Revision Number: " + Integer.toString(Globals.revisionNumber));
        for (Measurement measurement : measurements) {
                measurement.start();
        }
        (new Timer()).schedule(new Stop(), Globals.measureLength);
    }
	
	static public void appendToCache(String str) {
		try {
			OutputStreamWriter out = new OutputStreamWriter(context.openFileOutput(Globals.cache, Context.MODE_APPEND));
			String time = "TIME: " + Globals.format.format(System.currentTimeMillis()) + "\n";
			String rev = "REV: " + Integer.toString(Globals.revisionNumber) + "\n";				
			out.write(time + rev + str + "\n");
			out.close();
		} catch (FileNotFoundException e) {
			Log.e("PsychSurveys", "", e);
		} catch (IOException e) {
			Log.e("PsychSurveys", "", e);
		}
	}
}
