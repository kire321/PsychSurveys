package edu.stolaf.psychsurveys;

import java.io.FileOutputStream;

import com.loopj.android.http.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class Updater extends RepeatingTask {
	
	private String fileName = "PsychSurveys.apk";
	private AsyncHttpClient client = new AsyncHttpClient();
	
	AsyncHttpResponseHandler maybeDownloadUpdates = new ExceptionHandlingResponseHandler(wakeLock) {
		public void handle(String response) {
			if(response.equals("No update.\n\n")) {
				info("No update.");
				releaseWakeLock();
			} else if(response.equals("Update.\n\n")) {
				info("Updating.");
				client.get(Globals.url + fileName, installUpdates);
			} else {
				error("Unknown reply from server\n" + response);
				releaseWakeLock();
			}
		}
	};
	
	AsyncHttpResponseHandler installUpdates = new ExceptionHandlingResponseHandler(wakeLock) {
		@SuppressLint("WorldReadableFiles")
		@SuppressWarnings("deprecation")
		public void handle(int statusCode, org.apache.http.Header[] headers, byte[] responseBody) throws Exception {
			FileOutputStream out = context.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
			out.write(responseBody);
			out.close();			
			String path = context.getFilesDir().getAbsolutePath() + "/" + fileName;				
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			releaseWakeLock();
		}
	};
	
	public void run() throws Exception {				
		client.get(Globals.cgi + "?revNo=" + Integer.toString(Globals.revisionNumber), maybeDownloadUpdates);
	}
}
