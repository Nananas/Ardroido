package com.nana.testapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends ActionBarActivity {
	
	// BT stuff
	private ListView devicesListView;
	
	private ArrayAdapter<String> arrayAdapter;
	private BluetoothAdapter mBluetoothAdapter;

	
	
	/*
	 * On creation of this activity
	 */
    protected void onCreate(Bundle savedInstanceState) {
    	
    	// load layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // get references from layout
        devicesListView = (ListView)findViewById(R.id.listdevices);
        // update the list of paired devices for the first time
        updatePairedBTDevices();
        /* ListView:
         * enable clicking, which will activate the controlActivity with the appropriate intent
         */
        devicesListView.setOnItemClickListener(new OnItemClickListener() {
        	
        	public void onItemClick(AdapterView<?> parent, View v, int pos, long id)
        	{

        		// get device info to pass along
        		String item = arrayAdapter.getItem(pos);
        		
        		// switch activity to Control
        		switchToControl(item);
        		
        	}
		});
        
        // refresh button
        Button refreshButton = (Button)findViewById(R.id.buttonrefresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				updatePairedBTDevices();
			}
		});
        
        
    }
    
    /*
     * Called when returning to the application, e.g: when starting BT adapter or after pairing a new device
     * -> update the list of devices with the new ones
     */
    protected void onResume()
    {
    	updatePairedBTDevices();
    	
    	super.onResume();
    }
    
    private void updatePairedBTDevices()
    {
    	// check if the BT adapter is present and ON
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBluetoothAdapter.isEnabled())
        {
        	// send toast message if not
        	Toast.makeText(getApplicationContext(), "Bluetooth not enabled", Toast.LENGTH_LONG).show();
        }
        
        // get only paired devices
        final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        
        // populate an arrayList with the names of all paired devices, which are active a.t.m.
        ArrayList<String> s = new ArrayList<String>();
        for (BluetoothDevice bt : pairedDevices){
        	s.add(bt.getName() + "\n" + bt.getAddress());
        }
        
        // array containing name and MAC address of all paired devices, to pass to listView
        arrayAdapter = new ArrayAdapter<String>(
        		this,
        		android.R.layout.simple_list_item_1,
        		s);
                
        
        // update list with devices
        devicesListView.setAdapter(arrayAdapter);
    }
    
    public void switchToControl(String item)
    {
    	Intent intent = new Intent(this, com.nana.testapp.ControlActivity.class);
    	intent.putExtra("DEVICE_INFO", item);
    	startActivity(intent);
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
}
