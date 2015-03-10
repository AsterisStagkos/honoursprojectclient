package science.play.code;

import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.java_websocket.client.WebSocketClient;

import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import science.play.code.R;

import com.gc.android.market.api.MarketSession;
import com.google.android.gms.common.AccountPicker;

public class SearchableActivity extends ActionBarActivity implements OnClickListener{
	public static boolean shouldInstall = false;
	private static Socket connectionSocket;
	private static String query = "";
	private static Connections conn = new Connections();
	private static String authenticationToken = "invalid";
	private WebSocketClient mWebSocketClient;
	private static String displayData = "";
	private static MarketSession session;
	public static boolean dataReady = false;
	
	ListView list;
    CustomAdapter adapter;
    public  SearchableActivity CustomListView = null;
    public  ArrayList<SearchListModel> CustomListViewValuesArr = new ArrayList<SearchListModel>();
    
    public static void setDisplayData(String data) {
		displayData = data;
		dataReady = true;
	}
    public void enableStrictMode()
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
     
        StrictMode.setThreadPolicy(policy);
    }
    private  void pickUserAccount() {
	    String[] accountTypes = new String[]{"com.google"};
	    Intent intent = AccountPicker.newChooseAccountIntent(null, null,
	            accountTypes, true, null, null, null, null);
	    startActivityForResult(intent, MainActivity.REQUEST_CODE_PICK_ACCOUNT); 
		
		} 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_searchable);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		enableStrictMode();
		if (GetUsernameTask.getAndroidId() == null || GetUsernameTask.getSearchToken() == null) {
			pickUserAccount();
		}
		session = MarketApi.authenticate(GetUsernameTask.getAndroidId(), false, GetUsernameTask.getSearchToken());
		
		websockets web = new websockets();
		web.setContext(getApplicationContext());
		CustomListView = this;
		 // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    //  query = intent.getStringExtra(SearchManager.QUERY) + " " + GetUsernameTask.getSearchToken() + " " + GetUsernameTask.getAndroidId();
	    query = intent.getStringExtra(SearchManager.QUERY);
	    query = query.replaceAll(" ", "");
	    connectionSocket = MainActivity.connectionSocket;
//	      try {
//				Thread.sleep(100);
//				} catch (InterruptedException ex) {
//					Thread.currentThread().interrupt();
//				}
	      sendQuery();
	    }
