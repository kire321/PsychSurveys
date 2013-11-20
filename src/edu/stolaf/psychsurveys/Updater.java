package edu.stolaf.psychsurveys;

import java.io.IOException;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class Updater extends MinimalService {
	
	private String fileName = "PsychSurveys.apk";
	private String path = context.getFilesDir().getAbsolutePath() + "/" + fileName;
	
	private Boolean checkForUpdates() throws IOException, InterruptedException {
		String reply = Globals.execForOutput("curl -f " + Globals.cgi + "?revNo=" + Integer.toString(Globals.revisionNumber));
		if(reply.equals("No update.\n\n")) {
			Log.i("PsychSurveys", "No update.");
			return false;
		} else if(reply.equals("Update.\n\n")) {
			Log.i("PsychSurveys", "Updating.");
			return true;
		} else {
			Log.e("PyschSurveys", "Unknown reply from server\n" + reply);
			throw new IOException("Unknown reply from server");
		}
	}
	
	private void downloadUpdates() throws IOException, InterruptedException {		
		Globals.exec("curl -f " + Globals.url + fileName + " -o " + path); 
		Globals.exec("chmod 666 " + path);
	}
	
	private void installUpdates() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	
	public void run() {		
		try {
			if (checkForUpdates()) {
				downloadUpdates();	
				installUpdates();
			}
		} catch (IOException e) {
			Log.e("PsychSurveys", "", e);
		} catch (InterruptedException e) {
			Log.e("PsychSurveys", "", e);
		}
	}
}
