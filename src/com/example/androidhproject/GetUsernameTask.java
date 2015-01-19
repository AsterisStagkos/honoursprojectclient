package com.example.androidhproject;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

public class GetUsernameTask extends AsyncTask{
    Activity mActivity;
    String mScope;
    String mEmail;
    static String searchToken;
    static String downloadToken;
    static String androidId;
    private static final Uri URI = Uri.parse("content://com.google.android.gsf.gservices");
    private static final String ID_KEY = "android_id";

    GetUsernameTask(Activity activity, String name, String scope) {
        this.mActivity = activity;
        this.mScope = scope;
        this.mEmail = name;
    }

    /**
     * Executes the asynchronous job. This runs when you call execute()
     * on the AsyncTask instance.
     */
	@Override
	protected Void doInBackground(Object... params) {
		 try {
			 Log.d("In Background", "fetching token");
	         fetchToken();
	           
	        } catch (IOException e) {
	            // The fetchToken() method handles Google-specific exceptions,
	            // so this indicates something went wrong at a higher level.
	            // TIP: Check for network connectivity before starting the AsyncTask.
	        }
	        
		
		AccountManager am = AccountManager.get(mActivity.getBaseContext()); 
		Account[] accounts = am.getAccountsByType("com.google"); 
		if(accounts.length > 0){ 
		        try { 
		                AccountManagerFuture<Bundle> accountManagerFuture = 
		am.getAuthToken(accounts[0], "android", null, mActivity, null, 
		null); 
		                Bundle authTokenBundle = accountManagerFuture.getResult(); 
		                String authToken = 
		authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN).toString(); 
		                searchToken = authToken;
		               // Toast.makeText(mActivity, "Token set to " + authToken, Toast.LENGTH_SHORT).show();

 
		        } catch (Exception e) { 
		                Log.d("error with token", e.getMessage()); 
	
		        }  
	}
		return null; 
	} 
	public static String getDownloadToken() {
		return downloadToken;
	}
	public static String getSearchToken() {
		return searchToken;
	}
	public static String getAndroidId() {
		return androidId;
	}
    /**
     * Gets an authentication token from Google and handles any
     * GoogleAuthException that may occur.
     */
    protected String fetchToken() throws IOException {
        try {
        	Log.d("Google", "trying get token");
        	GoogleAuthUtil.clearToken(mActivity, downloadToken);
            String googleToken =  GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
            String id = getAndroidId(mActivity);
            Log.d("Google", "Google Token = " + googleToken);
            downloadToken = googleToken;
            androidId = id;
            return googleToken;
        } catch (UserRecoverableAuthException userRecoverableException) {
            // GooglePlayServices.apk is either old, disabled, or not present
            // so we need to show the user some UI in the activity to recover.
            ((MainActivity) mActivity).handleException(userRecoverableException);
        } catch (GoogleAuthException fatalException) {
            // Some other type of unrecoverable exception has occurred.
            // Report and log the error as appropriate for your app.
        }
        return null;
    }
    String getAndroidId(Context ctx) {
        String[] params = { ID_KEY };
        Cursor c = ctx.getContentResolver()
                .query(URI, null, null, params, null);

        if (!c.moveToFirst() || c.getColumnCount() < 2) {
            return null;
        }

        try {
            return Long.toHexString(Long.parseLong(c.getString(1)));
        } catch (NumberFormatException e) {
            Log.i("getAndroidId", e.getMessage());
            return null;
        }
    }



}