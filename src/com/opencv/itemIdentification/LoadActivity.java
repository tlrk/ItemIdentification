package com.opencv.itemIdentification;

import com.opencv.itemIdentification.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.TextView;

public class LoadActivity extends Activity {
	 private static final int LOAD_DISPLAY_TIME = 5000;
	 /** Called when the activity is first created. */ 
	 @Override 
	 public void onCreate(Bundle savedInstanceState) 
	 { 
		 super.onCreate(savedInstanceState);
		 getWindow().setFormat(PixelFormat.RGBA_8888);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

	     setContentView(R.layout.load);

	     //TextView versionNumber = (TextView) findViewById(R.id.versionNumber);
	    // versionNumber.setText("Version " + "1.0.1");
	 
	     new Handler().postDelayed(new Runnable() {
             public void run() {
                /* Create an Intent that will start the Main WordPress Activity. */
              Intent mainIntent = new Intent(LoadActivity.this, MainActivity.class);
              LoadActivity.this.startActivity(mainIntent);
              LoadActivity.this.finish();
              
              //实现zoomin和zoomout,即类似iphone的进入和退出时的效果 
              overridePendingTransition(R.animator.zoomin, R.animator.zoomout);  
             }
        }, LOAD_DISPLAY_TIME); 

    }
}
