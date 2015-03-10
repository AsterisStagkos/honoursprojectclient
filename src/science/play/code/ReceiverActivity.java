package science.play.code;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

import science.play.code.R;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ReceiverActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receiver);
		
		 final Button button = (Button) findViewById(R.id.button1);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showFile();
             }
         });
		try {
			int port = 12121;
			InetAddress address = InetAddress.getByName("172.20.123.0");
			String fileName = "application.apk";
		
			Connections conn = new Connections();	
			Socket s = null;
			do{
				s = conn.connect(address, port);
				Toast.makeText(this, "Waiting for connection", 100).show();
			} while (s == null);
			Toast.makeText(this, "Connected!", 500).show();
		//	conn.receive(s, getApplicationContext(), "", this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.receiver, menu);
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


	public void showFile() {
		try {
		Log.i("File Reading stuff", "trying to read");

		FileInputStream fIn = openFileInput("samplefile.txt");
        InputStreamReader isr = new InputStreamReader(fIn);
        /* Prepare a char-Array that will
         * hold the chars we read back in. */
        char[] inputBuffer = new char[fIn.available()];
        // Fill the Buffer with data from the file
        isr.read(inputBuffer);
        // Transform the chars to a String
        TextView text = (TextView) findViewById(R.id.textField1);
        String readString = new String(inputBuffer);
        text.setText("File contents: " + readString);
       
        // Check if we read back the same chars that we had written out

        // WOHOO lets Celebrate =)
        Log.i("File Reading stuff", "success = " + readString);

} catch (IOException ioe) {
        ioe.printStackTrace();
}
	}

	
}
