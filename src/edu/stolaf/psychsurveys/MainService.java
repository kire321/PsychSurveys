package edu.stolaf.psychsurveys;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;

import java.util.*;

public class MainService extends Service {
	static final int revisionNumber = 2;
	static final String cache = "PsychSurveys.cache";
	static final int measureLength = 10 * 1000;
	static final int measureFreq = 30 * 1000;
	static final int reportFreq = 60 * 1000;
	static final String ip = "192.168.1.5";
	static final int port = 8000;
	static final int updateFreq = 5 * 60 * 1000;
	
	static Context context;	
	static Timer timer;
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		context = getApplicationContext();		
    	timer = new Timer();
    	Measurer measurer = new Measurer();
    	measurer.measurements.add(new Accel());
    	measurer.measurements.add(new Sound());
    	measurer.measurements.add(new Loc());
		timer.scheduleAtFixedRate(measurer.start, 0, measureFreq);
		timer.scheduleAtFixedRate(new Reporter(), 0, reportFreq);
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

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new ToastBinder();
}
