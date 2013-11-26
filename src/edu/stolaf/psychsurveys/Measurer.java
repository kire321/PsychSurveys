package edu.stolaf.psychsurveys;

import android.annotation.SuppressLint;

import java.util.*;

class Stop extends TimerTask {
	
	public void run() {
		try {
			StringBuffer toWrite = new StringBuffer();								
			for (Measurement measurement : Measurer.measurements) {
				String result = measurement.stop();
				if (result != null)
					toWrite.append(result + "\n");
			}
			Measurer.appendToCache(new String(toWrite));
			if (!Measurer.wakeLock.isHeld()) {
				Measurer.error("Unheld wakelock in stop");
			} else {
				Measurer.wakeLock.release();
			}
		} catch (Exception e) {
			Measurer.dragnet(e);
		}
	}			
};

public class Measurer extends RepeatingTask {

	private static Measurement[] array = {new Bluetooth(), new Accel(), new Sound(), new Loc()};
	public static Vector<Measurement> measurements = new Vector<Measurement>(Arrays.asList(array));
	
	@SuppressLint("Wakelock")
	public void run() {		
        info("Revision Number: " + Integer.toString(Globals.revisionNumber));
        for (Measurement measurement : measurements) {
                measurement.start();
        }
        (new Timer()).schedule(new Stop(), Globals.measureLength);
    }
}
