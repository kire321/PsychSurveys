package edu.stolaf.psychsurveys;

public class Reporter extends RepeatingTask {
	
	public void run() {		
		String cache = context.getFilesDir().getAbsolutePath() + "/" + Globals.cache;
		if (exec("curl -f --data-binary @" + cache + " " + Globals.cgi))
			if( ! context.deleteFile(Globals.cache))
	        	error("Could not delete cache");
		wakeLock.release();
    }
}