//	   final Button connectButton = (Button) findViewById(R.id.connectButton);
//	   connectButton.setOnClickListener(this);
//        final Button queryButton = (Button) findViewById(R.id.queryButton);
//        queryButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                sendQuery();
//                Toast.makeText(getApplicationContext(), "Sent query: " + query, 100).show();
//            }
//        });
	}
	public static void setAuthToken(String authToken) {
		authenticationToken = authToken;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.searchable, menu);
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
	
	public void connectToServer() {
		Toast.makeText(this, "Attempting to Connect!", 1000).show();
		try {
			int port = 12121;
			InetAddress address = InetAddress.getByName("192.168.0.10");
			connectionSocket = conn.connect(address, port);
			Toast.makeText(this, "Waiting for connection", 100).show();
			Toast.makeText(this, "Connected!", 500).show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void sendQuery() {
	//	conn.sendData(connectionSocket, "query " + query);
	/*	try {
			Thread.sleep(3000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		byte[] queryMessage = ("query " + query).getBytes();
		web.sendMessage(queryMessage);
		Log.d("SearchAbleActivity", "sent data");
		try {
			Thread.sleep(3500);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			*/
		MarketApi.searchApp(query, session);
		while (!dataReady) {
			try {
				Thread.sleep(50);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
		}
		displayData(displayData);
		dataReady = false;
		//awaitResponse("query", "");
		
	}
	public void sendDownloadRequest(String assetID, String appName) {
		conn.sendData(connectionSocket, "download " + assetID + " " + appName);
		Log.d("SearchAbleActivity", "sent download request");
		try {
			Thread.sleep(2000);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		awaitResponse("download", appName);
	}
	public void awaitResponse(String option, String appName) {
		if (option.equals("query")) {
		String results = "";
		Log.d("SearchableActivity", "trying receiveData");
		results = conn.receiveData(connectionSocket);
		try {
			Thread.sleep(500);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		displayData(results);
		} else if (option.equals("download")) {
			//conn.receive(connectionSocket, getApplicationContext(), appName + ".apk", this);
			try {
				Thread.sleep(500);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}

		} 
		
	}
	public void setInstall(boolean shouldInstall) {
		this.shouldInstall = shouldInstall;
	}
	public void installApp(String appName) {
		Log.d("installApp", "Trying to install app " + appName + ".apk");
		Intent promptInstall = new Intent(Intent.ACTION_VIEW)
	    .setDataAndType(Uri.fromFile(new File(this.getFilesDir() +"/" + appName + ".apk")), 
	                    "application/vnd.android.package-archive");
		startActivity(promptInstall);
	}
	public void displayData(String data) {
		LinearLayout llActivity = (LinearLayout) findViewById(R.id.searchLayout);
		ListView llNewSearch = new ListView(this);
		llNewSearch.setClickable(true);
		
		ArrayList<String> installedApps = new ArrayList<String>();
		PackageManager pm = getPackageManager();
		List<PackageInfo> PackList = getPackageManager().getInstalledPackages(0);
		for (int i=0; i < PackList.size(); i++)
		{	    
			PackageInfo currentData = PackList.get(i);
			if ((currentData.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
	        {
				installedApps.add(currentData.applicationInfo.loadLabel(pm).toString());
	        }
		}
		
		
		String value[] = data.split(">nextapp<");
	
			
			for (int i = 0; i < value.length; i++) {
                
                final SearchListModel sched = new SearchListModel();
                     
                
                StringTokenizer resultToken = new StringTokenizer(value[i], "<");
			    String appName = "";
			    String assetID = "";
			    String creator = "";
			    String description = "";
			    if (resultToken.hasMoreTokens()) {
			        appName = resultToken.nextToken();
			    }
			    if (resultToken.hasMoreTokens()) {
			    	assetID = resultToken.nextToken();
			    }
			    if (resultToken.hasMoreTokens()) {
			    	creator = resultToken.nextToken();
			    }
			    if (resultToken.hasMoreTokens()) {
			    	description = resultToken.nextToken();
			    }
                  /******* Firstly take data in model object ******/
			    String[] appNameArr = appName.split(" ");
			    String firstPart = (appNameArr.length > 0) ? appNameArr[0] : "";
			    if (installedApps.contains(firstPart)) {
			    	sched.setisInstalled(true);
			    } else {
			    	sched.setisInstalled(false);
			    }
                   sched.setAppName(appName);
                   sched.setDescription(description);;
                   sched.setAssetId(assetID);
                   sched.setCreator(creator);
                   Bitmap icon = MarketApi.getImage(session, assetID);
                   if (icon != null) {
                	   sched.setAppIcon(icon);  
                   }
                   
                    
                /******** Take Model Object in ArrayList **********/
                CustomListViewValuesArr.add( sched );
            }
			
		//	ArrayAdapter<String>adapter=new ArrayAdapter<String>(getBaseContext(), R.layout.custom_list_layout,R.id.firstline,value);
			adapter = new CustomAdapter( CustomListView, CustomListViewValuesArr, getResources(), 1);

			llNewSearch.setAdapter(adapter);
			Log.d("display data", "before adding on click listener");
	
			llActivity.addView(llNewSearch);
				
			

	}

	@Override
	public void onClick(View v) {
		
	}
	 public void onItemClick(int mPosition)
     {
         SearchListModel tempValues = ( SearchListModel ) CustomListViewValuesArr.get(mPosition);

         Intent appDetailIntent = new Intent(SearchableActivity.this, AppDetailActivity.class);
         appDetailIntent.putExtra("App Name", tempValues.getAppName());
		 appDetailIntent.putExtra("asset ID", tempValues.getAssetId());
		 appDetailIntent.putExtra("Creator", tempValues.getCreator());
		 appDetailIntent.putExtra("Description", tempValues.getAppDescription());
		 appDetailIntent.putExtra("Icon", tempValues.getAppIcon());
		 appDetailIntent.putExtra("Is Installed", tempValues.isInstalled());
		 startActivity(appDetailIntent);                 

        
     }
}
