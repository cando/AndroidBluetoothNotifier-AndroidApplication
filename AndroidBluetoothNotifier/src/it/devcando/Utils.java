package it.devcando;

import java.util.ArrayList;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

//Code from https://github.com/grimpy/Botifier/blob/master/src/com/github/grimpy/botifier/Botification.java
public class Utils {

	private static String TAG = Utils.class.getSimpleName();
	
	private static final int TIMESTAMPID = 16908388;

	public static String extractTextFromNotification(Service service, Notification notification) {
		ArrayList<String> result = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			result =  extractTextFromNotification(service, notification.bigContentView);
		}
		if (result == null) {
			result = extractTextFromNotification(service, notification.contentView);
		}
		if (result == null){
			return "";
		}
		return TextUtils.join("\n", result);

	}

	public static ArrayList<String> extractTextFromNotification(Service service, RemoteViews view) {
		LayoutInflater inflater = (LayoutInflater) service.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ArrayList<String> result = new ArrayList<String>();
		if (view == null) {
			Log.d(TAG, "View is empty");
			return null;
		}
		try {
			int layoutid = view.getLayoutId();
			ViewGroup localView = (ViewGroup) inflater.inflate(layoutid, null);
			view.reapply(service.getApplicationContext(), localView);
			ArrayList<View> outViews = new ArrayList<View>();
			extractViewType(outViews, TextView.class, localView);
			for (View  ttv: outViews) {
				TextView tv = (TextView) ttv;
				String txt = tv.getText().toString();
				if (!TextUtils.isEmpty(txt) && tv.getId() != TIMESTAMPID) {
					result.add(txt);
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "FAILED to load notification " + e.toString());
			Log.wtf(TAG, e);
			return null;
			//notification might have dissapeared by now
		}
		Log.d(TAG, "Return result" + result);
		return result;
	}
	
	private static void extractViewType(ArrayList<View> outViews, Class<TextView> viewtype, View source) {
		if (ViewGroup.class.isInstance(source)) {
			ViewGroup vg = (ViewGroup) source;
			for (int i = 0; i < vg.getChildCount(); i++) {
				extractViewType(outViews, viewtype, vg.getChildAt(i));

			}
		} else if(viewtype.isInstance(source)) {
			outViews.add(source);
		}
	}
	
	public static Bitmap drawableToBitmap (Drawable drawable) {
	    if (drawable instanceof BitmapDrawable) {
	    	Bitmap bmp = ((BitmapDrawable)drawable).getBitmap();
	    	Bitmap result = Bitmap.createScaledBitmap(bmp, 64, 64, true );
	        return result;
	    }

	    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap); 
	    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	    drawable.draw(canvas);
	    
	    Bitmap result = Bitmap.createScaledBitmap( bitmap, 64, 64, true );
	    return result;
	}
}
