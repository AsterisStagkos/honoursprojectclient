package com.example.androidhproject;



import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.AdapterView.OnItemSelectedListener;

public class ChooseExperimentsActivity extends ActionBarActivity implements OnItemSelectedListener {

	private static boolean experiment1;
	private static boolean experiment2;
	private static boolean experiment3;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_experiments);
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

	public static boolean getExperiment1() {
		return experiment1;
	}
	public void onCheckboxClicked(View view) {
	    // Is the view now checked?
	    boolean checked = ((CheckBox) view).isChecked();
	    
	    // Check which checkbox was clicked
	    switch(view.getId()) {
	        case R.id.experiment_one:
	            if (checked) {
	            	experiment1 = true;
	            }
	            else {
	            	experiment1 = false;
	            }
	           
	            break;
	        case R.id.experiment_two:
	        	if (checked) {
	        		experiment2 = true;
	        	} else {
	        		experiment2 = false;
	        	}
	        	break;
	        case R.id.experiment_three:
	        	if (checked) {
	        		experiment3 = true;
	        	} else {
	        		experiment3 = false;
	        	}
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
}
