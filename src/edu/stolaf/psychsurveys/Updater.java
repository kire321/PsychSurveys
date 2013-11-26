package edu.stolaf.psychsurveys;

import java.io.IOException;

import android.content.Intent;
import android.net.Uri;

public class Updater extends RepeatingTask {
	
	private String fileName = "PsychSurveys.apk";
	private String path;
	
	private Boolean checkForUpdates() throws IOException, InterruptedException {
		String reply = execForOutput("curl -f " + Globals.cgi + "?revNo=" + Integer.toString(Globals.revisionNumber));
		if(reply.equals("No update.\n\n")) {
			info("No update.");
			return false;
		} else if(reply.equals("Update.\n\n")) {
			info("Updating.");
			return true;
		} else {
			error("Unknown reply from server\n" + reply);
			throw new IOException("Unknown reply from server");
		}
	}
	
	private void downloadUpdates() throws IOException, InterruptedException {		
		exec("curl -f " + Globals.url + fileName + " -o " + path); 
		exec("chmod 666 " + path);
	}
	
	private void installUpdates() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	
	public void run() {		
		path = context.getFilesDir().getAbsolutePath() + "/" + fileName;
		try {
			if (checkForUpdates()) {
				downloadUpdates();	
				installUpdates();
			}
		} catch (IOException e) {
			error("", e);
		} catch (InterruptedException e) {
			error("", e);
		}
		wakeLock.release();
	}
}
