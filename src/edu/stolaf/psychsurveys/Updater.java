package edu.stolaf.psychsurveys;

import java.io.IOException;
import java.io.StringWriter;
import java.util.TimerTask;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.apache.commons.io.IOUtils;

class Updater extends TimerTask {
	
	private String fileName = "PsychSurveys.apk";
	private String path = MainService.context.getFilesDir().getAbsolutePath() + "/" + fileName;
	
	private String execForOutput(String cmd) throws IOException, InterruptedException {
		Process process = Runtime.getRuntime().exec(cmd);
		int status = process.waitFor();
		if(status != 0) {
			Log.e("PsychSurveys", "Nonzero exit status when running " + cmd);
			throw new IOException("Nonzero exit status");
		}
		StringWriter writer = new StringWriter();
		IOUtils.copy(process.getInputStream(), writer);
		return writer.toString();
	}
	
	private Boolean checkForUpdates() throws IOException, InterruptedException {
		String reply = execForOutput("curl -f " + MainService.cgi + "?revNo=" + Integer.toString(MainService.revisionNumber));
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
		MainService.exec("curl -f " + MainService.url + fileName + " -o " + path); 
		MainService.exec("chmod 666 " + path);
	}
	
	private void installUpdates() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		MainService.context.startActivity(intent);
	}
	
	@Override
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
