package com.example.androidhproject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

import android.content.Context;
import android.util.Log;

public class Connections {
	static Socket connSocket;
	static boolean connected;
	private static String queryData = "lol";
	

	public Socket connect(final InetAddress address, final int port) {
		Log.d("Connections", "Connection Attempt");
		connSocket = null;
		connected = false;
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Log.d("Connections", "Thread Started");
				try {
					String sentence = "lol";
					String modifiedSentence;
					// BufferedReader inFromUser = new BufferedReader( new
					// InputStreamReader(System.in));
					connSocket = new Socket(address, port);
					DataOutputStream outToServer = new DataOutputStream(
							connSocket.getOutputStream());
					BufferedReader inFromServer = new BufferedReader(
							new InputStreamReader(connSocket.getInputStream()));
					sentence = "connected";
					outToServer.writeBytes(sentence + '\n');
					modifiedSentence = inFromServer.readLine();
					Log.d("Connections", "FROM SERVER: " + modifiedSentence);
					if (modifiedSentence.equals("CONNECTED")) {
						connected = true;
					}
				} catch (Exception e) {
					e.printStackTrace();

				}
			}
		});
		thread.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		return connSocket;
	}

	public void sendData(final Socket connectionSocket, String data) {
		try {
			String sentence = data;
			// BufferedReader inFromUser = new BufferedReader( new
			// InputStreamReader(System.in));
			DataOutputStream outToServer = new DataOutputStream(
					connectionSocket.getOutputStream());
			// BufferedReader inFromServer = new BufferedReader(new
			// InputStreamReader(connectionSocket.getInputStream()));
			outToServer.writeBytes(sentence + '\n');
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String receiveData(final Socket connectionSocket) {
		queryData = "";
		Log.d("receiveData", "Attempting to receive data ");
		Thread thread2 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				Log.d("receiveData", "run");
				try {
					Log.d("receiveData", "try");
					DataInputStream input = new DataInputStream(
							connectionSocket.getInputStream());
					Log.d("receiveData", "opened input stream");
					int dataSize = input.readInt();
					Log.d("receiveData", "dataSize: " + dataSize);
					if (dataSize == 0) {
						dataSize = input.readInt();
					}
					Log.d("receiveData", "dataSize: " + dataSize);
					byte[] result = new byte[dataSize];
					for (int i = 0; i < dataSize; i++) {
						result[i] = input.readByte();
					}
					queryData = new String(result);
					Log.d("receiveData", "data in: " + queryData);

				} catch (Exception e) {
					Log.d("receiveData", "exception" + e.toString());
					e.printStackTrace();

				}
			}
		});
		thread2.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		return queryData;
	}

	public void receive(final Socket clientSocket, final Context context,
			final String fileName, final AppDetailActivity appActivity) {
		Thread thread = new Thread(new Runnable() {
			private int fileSize = 0;
			private byte[] fileBytes;
			
			public void setFileSize(int size) {
				this.fileSize = size;
			}
			
			public void setFileBytes(byte[] bytes) {
				this.fileBytes = bytes;
			}
			@Override
			public void run() {
				try {
					int bytesRead;
					int current = 0;
					Log.d("receive", "Receive Thread Started");
					Log.d("receive", "opened socket");
//					DataInputStream input = new DataInputStream(clientSocket
//							.getInputStream());
					Log.d("receive", "opened input stream");
					DataOutputStream output = new DataOutputStream(clientSocket
							.getOutputStream());
					Log.d("receive", "opened data output stream");
					FileOutputStream fOut = context.openFileOutput(fileName,
							context.MODE_WORLD_READABLE);
					Log.d("receive", "opened file output stream");
					BufferedOutputStream bos = new BufferedOutputStream(fOut);

					// OutputStreamWriter osw = new OutputStreamWriter(fOut);
					Log.d("receive", "opened osw");
					// Step 1 read length
				//	long fileLength = input.readLong();
					long fileLength = websockets.fileSizeStatic;
					Log.d("receive", "file length: " + fileLength);
					if (fileLength > 0) {
						int fileLengthInt = (int) fileLength;
						Log.d("receive", "file length: " + fileLengthInt);
						// Step 2 read data
						byte[] data = new byte[fileLengthInt];
						data = websockets.fileBytesStatic;
					//	bytesRead = input.read(data, 0, data.length);
						bytesRead = fileSize;
						current = bytesRead;

						do {
//							bytesRead = input.read(data, current,
//									(data.length - current));
							bytesRead = bytesRead - 100;
							if (bytesRead > 0) {
								current += bytesRead;
							}
							appActivity
									.setProgressStatus((int) (((double) current / (double) fileLengthInt) * 100));
							// Log.d("receive", "Progress status set to " +
							// (int) (((double) current /(double) fileLengthInt)
							// * 100));
							// Log.d("receive", "Written bytes: " + current );
						} while (bytesRead > 0);
						bos.write(data, 0, current);
						bos.flush();
						// for (int i=0; i<fileLength; i++) {
						// data[i] = input.readByte();
						// Log.d("receive", "read byte: " + i);
						// }
						// input.read(data);
						// input.read(data, 0, fileLengthInt);
						// String st = new String(data);
						// fOut.write(data);
						// osw.writ;
						// osw.flush();
						// osw.close();
						fOut.close();
						Log.d("receive", "Written file " + fileName);
						// SearchableActivity.shouldInstall = true;

						appActivity.setInstall(true);
					} else {
						appActivity.setInstall(false);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

}
