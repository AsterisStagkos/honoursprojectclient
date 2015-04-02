package science.play.code;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ListView;
import science.play.code.R;

import com.gc.android.market.api.MarketSession;
import com.google.android.gms.common.AccountPicker;

public class SeeInstalledApps extends ActionBarActivity {
	CustomAdapter adapter;
	public SeeInstalledApps CustomListView = null;
	public ArrayList<SearchListModel> CustomListViewValuesArr = new ArrayList<SearchListModel>();
	private static boolean dataReady = false;
	private static String updateData = "";
	
    private  void pickUserAccount() {
	    String[] accountTypes = new String[]{"com.google"};
	    Intent intent = AccountPicker.newChooseAccountIntent(null, null,
	            accountTypes, true, null, null, null, null);
	    startActivityForResult(intent, MainActivity.REQUEST_CODE_PICK_ACCOUNT); 
		} 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_see_installed_apps);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		CustomListView = this;
		enableStrictMode();
		if (GetUsernameTask.getAndroidId() == null || GetUsernameTask.getSearchToken() == null) {
			pickUserAccount();
		}
		displayData();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.see_installed_apps, menu);
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
    public static void setDisplayData(String data) {
		updateData = data;
		dataReady = true;
	}
    @SuppressLint("NewApi")
	public void enableStrictMode()
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
     
        StrictMode.setThreadPolicy(policy);
    }
	public void displayData() {
		LinearLayout llActivity = (LinearLayout) findViewById(R.id.installedAppsLayout);

	    MarketSession session =MarketApi.authenticate(GetUsernameTask.getAndroidId(), false, GetUsernameTask.getSearchToken());

			ListView llNewSearch = new ListView(this);
			llNewSearch.setClickable(true);
			PackageManager pm = getPackageManager();
			List<PackageInfo> PackList = getPackageManager().getInstalledPackages(0);
			for (int i=0; i < PackList.size(); i++)
			    {	    
				PackageInfo currentData = PackList.get(i);
				if ((currentData.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
		        {
		        
                final SearchListModel sched = new SearchListModel();
			    String appName = "";
			    String assetID = "";
			    String creator = "";
			    String description = "";
			    String filePath = "";
			    String isExperimentString = "";
			    int appVersion = 0;
			    boolean needsUpdate = false;
			    boolean isIndependent = false;
			    boolean isExperiment = false;
			    appName = currentData.applicationInfo.loadLabel(pm).toString();
			    appVersion = currentData.versionCode;
			    Log.d("SeeInstalledApps appname: ", appName + " vc: " + appVersion + " vn: " + currentData.versionName);

			    MarketApi.searchUpdate(appName.replaceAll(" ", ""), session);
				while (!dataReady) {
					try {
						Thread.sleep(50);
						} catch (InterruptedException ex) {
							Thread.currentThread().interrupt();
						}
				}
				String[] appData = updateData.split(">nextapp<");
				for (int j =0; j < appData.length; j++) {
					if (appData[j].contains(appName)) {
						Log.d("Found matching app: ", appData[j] + "");
						String[] appDataSplit = appData[j].split("<");
						assetID = appDataSplit[1];
						String tempVc = appDataSplit[3];
						String tempVn = appDataSplit[2];
						for (int l = 4; l <appDataSplit.length; l++) {
							description+= appDataSplit[l];
						}
						Log.d("appDataSplit","Version Code: " + tempVc + " version name:" + tempVn);
						if (Integer.parseInt(tempVc) > appVersion) {
							needsUpdate = true;
						}
						break;
						
					}
				}
			//    currentData.versionName;
					
				   if (needsUpdate) {
					   creator = "UPDATE REQUIRED";
					   needsUpdate = true;
				   } else {
					   creator = "UP-TO-DATE";
				   }
			    
                  /******* Firstly take data in model object ******/
                   sched.setAppName(appName);
                   sched.setDescription(description);;
                   sched.setAssetId(assetID);
                   sched.setCreator(creator);
                   sched.setNeedsUpdate(needsUpdate);
                   sched.setisInstalled(true);
                   sched.setAppIcon(ChooseExperimentsActivity.drawableToBitmap(currentData.applicationInfo.loadIcon(pm)));
                   
                   Log.d("FilePath set to", filePath);
                   
                    
                /******** Take Model Object in ArrayList **********/
                CustomListViewValuesArr.add( sched );
		        }
            }
			
		//	ArrayAdapter<String>adapter=new ArrayAdapter<String>(getBaseContext(), R.layout.custom_list_layout,R.id.firstline,value);
			adapter = new CustomAdapter( CustomListView, CustomListViewValuesArr, getResources(), 3);

			llNewSearch.setAdapter(adapter);
			Log.d("display data", "before adding on click listener");
	
			llActivity.addView(llNewSearch);
				
			

	}
	public void onItemClick(int mPosition)
    {
        SearchListModel tempValues = ( SearchListModel ) CustomListViewValuesArr.get(mPosition);

        Intent appDetailIntent = new Intent(SeeInstalledApps.this, AppDetailActivity.class);
        appDetailIntent.putExtra("App Name", tempValues.getAppName());
		appDetailIntent.putExtra("asset ID", tempValues.getAssetId());
		appDetailIntent.putExtra("Creator", tempValues.getCreator());
		appDetailIntent.putExtra("Description", tempValues.getAppDescription());
		appDetailIntent.putExtra("Needs Update", tempValues.needsUpdate());
		appDetailIntent.putExtra("Is Installed", tempValues.isInstalled());
		startActivity(appDetailIntent);                 

       
    }
}
