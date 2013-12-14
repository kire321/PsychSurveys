package edu.stolaf.psychsurveys;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Globals {
	
	private Context context;
	
	public Globals(Context con) {
		context = con;
	}
	
	static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
	static final int revisionNumber = 2;
	static final String cache = "PsychSurveys.cache";
	static final String url = "http://www.cs.stolaf.edu/projects/sensors/";
	static final String cgi = url + "backend.cgi";
	static final String ip = "192.168.1.5";
	static final int port = 8000;
	static final int measureLength = 15*1000;
	
	static final int measureFreq = 10 * 60 * 1000; //for production
	//static final int measureFreq = 30 * 1000; //for testing
	static final int reportFreq = 60 * 60 * 1000; //for production
	//static final int reportFreq = 60 * 1000; //for testing
	static final int updateFreq = 60 * 60 * 1000; //for production
	//static final int updateFreq = 60 * 1000; //for testing
	
	/*private Measurer measurer = new Measurer();
	private Reporter reporter = new Reporter();
	private Updater updater = new Updater();*/
	
	void schedule(Class<?> cls, int delay, int period) {
		Intent intent = new Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, delay, period, pendingIntent);
	}
	
	void schedulePsychSurveysComponents() {				
		Log.i("PsychSurveys", "Starting Components");
		schedule(Measurer.class, 0, measureFreq);
		schedule(Reporter.class, measureFreq, reportFreq);
		schedule(Updater.class, 0, updateFreq);
	}
}
