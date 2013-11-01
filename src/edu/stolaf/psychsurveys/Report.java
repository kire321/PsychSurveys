package edu.stolaf.psychsurveys;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.TimerTask;

import android.util.Log;

class Reporter extends TimerTask {
	private StringBuilder readCache() {
		InputStream inputStream;
		try {
			inputStream = MainService.context.openFileInput(MainService.cache);
			if(inputStream == null) {
				Log.e("PsychSurveys", "file is null");
				return null;
			}
		} catch (FileNotFoundException e) {
			Log.e("PsychSurveys", "", e);
			return null;
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		String receiveString = "";
        StringBuilder stringBuilder = new StringBuilder();

        try {
	        while ( (receiveString = in.readLine()) != null ) {
	            stringBuilder.append(receiveString + "\n");
	        }	
	        inputStream.close();
        } catch (IOException e) {
			Log.e("PsychSurveys", "", e);
			return null;
		}

        return stringBuilder;
	}
	
	private Boolean send(StringBuilder stringBuilder) {
		try {
    		Socket socket;
    		socket = new Socket(MainService.ip, MainService.port);
    		OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
    		out.write(stringBuilder.toString());
    		out.close();
    		return true;
		} catch (UnknownHostException e) {
			Log.e("PsychSurveys", "", e);
			return false;
		} catch (IOException e) {
			Log.e("PsychSurveys", "", e);
			return false;
		}
	}
	
	@Override
	public void run() {
		StringBuilder stringBuilder = readCache();
		if (stringBuilder != null)
			if (send(stringBuilder))
				if( ! MainService.context.deleteFile(MainService.cache))
		        	Log.e("PsychSurveys", "Could not delete cache");
	}
}
