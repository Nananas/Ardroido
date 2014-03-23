package com.nana.testapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

public class BTManager extends Thread {
	private final BluetoothSocket mmSocket;
	private final BluetoothDevice mmDevice;
	
	private final InputStream mmInStream;
	private final OutputStream mmOutStream;
	
	private static final UUID MYUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	private char[] outputReference;
	
	private ProgressDialog progressDialog;
	
	private boolean stopMe;
	
	public BTManager (BluetoothDevice device, char[] outputRef, ProgressDialog progDialog)
	{
		
		System.out.println("starting thread");
		
		//mBluetoothAdapter = mBA;
		BluetoothSocket tmp = null;
		mmDevice = device;
		
		System.out.println("Creating Rfcomm socket to service");
		// Try making a BT socket using the specified UUID
		try{
			tmp = device.createRfcommSocketToServiceRecord(MYUUID);
		
		} catch (IOException e) {
			System.out.print("Something went wrong here, sorry");
		}
		
		mmSocket = tmp;
		
		// streams
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		
		try {
			tmpIn = mmSocket.getInputStream();
			tmpOut = mmSocket.getOutputStream();
		} catch (IOException e) {}
		
		mmInStream = tmpIn;
		mmOutStream = tmpOut;
		
		outputReference = outputRef;
		progressDialog = progDialog;
		stopMe = false;
	}
	
	public void run()
	{
		//mBluetoothAdapter.cancelDiscovery();
		System.out.println("starting thread");
		try{
			mmSocket.connect();
		} catch (IOException connectException){
			try {
				mmSocket.close();
			}catch (IOException closeException) {}
			
			System.out.print("Something went wrong here");
			return;
		}
				
		// Connected?
		if (mmSocket != null)
		{
			System.out.println("Connected to device " + mmDevice.getName());
			progressDialog.dismiss();
			
			System.out.println(mmOutStream);
			
			// continuous loop to send output
			while(!stopMe){
				
				// send stuff over
				write(outputReference);
				
				// wait a little
				try {
					Thread.sleep(70);					
				} catch (InterruptedException e){
					System.out.println("error interruptexception");
				}
			}
		}
	}
	
	
	/*
	 * Now write() is called every so seconds, but this can also be called from
	 * the ControlActivity, only when something changes with the output buffer
	 */
	public void write (char[] toBytes)
	{
		String buffer = new String(toBytes);
		try {
			mmOutStream.write(buffer.getBytes());
			System.out.println("Send " + buffer);
		} catch (IOException e) {
			System.out.println("could not send to BT device");
		}
	}
	
	
	public void cancel(){
		//TODO: when pressing back button, close connection
		try{
			// close socket
			mmSocket.close();
		} catch (IOException e){}
		
		// stop thread
		stopMe = true;
	}
}
