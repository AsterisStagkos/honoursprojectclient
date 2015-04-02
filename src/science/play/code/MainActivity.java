 package science.play.code;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.java_websocket.client.WebSocketClient;

import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import science.play.code.AppDetailActivity.WaitForDownload;

import science.play.code.R;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends ActionBarActivity implements OnClickListener {
	private WebSocketClient mWebSocketClient;
	public final MainActivity mainContext = MainActivity.this;
	public static Socket connectionSocket;
	static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
	static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
	private static final String SCOPE =
	        "androidsecure";
	public static final String PREFS_NAME = "MirrorPrefs";
	public static Connections conn = new Connections();
	String mEmail;
	private String ipAddress = "";
	private boolean firstOpen = true;
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		boolean networkConnected = isNetworkAvailable();
		if (!networkConnected) {
			NetworkNotConnected newDialog = new NetworkNotConnected();
			newDialog.show(getSupportFragmentManager(), "not connected");
		} else {
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		ipAddress = settings.getString("ip_address", "0.0.0.0");
//		final Button connectButton = (Button) findViewById(R.id.connectButton);
//		connectButton.setOnClickListener(this);
		Calendar now = Calendar.getInstance();
		long calendarTime = now.getTimeInMillis() + 1000 * 61;
		Calendar inAminute = Calendar.getInstance();
		inAminute.setTimeInMillis(calendarTime);
		setAlarm(inAminute);
		if (GetUsernameTask.getDownloadToken() == null) {
			pickUserAccount();
		}
		
	/*	final Button signInButton = (Button) findViewById(R.id.signInButton);
		signInButton.setOnClickListener(new View.OnClickListener() {
	           public void onClick(View v) {
	        	   pickUserAccount();
	           }
	       }); */
		final Button searchAppStore = (Button) findViewById(R.id.searchAppStoreButton);

		searchAppStore.setOnClickListener(new View.OnClickListener() {
	           public void onClick(View v) {
	        	   onSearchRequested();
	           }
	       });
		final Button experimentsButton = (Button) findViewById(R.id.experimentsButton);

		experimentsButton.setOnClickListener(new View.OnClickListener() {
	           public void onClick(View v) {
	        	   Intent experimentsIntent = new Intent(MainActivity.this, ChooseExperimentsActivity.class);
	        	   startActivity(experimentsIntent);
	           }
	       });
		
		final Button installedAppsButton = (Button) findViewById(R.id.installedAppsButton);

		installedAppsButton.setOnClickListener(new View.OnClickListener() {
	           public void onClick(View v) {
	        	   Intent installedAppsIntent = new Intent(MainActivity.this, SeeInstalledApps.class);
	        	   startActivity(installedAppsIntent);
	           }
	       });
		
		final Button questionsButton = (Button) findViewById(R.id.checkQuestionnairesButton);
		
		questionsButton.setOnClickListener(new View.OnClickListener() {
	           public void onClick(View v) {
	        	   websockets web = new websockets();
	        	   web.setContext(getApplicationContext());
	        	   web.connectWebSocket();
	        	   while (!web.isOpen()) {
	        		   try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	   }
	        	   web.sendMessage(("CheckQuestions " + GetUsernameTask.getUniquePsuedoID()).getBytes());
	        	   Intent seequestionsIntent = new Intent(MainActivity.this, SeeQuestionnaires.class);
	        	   startActivity(seequestionsIntent); 
	           }
	       });
		
//		final Button checkDownloadsButton = (Button) findViewById(R.id.checkForDownloadButton);


//		checkDownloadsButton.setOnClickListener(new View.OnClickListener() {
//	           public void onClick(View v) {
//	        	   websockets web = new websockets();
//	        	   web.setMainActivity(MainActivity.this);
//	        	   web.connectWebSocket();
//	        	   while (!web.isOpen()) {
//	        		   try {
//						Thread.sleep(50);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//	        	   }
//	        	   web.sendMessage(("CheckDownloads " + GetUsernameTask.getUniquePsuedoID()).getBytes());
//	        	      
//	           }
//	       });
		final Button currentExperimentButton = (Button) findViewById(R.id.currentExperimentButton);

		currentExperimentButton.setOnClickListener(new View.OnClickListener() {
	           public void onClick(View v) {
	        	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	        	Intent appDetailIntent = new Intent(MainActivity.this, AppDetailActivity.class);
	            appDetailIntent.putExtra("App Name", settings.getString("ExperimentDetails.Name", "No Experiment Currently Selected"));
	       		appDetailIntent.putExtra("asset ID", settings.getString("ExperimentDetails.AssetId", "Not Applicable"));
	       		appDetailIntent.putExtra("Creator", settings.getString("ExperimentDetails.Creator", "Not Applicable"));
	       		appDetailIntent.putExtra("Description", settings.getString("ExperimentDetails.Description", "Not Applicable"));
	       		appDetailIntent.putExtra("filePath", settings.getString("ExperimentDetails.filePath", "Not Applicable"));
	       		appDetailIntent.putExtra("isExperiment", settings.getBoolean("ExperimentDetails.isExperiment", false));
	       		appDetailIntent.putExtra("isIndependent", settings.getBoolean("ExperimentDetails.isIndependent", false));
	       		appDetailIntent.putExtra("Is Current Experiment", true);
	       		startActivity(appDetailIntent);    
	           }
	       });
		
	/*	final Button testSQLbutton = (Button) findViewById(R.id.testSQLbutton);

		testSQLbutton.setOnClickListener(new View.OnClickListener() {
	           public void onClick(View v) {
	        	   EditText sqlText = (EditText) findViewById(R.id.testSQL);
	        	   String sqlInput = "executeSQL " + sqlText.getText().toString();
	        	   Log.d("sql sending", sqlInput);
	        	   websockets web = new websockets();
	        	   web.connectWebSocket();
	        	   while (!web.isOpen()) {
	       			try {
	       			Thread.sleep(50);
	       			} catch(InterruptedException e) {
	       				e.printStackTrace();
	       			}
	       		}
	        	   web.sendMessage(sqlInput.getBytes());
	           }
	       });
	       
	       */
		
	}
	}
	private  void pickUserAccount() {
	    String[] accountTypes = new String[]{"com.google"};
	    Intent intent = AccountPicker.newChooseAccountIntent(null, null,
	            accountTypes, true, null, null, null, null);
	    startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT); 
		
		} 
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
	        // Receiving a result from the AccountPicker
	        if (resultCode == RESULT_OK) {
	            mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
	            // With the account name acquired, go get the auth token
	            getUsername();
	        } else if (resultCode == RESULT_CANCELED) {
	            // The account picker dialog closed without selecting an account.
	            // Notify users that they must pick an account to proceed.
	            Toast.makeText(this, R.string.pick_account, Toast.LENGTH_SHORT).show();
	        }
	    }
	    // Later, more code will go here to handle the result from some exceptions...
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public void setReadyForDownload() {
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("Download Ready")
		        .setContentText("Your download is now ready");
		// Creates an explicit intent for an Activity in your app
		Intent appDetailIntent = new Intent(this, AppDetailActivity.class);
		 SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

		if (!settings.getString("DownloadWaiting.Name", "No Downloads Waiting").equals("No Downloads Waiting")) {		            appDetailIntent.putExtra("App Name", settings.getString("DownloadWaiting.Name", "No Downloads Waiting"));
		       		appDetailIntent.putExtra("asset ID", settings.getString("DownloadWaiting.AssetId", "Not Applicable"));
		       		appDetailIntent.putExtra("Creator", settings.getString("DownloadWaiting.Creator", "Not Applicable"));
		       		appDetailIntent.putExtra("Description", settings.getString("DownloadWaiting.Description", "Not Applicable"));
		       		appDetailIntent.putExtra("filePath", settings.getString("DownloadWaiting.filePath", "Not Applicable"));
		       		appDetailIntent.putExtra("isExperiment", settings.getBoolean("DownloadWaiting.isExperiment", false));
		       		appDetailIntent.putExtra("isIndependent", settings.getBoolean("DownloadWaiting.isIndependent", false));
		       		appDetailIntent.putExtra("wasPendingDownload", true);
		     //  		startActivity(appDetailIntent);   
		       	//	AppDetailActivity.setStaticWS(web);
	  	   } 
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(AppDetailActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(appDetailIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(1, mBuilder.build());
		
		Log.d("MainActivity", "setReadyForDownload");
  	   
	}
	public void setNotReadyForDownload() {
		Log.d("MainActivity", "setNotReadyForDownload");
		Toast.makeText(MainActivity.this, "No Downloads Waiting", Toast.LENGTH_SHORT).show();
	}
	@Override
	public boolean onSearchRequested() {
	   // pauseSomeStuff();
	    return super.onSearchRequested();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			startActivity(settingsIntent);
			return true;
		}
		else if (id == R.id.searchApp) {
			onSearchRequested();
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onClick(View v) {
     //   connectToServer();
		//connectWebSocket();
		
	}
	/**
	 * Attempts to retrieve the username.
	 * If the account is not yet known, invoke the picker. Once the account is known,
	 * start an instance of the AsyncTask to get the auth token and do work with it.
	 */
	private void getUsername() {
	    if (mEmail == null) {
	        pickUserAccount();
	    } else {
//	        if (connectionSocket != null) {
//	            
//	        } else {
//	            Toast.makeText(this, R.string.not_online, Toast.LENGTH_LONG).show();
//	        }
	        new GetUsernameTask(MainActivity.this, mEmail, SCOPE).execute();
	    }
	}
	/**
	 * This method is a hook for background threads and async tasks that need to
	 * provide the user a response UI when an exception occurs.
	 */
	public void handleException(final Exception e) {
	    // Because this call comes from the AsyncTask, we must ensure that the following
	    // code instead executes on the UI thread.
	    runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	            if (e instanceof GooglePlayServicesAvailabilityException) {
	                // The Google Play services APK is old, disabled, or not present.
	                // Show a dialog created by Google Play services that allows
	                // the user to update the APK
	                int statusCode = ((GooglePlayServicesAvailabilityException)e)
	                        .getConnectionStatusCode();
	                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
	                        MainActivity.this,
	                        REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
	                dialog.show();
	            } else if (e instanceof UserRecoverableAuthException) {
	                // Unable to authenticate, such as when the user has not yet granted
	                // the app access to the account, but the user can fix this.
	                // Forward the user to an activity in Google Play services.
	                Intent intent = ((UserRecoverableAuthException)e).getIntent();
	                startActivityForResult(intent,
	                        REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
	            }
	        }
	    });
	}
	public void connectToServer() {
		Toast.makeText(this, "Attempting to Connect!", 1000).show();
		try {
			
			SharedPreferences settings = getSharedPreferences(SettingsActivity.PREFS_NAME, 0);
			ipAddress = settings.getString("ip_address", "0.0.0.0");
			int port = Integer.parseInt(settings.getString("port", "12121"));
			Log.d("connectToServer", "got ip address: " + ipAddress);
			InetAddress address = InetAddress.getByName(ipAddress);
			connectionSocket = conn.connect(address, port);
			Toast.makeText(this, "Waiting for connection", 100).show();
			if (connectionSocket == null) {
				Toast.makeText(this, "Could not connect", 500).show();
			} else {
				Toast.makeText(this, "Connected!", 500).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void sendCheckDownloads() {
		websockets web = new websockets();
		web.setContext(getApplicationContext());
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
	 private void setAlarm(Calendar targetCal) {

		 Log.d("MainActivity", "SetAlarm called");
	    	long calendarTime = targetCal.getTimeInMillis();
	        Intent intent = new Intent(getBaseContext(), ServerPinger.class);
	        PendingIntent pendingIntent = PendingIntent.getBroadcast(
	                getBaseContext(), 1, intent, 0);
	        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	        if (targetCal.compareTo(Calendar.getInstance()) < 0) {
	        	 calendarTime = targetCal.getTimeInMillis() + 1000 * 60 * 60 * 24 * 7;
	        } else {
	        	 
	        }
	        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2000,
	                pendingIntent);
	        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(),
	        		/*miliseconds*/   1000 * /*seconds*/60 * /*minutes*/10, pendingIntent);
	        Calendar calendarTimeObj = Calendar.getInstance();
	        Log.d("MainActivity","time now: " + calendarTimeObj.toString());
	        calendarTimeObj.setTimeInMillis(calendarTime);
	        Long timeUntilAlarminSeconds = (calendarTime - System.currentTimeMillis()) / 1000;
	        Log.d("MainActivity", "time of alarm" + calendarTimeObj.toString());
	        int day = (int)TimeUnit.SECONDS.toDays(timeUntilAlarminSeconds);        
	        long hours = TimeUnit.SECONDS.toHours(timeUntilAlarminSeconds) - (day *24);
	        long minute = TimeUnit.SECONDS.toMinutes(timeUntilAlarminSeconds) - (TimeUnit.SECONDS.toHours(timeUntilAlarminSeconds)* 60);
	    //    Toast.makeText(this,"Alarm set to " + day + " days, " + hours + " hours, and " + minute +" minutes from now" , Toast.LENGTH_LONG).show();

	    } 
	 private boolean isNetworkAvailable() {
		    ConnectivityManager connectivityManager 
		          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
		}
	

	 public class NetworkNotConnected extends DialogFragment {
			private String experimentParticipating = "";


			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				// Use the Builder class for convenient dialog construction
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("You are not connected to the Internet \n"
						+ "Please get online and try again")
						.setPositiveButton("Okay",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										getActivity().finish();
										System.exit(0);
									}
								});
				// Create the AlertDialog object and return it
				return builder.create();
			}
		}
	


}
