package com.cb.callrecorder;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallRecorderServiceAll extends Service{

	static MediaRecorder recorder;
	boolean recording,ringing,answered,outgoing;
	
	//Broadcast receiver for calls
	CallBroadcastReceiver cbr;
	
	//Phone number
	String phoneNumber;
	
	//Database class
	HelperCallRecordings hcr;
	
	//To check service running or not
	static boolean running=false; 
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.e("permanent service", "created");
		try
		{
			//To avoid running of service again and again
			//Toast.makeText(getApplicationContext(), "service started", 2000).show();
			if(running==false)
			{
				Intent i=new Intent(CallRecorderServiceAll.this,CallRecorderServiceAll.class);
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
			
			cbr=new CallBroadcastReceiver();
			IntentFilter ifl=new IntentFilter();
			ifl.addAction("android.intent.action.PHONE_STATE");
			ifl.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
			registerReceiver(cbr, ifl);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		try
		{
			running=false;
			
			unregisterReceiver(cbr);
			
			hcr.closeDatabase();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public class CallBroadcastReceiver extends BroadcastReceiver{
		
		public CallBroadcastReceiver()
		{
		}
		
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			try
			{
				answered=checkAnswered(arg1);

				Log.e("answer", ""+String.valueOf(answered));
				if(answered==true)
				{
					Log.e("answer", ""+String.valueOf(answered));
					startRecording();
					ringing=false;
					outgoing=false;
				}
			}catch(Exception e)
			{
				Log.e("exp", "exp " +e);
				e.printStackTrace();
			}
		}
		
		//Controls recording
		public void startRecording() throws Exception
		{
			recorder=new MediaRecorder();
			
			File sampleDir = Environment.getExternalStorageDirectory();
            File sample = new File(sampleDir.getAbsolutePath()+"/Call Recorder");
            sample.mkdirs();
            
            String fileName=String.valueOf(System.currentTimeMillis());
            
            File audiofile = new File(sample.getAbsolutePath()+"/sound"+fileName+".amr");

            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
            //recorder.setAudioEncoder(MediaRecorder.getAudioSourceMax());
            recorder.setAudioEncodingBitRate(16);
            recorder.setAudioSamplingRate(44100);
            recorder.setOutputFile(audiofile.getAbsolutePath());
            recorder.prepare();
            
         //  AudioManager audioManager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
          //get the current volume set
          //int deviceCallVol = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
          //set volume to maximum
            //      audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
            Log.e("recorder", ""+String.valueOf(recording));
          
            if(recording==false)
            	{
            		Log.e("recording", "started");
            		recorder.start();
            	}
            else
            	Log.e("not ", "recording");
            
            recording=true;
            
            if(phoneNumber!=null)
            {	
            	if(ringing == true)
            		hcr.insert(phoneNumber, fileName, audiofile.getAbsolutePath(),"incoming");
            	else if(outgoing==true)
            		hcr.insert(phoneNumber, fileName, audiofile.getAbsolutePath(),"outgoing");
            }

		}
		
		public boolean checkAnswered(Intent i) throws Exception
		{
			Log.e("call intent", "testing");
			if(i.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL))
			{
				phoneNumber=i.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
				
				outgoing=true;
				return false;
			}
			else
			{
				Bundle b=i.getExtras();
				String state=b.getString(TelephonyManager.EXTRA_STATE);
				
				Log.d("aa", state);
				
				if(state.equals(TelephonyManager.EXTRA_STATE_RINGING))
				{
					//Check to see if call was answered later
					ringing=true;
					
					phoneNumber=b.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
					
					return false;
				}
				else if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))
				{
					//call to be recorded if it was ringing or new outgoing
					if(ringing==true || outgoing==true)
					{
						//ringing=false;
						//outgoing=false;
						return true;
					}
					else
						return false;
				}
				else if(state.equals(TelephonyManager.EXTRA_STATE_IDLE))
				{
					ringing=false;
					
					//Stop recording if it was on
					if(recording==true)
					{	
						SharedPreferences sp=getSharedPreferences("com.rockinentser.callrecorder", Context.MODE_PRIVATE);
						String notificationMode=sp.getString("notificationmode", "false");
						
						//Generate notificaton only if shared preferences is true for notification
						if(notificationMode.equals("true"))
							generateNotification();
						recorder.stop();
						recorder.release();
						recording=false;
						
						answered=false;
						outgoing=false;
						
						phoneNumber=null;
					}
					
					return false;
				}
				else
					return false;
			}
				
		}
	}
	
	@SuppressWarnings("deprecation")
	public void generateNotification() throws Exception
	{
		NotificationManager nm=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
	    
		Notification nf;
		
		final int sdkVersion =Build.VERSION.SDK_INT;

		Intent i=new Intent(CallRecorderServiceAll.this,ContactsActivity.class);
		PendingIntent pi=PendingIntent.getActivity(this, 1, i, Intent.FILL_IN_DATA);
		
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
		}
	}
}
