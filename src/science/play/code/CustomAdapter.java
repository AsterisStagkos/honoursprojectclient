package science.play.code;

import java.util.ArrayList;

import science.play.code.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/********* Adapter class extends with BaseAdapter and implements with OnClickListener ************/
public class CustomAdapter extends BaseAdapter   implements OnClickListener {
          
         /*********** Declare Used Variables *********/
         private Activity activity;
         private ArrayList data;
         private static LayoutInflater inflater=null;
         public Resources res;
         SearchListModel tempValues=null;
         int i=0;
         int appActivity = 0;
          
         /*************  CustomAdapter Constructor *****************/
         public CustomAdapter(Activity a, ArrayList d,Resources resLocal, int appActivitey) {
              
                /********** Take passed values **********/
                 activity = a;
                 data=d;
                 res = resLocal;
                 appActivity = appActivitey;
                 /***********  Layout inflator to call external xml layout () ***********/
                  inflater = ( LayoutInflater )activity.
                                              getSystemService(Context.LAYOUT_INFLATER_SERVICE);
              
         }
      
         /******** What is the size of Passed Arraylist Size ************/
         public int getCount() {
              
             if(data.size()<=0)
                 return 1;
             return data.size();
         }
      
         public Object getItem(int position) {
             return position;
         }
      
         public long getItemId(int position) {
             return position;
         }
          
         /********* Create a holder Class to contain inflated xml file elements *********/
         public static class ViewHolder{
              
             public TextView appName;
             public TextView creator;
             public TextView description;
             public TextView assetId;
             public ImageView icon;
      
         }
      
         /****** Depends upon data size called for each row , Create each ListView row *****/
         public View getView(int position, View convertView, ViewGroup parent) {
              
             View vi = convertView;
             ViewHolder holder;
              
             if(convertView==null){
                  
                 /****** Inflate tabitem.xml file for each row ( Defined below ) *******/
                 vi = inflater.inflate(R.layout.custom_list_layout, null);
                  
                 /****** View Holder Object to contain tabitem.xml file elements ******/
 
                 holder = new ViewHolder();
                 holder.icon = (ImageView) vi.findViewById(R.id.iconView);
                 holder.appName = (TextView) vi.findViewById(R.id.firstline);
                 holder.creator=(TextView)vi.findViewById(R.id.secondline);
                 holder.description = (TextView) vi.findViewById(R.id.thirdline);
                 holder.assetId=(TextView)vi.findViewById(R.id.fourthline);
              //   holder.image=(ImageView)vi.findViewById(R.id.image);
                  
                /************  Set holder with LayoutInflater ************/
                 vi.setTag( holder );
             }
             else 
                 holder=(ViewHolder)vi.getTag();
              
             if(data.size()<=0)
             {
                 holder.appName.setText("No Data");
                  
             }
             else
             {
                 /***** Get each Model object from Arraylist ********/
                 tempValues=null;
                 tempValues = ( SearchListModel ) data.get( position );
                  
                 /************  Set Model values in Holder elements ***********/
 
                  holder.icon.setImageBitmap(tempValues.getAppIcon());
                  holder.appName.setText( tempValues.getAppName() );
                  holder.creator.setText( tempValues.getCreator() );
                 // holder.description.setText( tempValues.getAppDescription() );
                //  holder.assetId.setText( tempValues.getAssetId() );

//                   holder.image.setImageResource(
//                               res.getIdentifier(
//                               "com.androidexample.customlistview:drawable/"+tempValues.getImage()
//                               ,null,null));
                 
                   
                  /******** Set Item Click Listner for LayoutInflater for each row *******/
 
                  vi.setOnClickListener(new OnItemClickListener( position ));
             }
             return vi;
         }
          
         @Override
         public void onClick(View v) {
                 Log.v("CustomAdapter", "=====Row button clicked=====");
         }
          
         /********* Called when Item click in ListView ************/
         private class OnItemClickListener  implements OnClickListener{           
             private int mPosition;
              
             OnItemClickListener(int position){
                  mPosition = position;
             }
              
             @Override
             public void onClick(View arg0) {
 
               if (appActivity == 1) {
               SearchableActivity sct = (SearchableActivity)activity;
 
              /****  Call  onItemClick Method inside CustomListViewAndroidExample Class ( See Below )****/
 
                 sct.onItemClick(mPosition);
               } else if (appActivity == 2) {
            	   ChooseExperimentsActivity sct = (ChooseExperimentsActivity)activity;
            	   
                   /****  Call  onItemClick Method inside CustomListViewAndroidExample Class ( See Below )****/
      
                      sct.onItemClick(mPosition);
               } else if (appActivity == 3) {
            	   SeeInstalledApps sct = (SeeInstalledApps)activity;
            	   
                   /****  Call  onItemClick Method inside CustomListViewAndroidExample Class ( See Below )****/
      
                      sct.onItemClick(mPosition);
               } else if (appActivity == 4) {
            	   SeeQuestionnaires sct = (SeeQuestionnaires)activity;
            	   
                   /****  Call  onItemClick Method inside CustomListViewAndroidExample Class ( See Below )****/
      
                      sct.onItemClick(mPosition);
               }
             }               
         }   
     }