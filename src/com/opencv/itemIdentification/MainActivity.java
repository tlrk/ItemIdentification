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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private static final int CAMERA_ACTIVITY = 0;
	private static final int MSG_SERCH = 100002;//消息标识
	
	//存放训练集、结果集等数据的目录
	private String dataDir = Environment.getExternalStorageDirectory() + "/itemIdentification_data";
	
	//实时图片名，即相机所拍图片名
	private String filename;// = dataDir + "/toMatch/toMatch.jpg";
	private Uri outputFileUri;
	
	//训练集及结果集目录(SVM)
	private String trainNNDir = dataDir  + "/SVM/trainNN";
	private String resultNNDir = dataDir  + "/SVM/resultNN";
	
	////训练集及结果集目录(surf)
	private String imagesDir = dataDir + "/SURF/trainingSet";
	private String imagesSurfInfoDir = dataDir + "/SURF/trainingSetSURFFeatures";
	
	private String imagesDir1 = dataDir + "/SURF/trainingSet1";
	private String imagesSurfInfoDir1 = dataDir + "/SURF/trainingSetSURFFeatures1";
	
	//存放原始图片的目录
	private String rawImagesDir = dataDir + "/rawIamges";
	
	//进度对话框线程
    private Thread mThread;
    //进度对话框
    ProgressDialog dialog;
    
    private Intent mIntent;
    //匹配结果图片显示及字符显示
	private ImageView mImageView;
	private TextView resView;
	private String resStr = "";
	//匹配方法标志
	private Boolean isUesedSVM;
	//相机所拍的照片
	private Bitmap bm;
	static public String str = "";
	//进度对话框标题和主体文本显示
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
        public void handleMessage (Message msg) {//此方法在ui线程运行
        	
            if(msg.what == MSG_SERCH)
            {
            	String str;
               //获得底层c++识别图片得到的字符串
            	str = (String)(msg.obj);
            	//System.out.println("***************** str = " + str + "*********************");
            	
            	// 卸载dialog对象
            	dialog.dismiss();
            
            	
            	//取得要传递的字符串
            	String deliverResuletStr = dealwithResult(str);
            	//System.out.println("***************** deliverResuletStr = " + deliverResuletStr + "*********************");
            	
            	//启动显示匹配结果的Activity
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, ShowResultActivity.class);
				intent.putExtra("resultInfo", deliverResuletStr);
				//intent.putExtra("resultInfo", str);
				startActivity(intent);
					
				 //实现zoomin和zoomout,即类似iphone的进入和退出时的效果 
	             overridePendingTransition(R.animator.zoomin, R.animator.zoomout);  
	              
				 //在主界面中显示结果
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
 * SURF识别方法返回的字符串格式为，5个文件名，文件名之间用逗号隔开；
 * SVM识别方法返回的字符串格式为，5个类名，文件名之间用逗号隔开；
 * */
	private String dealwithResult(String resultStr)
	{
		//resStr格式：每张图片完整路径，每项输出信息
		String deliverResuletStr = "";
		//每张图片的类型和文件名信息，例如：0zara_6096_451_5.jpg，第一个字符描述是男装还是女装，后面是字符串是图片名
		String[] spiltStr = resultStr.split(",");
		//图片的完整路径
		String[] filenames = new String[spiltStr.length];
		//需要输出的信息
		String[] resultDescriptionStrs = new String[spiltStr.length+1];
		
		//计算每张图片的完整路径
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
		//第一张图片的描述信息，即整体描述信息
		if(!isUesedSVM)
		{
			resultDescriptionStrs[0] = //"识别结果：\n" + 
					"最佳匹配：" + spiltStr[0].substring(1, spiltStr[0].lastIndexOf("_")) + "\n" +
					"推荐衣服：" + spiltStr[1].substring(1, spiltStr[1].lastIndexOf("_")) + "\n" +
					"相似衣服1：" + spiltStr[2].substring(1, spiltStr[2].lastIndexOf("_")) + "\n" +
					"相似衣服2：" + spiltStr[3].substring(1, spiltStr[3].lastIndexOf("_")) + "\n" +
					"相似衣服3：" + spiltStr[4].substring(1, spiltStr[4].lastIndexOf("_"));
		}
		else {
			resultDescriptionStrs[0] = //"识别结果：\n" + 
					"推荐衣服1：" + spiltStr[0].substring(1, spiltStr[0].lastIndexOf("_")) + "\n" +
					"推荐衣服2：" + spiltStr[1].substring(1, spiltStr[1].lastIndexOf("_")) + "\n" +
					"推荐衣服3：" + spiltStr[2].substring(1, spiltStr[2].lastIndexOf("_")) + "\n" +
					"推荐衣服4：" + spiltStr[3].substring(1, spiltStr[3].lastIndexOf("_")) + "\n" +
					"推荐衣服5：" + spiltStr[4].substring(1, spiltStr[4].lastIndexOf("_"));
		}
		
		//分别计算每张图片的描述信息，描述信息包括类型、品牌、型号
		for(int i=1; i< resultDescriptionStrs.length; i++)
		{
			String[] clothesInfo = spiltStr[i-1].substring(1).split("_");
			String[] strs;
			if(clothesInfo.length == 3) //没有次型号
			{
				strs = new String[3];
				strs[0] = "类型：";
				strs[1] = "品牌：";
				strs[2] = "型号：";
			}
			else  //有次型号
			{
				strs = new String[4];
				strs[0] = "类型：";
				strs[1] = "品牌：";
				strs[2] = "型号：";
				strs[3] = "次型号：";
			}
			
			
			if(spiltStr[i-1].charAt(0) == '0')
			{
				strs[0] += "男装";
			}
			else {
				strs[0] += "女装";
			}
			
			//取得品牌和型号信息
			strs[1] += clothesInfo[0];
			strs[2] += clothesInfo[1];
			
			//取得次型号
			if(strs.length == 4)
			{
				strs[3] += clothesInfo[2];
			}
			
			resultDescriptionStrs[i] = "";
			for(int j=0; j< strs.length; j++)
			{
				resultDescriptionStrs[i] = resultDescriptionStrs[i] + strs[j] + "\n";
			}
			
			//计算resStr，即显示在主界面中的结果最佳匹配衣服的信息
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
		 * 将5张图片的路径和对应的描述信息合并在一起作为一个字符串，以便将其传递到ShowResultActivity
		 * */
		//合并5张图片的路径信息
		for(int i=0; i< filenames.length; i++)
		{
			deliverResuletStr = deliverResuletStr + filenames[i] + ",";
		}
/*
		//合并6张图片的描述信息
		for(int i=0; i< resultDescriptionStrs.length-1; i++)
		{
			deliverResuletStr = deliverResuletStr + resultDescriptionStrs[i] + ",";
		}
		deliverResuletStr = deliverResuletStr + resultDescriptionStrs[resultDescriptionStrs.length-1];
*/
		//合并6张图片的描述信息
		if(!isUesedSVM)
		{
			deliverResuletStr = deliverResuletStr + resultDescriptionStrs[0] + ",";
			deliverResuletStr = deliverResuletStr + "最佳匹配" + "\n" + resultDescriptionStrs[1] + ",";
			deliverResuletStr = deliverResuletStr + "推荐衣服" + "\n" + resultDescriptionStrs[2] + ",";
			deliverResuletStr = deliverResuletStr + "相似衣服1" + "\n" +resultDescriptionStrs[3] + ",";
			deliverResuletStr = deliverResuletStr + "相似衣服2" + "\n" +resultDescriptionStrs[4] + ",";
			deliverResuletStr = deliverResuletStr + "相似衣服3" + "\n" +resultDescriptionStrs[5];
		}
		else
		{
			deliverResuletStr = deliverResuletStr + resultDescriptionStrs[0] + ",";
			deliverResuletStr = deliverResuletStr + "相似衣服1" + "\n" + resultDescriptionStrs[1] + ",";
			deliverResuletStr = deliverResuletStr + "相似衣服2" + "\n" + resultDescriptionStrs[2] + ",";
			deliverResuletStr = deliverResuletStr + "相似衣服3" + "\n" +resultDescriptionStrs[3] + ",";
			deliverResuletStr = deliverResuletStr + "相似衣服4" + "\n" +resultDescriptionStrs[4] + ",";
			deliverResuletStr = deliverResuletStr + "相似衣服5" + "\n" +resultDescriptionStrs[5];
		}
		
		return deliverResuletStr;
	}
	
	//响应选择菜单事件
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
	                 mThread.start();//线程启动
				 }
			}
		}
	
	Runnable runnable = new Runnable() {
		
		@Override
		public void run() {// run()在新的线程中运行
			String tosendStr;
			
			//System.out.println("************* filename: " + filename + "***********");
			
			//SVM 快速识别
			if(isUesedSVM) 
			{
//				tosendStr = ImageProc.ProcBySvm(trainNNDir, filename, resultNNDir);
//				Log.i(TAG, tosendStr);
				
				tosendStr = "0zara_1608_303,0zara_0693_329,0selected_412427048,1basichouse_HMJP226C,0selected_412427014";
			}
			//请去识别
			else
			{
//				tosendStr = ImageProc.ProcBySurf(imagesDir, filename, imagesSurfInfoDir);
//				Log.i(TAG, tosendStr);
				tosendStr = "1only_113327003_04.jpg,0jackjones_212427019_4.jpg,1basichouse_HMCA723A_01.jpg,1basichouse_HMJP226C_03.jpg,0jackjones_212427006_5.jpg";
						
			}
			
			//发送消息，控制进度对话框的终止
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

