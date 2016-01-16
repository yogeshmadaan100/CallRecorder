package com.cb.callrecorder;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallRecorderServiceOptional extends Service{
	
	//Static to be accessed from the confimation page
	static MediaRecorder recorder;
	static boolean recording;
	//Phone state
	static String state;
	
	//Phone number
	static String phoneNumber;
	
	boolean incomingConfirmationLaunched;
	boolean outgoing;
	public static String calltype;
	//BroadCast receiver for calls
	CallBroadcastReceiver cbr;
	//Database Class
	static HelperCallRecordings hcr;
	
	//Check service running
	static boolean running=false;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.e("optional service", "created");
		try
		{
			if(running==false)
			{
				Intent i=new Intent(CallRecorderServiceOptional.this,CallRecorderServiceOptional.class);
				startService(i);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		try
		{
			hcr=new HelperCallRecordings(this);
			
			incomingConfirmationLaunched=false;
			outgoing=false;
			
			cbr=new CallBroadcastReceiver();
			IntentFilter ifl=new IntentFilter();
			ifl.addAction("android.intent.action.PHONE_STATE");
			ifl.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
			registerReceiver(cbr, ifl);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return START_NOT_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e("optional service", "destroyed");
		try
		{
			unregisterReceiver(cbr);
			hcr.closeDatabase();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//Controls recording
	public static void startRecording(String calltype) throws Exception
	{
		//calltype is received from confirmation pages 
		
		recorder=new MediaRecorder();
		
		File sampleDir = Environment.getExternalStorageDirectory();
        File sample = new File(sampleDir.getAbsolutePath()+"/Call Recorder");
        sample.mkdirs();
        
        String fileName=String.valueOf(System.currentTimeMillis());
        
        File audiofile = new File(sample.getAbsolutePath()+"/sound"+fileName+".amr");

        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(audiofile.getAbsolutePath());
        recorder.prepare();
        
        if(recording==false)
        {
        	recorder.start();
        	Log.e("recording", "started");
        }
        
        //Inserting in database
        if(phoneNumber!=null)
        	hcr.insert(phoneNumber, fileName, audiofile.getAbsolutePath(),calltype);
        
        recording=true;

	}
	
	public class CallBroadcastReceiver extends BroadcastReceiver{
		
		public CallBroadcastReceiver()
		{
			//calltype="incoming";
		}
		
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			try {
				manageRecording(arg1);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		public void manageRecording(Intent i) throws Exception
		{
			Log.e("managing", "call");
			//Checks Outgoing call
			if(i.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL))
			{
				Log.e("call type", "outgoing");
				phoneNumber=i.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
				calltype="outgoing";
				outgoing=true;
				Intent i1 = new Intent(CallApplication.mApplication,CallIconService.class);
				//startService(i1);
			}
			else
			{
				//Log.e("call type", "incoming");
				Bundle b=i.getExtras();
				state=b.getString(TelephonyManager.EXTRA_STATE);
				
				Log.d("aa", state);
				
				if(state.equals(TelephonyManager.EXTRA_STATE_RINGING))
				{
					//calltype="incoming";
					Intent i1 = new Intent(CallApplication.mApplication,CallIconService.class);
					startService(i1);
					
				}
				else if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))
				{
					//calltype="incoming";
					if(!calltype.equalsIgnoreCase("outgoing"))
						calltype="incoming";
					Intent i1 = new Intent(CallApplication.mApplication,CallIconService.class);
					startService(i1);
				}
				else if(state.equals(TelephonyManager.EXTRA_STATE_IDLE))
				{
					calltype="idle";
					if(recording==true)
					{
					recorder.stop();
					//recorder.release();
					recording=false;
					}
					CallIconService.remove();
					try{
						
						Intent i1 = new Intent(CallApplication.mApplication,CallIconService.class);
						
						stopService(i1);
					}
					catch(Exception e)
					{
						Log.e("stop", "exception"+e);
					}
						
				}
			}
				
		}
	}
	public static void stopRecording()
	{
		calltype="idle";
		if(recording==true)
		{
		recorder.stop();
		//recorder.release();
		recording=false;
		}
		
	}
	@SuppressWarnings("deprecation")
	public void generateNotification() throws Exception
	{
		NotificationManager nm=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
	    
		Notification nf;
		
		final int sdkVersion =Build.VERSION.SDK_INT;

		/*Intent i=new Intent(CallRecorderServiceOptional.this,RecordedCallsPage.class);
		PendingIntent pi=PendingIntent.getActivity(this, 1, i, 2);
		
		if (sdkVersion < Build.VERSION_CODES.HONEYCOMB)
		{
			nf=new Notification(android.R.drawable.alert_dark_frame,"Call Recorder",System.currentTimeMillis());
			nf.setLatestEventInfo(this, "Call Recorder", "call recorded", pi);
			
			nm.notify(3, nf);
		}
		else
		{
			NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
			nf = builder.setContentIntent(pi)
                    .setSmallIcon(android.R.drawable.alert_dark_frame).setTicker("Call Recorder").setWhen(System.currentTimeMillis())
                    .setAutoCancel(true).setContentTitle("Call Recorder")
                    .setContentText("call recorded").build();
			
			nm.notify(3,nf);
		}*/
	}
}
