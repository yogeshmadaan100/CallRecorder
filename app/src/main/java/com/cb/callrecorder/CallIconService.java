package com.cb.callrecorder;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.cb.callrecorder.R;


public class CallIconService extends Service {


	private static WindowManager windowManager;
	private static ImageView chatHead;
	public static Context mContext;
	public static CallIconService mService;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override 
	public void onCreate() {
		super.onCreate();
		mContext=this;
		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Log.e("fly bitch", "created");
		chatHead = new ImageView(this);
		mService=this;
		chatHead.setImageResource(R.drawable.stop);
		
		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.x = 0;
		params.y = 100;

		windowManager.addView(chatHead, params);

		try {
			chatHead.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					try {
						if(!CallRecorderServiceOptional.recording)
						{
							CallRecorderServiceOptional.startRecording(CallRecorderServiceOptional.calltype);
							chatHead.setImageResource(R.drawable.start);
						}
						else
						{
							CallRecorderServiceOptional.stopRecording();
							chatHead.setImageResource(R.drawable.stop);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							CallIconService.mContext);
			 
						// set title
						alertDialogBuilder.setTitle("Record Call");
			 
						// set dialog message
						alertDialogBuilder
							.setMessage("Do you Want to Record Call")
							.setCancelable(false)
							.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {
									// if this button is clicked, close
									// current activity
									try {
										CallRecorderServiceOptional.startRecording(CallRecorderServiceOptional.calltype);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							  })
							.setNegativeButton("No",new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int id) {
									// if this button is clicked, just close
									// the dialog box and do nothing
									dialog.cancel();
								}
							});
			 
							// create alert dialog
							AlertDialog alertDialog = alertDialogBuilder.create();
			 
							// show it
							//alertDialog.show();
				}
			});
			chatHead.setOnTouchListener(new View.OnTouchListener() {
				private WindowManager.LayoutParams paramsF = params;
				private int initialX;
				private int initialY;
				private float initialTouchX;
				private float initialTouchY;

				@Override 
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:

						// Get current time in nano seconds.

						initialX = paramsF.x;
						initialY = paramsF.y;
						initialTouchX = event.getRawX();
						initialTouchY = event.getRawY();
						break;
					case MotionEvent.ACTION_UP:
						break;
					case MotionEvent.ACTION_MOVE:
						paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
						paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
						windowManager.updateViewLayout(chatHead, paramsF);
						break;
					}
					return false;
				}
			});
		} catch (Exception e) {
			// TODO: handle exception
		}

	}
	
	void writeState(int state) {
	    Editor editor = getSharedPreferences("serviceStart", MODE_MULTI_PROCESS)
	            .edit();
	    editor.clear();
	    editor.putInt("normalStart", state);
	    editor.commit();
	}

	int getState() {
	    return getApplicationContext().getSharedPreferences("serviceStart",
	            MODE_MULTI_PROCESS).getInt("normalStart", 1);
	}
public static CallIconService getService()
{
	return mService;
}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    if (getState() == 0) {
	        writeState(1);
	        //stopSelf();
	    } else {
	        writeState(0);
	        //Toast.makeText(this, "onStartCommand", Toast.LENGTH_LONG).show();
	    }
	    return START_NOT_STICKY;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e("service", "destroyed");
		try{
			if (chatHead != null) windowManager.removeView(chatHead);
		}catch(Exception e)
		{
			
		}
	}
public static void remove()
{
	try{
		if (chatHead != null) windowManager.removeView(chatHead);
	}catch(Exception e)
	{
		
	}
}
}
