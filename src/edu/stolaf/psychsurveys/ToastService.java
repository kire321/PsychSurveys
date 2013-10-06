package edu.stolaf.psychsurveys;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Binder;
import android.widget.Toast;
import java.util.*;

public class ToastService extends Service {
	private Timer timer = new Timer();
	//private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    //private int NOTIFICATION = R.string.local_service_started;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class ToastBinder extends Binder {
        ToastService getService() {
            return ToastService.this;
        }
    }

    @Override
    public void onCreate() {
    	Toast.makeText(this, "Service created", Toast.LENGTH_SHORT).show();
    	//mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        //showNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
    	
    	timer.scheduleAtFixedRate(new TimerTask() {
    	
    		@Override
    		public void run() {
    			Toast.makeText(getApplicationContext(), "So much toast", Toast.LENGTH_SHORT).show();
    		}
    	}, 0, 10*1000);
    	
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        //mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new ToastBinder();

    /**
     * Show a notification while this service is running.
     */
    /*private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.local_service_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.stat_sample, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, LocalServiceActivities.Controller.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.local_service_label),
                       text, contentIntent);

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }*/


}
