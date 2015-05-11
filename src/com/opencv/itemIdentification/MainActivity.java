package com.opencv.itemIdentification;


import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	private static final int CAMERA_ACTIVITY = 0;
	private static final int MSG_SERCH = 100002;//ÏûÏ¢±êÊ¶
	
	//´æ·ÅÑµÁ·¼¯¡¢½á¹û¼¯µÈÊý¾ÝµÄÄ¿Â¼
	private String dataDir = Environment.getExternalStorageDirectory() + "/itemIdentification_data";
	
	//ÊµÊ±Í¼Æ¬Ãû£¬¼´Ïà»úËùÅÄÍ¼Æ¬Ãû
	private String filename;// = dataDir + "/toMatch/toMatch.jpg";
	private Uri outputFileUri;
	
	//ÑµÁ·¼¯¼°½á¹û¼¯Ä¿Â¼(SVM)
	private String trainNNDir = dataDir  + "/SVM/trainNN";
	private String resultNNDir = dataDir  + "/SVM/resultNN";
	
	////ÑµÁ·¼¯¼°½á¹û¼¯Ä¿Â¼(surf)
	private String imagesDir = dataDir + "/SURF/trainingSet";
	private String imagesSurfInfoDir = dataDir + "/SURF/trainingSetSURFFeatures";
	
	private String imagesDir1 = dataDir + "/SURF/trainingSet1";
	private String imagesSurfInfoDir1 = dataDir + "/SURF/trainingSetSURFFeatures1";
	
	//´æ·ÅÔ­Ê¼Í¼Æ¬µÄÄ¿Â¼
	private String rawImagesDir = dataDir + "/rawIamges";
	
	//½ø¶È¶Ô»°¿òÏß³Ì
    private Thread mThread;
    //½ø¶È¶Ô»°¿ò
    ProgressDialog dialog;
    
    private Intent mIntent;
    //Æ¥Åä½á¹ûÍ¼Æ¬ÏÔÊ¾¼°×Ö·ûÏÔÊ¾
	private ImageView mImageView;
	private TextView resView;
	private String resStr = "";
	//Æ¥Åä·½·¨±êÖ¾
	private Boolean isUesedSVM;
	//Ïà»úËùÅÄµÄÕÕÆ¬
	private Bitmap bm;
	static public String str = "";
	//½ø¶È¶Ô»°¿ò±êÌâºÍÖ÷ÌåÎÄ±¾ÏÔÊ¾
	private CharSequence strDialogTitle; 
	private CharSequence strDialogBody; 
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mImageView = (ImageView)findViewById(R.id.thumbnail);
		//mImageView.setAlpha(0);
		resView = (TextView)findViewById(R.id.res_view);
		resView.setTypeface(Typeface.MONOSPACE,Typeface.BOLD_ITALIC);
		strDialogTitle = MainActivity.this.getString(R.string.str_dialog_title); 
		strDialogBody = MainActivity.this.getString(R.string.str_dialog_body); 
	}
	
	private Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {//´Ë·½·¨ÔÚuiÏß³ÌÔËÐÐ
        	
            if(msg.what == MSG_SERCH)
            {
            	String str;
               //»ñµÃµ×²ãc++Ê¶±ðÍ¼Æ¬µÃµ½µÄ×Ö·û´®
            	str = (String)(msg.obj);
            	//System.out.println("***************** str = " + str + "*********************");
            	
            	// Ð¶ÔØdialog¶ÔÏó
            	dialog.dismiss();
            	
            	
            	//È¡µÃÒª´«µÝµÄ×Ö·û´®
            	String deliverResuletStr = dealwithResult(str);
            	//System.out.println("***************** deliverResuletStr = " + deliverResuletStr + "*********************");
            	
            	//Æô¶¯ÏÔÊ¾Æ¥Åä½á¹ûµÄActivity
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, ShowResultActivity.class);
				intent.putExtra("resultInfo", deliverResuletStr);
				startActivity(intent);
				
				 //ÊµÏÖzoominºÍzoomout,¼´ÀàËÆiphoneµÄ½øÈëºÍÍË³öÊ±µÄÐ§¹û 
	             overridePendingTransition(R.animator.zoomin, R.animator.zoomout);  
	              
				 //ÔÚÖ÷½çÃæÖÐÏÔÊ¾½á¹û
				 resView.setText(resStr);
				
            }
        }
    };
	
	@Override
	protected void onResume() {
		super.onResume();
		//mImageView.setImageResource(R.drawable.androidmarker);
		mImageView.setImageResource(R.drawable.home);
	}	

	protected void onPause() {
		super.onPause();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}
