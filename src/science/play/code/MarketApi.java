package science.play.code;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.MarketSession.Callback;
import com.gc.android.market.api.model.Market.AppsRequest;
import com.gc.android.market.api.model.Market.AppsResponse;
import com.gc.android.market.api.model.Market.GetImageRequest;
import com.gc.android.market.api.model.Market.GetImageRequest.AppImageUsage;
import com.gc.android.market.api.model.Market.GetImageResponse;
import com.gc.android.market.api.model.Market.ResponseContext;


public class MarketApi  {
	private static Bitmap bitmap;
	private static String searchData = "";
	private static String updateData = "";
	
	
	public static String searchApp(String query, MarketSession session) {
		try {
			AppsRequest appsRequest = AppsRequest.newBuilder().setQuery(query).setStartIndex(0).setEntriesCount(10).setWithExtendedInfo(true).build();
			
			session.append(appsRequest,  new Callback<AppsResponse>() { 
				@Override
				public void onResult(ResponseContext context, AppsResponse response) {
					System.out.println(response.toString());
					searchData = "";
					for (int i = 0; i<response.getAppCount(); i++) {
						// Title, ID, Creator, Description
					searchData += response.getApp(i).getTitle() + "<" + response.getApp(i).getId() + "<" + response.getApp(i).getCreator() + "<" + response.getApp(i).getExtendedInfo().getDescription() + ">nextapp<";
					//response.getApp(index)
					}
					//System.out.println(response.getApp(0).getId());
				}
			});
			session.flush();
			Log.d("MarketApi", "Got data: " + searchData);
			SearchableActivity.setDisplayData(searchData);
			return searchData;
		} catch(Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	public static String searchUpdate(String query, MarketSession session) {
		try {
			AppsRequest appsRequest = AppsRequest.newBuilder().setQuery(query).setStartIndex(0).setEntriesCount(10).setWithExtendedInfo(true).build();
			
			session.append(appsRequest,  new Callback<AppsResponse>() { 
				@Override
				public void onResult(ResponseContext context, AppsResponse response) {
			//		System.out.println(response.toString());
					updateData = "";
					for (int i = 0; i<response.getAppCount(); i++) {
						// Title, ID, Creator, Description
					updateData += response.getApp(i).getTitle() + "<" + response.getApp(i).getId() + "<" + response.getApp(i).getVersion() + "<" + response.getApp(i).getVersionCode() + "<" + response.getApp(i).getExtendedInfo().getDescription() +  ">nextapp<";
					//response.getApp(index)
					}
					//System.out.println(response.getApp(0).getId());
				}
			});
			session.flush();
		//	Log.d("MarketApi", "Got data: " + updateData);
			SeeInstalledApps.setDisplayData(updateData);
			return updateData;
		} catch(Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	public static MarketSession authenticate(String androidID, boolean secure, String token) {
		try{
			
			MarketSession session = new MarketSession(secure);
			System.out.println("Login...");
	//		session.setIsSecure(secure);
			session.setAndroidId(androidID);
			session.setAuthSubToken(token);		
			System.out.println("Login done");
			return session;
		} catch (Exception e) {
			MarketSession errorSession = new MarketSession(secure);		
			e.printStackTrace();
			return errorSession;
		}
		
	}
	
	public static Bitmap getImage(MarketSession session, String appId) {
		if (session != null && appId != null) {
		try{	
		
		GetImageRequest imgReq = GetImageRequest.newBuilder().setAppId(appId)
                .setImageUsage(AppImageUsage.ICON)
                .setImageId("1")
                .build();

			session.append(imgReq, new Callback<GetImageResponse>() {
                
                @Override
                public void onResult(ResponseContext context, GetImageResponse response) {
                        try {
                              byte[] data =response.getImageData().toByteArray();
                              bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
     
                        } catch(Exception ex) {
                                ex.printStackTrace();
                        }
                }
			});
		} catch(RuntimeException e) {
			e.printStackTrace();
		}
			session.flush();
			return bitmap;
		} else return null;
		}
	}
