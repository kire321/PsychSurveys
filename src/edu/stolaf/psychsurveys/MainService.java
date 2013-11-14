package edu.stolaf.psychsurveys;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;

import java.io.IOException;
import java.util.*;
import java.lang.Process;

public class MainService extends Service {
	static final int revisionNumber = 2; //These files differ from rn2 in production
	static final String cache = "PsychSurveys.cache";
	static final int measureLength = 15 * 1000; //bluetooth discovery takes 12 seconds
	static final int measureFreq = 30 * 1000;
	static final int reportFreq = 60 * 1000;
	static final String url = "www.cs.stolaf.edu/projects/sensors/";
	static final String cgi = url + "backend.cgi";
	static final String ip = "192.168.1.5";
	static final int port = 8000;
	static final int updateFreq = 5 * 60 * 1000;
	
	static Context context;	
	static Timer timer;
	
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
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		context = getApplicationContext();		
    	timer = new Timer();
    	Measurer measurer = new Measurer();
    	measurer.measurements.add(new Bluetooth());
    	measurer.measurements.add(new Accel());
    	measurer.measurements.add(new Sound());
    	measurer.measurements.add(new Loc());
		timer.scheduleAtFixedRate(measurer.start, 0, measureFreq);
		timer.scheduleAtFixedRate(new Reporter(), measureFreq, reportFreq);
		timer.scheduleAtFixedRate(new Updater(), 0, updateFreq);
		
        return START_STICKY;
    }
	
	public class ToastBinder extends Binder {
        MainService getService() {
            return MainService.this;
        }
    }
	
	@Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new ToastBinder();
}