/*
 * SURFÊ¶±ð·½·¨·µ»ØµÄ×Ö·û´®¸ñÊ½Îª£¬5¸öÎÄ¼þÃû£¬ÎÄ¼þÃûÖ®¼äÓÃ¶ººÅ¸ô¿ª£»
 * SVMÊ¶±ð·½·¨·µ»ØµÄ×Ö·û´®¸ñÊ½Îª£¬5¸öÀàÃû£¬ÎÄ¼þÃûÖ®¼äÓÃ¶ººÅ¸ô¿ª£»
 * */
	private String dealwithResult(String resultStr)
	{
		//resStr¸ñÊ½£ºÃ¿ÕÅÍ¼Æ¬ÍêÕûÂ·¾¶£¬Ã¿ÏîÊä³öÐÅÏ¢
		String deliverResuletStr = "";
		//Ã¿ÕÅÍ¼Æ¬µÄÀàÐÍºÍÎÄ¼þÃûÐÅÏ¢£¬ÀýÈç£º0zara_6096_451_5.jpg£¬µÚÒ»¸ö×Ö·ûÃèÊöÊÇÄÐ×°»¹ÊÇÅ®×°£¬ºóÃæÊÇ×Ö·û´®ÊÇÍ¼Æ¬Ãû
		String[] spiltStr = resultStr.split(",");
		//Í¼Æ¬µÄÍêÕûÂ·¾¶
		String[] filenames = new String[spiltStr.length];
		//ÐèÒªÊä³öµÄÐÅÏ¢
		String[] resultDescriptionStrs = new String[spiltStr.length+1];
		
		//¼ÆËãÃ¿ÕÅÍ¼Æ¬µÄÍêÕûÂ·¾¶
		for(int i=0; i< filenames.length; i++)
		{
			if(!isUesedSVM)
			{
				//filenames[i] = rawImagesDir + "/" + spiltStr[i].substring(1);
				String tempStr = spiltStr[i].substring(1).substring(0, spiltStr[i].substring(1).lastIndexOf("_"));
				
				String filename = rawImagesDir + "/" + tempStr + "_0" +  (i+1) + ".jpg";
				File file = new File(filename);
				if(!file.exists())
				{
					filename = rawImagesDir + "/" + tempStr + "_" +  (i+1) + ".jpg";
				}
				filenames[i] = filename;
				
				System.out.println("******************"+ filenames[i] +"******************");
			}
			else
			{
				String filename = rawImagesDir + "/" + spiltStr[i].substring(1) + "_01.jpg";
				File file = new File(filename);
				if(file.exists())
				{
					filenames[i] = filename;
					spiltStr[i] += "_01.jpg";
				}
				else {
					filenames[i] = rawImagesDir + "/" + spiltStr[i].substring(1) + "_1.jpg";
					spiltStr[i] += "_1.jpg";
				}
			}
		}
		//µÚÒ»ÕÅÍ¼Æ¬µÄÃèÊöÐÅÏ¢£¬¼´ÕûÌåÃèÊöÐÅÏ¢
		if(!isUesedSVM)
		{
			resultDescriptionStrs[0] = //"Ê¶±ð½á¹û£º\n" + 
					"×î¼ÑÆ¥Åä£º" + spiltStr[0].substring(1, spiltStr[0].lastIndexOf("_")) + "\n" +
					"ÍÆ¼öÒÂ·þ£º" + spiltStr[1].substring(1, spiltStr[1].lastIndexOf("_")) + "\n" +
					"ÏàËÆÒÂ·þ1£º" + spiltStr[2].substring(1, spiltStr[2].lastIndexOf("_")) + "\n" +
					"ÏàËÆÒÂ·þ2£º" + spiltStr[3].substring(1, spiltStr[3].lastIndexOf("_")) + "\n" +
					"ÏàËÆÒÂ·þ3£º" + spiltStr[4].substring(1, spiltStr[4].lastIndexOf("_"));
		}
		else {
			resultDescriptionStrs[0] = //"Ê¶±ð½á¹û£º\n" + 
					"ÍÆ¼öÒÂ·þ1£º" + spiltStr[0].substring(1, spiltStr[0].lastIndexOf("_")) + "\n" +
					"ÍÆ¼öÒÂ·þ2£º" + spiltStr[1].substring(1, spiltStr[1].lastIndexOf("_")) + "\n" +
					"ÍÆ¼öÒÂ·þ3£º" + spiltStr[2].substring(1, spiltStr[2].lastIndexOf("_")) + "\n" +
					"ÍÆ¼öÒÂ·þ4£º" + spiltStr[3].substring(1, spiltStr[3].lastIndexOf("_")) + "\n" +
					"ÍÆ¼öÒÂ·þ5£º" + spiltStr[4].substring(1, spiltStr[4].lastIndexOf("_"));
		}
		
		//·Ö±ð¼ÆËãÃ¿ÕÅÍ¼Æ¬µÄÃèÊöÐÅÏ¢£¬ÃèÊöÐÅÏ¢°üÀ¨ÀàÐÍ¡¢Æ·ÅÆ¡¢ÐÍºÅ
		for(int i=1; i< resultDescriptionStrs.length; i++)
		{
			String[] clothesInfo = spiltStr[i-1].substring(1).split("_");
			String[] strs;
			if(clothesInfo.length == 3) //Ã»ÓÐ´ÎÐÍºÅ
			{
				strs = new String[3];
				strs[0] = "ÀàÐÍ£º";
				strs[1] = "Æ·ÅÆ£º";
				strs[2] = "ÐÍºÅ£º";
			}
			else  //ÓÐ´ÎÐÍºÅ
			{
				strs = new String[4];
				strs[0] = "ÀàÐÍ£º";
				strs[1] = "Æ·ÅÆ£º";
				strs[2] = "ÐÍºÅ£º";
				strs[3] = "´ÎÐÍºÅ£º";
			}
			
			
			if(spiltStr[i-1].charAt(0) == '0')
			{
				strs[0] += "ÄÐ×°";
			}
			else {
				strs[0] += "Å®×°";
			}
			
			//È¡µÃÆ·ÅÆºÍÐÍºÅÐÅÏ¢
			strs[1] += clothesInfo[0];
			strs[2] += clothesInfo[1];
			
			//È¡µÃ´ÎÐÍºÅ
			if(strs.length == 4)
			{
				strs[3] += clothesInfo[2];
			}
			
			resultDescriptionStrs[i] = "";
			for(int j=0; j< strs.length; j++)
			{
				resultDescriptionStrs[i] = resultDescriptionStrs[i] + strs[j] + "\n";
			}
			
			//¼ÆËãresStr£¬¼´ÏÔÊ¾ÔÚÖ÷½çÃæÖÐµÄ½á¹û×î¼ÑÆ¥ÅäÒÂ·þµÄÐÅÏ¢
			if(i == 1)
			{
				resStr = "";
				for(int j=0; j< strs.length; j++)
				{
					resStr = resStr + "      " + strs[j] + "\n";
				}
			}
		}
		
		/*
		 * ½«5ÕÅÍ¼Æ¬µÄÂ·¾¶ºÍ¶ÔÓ¦µÄÃèÊöÐÅÏ¢ºÏ²¢ÔÚÒ»Æð×÷ÎªÒ»¸ö×Ö·û´®£¬ÒÔ±ã½«Æä´«µÝµ½ShowResultActivity
		 * */
		//ºÏ²¢5ÕÅÍ¼Æ¬µÄÂ·¾¶ÐÅÏ¢
		for(int i=0; i< filenames.length; i++)
		{
			deliverResuletStr = deliverResuletStr + filenames[i] + ",";
		}
/*
		//ºÏ²¢6ÕÅÍ¼Æ¬µÄÃèÊöÐÅÏ¢
		for(int i=0; i< resultDescriptionStrs.length-1; i++)
		{
			deliverResuletStr = deliverResuletStr + resultDescriptionStrs[i] + ",";
		}
		deliverResuletStr = deliverResuletStr + resultDescriptionStrs[resultDescriptionStrs.length-1];
*/
		//ºÏ²¢6ÕÅÍ¼Æ¬µÄÃèÊöÐÅÏ¢
		if(!isUesedSVM)
		{
			deliverResuletStr = deliverResuletStr + resultDescriptionStrs[0] + ",";
			deliverResuletStr = deliverResuletStr + "×î¼ÑÆ¥Åä" + "\n" + resultDescriptionStrs[1] + ",";
			deliverResuletStr = deliverResuletStr + "ÍÆ¼öÒÂ·þ" + "\n" + resultDescriptionStrs[2] + ",";
			deliverResuletStr = deliverResuletStr + "ÏàËÆÒÂ·þ1" + "\n" +resultDescriptionStrs[3] + ",";
			deliverResuletStr = deliverResuletStr + "ÏàËÆÒÂ·þ2" + "\n" +resultDescriptionStrs[4] + ",";
			deliverResuletStr = deliverResuletStr + "ÏàËÆÒÂ·þ3" + "\n" +resultDescriptionStrs[5];
		}
		else
		{
			deliverResuletStr = deliverResuletStr + resultDescriptionStrs[0] + ",";
			deliverResuletStr = deliverResuletStr + "ÏàËÆÒÂ·þ1" + "\n" + resultDescriptionStrs[1] + ",";
			deliverResuletStr = deliverResuletStr + "ÏàËÆÒÂ·þ2" + "\n" + resultDescriptionStrs[2] + ",";
			deliverResuletStr = deliverResuletStr + "ÏàËÆÒÂ·þ3" + "\n" +resultDescriptionStrs[3] + ",";
			deliverResuletStr = deliverResuletStr + "ÏàËÆÒÂ·þ4" + "\n" +resultDescriptionStrs[4] + ",";
			deliverResuletStr = deliverResuletStr + "ÏàËÆÒÂ·þ5" + "\n" +resultDescriptionStrs[5];
		}
		
		return deliverResuletStr;
	}
	
	//ÏìÓ¦Ñ¡Ôñ²Ëµ¥ÊÂ¼þ
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		
		switch(item.getItemId()) {
		case R.id.SVM:
			onClickSVMBtn(new View(this));
			break;
		case R.id.SIFT:
			onClickSIFTBtn(new View(this));
			break;
		}
		return true;
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (resultCode == RESULT_CANCELED) {
			showToast(this,"cancelled to match");
			return;
		}
		switch (requestCode) {

		case CAMERA_ACTIVITY: 
				dialog = ProgressDialog.show(MainActivity.this,strDialogTitle,strDialogBody,true);
				 //if(mThread == null)
				 {
	                 mThread = new Thread(runnable);
	                 mThread.start();//Ïß³ÌÆô¶¯
				 }
			}
		}
	
	Runnable runnable = new Runnable() {
		
		@Override
		public void run() {// run()ÔÚÐÂµÄÏß³ÌÖÐÔËÐÐ
			String tosendStr;
			
			//System.out.println("************* filename: " + filename + "***********");
			
			if(isUesedSVM) 
			{
				tosendStr = ImageProc.ProcBySvm(trainNNDir, filename, resultNNDir);
			}
			else
			{
				tosendStr = ImageProc.ProcBySurf(imagesDir, filename, imagesSurfInfoDir);
				
				System.out.println("************************"+  tosendStr +"****************************");
				
			}
			//·¢ËÍÏûÏ¢£¬¿ØÖÆ½ø¶È¶Ô»°¿òµÄÖÕÖ¹
			mHandler.obtainMessage(MSG_SERCH, tosendStr).sendToTarget();
		}
	};
	
	/**
	 * Utility method for displaying a Toast.
	 * @param mContext
	 * @param text
	 */
	private void showToast(Context mContext, String text) {
		Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
	}

	 public void onClickSVMBtn(View v)
	 {
		mIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		//mIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString()); 
		
		filename = System.currentTimeMillis()+ ".jpeg";
		File file = new File(dataDir + "/takePhotos", filename);
		outputFileUri = Uri.fromFile(file);
		filename = dataDir + "/takePhotos/" + filename;
		
		mIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri); 
		
		isUesedSVM = true;
		startActivityForResult(mIntent, CAMERA_ACTIVITY);	
		
	 }
	 
	 
	 public void onClickSIFTBtn(View v)
	 {
		mIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		//mIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString()); 
		
		filename = System.currentTimeMillis()+ ".jpeg";
		File file = new File(dataDir + "/takePhotos", filename);
		outputFileUri = Uri.fromFile(file);
		filename = dataDir + "/takePhotos/" + filename;
			
		mIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri); 
		
		isUesedSVM = false;
		startActivityForResult(mIntent, CAMERA_ACTIVITY);
		  
	 }
}

