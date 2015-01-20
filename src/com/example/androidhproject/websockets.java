package com.example.androidhproject;

import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

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
	static int noOfPackets = 100;
	static AppDetailActivity whichActivity;
	static Context appContext;
	static String appName;
	public static void setThread(Thread newThread) {
		thread = newThread;
	}
	
//	public websockets(WebSocketClient mWebSocketClient) {
//		this.mWebSocketClient = mWebSocketClient;
//	}
	
	public static void setContext(Context context) {
		appContext = context;
	}
	public static void seActivity(AppDetailActivity activity) {
		whichActivity = activity;
	}
	public static void setAppName(String appname) {
		appName = appname;
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
		      } else if (firstToken.equals("noOfPackets")) {
		    	  if (messageTok.hasMoreTokens()) {
		    		  noOfPackets = Integer.parseInt(messageTok.nextToken());
		    		  Log.d("noOfPackets" ,"set to: " + noOfPackets);
		    	  }
		    	//  noOfPackets = Integer.parseInt(messageTok.nextToken());
		      } else {
		    	//  Log.d("received message", message);
		    	  nextPacket = messageReceived;
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
	 public static void receive(String fileName) {
	        int prevPacketNo = -1;
	        try {
	        //	websockets.nextPacket = new byte[1029];
	       // 	Thread.sleep(1500);
	            // Array b will hold the packet received. Has a header of size 3 to indicate
	            // the packet number and whether it is the last packet or not
	            // Array ack will hold the acknowledgement that the receiver will send back to the sender
	            // for each packet it receives
	            byte[] b = new byte[1029];
	            byte[] ack = new byte[1];
	         //   DatagramSocket udpSocket = new DatagramSocket(port);
	            FileOutputStream f = appContext.openFileOutput(appName,
						appContext.MODE_WORLD_READABLE);
	            ByteBuffer byteBuffer;
	            ByteBuffer ackByteBuffer;
	            while (true) {
	            	if (noOfPackets == 0) {
	            		Log.d("error detected", "closing thread");
	                	whichActivity.setInstall(false);
	                	break;
	                }
	                // Receive a packet and store it in array b
//	                DatagramPacket packet = new DatagramPacket(b, b.length);
//	                udpSocket.receive(packet);
	            	b = websockets.nextPacket;
	                // Use a byte buffer to decipher the packet number and packet size
	                byteBuffer = ByteBuffer.wrap(b, 0, 5);
	                int packetNo = (int) byteBuffer.getShort(0);
	             //   Log.d("received packet", packetNo + "");
	                whichActivity.setProgressStatus((int)(((double)packetNo/(double) noOfPackets) * 100));
	                
	                int packetSize = (int) byteBuffer.getShort(3);
	                // Create a 1byte acknowledgement with the value 0 or 1 depending on the packet number
	                // of the packet received. Use a byte buffer to add that to a byte array and then
	                // send it back through the same port that we receive data from
	               
	                int bit = packetNo;
	                ack[0] = 0;
	                ackByteBuffer = ByteBuffer.wrap(ack);
	                ackByteBuffer.put((byte) bit);
	                String reply = "ack " + bit;
//	                DatagramPacket ACK = new DatagramPacket(ack, 1,
//	                        packet.getAddress(), packet.getPort());
	            //   System.out.println("Sent ack: " + ack[0]);
	                SearchableActivity.web.sendMessage(reply.getBytes());
	           //     Log.d("send ack: " ,""+ bit);
	              //  udpSocket.send(ACK);
	                // Write files only if they are not duplicate.
	                if (!(packetNo <= prevPacketNo)) {
	                    Log.d("Packet Number written to file: ",""+ packetNo);
	                    f.write(b, 5, packetSize);
	                } else {
	                    // Dont do anything if received duplicate file, just send back the same ack as before.
	                    //System.out.println("Duplicate packet received and disgarded: " + packetNo);
	                }
	                prevPacketNo = packetNo;
	                // Break out of loop if receive last packet, and close the socket and output stream
	                if (b[2] == 1) {
	                	whichActivity.setProgressStatus(100);
	                	whichActivity.setInstall(true);
	                    break;
	                }
	                
	            }
	        //    udpSocket.close();
	            f.close();
	        } catch (Exception e) {
	            System.out.println("Exception: " + e);
	        }
	    }

}
