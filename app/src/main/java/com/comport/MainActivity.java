package com.comport;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import com.bean.ComBean;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android_serialport_api.SerialPortFinder;

public class MainActivity extends Activity {
	EditText editTextPortData;
	Button ButtonClear;
	ToggleButton toggleButtonPort;
	Spinner SpinnerPort;
	Spinner SpinnerBaudRate;
	SerialControl ComPort;
	DispQueueThread DispQueue;
	SerialPortFinder mSerialPortFinder;




    /** Called when the activity is first created. */
    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		ComPort = new SerialControl();
        DispQueue = new DispQueueThread();
        DispQueue.start();
        setControls();
    }
    @Override
    public void onDestroy(){
		CloseComPort(ComPort);
    	super.onDestroy();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
        CloseComPort(ComPort);
	    setContentView(R.layout.main);
		setControls();
    }




    //----------------------------------------------------
    private void setControls()
	{
    	String appName = getString(R.string.app_name);
        try {
			PackageInfo pinfo = getPackageManager().getPackageInfo("com.comport", PackageManager.GET_CONFIGURATIONS);
			String versionName = pinfo.versionName;
			setTitle(appName+" Version "+versionName);
        } catch (NameNotFoundException e) {
        	e.printStackTrace();
        }
		editTextPortData=(EditText)findViewById(R.id.editTextPortData);

		Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/digital.TTF");

		editTextPortData.setTypeface(custom_font);

		ButtonClear=(Button)findViewById(R.id.buttonClear);

		toggleButtonPort=(ToggleButton)findViewById(R.id.toggleButtonPort);
       	SpinnerPort=(Spinner)findViewById(R.id.SpinnerPort);
    	SpinnerBaudRate=(Spinner)findViewById(R.id.SpinnerBaudrate);

    	ButtonClear.setOnClickListener(new ButtonClickEvent());

    	toggleButtonPort.setOnCheckedChangeListener(new ToggleButtonCheckedChangeEvent());
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.baudrates_value,android.R.layout.simple_spinner_item);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	SpinnerBaudRate.setAdapter(adapter);

    	SpinnerBaudRate.setSelection(2);

    	
    	mSerialPortFinder= new SerialPortFinder();
    	String[] entryValues = mSerialPortFinder.getAllDevicesPath();
    	List<String> allDevices = new ArrayList<String>();
//		for (int i = 0; i < entryValues.length; i++) {
//			allDevices.add(entryValues[i]);
//		}
		allDevices.add("/dev/ttyMT1");
		ArrayAdapter<String> aspnDevices = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, allDevices);
		aspnDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		SpinnerPort.setAdapter(aspnDevices);

//		if (allDevices.size()>0)
//		{
//			SpinnerPort.setSelection(8);
//		}

		SpinnerPort.setOnItemSelectedListener(new ItemSelectedEvent());

		SpinnerBaudRate.setOnItemSelectedListener(new ItemSelectedEvent());

	}
    //----------------------------------------------------
    class ItemSelectedEvent implements Spinner.OnItemSelectedListener{
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
		{
			if ((arg0 == SpinnerPort) || (arg0 == SpinnerBaudRate))
			{
				CloseComPort(ComPort);
				toggleButtonPort.setChecked(false);
			}
		}

		public void onNothingSelected(AdapterView<?> arg0)
		{}
    	
    }
 	//----------------------------------------------------
    class ButtonClickEvent implements View.OnClickListener {
		public void onClick(View v)
		{
			if (v == ButtonClear){
				editTextPortData.setText("");
			}
		}
    }
    //----------------------------------------------------
    class ToggleButtonCheckedChangeEvent implements ToggleButton.OnCheckedChangeListener{
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (buttonView == toggleButtonPort){
				if (isChecked){
                     Log.e("MainActivty",""+SpinnerPort.getSelectedItem().toString());
					 Log.e("MainActivty",""+SpinnerBaudRate.getSelectedItem().toString());
					ComPort.setPort(SpinnerPort.getSelectedItem().toString());
					ComPort.setBaudRate(SpinnerBaudRate.getSelectedItem().toString());
					OpenComPort(ComPort);

				}else {
					CloseComPort(ComPort);

				}
			}
		}
    }
    //----------------------------------------------------
    private class SerialControl extends SerialHelper{

//		public SerialControl(String sPort, String sBaudRate){
//			super(sPort, sBaudRate);
//		}
		public SerialControl(){
		}

		@Override
		protected void onDataReceived(final ComBean ComRecData)
		{
				DispQueue.AddQueue(ComRecData);

		}

	}
    //----------------------------------------------------
    private class DispQueueThread extends Thread{
		private Queue<ComBean> QueueList = new LinkedList<ComBean>(); 
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				final ComBean ComData;


		        while((ComData=QueueList.poll())!=null)
		        {
		        	runOnUiThread(new Runnable()
					{
						public void run()
						{
							DispRecData(ComData);
						}
					});
		        	try
					{
		        		Thread.sleep(100);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					break;

				}

			}

		}

		public synchronized void AddQueue(ComBean ComData){
			QueueList.add(ComData);
		}
	}



	//----------------------------------------------------
    private void setDelayTime(TextView v){
			SetiDelayTime(ComPort, v.getText().toString());
	    }

    //----------------------------------------------------
    private void SetiDelayTime(SerialHelper ComPort,String sTime){
    	ComPort.setiDelay(Integer.parseInt(sTime));
    }

    //----------------------------------------------------
    private void DispRecData(ComBean ComRecData){
    	StringBuilder sMsg=new StringBuilder();
		sMsg.append(new String(ComRecData.bRec));

		try{
			Log.e("Value ","weight value : "+sMsg);
		//	editTextPortData.setText(sMsg.substring(1,8));

			editTextPortData.setText(sMsg);

		}catch (Exception e)
		{
			//ShowMessage(e.toString());
		}

    }

     //----------------------------------------------------
    private void CloseComPort(SerialHelper ComPort){
    	if (ComPort!=null){
    		ComPort.stopSend();
    		ComPort.close();
		}
    }
    //----------------------------------------------------
    private void OpenComPort(SerialHelper ComPort){
    	try
		{
			ComPort.open();
		} catch (SecurityException e) {
			ShowMessage("SecurityException!");
		} catch (IOException e) {
			ShowMessage("IOException!");
		} catch (InvalidParameterException e) {
			ShowMessage("InvalidParameterException!");
		}
    }
    //------------------------------------------
  	private void ShowMessage(String sMsg)
  	{
  		Toast.makeText(this, sMsg, Toast.LENGTH_SHORT).show();
  	}
}