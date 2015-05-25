package com.opencv.itemIdentification;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ShowResultActivity extends Activity {

	private String resultStr;
	private TextView deliverDataView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showresult);
		
		resultStr = this.getIntent().getStringExtra("resultInfo");
		deliverDataView = (TextView)findViewById(R.id.deliverDataView); 
		
		//传递数据，以便在页面显示
		deliverDataView.setText(resultStr);
	}
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
         
         //实现zoomin和zoomout,即类似iphone的进入和退出时的效果 
         overridePendingTransition(R.animator.zoomin, R.animator.zoomout); 
	}
}
