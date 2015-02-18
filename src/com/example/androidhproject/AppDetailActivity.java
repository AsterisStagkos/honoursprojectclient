package com.example.androidhproject;

import java.io.File;
import java.net.Socket;
import java.util.StringTokenizer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AppDetailActivity extends ActionBarActivity {

	private String appName = "";
	private String assetID = "";
	private String creator = "";
	private String appdescription = "";
	private Bitmap appIcon;
	private String filePath = "";
	private static Connections conn = new Connections();
	private static Socket connectionSocket;
	private LinearLayout detailLayout;
	private LinearLayout buttonLayout;
	private int mProgressStatus = 0;
	private boolean downloadOrInstall;
	private ProgressBar mProgress;
	Button downloadButton;
	TextView appDescription;
	Handler mHandler;
	AppDetailActivity thisActivity = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		downloadOrInstall = false;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_detail);
		connectionSocket = MainActivity.connectionSocket;
		Intent intent = getIntent();
		appName = intent.getExtras().getString("App Name");
		assetID = intent.getExtras().getString("asset ID");
		creator = intent.getExtras().getString("Creator");
		appdescription = intent.getExtras().getString("Description");
		appIcon = intent.getExtras().getParcelable("Icon");
		filePath = intent.getExtras().getString("filePath");

		detailLayout = (LinearLayout) findViewById(R.id.app_detail_layout);
		if (appIcon != null) {
			ImageView imageView = (ImageView) findViewById(R.id.appIconView);
			imageView.setImageBitmap(appIcon);
		}
		buttonLayout = (LinearLayout) findViewById(R.id.button_layout);
		TextView nameView = (TextView) findViewById(R.id.app_name);
		nameView.setText(appName);
		nameView.setTextSize(25);

		TextView creatorView = (TextView) findViewById(R.id.creator_name);
		creatorView.setText(creator);
		creatorView.setTextSize(10);
		StringTokenizer st = new StringTokenizer(appName);
		if (st.hasMoreTokens()) {
			appName = st.nextToken();
		}
		if (st.hasMoreTokens()) {
			appName += st.nextToken();
		}
		if (st.hasMoreTokens()) {
			appName += st.nextToken();
		}

		appDescription = (TextView) findViewById(R.id.app_description);
		appDescription.setText(appdescription);
		appDescription.setMovementMethod(new ScrollingMovementMethod());

		File appFile = new File(this.getFilesDir() + "/" + appName + ".apk");

		downloadButton = (Button) findViewById(R.id.download_button);
		if (1 == 1 || !appFile.exists()) {
			downloadButton.setText("Download");
			downloadButton.setBackgroundColor(Color.MAGENTA);
		} else {
			downloadButton.setText("Install");
			downloadButton.setBackgroundColor(Color.MAGENTA);
			downloadOrInstall = true;
		}

		downloadButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (!downloadOrInstall) {
					sendDownloadRequest(assetID, appName);
				} else {
					installApp(appName);
				}

			}
		});

	}

	public void setInstall(boolean shouldInstall) {
		downloadOrInstall = shouldInstall;
		if (downloadOrInstall) {
			downloadButton.post(new Runnable() {
				public void run() {
					downloadButton.setClickable(true);
					downloadButton.setBackgroundColor(Color.MAGENTA);
					downloadButton.setText("Install");
				}
			});
		} else {
			// Views can only be touched within their own threads
			// We use posts to touch views from outside the threads they were
			// created in.
			appDescription.post(new Runnable() {
				public void run() {
					appDescription.setTextSize(20);
					appDescription.setText("App Not Found");
				}
			});
			mProgress.post(new Runnable() {
				public void run() {
					mProgress.setVisibility(View.INVISIBLE);
				}
			});
			mProgressStatus = 100;
			downloadButton.post(new Runnable() {
				public void run() {
					downloadButton.setClickable(true);
					downloadButton.setBackgroundColor(Color
							.parseColor("#808080"));
					downloadButton.setText("Download");
				}
			});
		}
		Log.d("setInstall", "downloadOrInstall = " + downloadOrInstall);
	}

	public void setProgressStatus(int status) {
		this.mProgressStatus = status;
	}

	public int getProgressStatus() {
		return this.mProgressStatus;
	}

	public void sendDownloadRequest(String assetID, String appName) {
		Log.d("download request", "SENT, should go grey now");
		downloadButton.setClickable(false);
		downloadButton.setBackgroundColor(Color.parseColor("#808080"));
		int whichExperiment = 0;
		if (ChooseExperimentsActivity.getExperiment1()) {
			whichExperiment = 1;
		}
		// conn.sendData(connectionSocket, "download " + assetID + " " + appName
		// + " " + whichExperiment + " " + GetUsernameTask.getDownloadToken() +
		// " " + GetUsernameTask.getAndroidId());
		if (filePath == null) {
			SearchableActivity.web = new websockets();
			SearchableActivity.web.connectWebSocket();
			while (!SearchableActivity.web.isOpen()) {
				try {
				Thread.sleep(50);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
			SearchableActivity.web
					.sendMessage(("download " + assetID + " " + appName + " "
							+ whichExperiment + " "
							+ GetUsernameTask.getDownloadToken() + " " + GetUsernameTask
							.getAndroidId()).getBytes());
			Log.d("Sent download request", "assetId: " + assetID);
			Log.d("AppDetailActivity", "sent download request");
		} else {
			ChooseExperimentsActivity.web.sendMessage(("DownloadExperiment " + filePath).getBytes());
			Log.d("Download Request Sent", "For file " + filePath);
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		awaitResponse("download", appName);
	}

	private void awaitResponse(String option, final String appName) {
		if (option.equals("download")) {
			// setInstall(false);
			mProgress = new ProgressBar(AppDetailActivity.this, null,
					android.R.attr.progressBarStyleHorizontal);
			// mProgress.setIndeterminate(false);
			mProgress.setVisibility(View.VISIBLE);
			;
			mProgressStatus = 0;
			mHandler = new Handler();
			buttonLayout.addView(mProgress);
			// setContentView(detailLayout);
			websockets.setContext(getApplicationContext());
			websockets.seActivity(thisActivity);
			if (appIcon != null) {
			websockets.setAppName(appName + ".apk");
			Thread threadReceive = new Thread(new Runnable() {
				public void run() {
					Log.d("thread receive started", "yea it started");
					websockets.receive(appName, SearchableActivity.web);
				}
			});
			threadReceive.start();
			} else {
				websockets.setAppName(filePath.substring(0, filePath.length()-4));
				Thread threadReceive = new Thread(new Runnable() {
					public void run() {
						Log.d("thread receive started", "yea it started");
						websockets.receive(appName, ChooseExperimentsActivity.web);
					}
				});
				threadReceive.start();
			}
			
			// websockets.receive(appName);
			// conn.receive(connectionSocket, getApplicationContext(), appName +
			// ".apk", thisActivity);
			Thread receiveThread = new Thread(new Runnable() {
				public void run() {
					while (mProgressStatus < 100) {
						mProgressStatus = getProgressStatus();
						// Log.d("awaitResponse", "mProgressStatus: " +
						// mProgressStatus);
						// mProgress.setProgress(mProgressStatus);
						// Update the progress bar
						mHandler.post(new Runnable() {
							public void run() {
								// Log.d("mHandler", "progress set to: " +
								// mProgressStatus);
								mProgress.setProgress(mProgressStatus);
							}
						});
					}
					// setInstall(true);
				}
			});
			// Thread progressThread = new Thread(new Runnable() {
			// public void run() {
			// while (mProgressStatus < 100) {
			// mProgressStatus = getProgressStatus();
			// Log.d("awaitResponse", "mProgressStatus: " + mProgressStatus);
			//
			// // Update the progress bar
			// mHandler.post(new Runnable() {
			// public void run() {
			// mProgress.setProgress(mProgressStatus);
			// }
			// });
			// }
			// }
			// });
			// progressThread.start();
			receiveThread.start();

			// while (!shouldInstall) {
			// try {
			// Thread.sleep(500);
			// } catch (InterruptedException ex) {
			// Thread.currentThread().interrupt();
			// }
			// }
			// Log.d("AppDetailActivity", "shouldInstall is true");
			// Button installButton = new Button(this);
			// installButton.setText("Install");
			// installButton.setOnClickListener(new View.OnClickListener() {
			// public void onClick(View v) {
			// installApp(appName);
			// }
			// });
			// detailLayout.addView(installButton);

		}

	}

	public void installApp(String appName) {
		if (appIcon != null) {
		Log.d("installApp", "Trying to install app " + appName + ".apk");
		Intent promptInstall = new Intent(Intent.ACTION_VIEW).setDataAndType(
				Uri.fromFile(new File(this.getFilesDir() + "/" + appName
						+ ".apk")), "application/vnd.android.package-archive");
		startActivity(promptInstall);
		} else {
			Log.d("installApp", "Trying to install app " + filePath);
			Intent promptInstall = new Intent(Intent.ACTION_VIEW).setDataAndType(
					Uri.fromFile(new File(this.getFilesDir() + "/" + filePath)), "application/vnd.android.package-archive");
			startActivity(promptInstall);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_detail, menu);
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
