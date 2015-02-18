package com.example.androidhproject;

import android.graphics.Bitmap;

public class SearchListModel {
    
	private Bitmap image;
    private  String AppName="";
    private  String Creator="";
    private  String AppDescription="";
    private String assetId="";
    private String filePath = "";
     
    /*********** Set Methods ******************/
     public void setFilePath(String filePath) {
    	this.filePath = filePath;
    }
    public void setAppIcon(Bitmap icon) {
    	this.image = icon;
    }
    public void setAppName(String AppName)
    {
        this.AppName = AppName;
    }
    public void setAssetId(String assetId)
    {
        this.assetId = assetId;
    }
     
    public void setCreator(String Creator)
    {
        this.Creator = Creator;
    }
     
    public void setDescription(String AppDescription)
    {
        this.AppDescription = AppDescription;
    }
     
    /*********** Get Methods ****************/
     
    public Bitmap getAppIcon() {
    	return this.image;
    }
    public String getAppName()
    {
        return this.AppName;
    }
    
    public String getAssetId()
    {
        return this.assetId;
    }
     
     
    public String getCreator()
    {
        return this.Creator;
    }
 
    public String getAppDescription()
    {
        return this.AppDescription;
    }   
    public String getFilePath() {
    	return this.filePath;
    }
}
