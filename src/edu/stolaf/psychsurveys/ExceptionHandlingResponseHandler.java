package edu.stolaf.psychsurveys;

import org.apache.http.Header;

import com.loopj.android.http.AsyncHttpResponseHandler;

import android.os.PowerManager.WakeLock;

public class ExceptionHandlingResponseHandler extends AsyncHttpResponseHandler {

	private WakeLock wakeLock;
	
	ExceptionHandlingResponseHandler(WakeLock wl) {
		wakeLock = wl;
	}
	
	public void handle(String response) throws Exception {
		RepeatingTask.error("Default implementation of string overload called");
	};
	public void handle(int statusCode, org.apache.http.Header[] headers, byte[] responseBody) throws Exception {
		RepeatingTask.error("Default implementation of byte array overload called");
	};
	
	public void onSuccess(String response) {
		try {
			RepeatingTask.info("string onSuccess called");
			handle(response);
		} catch (Exception e) {
			RepeatingTask.dragnet(e);
			wakeLock.release();
		}
	}
	
	public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] responseBody) {
		try {
			RepeatingTask.info("bytearray onSuccess called");
			handle(statusCode, headers, responseBody);
		} catch (Exception e) {
			RepeatingTask.dragnet(e);
			wakeLock.release();
		}
	}
	
	public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable throwable) {
		wakeLock.release();
		RepeatingTask.error("Status code " + Integer.toString(statusCode), throwable);
	}
}
