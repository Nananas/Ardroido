package com.nana.testapp;

import java.io.InputStream;
import java.io.OutputStream;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;


/*
 * Activity with two buttons that calculates the desired output chars depending
 * on the position of the touches on each of the two buttons
 */
public class ControlActivity extends ActionBarActivity {
	
	// handling update calls to textView in a non-UI thread
	private Handler mHandler = new Handler();
	
	static final int MAXSPEED = 10;
	static final int THRESHOLD = 3;
	
	Button buttonR;
	Button buttonL;
	Button buttonLaser;
	Button buttonFire;
	
	TextView textview;
	
	char[] output;
	
	// bluetooth stuff
	private BluetoothDevice mmDevice;
	private BluetoothAdapter mmBluetoothAdapter;
	private BTManager mBTManager;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
    	System.out.println("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        
        /*
         * BT stuff here -------------------
         */
        
        // get device info from intent
        Intent intent = getIntent();
        String deviceInfo = intent.getStringExtra("DEVICE_INFO");
        String deviceName = deviceInfo.split("\n")[0];	// maybe show this somewhere
        String deviceMAC = deviceInfo.split("\n")[1];
        
        // get device
        mmBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mmDevice = mmBluetoothAdapter.getRemoteDevice(deviceMAC);
       
        
        /*
         * INPUT here ----------------------
         */
        
        buttonR = (Button) findViewById(R.id.buttonright);
    	buttonL = (Button) findViewById(R.id.buttonleft);
    	buttonLaser = (Button) findViewById(R.id.laserPos);
    	buttonFire = (Button) findViewById(R.id.laserAan);
    	
    	textview = (TextView) findViewById(R.id.textview);
    	
    	output = new char[8];
    	output[0] = '0';
    	output[1] = '0';
    	output[2] = '0';
    	output[3] = '0';
    	output[4] = '0';
    	output[5] = '0';
    	output[6] = 'e';
    	
    	// update textview message
    	updateOutput();
    	
    	System.out.println(buttonR);
    	buttonR.setOnTouchListener(new View.OnTouchListener()
    	{
    		public boolean onTouch(View v, MotionEvent e)
    		{
    			switch(e.getAction())
    			{
	    			case MotionEvent.ACTION_DOWN:
	    			case MotionEvent.ACTION_MOVE:
	    				// ratio
	        			float dy = e.getY() - buttonR.getHeight()/2;
	        			float dydH = - dy/(buttonR.getHeight()/2);
	        			
	        			int speedRight = (int) Math.round(dydH * MAXSPEED);
	        			
	        			// dont go past maximum speed;
	        			if (speedRight >= MAXSPEED) speedRight = MAXSPEED-1;
	        			if (speedRight <= -MAXSPEED) speedRight = -MAXSPEED+1;
	        			
	
	        			// calculate 3th & 4th byte to send
	        			if (speedRight > THRESHOLD) 	// moving forward
	        			{
	        				output[2]='0';
	        				output[3]=(char)(speedRight + 48);
	        			}
	        			else if (speedRight < THRESHOLD) // moving backward
	        			{
	        				output[2]='-';
	        				output[3]=(char) (-speedRight + 48);
	        			}
	        			else 	// not moving
	        			{
	        				output[2]='0';
	        				output[3]='0';
	        			}
	        			
	        			
	        			// update textview message
	        			updateOutput();
	        			
	        			break;
	    			case MotionEvent.ACTION_UP:
	    				// set output back to zero
	    				output[2] = '0';
	    				output[3]= '0';
	    				
	    				updateOutput();
	    				
	    				break;
    			}
    			
    			return true;
    		}
    	});
    	
    	buttonL.setOnTouchListener(new View.OnTouchListener()
    	{
    		public boolean onTouch(View v, MotionEvent e)
    		{    	
    			switch(e.getAction())
    			{
    			
	    			case MotionEvent.ACTION_DOWN:
	    			case MotionEvent.ACTION_MOVE:
		    			// ratio
		    			float dy = e.getY() - buttonL.getHeight()/2;
		    			float dydH = - dy/(buttonL.getHeight()/2);
		    			
		    			int speedLeft = (int) Math.round(dydH * MAXSPEED);
		    			
		    			// dont go past maximum speed;
		    			if (speedLeft >= MAXSPEED) speedLeft = MAXSPEED-1;
		    			if (speedLeft <= -MAXSPEED) speedLeft = -MAXSPEED+1;
		    			
		
		    			// calculate 3th & 4th byte to send
		    			if (speedLeft > THRESHOLD) 	// moving forward
		    			{
		    				output[0]='0';
		    				output[1]=(char) (speedLeft + 48);	// numbers start at 49
		    			}
		    			else if (speedLeft < THRESHOLD) // moving backward
		    			{
		    				output[0]='-';
		    				output[1]=(char)(-speedLeft + 48); // numbers start at 49
		    			}
		    			else 	// not moving
		    			{
		    				output[0]='0';
		    				output[1]='0';
		    			}
	        			
	        			
		    			
		    			updateOutput();
		    			
		    			break;
		    			
	    		case MotionEvent.ACTION_UP:
					// set output back to zero
					output[0] = '0';
					output[1]=  '0';
					
					updateOutput();
					break;
    			}
    			
    			return true;
    		}
    	});
    	
    	buttonLaser.setOnTouchListener(new View.OnTouchListener()
    	{
    		public boolean onTouch(View v, MotionEvent e)
    		{
    			switch(e.getAction())
    			{
	    			case MotionEvent.ACTION_DOWN:
	    			case MotionEvent.ACTION_MOVE:
	    				// ratio
	        			float dy = e.getY() - buttonLaser.getHeight()/2;
	        			float dydH = - dy/(buttonLaser.getHeight()/2);
	        			
	        			int speedLaser = (int) Math.round(dydH * MAXSPEED);
	        			
	        			// dont go past maximum speed;
	        			if (speedLaser >= MAXSPEED) speedLaser = MAXSPEED-1;
	        			if (speedLaser <= -MAXSPEED) speedLaser = -MAXSPEED+1;
	        			
	
	        			// calculate 3th & 4th byte to send
	        			if (speedLaser > THRESHOLD) 	// moving forward
	        			{
	        				output[4]='1';
	        			}
	        			else if (speedLaser < THRESHOLD) // moving backward
	        			{
	        				output[4]='-';
	        			}
	        			else 	// not moving
	        			{
	        				output[4]='0';
	        			}
	        			
	        			// update textview message
	        			updateOutput();
	        			
	        			break;
	    			case MotionEvent.ACTION_UP:
	    				// set output back to zero
	    				output[4] = '0';
	    				output[5]= '0';
	    				
	    				updateOutput();
	    				
	    				break;
    			}
    			
    			return true;
    		}
    	});
    	
    	buttonFire.setOnTouchListener(new View.OnTouchListener()
    	{
    		public boolean onTouch(View v, MotionEvent e)
    		{
    			switch(e.getAction())
    			{
	    			case MotionEvent.ACTION_DOWN:
	    				
	    			case MotionEvent.ACTION_MOVE:
	    				output[5]='1';
	    				
	    				updateOutput();
	    				break;
	    			case MotionEvent.ACTION_UP:
	    				// set output back to zero
	    				output[5]= '0';
	    				
	    				updateOutput();
	    				
	    				break;
    			}
    			
    			return true;
    		}
    	});
    	
    	
    	/*
    	 * STARTUP here --------------
    	 */
    	// we want a dialog showing we are still trying to connect
    	ProgressDialog progDialog = ProgressDialog.show(ControlActivity.this, "Connecting to device", deviceName);
    	// start connection thread
        mBTManager = new BTManager(mmDevice, output, progDialog);
        mBTManager.start();
        
       
    }
    
    public void updateOutput()
    {
    	mHandler.post(new Runnable(){
    		public void run(){
    			textview.setText(new String(output));
    		}
    	});
    }
    
    protected void onStop()
    {
    	mBTManager.cancel();
    	super.onStop();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
	@Override
    public void onBackPressed() {
    	// make sure to finish this activity, so that we cannot return
		// using the back button
		finish();
		super.onBackPressed();
    }
}
