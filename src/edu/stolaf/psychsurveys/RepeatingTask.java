package edu.stolaf.psychsurveys;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public abstract class RepeatingTask extends BroadcastReceiver implements Runnable {
	//Subclasses are responsible for releasing the wakelock!!!
	//Subclasses cannot block the thread!!!
	//Subclasses cannot refer to context in their constructor
	public static Context context;
	public static PowerManager.WakeLock wakeLock;
	
	@SuppressLint("Wakelock")
	@Override
    public void onReceive(Context con, Intent intent) 
    {   
		context = con;
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PsychSurveys");
        wakeLock.acquire();
        run();
    }
}
