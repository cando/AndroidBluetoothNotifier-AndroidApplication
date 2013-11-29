package it.devcando;

import com.devcando.R;

import android.app.Activity;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		CheckBTState();

		Button b = (Button) findViewById(R.id.button1);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Notify();
			}
		});
	}

	protected void Notify() {
		NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		NotificationCompat.Builder ncomp = new NotificationCompat.Builder(this);
		ncomp.setContentTitle("My Notification");
		ncomp.setContentText("Notification Listener Service Example");
		ncomp.setTicker("Notification Listener Service Example");
		ncomp.setSmallIcon(R.drawable.ic_launcher);
		ncomp.setAutoCancel(true);
		nManager.notify((int) System.currentTimeMillis(), ncomp.build());
	}

	private void CheckBTState() {
		// Check for Bluetooth support and then check to make sure it is turned on
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			Log.e("Fatal Error", "Bluetooth Not supported. Aborting.");
		} else {
			if (!btAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, 1);
			}
		}
	}
}