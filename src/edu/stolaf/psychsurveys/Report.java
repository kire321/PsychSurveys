package edu.stolaf.psychsurveys;

import android.util.Log;

class Reporter extends MinimalService {
	
	public void run() {		
		String cache = Globals.context.getFilesDir().getAbsolutePath() + "/" + Globals.cache;
		if (Globals.exec("curl -f --data-binary @" + cache + " " + Globals.cgi))
			if( ! Globals.context.deleteFile(Globals.cache))
	        	Log.e("PsychSurveys", "Could not delete cache");
    }
}