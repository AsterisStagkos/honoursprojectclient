 package com.example.androidhproject;

import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends ActionBarActivity implements OnClickListener {
	private WebSocketClient mWebSocketClient;
	public static Socket connectionSocket;
	static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
	static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
	private static final String SCOPE =
	        "androidsecure";
	public static final String PREFS_NAME = "MirrorPrefs";
	public static Connections conn = new Connections();
	String mEmail;
	private String ipAddress = "";
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
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		ipAddress = settings.getString("ip_address", "0.0.0.0");
//		final Button connectButton = (Button) findViewById(R.id.connectButton);
//		connectButton.setOnClickListener(this);
		
		final Button signInButton = (Button) findViewById(R.id.signInButton);
	//	connectButton.setOnClickListener(this);
		
		signInButton.setOnClickListener(new View.OnClickListener() {
	           public void onClick(View v) {
	        	   pickUserAccount();
	           }
	       });
		
		final Button experimentsButton = (Button) findViewById(R.id.experimentsButton);
	//	connectButton.setOnClickListener(this);
		
		experimentsButton.setOnClickListener(new View.OnClickListener() {
	           public void onClick(View v) {
	        	   Intent experimentsIntent = new Intent(MainActivity.this, ChooseExperimentsActivity.class);
	        	   startActivity(experimentsIntent);
	           }
	       });
		

	}
	private void pickUserAccount() {
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
		connectWebSocket();
		
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
	

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	

	public void connectWebSocket() {
		  URI uri;
		  
		  try {
			  Log.d("Websocket", "trying to connect");
		    uri = new URI("ws://evening-peak-5779.herokuapp.com/pingWs");
		  } catch (URISyntaxException e) {
		    e.printStackTrace();
		    return;
		  }

		  mWebSocketClient = new WebSocketClient(uri) {
		    @Override
		    public void onOpen(ServerHandshake serverHandshake) {
		      Log.d("Websocket", "Opened");
		      String heyBytes = "Hey";
		      byte[] heyinBytes = heyBytes.getBytes();
		      mWebSocketClient.send(heyinBytes);
		      mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
		    }

		    @Override
		    public void onMessage(String s) {
		      final String message = s;
		      Log.d("received message", s);
		      SearchableActivity.setDisplayData(s);
		     
		    }

		    @Override
		    public void onClose(int i, String s, boolean b) {
		      Log.d("Websocket", "Closed " + s);
		    }

		    @Override
		    public void onError(Exception e) {
		      Log.i("Websocket", "Error " + e.getMessage());
		    }
		  };
		  Log.d("Websocket", "trying to connect 2");
		  mWebSocketClient.connect();
		  Log.d("Websocket", "trying to connect 3");
		}


}
