package com.example.androidhproject;

import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class websockets {
	private static WebSocketClient mWebSocketClient;
	private static Thread thread;
	public static int fileSizeStatic;
	public static byte[] fileBytesStatic;
	static byte[] nextPacket;
	private static int noOfPackets = 100;
	private static boolean packetsSet = false;
	static AppDetailActivity whichActivity;
	static Context appContext;
	static String appName;
	private boolean isOpen = false;
	static String filePath;
	static boolean startDownload = false;
	static TreeMap<Integer, byte[]> packets = new TreeMap<Integer, byte[]>(); 
	public static void setThread(Thread newThread) {
		thread = newThread;
	}
	
//	public websockets(WebSocketClient mWebSocketClient) {
//		this.mWebSocketClient = mWebSocketClient;
//	}
	
	public boolean isOpen() {
		return isOpen;
	}
	public static void setContext(Context context) {
		appContext = context;
	}
	public static void setFilePath(String filepath) {
		filePath = filepath;
	}
	public static void seActivity(AppDetailActivity activity) {
		whichActivity = activity;
	}
	public static void setAppName(String appname) {
		appName = appname;
	}
	public void setNoOfPackets(int number) {
		noOfPackets = number;
		packetsSet = true;
	}
	public void sendMessage(byte[] message) {
		//Log.d("message to send: ", message);
		//Log.d("socket is: " , mWebSocketClient.toString());
		try {
		mWebSocketClient.send(message);
		} catch (WebsocketNotConnectedException e) {
			 Toast.makeText(appContext, "Web not connected, try again in 10 seconds", Toast.LENGTH_SHORT).show();
			 e.printStackTrace();
			
		}
		
	}
	public void connectWebSocket() {
		  URI uri;
		  
		  try {
			  Log.d("Websocket", "trying to connect");
		    uri = new URI("ws://evening-peak-5779.herokuapp.com/pingWs");
		  } catch (URISyntaxException e) {
		    e.printStackTrace();
		    return;
		  }

		  mWebSocketClient = new WebSocketClient(uri) {
		    @Override
		    public void onOpen(ServerHandshake serverHandshake) {
		      Log.d("Websocket", "Opened");
		      isOpen = true;
		      String heyBytes = "Hey";
		      byte[] heyinBytes = heyBytes.getBytes();
		      mWebSocketClient.send(heyinBytes);
		      mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
		    }
		    @Override
		    public void onMessage(ByteBuffer bytes) {
		    byte[] messageReceived = new byte[bytes.capacity()];
		    bytes.get(messageReceived, 0, bytes.capacity());
		      String message = new String(messageReceived);
		   //   Log.d("received byte message", message);
		      StringTokenizer messageTok = new StringTokenizer(message);
		      String firstToken = "";
		      if (messageTok.hasMoreTokens()) {
		    	  firstToken = messageTok.nextToken();
		      }
		      if (firstToken.equals("query")) {
		    	  Log.d("received byte message", message);
		    	  message = message.substring(5);
			      SearchableActivity.setDisplayData(message);
		      } else if (firstToken.equals("experiments")) {
		    	  ChooseExperimentsActivity.setDisplayData(message);
		      } else if (firstToken.equals("noOfPackets")) {
		    	  if (messageTok.hasMoreTokens()) {
		    		  setNoOfPackets(Integer.parseInt(messageTok.nextToken()));
		    		  
		    		  Log.d("noOfPackets" ,"set to: " + noOfPackets);
		    	  }
		    	//  noOfPackets = Integer.parseInt(messageTok.nextToken());
		      } else {
		    	//  Log.d("received message", message);
		    	  nextPacket = messageReceived;
		    	  ByteBuffer byteBuffer = ByteBuffer.wrap(messageReceived, 0, 5);
	              int packetNo = (int) byteBuffer.getShort(0);
	             
	              if (!packets.containsKey(packetNo)) {
	            	  Log.d("receiver", "inserted packet: " + packetNo + " into packets[]");
	            	  packets.put(packetNo, messageReceived);
	            	  if (packetNo == 0) {
	            		  startDownload = true;
	            	  }
	              }
//		    	  int fileSize = bytes.getShort(0);
//		    	  fileSizeStatic = fileSize;
//		    	  Log.d("fileSize = ", fileSize + "");
//		    	  bytes.get(fileBytesStatic, 2, bytes.capacity());
		      }
		     
		     
		    }
		    @Override
		    public void onMessage(String s) {
		      final String message = s;
		      Log.d("received message", s);
		      SearchableActivity.setDisplayData(s);
		     
		    }

		    @Override
		    public void onClose(int i, String s, boolean b) {
		    	isOpen = false;
		      Log.i("Websocket", "Closed " + s);
		    }

		    @Override
		    public void onError(Exception e) {
		      Log.i("Websocket", "Error " + e.getMessage());
		    }
		  };
		  Log.d("Websocket", "trying to connect 2");
		  mWebSocketClient.connect();
		  Log.d("Websocket", "trying to connect 3");
		}
	 public static void receive(String fileName, websockets web) {
	        int expectedSeqNumber = 0;
	        try {
	        //	websockets.nextPacket = new byte[1029];
	       // 	Thread.sleep(1500);
	            // Array b will hold the packet received. Has a header of size 3 to indicate
	            // the packet number and whether it is the last packet or not
	            // Array ack will hold the acknowledgement that the receiver will send back to the sender
	            // for each packet it receives
	            byte[] b = new byte[1029];
	   //         byte[] ack = new byte[4];
	         //   DatagramSocket udpSocket = new DatagramSocket(port);
	            FileOutputStream f = appContext.openFileOutput(appName,
						appContext.MODE_WORLD_READABLE);
	            ByteBuffer byteBuffer;
	     //       ByteBuffer toSendByteBuffer = ByteBuffer.wrap(ack);
	            while (!packetsSet) {
	            	Thread.sleep(25);
	            }
	            while (true) {
	            	Thread.sleep(25);
	            	
	            	if (noOfPackets == 0) {
	            		Log.d("error detected", "closing thread");
	                	whichActivity.setInstall(false);
	                	packetsSet = false;
	                	break;
	                }
	                // Receive a packet and store it in array b
//	                DatagramPacket packet = new DatagramPacket(b, b.length);
//	                udpSocket.receive(packet);
	            	if (packets.containsKey(expectedSeqNumber) && startDownload) {
	            		b = packets.get(expectedSeqNumber).clone();
	            		 // Use a byte buffer to decipher the packet number and packet size
		                byteBuffer = ByteBuffer.wrap(b, 0, 5);
		                int packetNo = (int) byteBuffer.getShort(0);
		             //   Log.d("received packet", packetNo + "");
		                whichActivity.setProgressStatus((int)(((double)expectedSeqNumber/(double) noOfPackets) * 100));
		                
		                int packetSize = (int) byteBuffer.getShort(3);
		                Log.d("Packet Number written to file: ",""+ packetNo + " ack sent: " + expectedSeqNumber);
		               	 
	                    f.write(b, 5, packetSize);
	             //       packets.remove(packetNo);
	                    web.sendMessage(("ack " + expectedSeqNumber).getBytes());
	                    expectedSeqNumber++;
	                    while (packets.containsKey(expectedSeqNumber)) {
	                    	b = packets.get(expectedSeqNumber).clone();
	                    	byteBuffer = ByteBuffer.wrap(b, 0, 5);
	 		                packetNo = (int) byteBuffer.getShort(0);
	 		                whichActivity.setProgressStatus((int)(((double)expectedSeqNumber/(double) noOfPackets) * 100));
			                
			                packetSize = (int) byteBuffer.getShort(3);
			                Log.d("Packet Number written to file: ",""+ packetNo + " ack sent: " + expectedSeqNumber);
			               	 
		                    f.write(b, 5, packetSize);
		                    web.sendMessage(("ack " + expectedSeqNumber).getBytes());
		                    expectedSeqNumber++;
	                    	
	                    }
		                
	            	} else {
	            		int ackToSend = expectedSeqNumber-1;
	            		if (ackToSend < 0) {
	            			ackToSend = 0;
	            		}
	            		Log.d("receiver", "not found packet: " + expectedSeqNumber + " sent ack: " + ackToSend);
	            		web.sendMessage(("ack " + (ackToSend)).getBytes());
	            	}
	               

	                if (b[2] == 1) {
	                	startDownload = false;
	                	packetsSet = false;
	                	web.sendMessage("ack final".getBytes());
	                	whichActivity.setProgressStatus(100);
	                	whichActivity.setInstall(true);
	                	packets.clear();
	                    break;
	                }
	                
	            }
	        //    udpSocket.close();
	            f.close();
	        } catch (Exception e) {
	            System.out.println("Exception: " + e);
	            e.printStackTrace();
	        }
	    }

}
