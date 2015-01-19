package com.example.androidhproject;

public class SearchListModel {
    
    private  String AppName="";
    private  String Creator="";
    private  String AppDescription="";
    private String assetId="";
     
    /*********** Set Methods ******************/
     
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
}
