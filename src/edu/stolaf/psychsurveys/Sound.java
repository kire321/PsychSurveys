package edu.stolaf.psychsurveys;

import java.io.IOException;

import android.media.MediaRecorder;

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
			Measurer.error("", e);
		} catch (RuntimeException e) {
			recorder = null;
		}
	}

	@Override
	public void stop() throws Exception {
		if(recorder != null) {
			Measurer.toUpload.put("sound", recorder.getMaxAmplitude());
			recorder.stop();
			recorder.release();
		}
	}

}
