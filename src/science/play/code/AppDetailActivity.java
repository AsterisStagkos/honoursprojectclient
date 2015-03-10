package science.play.code;

import java.io.File;
import java.net.Socket;
import java.util.StringTokenizer;

import science.play.code.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AppDetailActivity extends ActionBarActivity {

	private String appName = "";
	private String assetID = "";
	private String creator = "";
	private String appdescription = "";
	private Bitmap appIcon;
	private String filePath = "";
	private int buttonStatus = Constants.BTN_STATUS_DOWNLOAD;
	private boolean isExperiment = false;
	private boolean isIndependent = true;
	private boolean wasPendingDownload = false;
	private boolean needsUpdate = false;
	private boolean isInstalled = false;
	private boolean currentExperiment = false;
	private static Connections conn = new Connections();
	private static Socket connectionSocket;
	public static boolean currentlyDownloading = false;
	private LinearLayout detailLayout;
	private LinearLayout buttonLayout;
	private int mProgressStatus = 0;
	private int currentPacket = 0;
	private int totalPackets = 0;
	private TextView exactProgress;
	private ProgressBar mProgress;
	Button downloadButton;
	TextView appDescription;
	Handler mHandler;
	static websockets staticweb = null;
	AppDetailActivity thisActivity = this;

	public static void setStaticWS(websockets web) {
		staticweb = web;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_detail);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		connectionSocket = MainActivity.connectionSocket;
		Intent intent = getIntent();
		try {
		appName = intent.getExtras().getString("App Name");
		assetID = intent.getExtras().getString("asset ID");
		creator = intent.getExtras().getString("Creator");
		appdescription = intent.getExtras().getString("Description");
		appIcon = intent.getExtras().getParcelable("Icon");
		filePath = intent.getExtras().getString("filePath");
		isExperiment = intent.getExtras().getBoolean("isExperiment");
		isIndependent = intent.getExtras().getBoolean("isIndependent");
		wasPendingDownload = intent.getExtras()
				.getBoolean("wasPendingDownload");
		needsUpdate = intent.getExtras().getBoolean("Needs Update");
		isInstalled = intent.getExtras().getBoolean("Is Installed");
		currentExperiment = intent.getExtras().getBoolean(
				"Is Current Experiment");
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
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

		// File appFile = new File(this.getFilesDir() + "/" + appName +
		// ".apk");

		downloadButton = (Button) findViewById(R.id.download_button);
		/*
		 * If we already own the app file, simply display the button to Install
		 * it, since we don't need to download it.
		 */
		determineStatus();

		if (appName.equals("Not Participating in Any Experiments")) {
			downloadButton.setVisibility(View.INVISIBLE);
		}

		downloadButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				/*
				 * if status = Download
				 */
				if (buttonStatus == Constants.BTN_STATUS_DOWNLOAD) {
					downloadButton.setClickable(false);
					sendDownloadRequest(assetID, appName, false);

				}
				/*
				 * if status = Install
				 */
				else if (buttonStatus == Constants.BTN_STATUS_INSTALL) {
					installApp(appName);
				}
				/*
				 * if status = Participate
				 */
				else if (buttonStatus == Constants.BTN_STATUS_PARTICIPATE) {
					participateInExperiment();
				} else if (buttonStatus == Constants.BTN_STATUS_UNPARTICIPATE) {
					unparticipate();
				} else if (buttonStatus == Constants.BTN_STATUS_UPDATE) {
					sendDownloadRequest(assetID, appName, true);
				} else if (buttonStatus == Constants.BTN_STATUS_DOWNLOAD_AGAIN) {
					downloadButton.setClickable(false);
					sendDownloadRequest(assetID, appName, false);
				} else if (buttonStatus == Constants.BTN_STATUS_CANCEL) {
					mProgress.setVisibility(View.INVISIBLE);
					mProgress.setProgress(100);
					currentPacket = 0;
					exactProgress.setVisibility(View.INVISIBLE);
					exactProgress.setText("0 / 0");
					staticweb.getWebSocket().close();
					staticweb.setClosed();
					determineStatus();
				}

			}
		});
		if (wasPendingDownload) {
			websockets web = new websockets();
			web.setContext(this);
			web.connectWebSocket();
			while (!web.isOpen()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			web.sendMessage(("GetAvailableDownload " + GetUsernameTask
					.getUniquePsuedoID()).getBytes());

			awaitResponse("download", appName, web);
		} else {

		}
	}

	public void determineStatus() {
		if (!isIndependent && isExperiment) {
			SharedPreferences settings = getSharedPreferences(
					SettingsActivity.PREFS_NAME, 0);
			if (settings.getString("ExperimentDetails.AssetId",
					"Not Applicable").equals(assetID)) {
				setStatus(Constants.BTN_STATUS_UNPARTICIPATE);
			} else {
				setStatus(Constants.BTN_STATUS_PARTICIPATE);
			}
		} else if (needsUpdate) {
			setStatus(Constants.BTN_STATUS_UPDATE);
		} else if (isInstalled) {
			setStatus(Constants.BTN_STATUS_DOWNLOAD_AGAIN);
		} else if (currentExperiment) {
			setStatus(Constants.BTN_STATUS_NOBUTTON);
		} else if (!wasPendingDownload) {
			setStatus(Constants.BTN_STATUS_DOWNLOAD);
		} else {
			setStatus(Constants.BTN_STATUS_NOBUTTON);
		}
	}

	/*
	 * Set the status of the download button: 1 = Download 2 = Install 3 =
	 * Participate 4 = App Not Found
	 */
	public void setStatus(int status) {
		buttonStatus = status;
		if (status == Constants.BTN_STATUS_DOWNLOAD) {
			downloadButton.post(new Runnable() {
				public void run() {
					downloadButton.setClickable(true);
					// downloadButton.setBackgroundColor(Color.MAGENTA);
					downloadButton
							.setBackgroundResource(R.drawable.mybuttondownload);
					downloadButton.setText("Download");
				}
			});
		} else if (status == Constants.BTN_STATUS_INSTALL) {
			downloadButton.post(new Runnable() {
				public void run() {
					downloadButton.setClickable(true);
					// downloadButton.setBackgroundColor(Color.MAGENTA);
					downloadButton
							.setBackgroundResource(R.drawable.mybuttonparticipate);
					downloadButton.setText("Install");
				}
			});
		} else if (status == Constants.BTN_STATUS_PARTICIPATE) {
			downloadButton.post(new Runnable() {
				public void run() {
					downloadButton.setClickable(true);
					// downloadButton.setBackgroundColor(Color.GREEN);
					downloadButton
							.setBackgroundResource(R.drawable.mybuttonparticipate);
					downloadButton.setText("Participate");
				}
			});
		} else if (status == Constants.BTN_STATUS_APPNOTFOUND) {
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
					downloadButton
							.setBackgroundResource(R.drawable.mybuttonnoaction);
					// downloadButton.setBackgroundColor(Color
					// .parseColor("#808080"));
					downloadButton.setText("Download");
				}
			});
		} else if (status == Constants.BTN_STATUS_UNPARTICIPATE) {
			downloadButton.post(new Runnable() {
				public void run() {
					downloadButton.setClickable(true);
					// downloadButton.setBackgroundColor(Color.CYAN);
					downloadButton
							.setBackgroundResource(R.drawable.mybuttondontparticipate);
					downloadButton.setText("Stop Participating");
				}
			});
		} else if (status == Constants.BTN_STATUS_NOBUTTON) {
			downloadButton.post(new Runnable() {
				public void run() {
					downloadButton.setClickable(false);
					// downloadButton.setBackgroundColor(Color.GRAY);
					downloadButton
							.setBackgroundResource(R.drawable.mybuttonnoaction);
					downloadButton.setText("Not Applicable");
				}
			});
		} else if (status == Constants.BTN_STATUS_UPDATE) {
			downloadButton.post(new Runnable() {
				public void run() {
					downloadButton.setClickable(true);
					// downloadButton.setBackgroundColor(Color.GRAY);
					downloadButton
							.setBackgroundResource(R.drawable.mybuttondownload);
					downloadButton.setText("Update");
				}
			});
		} else if (status == Constants.BTN_STATUS_DOWNLOAD_AGAIN) {
			downloadButton.post(new Runnable() {
				public void run() {
					downloadButton.setClickable(true);
					// downloadButton.setBackgroundColor(Color.GRAY);
					downloadButton
							.setBackgroundResource(R.drawable.mybuttondownload);
					downloadButton.setText("Re-download");
				}
			});
		} else if (status == Constants.BTN_STATUS_CANCEL) {
			downloadButton.post(new Runnable() {
				public void run() {
					downloadButton.setClickable(true);
					// downloadButton.setBackgroundColor(Color.GRAY);
					downloadButton
							.setBackgroundResource(R.drawable.mybuttondontparticipate);
					downloadButton.setText("Cancel");
				}
			});
			
		}
		Log.d("setStatus", "button status = " + buttonStatus);
	}

	public void setProgressStatus(int status) {
		this.mProgressStatus = status;
	}

	public void setProgressValues(final int currentPacket,
			final int totalPackets) {
		this.currentPacket = currentPacket;
		this.totalPackets = totalPackets;
		exactProgress.post(new Runnable() {
			public void run() {
				exactProgress.setText(currentPacket + " / " + totalPackets);
			}
		});
	}

	public int getProgressStatus() {
		return this.mProgressStatus;
	}

	public void sendDownloadRequest(String assetID, String appName,
			boolean update) {
		Log.d("download request", "SENT, should go grey now");
		int whichExperiment = 0;
		if (ChooseExperimentsActivity.getExperiment1()) {
			whichExperiment = 1;
		}
		websockets web = new websockets();
		web.setContext(this);
		staticweb = web;
		web.connectWebSocket();
		while (!web.isOpen()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (!isExperiment) {
			web.sendMessage(("download " + assetID + " " + appName + " "
					+ whichExperiment + " "
					+ GetUsernameTask.getDownloadToken() + " "
					+ GetUsernameTask.getAndroidId() + " " + GetUsernameTask
					.getUniquePsuedoID()).getBytes());
			Log.d("Sent download request", "assetId: " + assetID);
			Log.d("AppDetailActivity", "sent download request");
		} else {
			web.sendMessage(("DownloadExperiment " + filePath + " " + GetUsernameTask
					.getUniquePsuedoID()).getBytes());
			Log.d("Download Request Sent", "For file " + filePath);
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		SharedPreferences settings = getSharedPreferences(
				SettingsActivity.PREFS_NAME, 0);
		String experimentParticipated = settings.getString(
				"ExperimentDetails.Name",
				"Not Participating in Any Experiments");
		if (!experimentParticipated
				.equals("Not Participating in Any Experiments")
				&& !isExperiment) {
			WaitForDownload newDialog = new WaitForDownload(
					experimentParticipated);
			newDialog.show(getSupportFragmentManager(), "waitForDownload");
			// Toast.makeText(
			// AppDetailActivity.this,
			// "You are participating in Experiment: "
			// + experimentParticipated
			// +
			// " \n Please allow some time for the application to be instrumented, \n and check back later for the download via the Main Menu",
			// 5000).show();
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("DownloadWaiting.AssetId", assetID);
			editor.putString("DownloadWaiting.Name", appName);
			editor.putString("DownloadWaiting.Creator", creator);
			editor.putString("DownloadWaiting.Description", appdescription);
			editor.putString("DownloadWaiting.filePath", filePath);
			editor.putBoolean("DownloadWaiting.isExperiment", isExperiment);
			editor.putBoolean("DownloadWaiting.isIndependent", isIndependent);
			editor.commit();
		} else {
			currentlyDownloading = true;
			awaitResponse("download", appName, web);
		}
		downloadButton.setClickable(true);
	}

	private void awaitResponse(String option, final String appName,
			final websockets web) {
		staticweb = web;
		setStatus(Constants.BTN_STATUS_CANCEL);	
		if (option.equals("download")) {
			// setInstall(false);
			exactProgress = new TextView(AppDetailActivity.this);
			exactProgress.setGravity(Gravity.RIGHT);
			exactProgress.setText("Calculating... please wait");
			mProgress = new ProgressBar(AppDetailActivity.this, null,
					android.R.attr.progressBarStyleHorizontal);
			// mProgress.setIndeterminate(false);
			mProgress.setVisibility(View.VISIBLE);
			exactProgress.setVisibility(View.VISIBLE);
			
			mProgressStatus = 0;
			mHandler = new Handler();
			buttonLayout.removeAllViews();
			buttonLayout.addView(downloadButton);
			buttonLayout.addView(mProgress);
			buttonLayout.addView(exactProgress);
			// setContentView(detailLayout);
			// final websockets web = new websockets();
			// web.connectWebSocket();
			while (!web.isOpen()) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			web.setContext(getApplicationContext());
			web.seActivity(thisActivity);
			if (!isExperiment) {
				web.setAppName(appName + ".apk");
			//	web.setContext(AppDetailActivity.this);
				Thread threadReceive = new Thread(new Runnable() {
					public void run() {
						Log.d("thread receive started", "yea it started");
						web.receive(appName, web);
					}
				});
				threadReceive.start();
			} else {
				web.setAppName(filePath);//.substring(0, filePath.length() - 4));
			//	web.setContext(AppDetailActivity.this);
				Thread threadReceive = new Thread(new Runnable() {
					public void run() {
						Log.d("thread receive started", "yea it started");
						web.receive(appName, web);
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

			receiveThread.start();

		}

	}

	public void installApp(String appName) {
		if (!isExperiment) {
			Log.d("installApp", "Trying to install app " + appName + ".apk");
			Intent promptInstall = new Intent(Intent.ACTION_VIEW)
					.setDataAndType(
							Uri.fromFile(new File(this.getFilesDir() + "/"
									+ appName + ".apk")),
							"application/vnd.android.package-archive");
			startActivity(promptInstall);
		} else {
			Log.d("installApp", "Trying to install app " + filePath);
			Intent promptInstall = new Intent(Intent.ACTION_VIEW)
					.setDataAndType(
							Uri.fromFile(new File(this.getFilesDir() + "/"
									+ filePath)),
							"application/vnd.android.package-archive");
			startActivity(promptInstall);
		}
	}

	public void participateInExperiment() {

		websockets web = new websockets();
		web.setContext(this);
		web.connectWebSocket();
		while (!web.isOpen()) {
			try {
				Thread.sleep(500);
				Log.d("sleeping", "sleeping");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		/*
		 * Tell the server that you want to participate in this experiment from
		 * now on Then Record on phone that you are participating in this
		 * experiment
		 */
		web.sendMessage(("participate " + assetID + " " + GetUsernameTask
				.getUniquePsuedoID()).getBytes());
		Log.d("Sent download request", "assetId: " + assetID + " uniqueID: "
				+ GetUsernameTask.getUniquePsuedoID());
		Log.d("AppDetailActivity", "sent download request");

		SharedPreferences settings = getSharedPreferences(
				SettingsActivity.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		editor.putString("ExperimentDetails.AssetId", assetID);
		editor.putString("ExperimentDetails.Name", appName);
		editor.putString("ExperimentDetails.Creator", creator);
		editor.putString("ExperimentDetails.Description", appdescription);
		editor.putString("ExperimentDetails.filePath", filePath);
		editor.putBoolean("ExperimentDetails.isExperiment", isExperiment);
		editor.putBoolean("ExperimentDetails.isIndependent", isIndependent);
		Toast.makeText(AppDetailActivity.this,
				"Now Participating in Experiment: " + appName, 500).show();
		editor.commit();
		setStatus(Constants.BTN_STATUS_UNPARTICIPATE);
	}

	public void unparticipate() {
		websockets web = new websockets();
		web.setContext(this);
		web.connectWebSocket();
		while (!web.isOpen()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		/*
		 * Tell the server that you DO NOT want to participate in this
		 * experiment from now on Then Record on phone that you are NOT
		 * participating in this experiment
		 */
		web.sendMessage(("participate " + "NoExperimentSelected" + " " + GetUsernameTask
				.getUniquePsuedoID()).getBytes());
		Log.d("Sent download request", "assetId: " + assetID + " uniqueID: "
				+ GetUsernameTask.getUniquePsuedoID());
		Log.d("AppDetailActivity", "sent download request");

		SharedPreferences settings = getSharedPreferences(
				SettingsActivity.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		editor.putString("ExperimentDetails.AssetId", "Not Applicable");
		editor.putString("ExperimentDetails.Name",
				"Not Participating in Any Experiments");
		editor.putString("ExperimentDetails.Creator", "Not Applicable");
		editor.putString("ExperimentDetails.Description", "Not Applicable");
		editor.putString("ExperimentDetails.filePath", "Not Applicable");
		editor.putBoolean("ExperimentDetails.isExperiment", false);
		editor.putBoolean("ExperimentDetails.isIndependent", false);
		Toast.makeText(AppDetailActivity.this,
				"Now NOT Participating in Experiment: " + appName, 500).show();
		editor.commit();
		setStatus(Constants.BTN_STATUS_PARTICIPATE);
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

	public class WaitForDownload extends DialogFragment {
		private String experimentParticipating = "";

		public WaitForDownload(String experiment) {
			experimentParticipating = experiment;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(
					"You are participating in Experiment: "
							+ experimentParticipating
							+ "\n Please allow some time for the application to be instrumented,\n You will be notified when the application is ready for download")
					.setPositiveButton("Okay",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// Yas
								}
							});
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}
}
