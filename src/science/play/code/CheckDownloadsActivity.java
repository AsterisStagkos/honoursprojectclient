package science.play.code;





import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class CheckDownloadsActivity extends BroadcastReceiver {

String tag = "AlarmReceiver";
	
    @Override
    public void onReceive(Context context, Intent k2) {
    	SharedPreferences settings3 = context.getSharedPreferences(MainActivity.PREFS_NAME, 0);
    	boolean isDownloading = settings3.getBoolean("currentlydownloading", false);
    	Log.d("onReceive", "Alarm triggered");
        // TODO Auto-generated method stub
    	PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        // Only trigger alarm if not currently downloading
        Log.d("currentlyDownloading", isDownloading + "");
        if (!isDownloading) {
        websockets web = new websockets();
		 web.setMainActivity(CheckDownloadsActivity.this);
		 web.setContext(context);
   	web.connectWebSocket();
   	 while (!web.isOpen()) {
 		   try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 	   }
 	   web.sendMessage(("CheckDownloads " + GetUsernameTask.getUniquePsuedoID()).getBytes());
        }
    	wl.release();
    }
    
    @SuppressLint("NewApi")
	public void setReadyForDownload(Context context, String scriptName) {
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(context)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("Download Ready")
		        .setContentText("Your download is now ready");
		// Creates an explicit intent for an Activity in your app
		Intent appDetailIntent = new Intent(context, AppDetailActivity.class);
		SharedPreferences settings = context.getSharedPreferences(SettingsActivity.PREFS_NAME, 0);

	//	if (!settings.getString("DownloadWaiting.Name", "No Downloads Waiting").equals("No Downloads Waiting")) {		           
					appDetailIntent.putExtra("App Name", settings.getString("DownloadWaiting.Name", "Download Waiting"));
		       		appDetailIntent.putExtra("asset ID", settings.getString("DownloadWaiting.AssetId", "Not Applicable"));
		       		appDetailIntent.putExtra("Creator", settings.getString("DownloadWaiting.Creator", "Not Applicable"));
		       		appDetailIntent.putExtra("Description", settings.getString("DownloadWaiting.Description", "Not Applicable"));
		       		appDetailIntent.putExtra("filePath", scriptName);
		       		appDetailIntent.putExtra("isExperiment", settings.getBoolean("DownloadWaiting.isExperiment", false));
		       		appDetailIntent.putExtra("isIndependent", settings.getBoolean("DownloadWaiting.isIndependent", false));
		       		appDetailIntent.putExtra("wasPendingDownload", true);
		     //  		startActivity(appDetailIntent);   
		       	//	AppDetailActivity.setStaticWS(web);
	  	//   } 
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(appDetailIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(1, mBuilder.build());
		
		Log.d("MainActivity", "setReadyForDownload");
  	   
	}
    
    public void setNotification(Context context, String notification) {
    	NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(context)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("Notification")
		        .setContentText(notification);
    	// The stack builder object will contain an artificial back stack for the
    			// started Activity.
    			// This ensures that navigating backward from the Activity leads out of
    			// your application to the Home screen.
    			TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    			// Adds the back stack for the Intent (but not the Intent itself)
    			stackBuilder.addParentStack(AppDetailActivity.class);
    			// Adds the Intent that starts the Activity to the top of the stack
    			Intent notificationIntent = new Intent(context, NotificationActivity.class);
    			notificationIntent.putExtra("Notification", notification);
    			stackBuilder.addNextIntent(notificationIntent);
    			PendingIntent resultPendingIntent =
    			        stackBuilder.getPendingIntent(
    			            0,
    			            PendingIntent.FLAG_UPDATE_CURRENT
    			        );
    			mBuilder.setContentIntent(resultPendingIntent);
    			NotificationManager mNotificationManager =
    			    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    			// mId allows you to update the notification later on.
    			mNotificationManager.notify(1, mBuilder.build());
    }
}
