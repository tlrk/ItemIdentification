package com.opencv.itemIdentification;

import android.R.string;

public class ImageProc {
	
	static 
	{  
		System.loadLibrary("image_proc");   
	}  
	
	public static native String ProcBySurf(String dir, String fileName, String infoFilename); 
	public static native String ProcBySvm(String dir, String fileName, String SVMdir); 
}
