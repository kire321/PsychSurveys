package edu.stolaf.psychsurveys;

import java.util.TimerTask;

import android.util.Log;

class Reporter extends TimerTask {
	@Override
	public void run() {
		String cache = MainService.context.getFilesDir().getAbsolutePath() + "/" + MainService.cache;
		Log.i("cache", cache);
		if (MainService.exec("curl -f --data-binary @" + cache + " " + MainService.cgi))
			if( ! MainService.context.deleteFile(MainService.cache))
	        	Log.e("PsychSurveys", "Could not delete cache");
	}	
}