package edu.stolaf.psychsurveys;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import java.util.*;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

class Stop extends TimerTask {
	
	public void run() {
		try {
			for (Measurement measurement : Measurer.measurements) {
				measurement.stop();
			}
			
			Measurer.toUpload.put("revNo", Globals.revisionNumber);
			Globals.client.post(Measurer.context, Globals.cgi + "?survey", new StringEntity(Measurer.toUpload.toString()), "application/json", new ExceptionHandlingResponseHandler(Measurer.wakeLock) {
				public void handle(String response) throws Exception {									
					if(response.equals("No survey.\n\n")) {
						Measurer.info("No survey.");
						releaseWakeLock();
					} else {
						Globals.json = new JSONObject(response);
						
						NotificationCompat.Builder builder = new NotificationCompat.Builder(Measurer.context)
							.setSmallIcon(R.drawable.ic_launcher)  
					        .setContentTitle("Take a Survey");  
						Intent notifyIntent = new Intent(Measurer.context, SurveyActivity.class);
						notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						PendingIntent contentIntent = PendingIntent.getActivity(Measurer.context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
						builder.setContentIntent(contentIntent);
						builder.setAutoCancel(true);
						builder.setLights(Color.BLUE, 500, 500);
						long[] pattern = {500,500,500,500,500,500,500,500,500};
						builder.setVibrate(pattern);
						Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
						builder.setSound(alarmSound);
						builder.setStyle(new NotificationCompat.InboxStyle());  
						NotificationManager mNotificationManager = (NotificationManager) Measurer.context.getSystemService(Context.NOTIFICATION_SERVICE);
						mNotificationManager.notify(1, builder.build());
						
						releaseWakeLock();
					}
				}
			});
			
			
		} catch (Exception e) {
			Measurer.dragnet(e);
		}
	}			
};

public class Measurer extends RepeatingTask {

	private static Measurement[] array = {new Bluetooth(), new Accel(), new Sound(), new Loc()};
	public static Vector<Measurement> measurements = new Vector<Measurement>(Arrays.asList(array));
	public static JSONObject toUpload = new JSONObject();
	
	@SuppressLint("Wakelock")
	public void run() {		
        info("Revision Number: " + Integer.toString(Globals.revisionNumber));
        for (Measurement measurement : measurements) {
                measurement.start();
        }
        (new Timer()).schedule(new Stop(), Globals.measureLength);
    }
}
