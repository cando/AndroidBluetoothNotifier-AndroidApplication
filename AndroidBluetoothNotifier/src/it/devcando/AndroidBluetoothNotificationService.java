package it.devcando;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class AndroidBluetoothNotificationService extends NotificationListenerService {

	private String TAG = this.getClass().getSimpleName();
	private InnerHandler mHandler;
	
	//http://stackoverflow.com/questions/11278875/handlers-and-memory-leaks-in-android
	//TODO understand why
	static class InnerHandler extends Handler{
        WeakReference<AndroidBluetoothNotificationService> mSrv;

        InnerHandler(AndroidBluetoothNotificationService aSrv) {
            mSrv = new WeakReference<AndroidBluetoothNotificationService>(aSrv);
        }

        @Override
        public void handleMessage(Message msg){
			StatusBarNotification stn = (StatusBarNotification)msg.obj;
			if (stn == null) {
				return;
			}
			Notification not = stn.getNotification();
			if (not == null) {
				return;
			}
			Log.d(mSrv.get().TAG,stn.getPackageName() );
			
			//skip android system notification
			if(stn.getPackageName().equals("android"))
				return;

			String message =  Utils.extractTextFromNotification(mSrv.get(), not);
			Drawable icon = null;
			try {
				icon = mSrv.get().getPackageManager().getApplicationIcon(stn.getPackageName());
			} catch (NameNotFoundException e) {
				Log.w(mSrv.get().TAG, "Can't find " + stn.getPackageName() + " icon.");
			}

			Intent i = new  Intent("com.devcando.NOTIFICATION_LISTENER");
			i.putExtra("notification_text", message);
			
			if(icon != null)
			{
				Log.d(mSrv.get().TAG, "Creating bitmap icon");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				Bitmap bitmap = Utils.drawableToBitmap(icon);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); 
				byte[] b = baos.toByteArray();
				i.putExtra("icon", b);
			}
			mSrv.get().sendBroadcast(i);
		}       
    }

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service started");
		mHandler = new InnerHandler(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Service stopped");
	}

	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		Log.i(TAG,"**********  onNotificationPosted");
		Message msg = new Message();
        msg.obj = sbn;
        msg.arg1 = 0;
        mHandler.sendMessage(msg);
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		return;
		//        Log.i(TAG,"********** onNOtificationRemoved");
		//        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());
		//        Intent i = new  Intent("com.devcando.NOTIFICATION_LISTENER");
		//        i.putExtra("notification_event","onNotificationRemoved :" + sbn.getPackageName() + "\n");
		//
		//        sendBroadcast(i);
	}

}
