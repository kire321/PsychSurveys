package edu.stolaf.psychsurveys;

import android.util.Log;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import java.util.*;

class Stop extends TimerTask {
	
	public void run() {
		StringBuffer toWrite = new StringBuffer();								
		for (Measurement measurement : Measurer.measurements) {
			String result = measurement.stop();
			if (result != null)
				toWrite.append(result + "\n");
		}
		Globals.appendToCache(new String(toWrite));
		Measurer.wakeLock.release();
	}			
};

public class Measurer extends MinimalService {

	public static Vector<Measurement> measurements = new Vector<Measurement>();
	public static WakeLock wakeLock;
	
	@SuppressLint("Wakelock")
	public void run() {		
		PowerManager powerManager = (PowerManager) Globals.context.getSystemService(Context.POWER_SERVICE); 
    	wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MainService");
        wakeLock.acquire();
		
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
}
