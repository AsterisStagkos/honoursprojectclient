package science.play.code;

import java.util.ArrayList;
import java.util.StringTokenizer;

import science.play.code.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ListView;

public class SeeQuestionnaires extends ActionBarActivity {
	CustomAdapter adapter;
	public SeeQuestionnaires CustomListView = null;
	public ArrayList<SearchListModel> CustomListViewValuesArr = new ArrayList<SearchListModel>();
	private static boolean dataReady = false;
	private static String updateData = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_see_questionnairs);
		CustomListView = this;
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	//	enableStrictMode();
		while (!dataReady) {
			try {
				Thread.sleep(50);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
		}
		displayData(updateData);

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
    public void enableStrictMode()
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
     
        StrictMode.setThreadPolicy(policy);
    }
	public void displayData(String data) {
		LinearLayout llActivity = (LinearLayout) findViewById(R.id.questionsLayout);
		String value[] = data.split(">nextapp<");
			
			ListView llNewSearch = new ListView(this);
			llNewSearch.setClickable(true);
			
			for (int i = 0; i < value.length; i++) {
				String currentData = value[i];
                final SearchListModel sched = new SearchListModel();
                     
                Log.d("questionnaires", currentData);
                StringTokenizer resultToken = new StringTokenizer(currentData);
			    String appName = "";
			    String description = "";
			    if (resultToken.hasMoreTokens()) {
			        appName = resultToken.nextToken();
			    }
			    if (resultToken.hasMoreTokens()) {
			    	description = resultToken.nextToken();
			    }
			   
                  /******* Firstly take data in model object ******/
                   sched.setAppName(appName);
                   sched.setDescription(description);
                   sched.setAppIcon(ChooseExperimentsActivity.drawableToBitmap(getResources().getDrawable(R.drawable.questionmark)));
                   
                   
                    
                /******** Take Model Object in ArrayList **********/
                CustomListViewValuesArr.add( sched );
            }
			
		//	ArrayAdapter<String>adapter=new ArrayAdapter<String>(getBaseContext(), R.layout.custom_list_layout,R.id.firstline,value);
			adapter = new CustomAdapter( CustomListView, CustomListViewValuesArr, getResources(), 4);

			llNewSearch.setAdapter(adapter);
			Log.d("display data", "before adding on click listener");
	
			llActivity.addView(llNewSearch);
				
			

	}
	public void onItemClick(int mPosition)
    {
        SearchListModel tempValues = ( SearchListModel ) CustomListViewValuesArr.get(mPosition);
        String url = tempValues.getAppDescription();
        Log.d("url: ", url);
        if (!url.startsWith("http://") && !url.startsWith("https://"))
        	   url = "http://" + url;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);              

       
    }
}
