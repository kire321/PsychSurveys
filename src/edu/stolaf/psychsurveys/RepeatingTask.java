package edu.stolaf.psychsurveys;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public abstract class RepeatingTask extends BroadcastReceiver {
	//Subclasses are responsible for releasing the wakelock
	//Subclasses are responsible for handling exceptions in anything they schedule
	//Subclasses cannot block the thread
	//Subclasses cannot refer to context in their constructor
	public static Context context;
	public static PowerManager.WakeLock wakeLock;	
	public static String tag = "PsychSurveys";
	
	public abstract void run() throws Exception;
	
	public static void info(String msg) {
		Log.i(tag, msg);
	}
	
	public static void error(String msg) {
		appendToCache(msg + "\n");
		Log.e(tag, msg);
	}
	
	public static void error(String msg, Throwable t) {
		appendToCache(msg + "\n" + Log.getStackTraceString(t) + "\n");
		Log.e(tag, msg, t);		
	}
	
	public static void dragnet(Throwable t) {
		error("Dragnet exception handling", t);
	}
	
	static public void appendToCache(String str) {
		try {
			OutputStreamWriter out = new OutputStreamWriter(context.openFileOutput(Globals.cache, Context.MODE_APPEND));
			String time = "TIME: " + Globals.format.format(System.currentTimeMillis()) + "\n";
			String rev = "REV: " + Integer.toString(Globals.revisionNumber) + "\n";				
			out.write(time + rev + str + "\n");
			out.close();
		} catch (FileNotFoundException e) {
			error("", e);
		} catch (IOException e) {
			error("", e);
		}
	}
	
	@SuppressLint("Wakelock")
	@Override
    public void onReceive(Context con, Intent intent) 
    {   
		try {
			context = con;
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
	        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PsychSurveys");
	        wakeLock.acquire();
	        run();
		} catch (Exception e) {
			dragnet(e);
		}
    }
}
