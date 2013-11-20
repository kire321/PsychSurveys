package edu.stolaf.psychsurveys;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public abstract class MinimalService extends Service implements Runnable {

	@Override
    public int onStartCommand(Intent in, int flags, int startId) {		
		run();
		return START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
