package edu.stolaf.psychsurveys;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TimerTask;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

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
		StringBuilder stringBuilder = new StringBuilder();
		InputStreamReader stream = new InputStreamReader(process.getInputStream());
		char ch;
		while ((ch = (char) stream.read()) != -1)
			stringBuilder.append(ch);
		return stringBuilder.toString();
	}
	
	private Boolean checkForUpdates() throws IOException, InterruptedException {
		String reply = execForOutput("curl -f " + MainService.cgi + "?revNo=" + Integer.toString(MainService.revisionNumber));
		if(reply.equals("No update.\n")) {
			Log.i("PsychSurveys", "No update.");
			return false;
		} else if(reply.equals("Update.\n")) {
			Log.i("PsychSurveys", "Updating.");
			return true;
		} else {
			Log.e("PyschSurveys", "Unknown reply from server");
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
