package edu.stolaf.psychsurveys;

import java.io.IOException;

import android.media.MediaRecorder;
import android.util.Log;

public class Sound implements Measurement {
	MediaRecorder recorder;
	
	@Override
	public void start() {
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOutputFile("/dev/null");
		try {
			recorder.prepare();
			recorder.start();	
			recorder.getMaxAmplitude();
		} catch (IOException e) {
			recorder = null;
			Log.e("PsychSurveys", "", e);
		} catch (RuntimeException e) {
			recorder = null;
			Log.e("PsychSurveys", "", e);
		}
	}

	@Override
	public String stop() {
		if(recorder == null) {
			return "Error recording sound.\n";
		} else {
			String sound = "SOUND: " + Integer.toString(recorder.getMaxAmplitude());
			recorder.stop();
			recorder.release();
			return sound;
		}
	}

}