package edu.stolaf.psychsurveys;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimerTask;
import java.util.Vector;

import android.content.Context;
import android.util.Log;

class Measurer {
	public final Vector<Measurement> measurements;
	public final TimerTask start;
	public final TimerTask stop;
	public static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
	
	public Measurer() {
		measurements = new Vector<Measurement>();
		start = new TimerTask() {
			public void run() {
				Log.i("PsychSurveys", "Revision Number: " + Integer.toString(MainService.revisionNumber));
				for (Measurement measurement : measurements) {
					measurement.start();
				}
				MainService.timer.schedule(stop, MainService.measureLength);
			}
		};
		stop = new TimerTask() {
			public void run() {
				StringBuffer toWrite = new StringBuffer();								
				for (Measurement measurement : measurements) {
					toWrite.append(measurement.stop() + "\n");
				}
				appendToCache(new String(toWrite));
			}			
		};
	}
	
	static public void appendToCache(String str) {
		try {
			OutputStreamWriter out = new OutputStreamWriter(MainService.context.openFileOutput(MainService.cache, Context.MODE_APPEND));
			String time = "TIME: " + format.format(System.currentTimeMillis()) + "\n";
			String rev = "REV: " + Integer.toString(MainService.revisionNumber) + "\n";				
			out.write(time + rev + str + "\n");
			out.close();
		} catch (FileNotFoundException e) {
			Log.e("PsychSurveys", "", e);
		} catch (IOException e) {
			Log.e("PsychSurveys", "", e);
		}
	}
}