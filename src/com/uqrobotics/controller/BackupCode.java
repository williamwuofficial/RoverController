package com.uqrobotics.controller;

public class BackupCode {

}

/*
package com.uqrobotics.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.csse.bluetooth.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private EditText eNP2CmdField;
	private Button bSendCommand;
	private TextView tvResponseField;

	private EditText eRfChSet;
	private Button bChSet;
	private ImageButton bPasskey;
	private ImageButton bFwd;
	private ImageButton bRev;
	private ImageButton bLeft;
	private ImageButton bRight;

	private EditText eSpeed;
	private EditText eDuration;

	char[] cMessageArray;

	private BluetoothAdapter bluetoothAdapter = null;
	private BluetoothDevice bluetoothDevice = null;
	final String deviceUUID = "00001101-0000-1000-8000-00805F9B34FB";
	private UUID parsedUUID = null;
	final String deviceMAC = "00:06:66:63:A1:FE";

	private BluetoothSocket bluetoothSocket = null;
	private OutputStream bluetoothOutStream;

	private Handler responseHandler;
	BluetoothSocketListener responseListener;
	Thread responseThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		eNP2CmdField = (EditText) findViewById(R.id.roverCmdField);
		bSendCommand = (Button) findViewById(R.id.cmdButton);
		tvResponseField = (TextView) findViewById(R.id.resultsTextView);

		eRfChSet = (EditText) findViewById(R.id.etRfChField);
		bChSet = (Button) findViewById(R.id.bRfChset);
		bPasskey = (ImageButton) findViewById(R.id.bGetPasskey);
		bFwd = (ImageButton) findViewById(R.id.bRoverFwd);
		bRev = (ImageButton) findViewById(R.id.bRoverRev);
		bLeft = (ImageButton) findViewById(R.id.bRoverRL);
		bRight = (ImageButton) findViewById(R.id.bRoverRR);

		eSpeed = (EditText) findViewById(R.id.etSpeed);
		eDuration = (EditText) findViewById(R.id.etDuration);

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (!bluetoothAdapter.isEnabled()) {
			Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(turnOn, 0);
			Toast.makeText(getApplicationContext(), "Enabling bluetooth",
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(), "Bluetooth active",
					Toast.LENGTH_LONG).show();
		}

		while (!bluetoothAdapter.isEnabled()) {
			try {
				Toast.makeText(getApplicationContext(),
						"Awaiting bluetooth activation", Toast.LENGTH_LONG)
						.show();
				Thread.sleep(1000L);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				return;
			}
		}

		try {
			if (parsedUUID == null) {
				parsedUUID = UUID.fromString(deviceUUID);
				bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceMAC);
				bluetoothSocket = bluetoothDevice
						.createInsecureRfcommSocketToServiceRecord(parsedUUID);
				Toast.makeText(getApplicationContext(), "Socket created",
						Toast.LENGTH_SHORT).show();
			}
		} catch (Throwable e) {
			Toast.makeText(getApplicationContext(), "Error in socket creation",
					Toast.LENGTH_LONG).show();
		}

		try {
			bluetoothSocket.connect();
			bluetoothOutStream = bluetoothSocket.getOutputStream();
			Toast.makeText(getApplicationContext(), "Socket connected",
					Toast.LENGTH_SHORT).show();
		} catch (Throwable e) {
			Toast.makeText(getApplicationContext(),
					"Error in socket connection", Toast.LENGTH_LONG).show();
		}

		bSendCommand.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					bluetoothOutStream
							.write((eNP2CmdField.getText().toString() + "\r")
									.getBytes());
					bluetoothOutStream.flush();
					eNP2CmdField.setText("");
					tvResponseField.setText("");
					Toast.makeText(getApplicationContext(), "Sending NP2 cmd",
							Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(),
							"Error sending rover cmd", Toast.LENGTH_LONG)
							.show();
				}
			}
		});

		responseHandler = new Handler();
		responseListener = new BluetoothSocketListener(bluetoothSocket,
				responseHandler, tvResponseField);
		responseThread = new Thread(responseListener);
		responseThread.start();

		bChSet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					bluetoothOutStream.write(("rfchset "
							+ Integer.parseInt(eRfChSet.getText().toString()) + "\r")
							.getBytes());
					bluetoothOutStream.flush();
					eNP2CmdField.setText("");
					Toast.makeText(getApplicationContext(), "Sending NP2 cmd",
							Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(),
							"Error sending rover cmd", Toast.LENGTH_LONG)
							.show();
				} catch (NumberFormatException stringErr) {
					Toast.makeText(getApplicationContext(),
							"ERROR! Channel not valid", Toast.LENGTH_LONG)
							.show();
				}
			}
		});

		bPasskey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					bluetoothOutStream.write(("getpasskey" + "\r").getBytes());
					bluetoothOutStream.flush();
					eNP2CmdField.setText("");
					Toast.makeText(getApplicationContext(), "Sending NP2 cmd",
							Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(),
							"Error sending rover cmd", Toast.LENGTH_LONG)
							.show();
				}
			}
		});

		bFwd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					bluetoothOutStream.write(("forward "
							+ Integer.parseInt(eSpeed.getText().toString())
							+ " "
							+ Integer.parseInt(eDuration.getText().toString()) + "\r")
							.getBytes());
					bluetoothOutStream.flush();
					eNP2CmdField.setText("");
					Toast.makeText(getApplicationContext(), "Sending NP2 cmd",
							Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(),
							"Error sending rover cmd", Toast.LENGTH_LONG)
							.show();
				} catch (NumberFormatException stringErr) {
					Toast.makeText(getApplicationContext(),
							"ERROR! Speed or Duration not valid",
							Toast.LENGTH_LONG).show();
				}
			}
		});

		bRev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					bluetoothOutStream.write(("reverse "
							+ Integer.parseInt(eSpeed.getText().toString())
							+ " "
							+ Integer.parseInt(eDuration.getText().toString()) + "\r")
							.getBytes());
					bluetoothOutStream.flush();
					eNP2CmdField.setText("");
					Toast.makeText(getApplicationContext(), "Sending NP2 cmd",
							Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(),
							"Error sending rover cmd", Toast.LENGTH_LONG)
							.show();
				} catch (NumberFormatException stringErr) {
					Toast.makeText(getApplicationContext(),
							"ERROR! Speed or Duration not valid",
							Toast.LENGTH_LONG).show();
				}
			}
		});

		bLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					bluetoothOutStream.write(("rotate left "
							+ Integer.parseInt(eSpeed.getText().toString())
							+ " "
							+ Integer.parseInt(eDuration.getText().toString()) + "\r")
							.getBytes());
					bluetoothOutStream.flush();
					eNP2CmdField.setText("");
					Toast.makeText(getApplicationContext(), "Sending NP2 cmd",
							Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(),
							"Error sending rover cmd", Toast.LENGTH_LONG)
							.show();
				} catch (NumberFormatException stringErr) {
					Toast.makeText(getApplicationContext(),
							"ERROR! Speed or Duration not valid",
							Toast.LENGTH_LONG).show();
				}
			}
		});

		bRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					bluetoothOutStream.write(("rotate right "
							+ Integer.parseInt(eSpeed.getText().toString())
							+ " "
							+ Integer.parseInt(eDuration.getText().toString()) + "\r")
							.getBytes());
					bluetoothOutStream.flush();
					eNP2CmdField.setText("");
					Toast.makeText(getApplicationContext(), "Sending NP2 cmd",
							Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(),
							"Error sending rover cmd", Toast.LENGTH_LONG)
							.show();
				} catch (NumberFormatException stringErr) {
					Toast.makeText(getApplicationContext(),
							"ERROR! Speed or Duration not valid",
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			bluetoothOutStream.flush();
			bluetoothOutStream.close();
			bluetoothSocket.close();
			Toast.makeText(getApplicationContext(), "Shutting down connection",
					Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(),
					"Error shutting down connection", Toast.LENGTH_LONG).show();
		}

		BluetoothAdapter.getDefaultAdapter().disable();
		Toast.makeText(getApplicationContext(), "Turning off bluetooth",
				Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class MessagePoster implements Runnable {
		private TextView responseView;
		private String response;

		public MessagePoster(TextView responseView, String response) {
			this.responseView = responseView;
			this.response = response;
		}

		public void run() {
			responseView.setText(response);
		}
	}

	private class BluetoothSocketListener implements Runnable {
		private BluetoothSocket socket;
		private TextView textView;
		private Handler handler;

		public BluetoothSocketListener(BluetoothSocket socket, Handler handler,
				TextView textView) {
			this.socket = socket;
			this.textView = textView;
			this.handler = handler;
		}

		public void run() {
			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
			int bytesRead = 0;
			int curLength = 0;
			StringBuilder sBuilder = new StringBuilder();
			sBuilder.setLength(0);
			String message = "";
			String mPost = "";
			try {
				InputStream inputStream = socket.getInputStream();
				while (true) {
					bytesRead = inputStream.read(buffer, curLength,
							buffer.length - curLength);
					if (bytesRead > 0) {
						curLength += bytesRead;
					}

					if (curLength > 0) {
						message = new String(buffer, 0, bytesRead);
						if (message.contains("$")) {
							sBuilder.append(message);
							mPost = sBuilder.toString();
							handler.post(new MessagePoster(textView, (mPost)
									.substring(0, mPost.length() - 1)));
							sBuilder.setLength(0);
						} else {
							sBuilder.append(message);
						}
						curLength = bytesRead = 0;
					}

				}
			} catch (IOException e) {
				Log.d("BLUETOOTH_COMMS", e.getMessage());
				Toast.makeText(getApplicationContext(),
						"Error reading from socket", Toast.LENGTH_LONG).show();
			}
		}
	}

	private boolean isValidMessageChar(String message) {
		return message.matches("[a-zA-Z_0-9]|[ .,?<>]+");
	}

}

*/