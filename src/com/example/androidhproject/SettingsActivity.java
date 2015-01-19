package com.example.androidhproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends ActionBarActivity {
	public static final String PREFS_NAME = "MirrorPrefs";
	
	private String ipAddress;
	private String email;
	private String password;
	private String port;
	EditText ipAddressView;
	EditText portView;
	EditText emailView;
	EditText passwordView;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		ipAddressView = (EditText) findViewById(R.id.ip_address);
		portView = (EditText) findViewById(R.id.port);
		emailView = (EditText) findViewById(R.id.email_address);
		passwordView = (EditText) findViewById(R.id.password);
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		
		ipAddressView.setHint(settings.getString("ip_address", "0.0.0.0"));
		portView.setHint(settings.getString("port", "0.0.0.0"));
		emailView.setHint(settings.getString("email", "email"));
		passwordView.setHint("password");
        
		Button submitButton = (Button) findViewById(R.id.submit_button);
		submitButton.setText("Submit");
		
		
		submitButton.setOnClickListener(new View.OnClickListener() {
	           public void onClick(View v) {
	        	ipAddress = ipAddressView.getText().toString();
	        	port = portView.getText().toString();
	       		email = emailView.getText().toString();
	       		password = passwordView.getText().toString();
	       		Log.v("ipAddress", ipAddressView.getText().toString());
	       		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	       		SharedPreferences.Editor editor = settings.edit();
	       		
	       		editor.putString("ip_address", ipAddress);
	       		editor.putString("port", port);
	       		editor.putString("email", email);
	       		editor.putString("password", password);
	       		Toast.makeText(SettingsActivity.this, "Changes Saved", 500).show();
	       		editor.commit();
	       		
	       		Toast.makeText(SettingsActivity.this, "Ip Address Saved: " + settings.getString("ip_address", "0.0.0.0") + " port: " + settings.getString("port", "5000"), 500).show();
	             
	           }
	       });
		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
