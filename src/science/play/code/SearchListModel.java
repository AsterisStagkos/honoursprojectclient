package science.play.code;

import android.graphics.Bitmap;

public class SearchListModel {
    
	private Bitmap image;
    private  String AppName="";
    private  String Creator="";
    private  String AppDescription="";
    private String assetId="";
    private String filePath = "";
    private boolean isExperiment = false;
    private boolean isIndependent = false;
    private boolean needsUpdate = false;
    private boolean isInstalled = false;
     
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
    public void setIsExperiment(boolean isExperiment)
    {
        this.isExperiment = isExperiment;
    }
    public void setIsIndependent(boolean isIndependent)
    {
        this.isIndependent = isIndependent;
    }
    public void setNeedsUpdate(boolean needsUpdate)
    {
        this.needsUpdate = needsUpdate;
    }
    public void setisInstalled(boolean isInstalled)
    {
        this.isInstalled = isInstalled;
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
    public boolean isExperiment() {
    	return this.isExperiment;
    }
    public boolean isIndependent() {
    	return this.isIndependent;
    }
    public boolean needsUpdate() {
    	return this.needsUpdate;
    }
    public boolean isInstalled() {
    	return this.isInstalled;
    }
}
