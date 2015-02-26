package com.example.androidhproject;



import java.util.ArrayList;
import java.util.StringTokenizer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemSelectedListener;

public class ChooseExperimentsActivity extends ActionBarActivity implements OnItemSelectedListener {

	private static boolean experiment1;
	private static boolean experiment2;
	private static boolean experiment3;
	public static String displayData = "";
	CustomAdapter adapter;
	public  ChooseExperimentsActivity CustomListView = null;
	public  ArrayList<SearchListModel> CustomListViewValuesArr = new ArrayList<SearchListModel>();
	public static websockets web;
	    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_experiments);
		CustomListView = this;
		web.setContext(getApplicationContext());
		web = new websockets();
		web.connectWebSocket();
		try {
			Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		web.sendMessage("Experiment".getBytes());
		try {
		Thread.sleep(3000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		displayData(displayData);
	}

	public static void setDisplayData(String data) {
		displayData = data;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.choose_experiments, menu);
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

	public void displayData(String data) {
		LinearLayout llActivity = (LinearLayout) findViewById(R.id.experimentsLayout);
		data = data.substring(11);
		String value[] = data.split(">nextapp<");
			
			ListView llNewSearch = new ListView(this);
			llNewSearch.setClickable(true);
			
			for (int i = 0; i < value.length; i++) {
                
                final SearchListModel sched = new SearchListModel();
                     
                
                StringTokenizer resultToken = new StringTokenizer(value[i], "<");
			    String appName = "";
			    String assetID = "";
			    String creator = "";
			    String description = "";
			    String filePath = "";
			    String isExperimentString = "";
			    boolean isIndependent = false;
			    boolean isExperiment = false;
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
			    if (resultToken.hasMoreTokens()) {
			    	filePath = resultToken.nextToken();
			    }
			    if (resultToken.hasMoreTokens()) {
			    	isExperimentString = resultToken.nextToken();
			    	Log.d("isExperimentString: " ,"is experiment string: " + isExperimentString);
			    	if (isExperimentString.equals("true")) {
			    		isIndependent = true;
			    	}
			    	isExperiment = true;
			    }
                  /******* Firstly take data in model object ******/
                   sched.setAppName(appName);
                   sched.setDescription(description);;
                   sched.setAssetId(assetID);
                   sched.setCreator(creator);
                   sched.setFilePath(filePath);
                   sched.setIsExperiment(isExperiment);
                   sched.setIsIndependent(isIndependent);
                   
                   Log.d("FilePath set to", filePath);
                   
                    
                /******** Take Model Object in ArrayList **********/
                CustomListViewValuesArr.add( sched );
            }
			
		//	ArrayAdapter<String>adapter=new ArrayAdapter<String>(getBaseContext(), R.layout.custom_list_layout,R.id.firstline,value);
			adapter = new CustomAdapter( CustomListView, CustomListViewValuesArr, getResources(), 2);

			llNewSearch.setAdapter(adapter);
			Log.d("display data", "before adding on click listener");
	
			llActivity.addView(llNewSearch);
				
			

	}
	
	public static boolean getExperiment1() {
		return experiment1;
	}
	public void onCheckboxClicked(View view) {
	    // Is the view now checked?
	    boolean checked = ((CheckBox) view).isChecked();
	    
	    // Check which checkbox was clicked
	    switch(view.getId()) {
//	        case R.id.experiment_one:
//	            if (checked) {
//	            	experiment1 = true;
//	            }
//	            else {
//	            	experiment1 = false;
//	            }
//	           
//	            break;
//	        case R.id.experiment_two:
//	        	if (checked) {
//	        		experiment2 = true;
//	        	} else {
//	        		experiment2 = false;
//	        	}
//	        	break;
//	        case R.id.experiment_three:
//	        	if (checked) {
//	        		experiment3 = true;
//	        	} else {
//	        		experiment3 = false;
//	        	}
	    	default:
	        	break;
	        // TODO: Veggie sandwich
	    }
	}
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
	public void onItemClick(int mPosition)
    {
        SearchListModel tempValues = ( SearchListModel ) CustomListViewValuesArr.get(mPosition);

        Intent appDetailIntent = new Intent(ChooseExperimentsActivity.this, AppDetailActivity.class);
        appDetailIntent.putExtra("App Name", tempValues.getAppName());
		appDetailIntent.putExtra("asset ID", tempValues.getAssetId());
		appDetailIntent.putExtra("Creator", tempValues.getCreator());
		appDetailIntent.putExtra("Description", tempValues.getAppDescription());
		appDetailIntent.putExtra("filePath", tempValues.getFilePath());
		appDetailIntent.putExtra("isExperiment", tempValues.isExperiment());
		appDetailIntent.putExtra("isIndependent", tempValues.isIndependent());
		startActivity(appDetailIntent);                 

       
    }
}
