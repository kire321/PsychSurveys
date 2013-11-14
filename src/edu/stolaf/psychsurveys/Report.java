package edu.stolaf.psychsurveys;

import android.util.Log;

import java.util.TimerTask;

class Reporter extends TimerTask {
	@Override
	public void run() {
		String cache = MainService.context.getFilesDir().getAbsolutePath() + "/" + MainService.cache;
		if (MainService.exec("curl -f --data-binary @" + cache + " " + MainService.cgi))
			if( ! MainService.context.deleteFile(MainService.cache))
	        	Log.e("PsychSurveys", "Could not delete cache");
	}	
}