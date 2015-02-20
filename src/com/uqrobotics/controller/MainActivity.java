package com.uqrobotics.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.uqrobotics.rovercontroller.R;
import com.uqrobotics.network.BluetoothComm;
import com.uqrobotics.network.NetworkStream;

import android.app.Activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

	NetworkStream networkStream = new BluetoothComm();
	private boolean isConnected = false;
	
	private Button bConnect;
	private Button bDisconnect;
	
	//Portrait Widgets
	private ImageButton bFwd;
	private ImageButton bRev;
	private ImageButton bLeft;
	private ImageButton bRight;
	private SeekBar sbSpeed;
	private SeekBar sbSpeedDiff;
	private Switch swAccel;
	boolean isAccel = false;
	
	//Landscape Widgets
	private TextView tvAccelData;
	private AccelControl task = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		bConnect = (Button) findViewById(R.id.bConnect);
		bConnect.setOnClickListener(this);
		bConnect.setEnabled(!isConnected);
		bDisconnect = (Button) findViewById(R.id.bDisconnect);
		bDisconnect.setOnClickListener(this);
		bDisconnect.setEnabled(isConnected);
		
		if(!networkStream.isStreamEnabled()){
			Toast.makeText(getApplicationContext(), "ERROR! Please enable" + networkStream.toString(), Toast.LENGTH_LONG).show();
		}
		
		if (getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT) {
			bFwd = (ImageButton) findViewById(R.id.bRoverFwd);
			bFwd.setOnClickListener(this);
			bRev = (ImageButton) findViewById(R.id.bRoverRev);
			bRev.setOnClickListener(this);
			bLeft = (ImageButton) findViewById(R.id.bRoverRL);
			bLeft.setOnClickListener(this);
			bRight = (ImageButton) findViewById(R.id.bRoverRR);
			bRight.setOnClickListener(this);
			sbSpeed = (SeekBar) findViewById(R.id.sbSpeed);
			//sbSpeed.setVisibility(View.INVISIBLE);
			sbSpeedDiff = (SeekBar) findViewById(R.id.sbSpeedDiff);
			sbSpeedDiff.setVisibility(View.INVISIBLE);
			swAccel = (Switch) findViewById(R.id.swAccel);
			swAccel.setVisibility(View.INVISIBLE);
		} else if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
			tvAccelData = (TextView) findViewById(R.id.tvAccelData);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		isConnected = false;
		networkStream.disconnect();
		
		if(task != null){
			task.cancel(true);
			task = null;
		}
	}



	@Override
	public void onClick(View v) {
		int speed;
		switch(v.getId()){
			case R.id.bConnect:
				if (networkStream.connect()){
					isConnected = true;
					Toast.makeText(getApplicationContext(), "Connection successful", Toast.LENGTH_LONG).show();
					if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
						task = new AccelControl(MainActivity.this, tvAccelData, bDisconnect, networkStream);
					    task.execute();
					}
				} else {
					Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_LONG).show();
				}				
			break;
				
			case R.id.bDisconnect:
				if (networkStream.disconnect()){
					isConnected = false;
					Toast.makeText(getApplicationContext(), "Disconnection successful", Toast.LENGTH_LONG).show();
					if(task != null){
						task.cancel(true);
						task= null;
					}
				} else {
					Toast.makeText(getApplicationContext(), "Disconnection failed", Toast.LENGTH_LONG).show();
				}
			break;
			
			case R.id.bRoverFwd:
				speed = (int) ((sbSpeed.getProgress()/100.0)*127.0);
				System.out.println("Progress bar " + sbSpeed.getProgress());
				System.out.println("Setting speed " + speed);
				networkStream.write("M " + speed + " " + speed);
			break;
			
			case R.id.bRoverRL:
				speed = (int) ((sbSpeed.getProgress()/100.0)*127.0);
				System.out.println("Progress bar " + sbSpeed.getProgress());
				System.out.println("Setting speed " + speed);
				networkStream.write("M 0 " + speed);
			break;
			
			case R.id.bRoverRR:
				speed = (int) ((sbSpeed.getProgress()/100.0)*127.0);
				System.out.println("Progress bar " + sbSpeed.getProgress());
				System.out.println("Setting speed " + speed);
				networkStream.write("M " + speed + " 0");
			break;
			
			case R.id.bRoverRev:
				speed = (int) ((sbSpeed.getProgress()/100.0)*127.0);
				System.out.println("Progress bar " + sbSpeed.getProgress());
				System.out.println("Setting speed " + speed);
				networkStream.write("M -" + speed + " -" + speed);
			break;
			
			default:
			break;
		
		}
		
		bConnect.setEnabled(!isConnected);
		bDisconnect.setEnabled(isConnected);
	}
	
	public int getScreenOrientation() {
	    Display getOrient = getWindowManager().getDefaultDisplay();
	    int orientation = Configuration.ORIENTATION_UNDEFINED;
	    if(getOrient.getWidth()==getOrient.getHeight()){
	        orientation = Configuration.ORIENTATION_SQUARE;
	    } else{ 
	        if(getOrient.getWidth() < getOrient.getHeight()){
	            orientation = Configuration.ORIENTATION_PORTRAIT;
	        }else { 
	             orientation = Configuration.ORIENTATION_LANDSCAPE;
	        }
	    }
	    return orientation;
	}
	
	private class AccelControl extends AsyncTask<Void, Void, Boolean> implements SensorEventListener{
		private Activity activity;
		private TextView textView;
		private Button button;
		private NetworkStream networkStream;
		
		private long lastTime;
		private static final long PERIOD_DELAY = 220;
		
		private SensorManager sensorManager;
		double ax,ay,az;
		
		private boolean running;
		
		public AccelControl(Activity act, TextView tv, Button b, NetworkStream ns){
			this.activity = act;
			this.textView = tv;
			this.button = b;
			this.networkStream = ns;
			
			sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
	        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
	        
	        running = true;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			lastTime = System.currentTimeMillis();
			while(true){
				if ( (System.currentTimeMillis() - lastTime) >= PERIOD_DELAY) {
					activity.runOnUiThread(new Runnable(){
						public void run(){
							textView.setText(	"X: " + ax + 
												"\nY: " + ay + 
												"\nZ: " + az);
							
							//ay determines rotation -9.8 is turn left 
							//az determines speed 9.8 max
							int speed = (int) ((az/9.8)*127);
							int diff = (int) ((ay/9.8)*127);
							int left_speed = speed + diff;
							if (left_speed > 127) {left_speed = 127;} else if (left_speed < -128) {left_speed = -128;}
							int right_speed = speed - diff;
							if (right_speed > 127) {right_speed = 127;} else if (right_speed < -128) {right_speed = -128;}
							//System.out.println("M " + left_speed + " " + right_speed);
							networkStream.write("M " + left_speed + " " + right_speed);
						}
					});
					lastTime = System.currentTimeMillis();
				}
				if(isCancelled()) break;
			}
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result){
			if (!result) {			
				activity.runOnUiThread(new Runnable() {
			        public void run() {
			        	button.performClick();
			        	Toast.makeText(activity.getApplicationContext(), "ERROR! Aborting control", Toast.LENGTH_LONG).show();
			        }
			    });
			}
		}
		
		@Override
		protected void onCancelled(){
			running = false;
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
	            ax=event.values[0];
                ay=event.values[1];
                az=event.values[2];
	        }
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
		
	}

	
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	*/
}


