package edu.stolaf.psychsurveys;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.io.IOUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Globals {
	
	static Context context;
	
	public Globals(Context con) {
		context = con;
	}
	
	static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
	static final int revisionNumber = 3;
	static final String cache = "PsychSurveys.cache";
	static final String url = "www.cs.stolaf.edu/projects/sensors/";
	static final String cgi = url + "backend.cgi";
	static final String ip = "192.168.1.5";
	static final int port = 8000;
	static final int measureLength = 15*1000;
	
	//static final int measureFreq = 10 * 60 * 1000; //for production
	static final int measureFreq = 30 * 1000; //for testing
	//static final int reportFreq = 24 * 60 * 60 * 1000; //for production
	static final int reportFreq = 60 * 1000; //for testing
	//static final int updateFreq = 24 * 60 * 60 * 1000; //for production
	static final int updateFreq = 60 * 1000; //for testing
	
	void schedule(Class<?> cls, int delay, int period) {
		Intent intent = new Intent();
        intent.setClass(context, cls);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, delay, period, pendingIntent);
	}
	
	void schedulePsychSurveysComponents() {				
		schedule(Measurer.class, 0, measureFreq);
		schedule(Reporter.class, measureFreq, reportFreq);
		schedule(Updater.class, 0, updateFreq);
	}

	static public void appendToCache(String str) {
		try {
			OutputStreamWriter out = new OutputStreamWriter(context.openFileOutput(cache, Context.MODE_APPEND));
			String time = "TIME: " + format.format(System.currentTimeMillis()) + "\n";
			String rev = "REV: " + Integer.toString(revisionNumber) + "\n";				
			out.write(time + rev + str + "\n");
			out.close();
		} catch (FileNotFoundException e) {
			Log.e("PsychSurveys", "", e);
		} catch (IOException e) {
			Log.e("PsychSurveys", "", e);
		}
	}
	
	static public Boolean exec(String cmd) {
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			int status = process.waitFor();
			if(status != 0) {
				Log.e("PsychSurveys", "Nonzero exit status when running " + cmd);
				return false;
			}
			return true;
		} catch (IOException e) {
			Log.e("PsychSurveys", "", e);
			return false;
		} catch (InterruptedException e) {
			Log.e("PsychSurveys", "", e);
			return false;
		}		
	}
	
	static public String execForOutput(String cmd) throws IOException, InterruptedException {
		Process process = Runtime.getRuntime().exec(cmd);
		int status = process.waitFor();
		if(status != 0) {
			Log.e("PsychSurveys", "Nonzero exit status when running " + cmd);
			throw new IOException("Nonzero exit status");
		}
		StringWriter writer = new StringWriter();
		IOUtils.copy(process.getInputStream(), writer);
		return writer.toString();
	}
}
