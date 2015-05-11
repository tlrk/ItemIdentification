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
		
		//�������ݣ��Ա���ҳ����ʾ
		deliverDataView.setText(resultStr);
	}
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
         
         //ʵ��zoomin��zoomout,������iphone�Ľ�����˳�ʱ��Ч�� 
         overridePendingTransition(R.animator.zoomin, R.animator.zoomout); 
	}
}
