package it.devcando;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {

	private final String TAG = this.getClass().getSimpleName();
	// Well known SPP UUID
	private static final UUID SPP_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	// Insert your server's MAC address
	private static String address = "08:3E:8E:9F:DE:74";

	BluetoothSocket btSocket = null;
	BluetoothDevice btDevice = null;
	OutputStream btOutputStream = null;
	InputStream btInputStream = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF){
			Log.d(TAG,"Stopping Notification Listener service");
			//FIXME doesn't work!
			//Only system can start/stop a notification listener service! WTF!
			//context.stopService(new Intent(context, AndroidBluetoothNotificationService.class));
		}
		if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON){
			Log.d(TAG,"Starting Notification Listener  service");
			//FIXME doesn't work!
			//Only system can start/stop a notification listener service! WTF!
			//context.startService(new Intent(context, AndroidBluetoothNotificationService.class));
		}
		if(action.equals("android.bluetooth.device.action.ACL_CONNECTED")) {
			//FIXME doesn't work with PC!! I never get this event
			Toast t = Toast.makeText(context, "Device Bluetooth Connected", Toast.LENGTH_SHORT);
			t.show();
			Log.d(TAG,"Device Bluetooth Connected");
			//TODO
		}
		if(action.equals("android.bluetooth.device.action.ACL_DISCONNECTED") ||action.equals("android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED")){
			//FIXME doesn't work with PC!! I never get this event
			Toast t = Toast.makeText(context, "Device Bluetooth Disconnected", Toast.LENGTH_SHORT);
			t.show();
			Log.d(TAG,"Device Bluetooth Disconnected");
			//TODO
		}
		if(action.equals("com.devcando.NOTIFICATION_LISTENER")) {
			String text = intent.getStringExtra("notification_text");
			Log.d(TAG, "Received  notification.");
			byte[] icon = intent.getByteArrayExtra("icon");
			SendMessage(text, icon);
		}

	}

	private void closeConnection() {
		if (btOutputStream != null) {
			try {
				btOutputStream.close();
			} catch (Exception e) {
			}
			btOutputStream = null;
		}

		if (btSocket != null) {
			try {
				btSocket.close();
			} catch (Exception e) {
			}
			btSocket = null;
		}
	}

	private void SendMessage(String message, byte[] icon) {
		if (message.length() == 0)
			return;
		
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		btDevice = btAdapter.getRemoteDevice(address);
		// Two things are needed to make a connection:
		// A MAC address, which we got above.
		// A Service ID or UUID. In this case we are using the
		// UUID for SPP.
		try {
			btSocket = btDevice.createRfcommSocketToServiceRecord(SPP_UUID);
		} catch (IOException e) {
			Log.e("Fatal Error", "SendMessage: socket create failed. \n"
					+ e.getMessage() + ".");
			return;
		}

		// Discovery is resource intensive. Make sure it isn't going on
		// when you attempt to connect and pass your message.
		btAdapter.cancelDiscovery();
		// Establish the connection. This will block until it connects.
		try {
			btSocket.connect();
		} catch (IOException e) {
			Log.e("Fatal Error",
					"SendMessage: unable to connect to socket. \n"
							+ e.getMessage() + ".");
			return;
		}

		try {
			btOutputStream = btSocket.getOutputStream();
			btInputStream = btSocket.getInputStream();
		} catch (IOException e) {
			Log.e(
					"Fatal Error",
					"SendMessage: output stream creation failed. \n"
							+ e.getMessage() + ".");
			return;
		}

		message += "\r\ncom.devcando.stop\r\n"; //Finish message, send stop command.
		byte[] msgBuffer = message.getBytes();
		try {
			//Write notification text
			btOutputStream.write(msgBuffer);
			//This read is just for synchronization...
			btInputStream.read();
			//Write icon
			if(icon != null)
				btOutputStream.write(icon);
		} catch (IOException e) {
			String msg = "SendMessage: and an exception occurred during write: "
					+ e.getMessage();
			Log.e("Fatal Error", msg);
			return;
		}

		closeConnection();
	}
}