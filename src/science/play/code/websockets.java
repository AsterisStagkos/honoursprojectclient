package science.play.code;

import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class websockets {
	private WebSocketClient mWebSocketClient;
	public int fileSizeStatic;
	public byte[] fileBytesStatic;
	private CheckDownloadsActivity mainActivity;
	static byte[] nextPacket;
	private int noOfPackets = 100;
	private static boolean packetsSet = false;
	private AppDetailActivity whichActivity;
	private Context appContext;
	private String appName;
	private boolean isOpen = false;
	private String filePath;
	private boolean startDownload = false;
	private static boolean currentlyDownloading = false;
	private TreeMap<Integer, byte[]> packets = new TreeMap<Integer, byte[]>(); 
	
//	public websockets(WebSocketClient mWebSocketClient) {
//		this.mWebSocketClient = mWebSocketClient;
//	}
	
	public boolean isOpen() {
		return isOpen;
	}
	public void setClosed() {
		isOpen = false;
	}
	public void setContext(Context context) {
		appContext = context;
	}
	public void setFilePath(String filepath) {
		filePath = filepath;
	}
	public void seActivity(AppDetailActivity activity) {
		whichActivity = activity;
	}
	public void setMainActivity(CheckDownloadsActivity activity) {
		mainActivity = activity;
	}
	public void setAppName(String appname) {
		appName = appname;
	}
	public void setNoOfPackets(int number) {
		noOfPackets = number;
		packetsSet = true;
	}
	public void sendMessage(byte[] message) {
		//Log.d("message to send: ", message.toString());
		//Log.d("socket is: " , mWebSocketClient.toString());
		if (mWebSocketClient.isOpen()) {
		try {
		mWebSocketClient.send(message);
		} catch (WebsocketNotConnectedException e) {
		//	 Toast.makeText(appContext, "Web not connected, try again in 10 seconds", Toast.LENGTH_SHORT).show();
			 e.printStackTrace();
			
		}
		} else {
			try {
				Thread.sleep(100);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	public WebSocketClient getWebSocket() {
		return mWebSocketClient;
	}
	public void connectWebSocket() {
    	SharedPreferences settings = appContext.getSharedPreferences(MainActivity.PREFS_NAME, 0);
    	boolean isDownloading = settings.getBoolean("currentlydownloading", false);
	//      if (!currentlyDownloading && !isDownloading) {
		  URI uri;
		  
		  try {
			  Log.d("Websocket", "trying to connect");
		    uri = new URI("ws://evening-peak-5779.herokuapp.com/pingWs");
		  } catch (URISyntaxException e) {
		    e.printStackTrace();
		    return;
		  }
		  Draft protocolDraft = new Draft_17();
			HashMap<String, String> headers = new HashMap<String, String>();
			headers.put("connection", "keep-alive");
			headers.put("connection", "Upgrade");
			mWebSocketClient = new WebSocketClient(uri, protocolDraft, headers, Integer.MAX_VALUE) {
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
		    	  Log.d("received byte message", message);
		    	  ChooseExperimentsActivity.setDisplayData(message);
		      } else if (firstToken.equals("noOfPackets")) {
		    	  if (messageTok.hasMoreTokens()) {
		    		  setNoOfPackets(Integer.parseInt(messageTok.nextToken()));
		    		  
		    		  Log.d("noOfPackets" ,"set to: " + noOfPackets);
		    	  }
		    	//  noOfPackets = Integer.parseInt(messageTok.nextToken());
		      } else if (firstToken.equals("DownloadAvailable")) {
		    	  String scriptName = "";
		    	  if (messageTok.hasMoreTokens()) {
		    		  scriptName = messageTok.nextToken();
		    	  }
		    	  mainActivity.setReadyForDownload(appContext, scriptName);
		      } else if (firstToken.equals("DownloadNotAvailable")) {
		    //	  mainActivity.setNotReadyForDownload();
		      } else if (firstToken.equals("QuestionnairAvailable")) {
		    	  Log.d("qmsg", message);
		    	  message = message.substring(22);
		    	  SeeQuestionnaires.setDisplayData(message);
		      } else if (firstToken.equals("QuestionnairNotAvailable")) {
		    	  SeeQuestionnaires.setDisplayData("NotAvailable www.google.com");
		      } else if(firstToken.equals("NotificationAvailable")) {
		    	  String notification = message.substring(21);
		    	  mainActivity.setNotification(appContext, notification);
		      } else if (firstToken.equals("Ping")) {
		    	  Log.d("Ping", "Ping");
		    	  mWebSocketClient.send("Pong".getBytes());
		      }
		      
		      else {
		    	//  Log.d("received message", message);
		    	  nextPacket = messageReceived;
		    	  ByteBuffer byteBuffer = ByteBuffer.wrap(messageReceived, 0, 7);
	              int packetNo = (int) byteBuffer.getInt(0);
	             
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
		      Log.i("Websocket", "Websocket Error " + e.getMessage());
		      e.printStackTrace();
		    }
		  };
		  Log.d("Websocket", "trying to connect 2");
		  mWebSocketClient.connect();
	//	  mWebSocketClient.
		  Log.d("Websocket", "trying to connect 3");
	     // }
		}
	 public void receive(String fileName, websockets web) {
	        int expectedSeqNumber = 0;
	        try {
	        	currentlyDownloading = true;
	        	packetsSet = false;
	        	SharedPreferences settings = whichActivity.getSharedPreferences(MainActivity.PREFS_NAME, 0);
	       		SharedPreferences.Editor editor = settings.edit();
	       		
	       		editor.putBoolean("currentlydownloading", true);
	       		editor.commit();
	        //	websockets.nextPacket = new byte[1029];
	       // 	Thread.sleep(1500);
	            // Array b will hold the packet received. Has a header of size 3 to indicate
	            // the packet number and whether it is the last packet or not
	            // Array ack will hold the acknowledgement that the receiver will send back to the sender
	            // for each packet it receives
	            byte[] b = new byte[1031];
	   //         byte[] ack = new byte[4];
	         //   DatagramSocket udpSocket = new DatagramSocket(port);
	            FileOutputStream f = appContext.openFileOutput(appName,
						appContext.MODE_WORLD_READABLE);
	            ByteBuffer byteBuffer;
	     //       ByteBuffer toSendByteBuffer = ByteBuffer.wrap(ack);
	            while (!packetsSet) {
	            	Log.d("packets not set", "packets not set");
	            	Thread.sleep(25);
	            }
	            while (isOpen) {
		        	currentlyDownloading = true;

	            	Thread.sleep(25);
	            	
	            	if (noOfPackets == 0) {
	            		Log.d("error detected", "closing thread");
	                	whichActivity.setStatus(Constants.BTN_STATUS_APPNOTFOUND);
	                	packetsSet = false;
	                	break;
	                }
	                // Receive a packet and store it in array b
//	                DatagramPacket packet = new DatagramPacket(b, b.length);
//	                udpSocket.receive(packet);
	            	int packetNo = 0;
	            	int packetSize = 0;
	            	if (packets.containsKey(expectedSeqNumber) && startDownload) {
	            		try {
	            		b = packets.get(expectedSeqNumber);
	            		 // Use a byte buffer to decipher the packet number and packet size
		                byteBuffer = ByteBuffer.wrap(b, 0, 7);
		                packetNo = (int) byteBuffer.getInt();
		             //   Log.d("received packet", packetNo + "");
		                whichActivity.setProgressStatus((int)(((double)expectedSeqNumber/(double) noOfPackets) * 100));
		                
		                packetSize = (int) byteBuffer.getShort(5);
		                Log.d("Packet Number written to file: ",""+ packetNo + " ack sent: " + expectedSeqNumber);
		               	 
	                    f.write(b, 7, packetSize);
	             //       packets.remove(packetNo);
	                    web.sendMessage(("ack " + expectedSeqNumber).getBytes());
	                    expectedSeqNumber++;
	            		} catch (NullPointerException e) {
	            			e.printStackTrace();
	            		}
	                    while (packets.containsKey(expectedSeqNumber) && packets.get(expectedSeqNumber) != null) {
	                    	try {
	                    	b = packets.get(expectedSeqNumber).clone();
	                    	byteBuffer = ByteBuffer.wrap(b, 0, 7);
	 		                packetNo = (int) byteBuffer.getInt(0);
	 		                whichActivity.setProgressStatus((int)(((double)expectedSeqNumber/(double) noOfPackets) * 100));
	 		                whichActivity.setProgressValues(expectedSeqNumber, noOfPackets);
			                
			                packetSize = (int) byteBuffer.getShort(5);
			                Log.d("Packet Number written to file: ",""+ packetNo + " ack sent: " + expectedSeqNumber);
			               	 
		                    f.write(b, 7, packetSize);
		                    if (expectedSeqNumber > 513) {
		                    	packets.remove(expectedSeqNumber - 512);
		                    }
		                    web.sendMessage(("ack " + expectedSeqNumber).getBytes());
		                    expectedSeqNumber++;
	                    	} catch (NullPointerException e) {
	                    		e.printStackTrace();
	                    	}
	                    	
	                    }
		                
	            	} else {
	            		int ackToSend = expectedSeqNumber-1;
	            		if (ackToSend < 0) {
	            			ackToSend = 0;
	            		}
	           // 		Log.d("receiver", "not found packet: " + expectedSeqNumber + " sent ack: " + ackToSend);
	            		web.sendMessage(("ack " + (ackToSend)).getBytes());
	            	}
	               

	                if (b[4] == 1) {
	                	SharedPreferences settings2 = whichActivity.getSharedPreferences(MainActivity.PREFS_NAME, 0);
	    	       		SharedPreferences.Editor editor2 = settings2.edit();
	    	       		
	    	       		editor2.putBoolean("currentlydownloading", false);
	    	       		editor2.commit();
	                	currentlyDownloading = false;
	                	startDownload = false;
	                	packetsSet = false;
	                	web.sendMessage("ack final".getBytes());
	                	whichActivity.setProgressStatus(100);
	                	whichActivity.setStatus(Constants.BTN_STATUS_INSTALL);
	                	Log.d("receiver", " download finished, set status to install");
	                	packets.clear();
	                	web.sendMessage("ack final".getBytes());
	                    break;
	                }
	                
	            }
	            SharedPreferences settings3 =  whichActivity.getSharedPreferences(MainActivity.PREFS_NAME, 0);
	       		SharedPreferences.Editor editor3 = settings3.edit();
	       		
	       		editor3.putBoolean("currentlydownloading", false);
	       		editor3.commit();
	            currentlyDownloading = false;
	            AppDetailActivity.currentlyDownloading = false;
	        //    udpSocket.close();
	            f.close();
	        } catch (Exception e) {
	            System.out.println("Exception: " + e);
	            e.printStackTrace();
	        }
	    }
	 public static boolean isDownloading() {
		 return currentlyDownloading;
	 }
	 
	 

}
