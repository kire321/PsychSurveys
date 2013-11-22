package edu.stolaf.psychsurveys;

import android.util.Log;

public class Reporter extends RepeatingTask {
	
	public void run() {		
		String cache = context.getFilesDir().getAbsolutePath() + "/" + Globals.cache;
		if (Globals.exec("curl -f --data-binary @" + cache + " " + Globals.cgi))
			if( ! context.deleteFile(Globals.cache))
	        	Log.e("PsychSurveys", "Could not delete cache");
		wakeLock.release();
    }
}