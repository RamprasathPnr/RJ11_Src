package com.bean;

import android.util.Log;

import java.text.SimpleDateFormat;

public class ComBean {
		public byte[] bRec=null;
		public String sRecTime="";
		public String sComPort="";
		public ComBean(String sPort,byte[] buffer,int size){
			sComPort=sPort;
			bRec=new byte[size];
			for (int i = 0; i < size; i++)
			{
				bRec[i]=buffer[i];
			}

		/*	float i =bRec[1] | bRec[2] << 8;
			Log.e("","flaot value "+i);
*/
			SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss");       
			sRecTime = sDateFormat.format(new java.util.Date()); 
		}
}