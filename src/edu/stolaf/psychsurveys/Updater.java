package edu.stolaf.psychsurveys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.TimerTask;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

class Updater extends TimerTask {
	
	private String updateFileName = MainService.context.getFilesDir().getAbsolutePath() + "/PsychSurveys.apk";
	
	private void exec(String[] cmd) throws IOException, InterruptedException {
		Process process = Runtime.getRuntime().exec(cmd);
		int status = process.waitFor();
		if(status != 0) {
			Log.e("PsychSurveys", "Download updates gave exit status" + Integer.toString(status));
			return;
		}
	}
	
	private Boolean checkForUpdates() throws IOException {
		Socket socket = new Socket(MainService.ip, MainService.port);
		OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out.write("update? revNo=\"" + Integer.toString(MainService.revisionNumber) + "\"\n");
		out.flush();
		String reply = in.readLine();
		in.close();
		out.close();
		if(reply.equals("No update.")) {
			Log.i("PsychSurveys", "No update.");
			return false;
		} else if(reply.equals("Update.")) {
			Log.i("PsychSurveys", "Updating.");
			return true;
		} else {
			Log.e("PyschSurveys", "Unknown reply from server");
			throw new IOException("Unknown reply from server");
		}
	}
	
	private void downloadUpdates() throws IOException, InterruptedException {		
		String[] downloadCMD = {
				"sh",
				"-c",
				"echo \"update.\" | nc " + MainService.ip + " " + Integer.toString(MainService.port) + " > " + updateFileName
		};
		String[] permissionCMD = {"chmod", "666", updateFileName};
		exec(downloadCMD);
		exec(permissionCMD);
	}
	
	private void installUpdates() {
		Intent intent = new Intent();
		String path = "file://" + updateFileName;
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse(path), "application/vnd.android.package-archive");
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
		} catch (UnknownHostException e) {
			Log.e("PsychSurveys", "", e);
		} catch (IOException e) {
			Log.e("PsychSurveys", "", e);
		} catch (InterruptedException e) {
			Log.e("PsychSurveys", "", e);
		}
	}
}
