package edu.stolaf.psychsurveys;

import java.io.FileInputStream;

import com.loopj.android.http.*;

public class Reporter extends RepeatingTask {
	
	public void run() throws Exception {		
		RequestParams params = new RequestParams();
		final FileInputStream in = context.openFileInput(Globals.cache);
		params.put("key", in);
		Globals.client.post(Globals.cgi, params, new ExceptionHandlingResponseHandler(wakeLock) {
			public void handle(String response) throws Exception {
				in.close();
				if( ! context.deleteFile(Globals.cache))
		        	error("Could not delete cache");
				releaseWakeLock();									
			}
		});					
    }
}