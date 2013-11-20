package edu.stolaf.psychsurveys;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public abstract class MinimalService extends Service implements Runnable {

	public static Context context;
	
	@Override
    public int onStartCommand(Intent in, int flags, int startId) {		
		context = getApplicationContext();
		run();
		return START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
