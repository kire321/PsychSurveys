package edu.stolaf.psychsurveys;

import org.apache.http.Header;

import com.loopj.android.http.AsyncHttpResponseHandler;

import android.os.PowerManager.WakeLock;

public class ExceptionHandlingResponseHandler extends AsyncHttpResponseHandler {

	private WakeLock wakeLock;
	
	ExceptionHandlingResponseHandler(WakeLock wl) {
		wakeLock = wl;
	}
	
	public void handle(String response) throws Exception {};
	public void handle(int statusCode, org.apache.http.Header[] headers, byte[] responseBody) throws Exception {};
	
	public void releaseWakeLock() {
		if (wakeLock.isHeld()) {
			wakeLock.release();
		} else {			
			RepeatingTask.error("Unheld wakelock");
		}
	}
	
	public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] responseBody) {
		try {
			handle(statusCode, headers, responseBody);
			handle(new String(responseBody));
		} catch (Exception e) {
			RepeatingTask.dragnet(e);
			releaseWakeLock();
		}
	}
	
	public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable throwable) {
		releaseWakeLock();
		RepeatingTask.error("Status code " + Integer.toString(statusCode), throwable);
	}
}
