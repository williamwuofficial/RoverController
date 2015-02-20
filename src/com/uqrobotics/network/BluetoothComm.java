package com.uqrobotics.network;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

public class BluetoothComm implements NetworkStream {

	private BluetoothAdapter bluetoothAdapter = null;
	private BluetoothDevice bluetoothDevice = null;
	final String deviceUUID = "00001101-0000-1000-8000-00805F9B34FB"; // Default as per documentation
	private UUID parsedUUID = null;
	final String deviceMAC = "00:06:66:63:A1:FE";
	
	private BluetoothSocket bluetoothSocket = null;
	private OutputStream bluetoothOutStream;

	public BluetoothComm(){
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	@Override
	public boolean isStreamEnabled(){
		return bluetoothAdapter.isEnabled();
	}
	
	@Override
	public boolean connect() {
		
		if (!bluetoothAdapter.isEnabled()) {
			return false;
		}
		
		try {
			if (parsedUUID == null) {
				parsedUUID = UUID.fromString(deviceUUID);
				bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceMAC);
			}
			bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(parsedUUID);
			bluetoothSocket.connect();
			bluetoothOutStream = bluetoothSocket.getOutputStream();
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			return false;
		}

		return true;
	}

	@Override
	public boolean disconnect() {
		try {
			bluetoothOutStream.flush();
			bluetoothOutStream.close();
			bluetoothSocket.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return false;			
		}
		
		return true;
	}

	@Override
	public boolean write(String message) {
		try {
			bluetoothOutStream.write((message + "\n").getBytes());
			bluetoothOutStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Override
	public String toString(){
		return "Bluetooth";
	}
	

	
}
