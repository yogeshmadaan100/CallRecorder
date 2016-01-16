package com.cb.callrecorder;




import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class CallApplication extends Application {
	public static CallApplication mApplication;
	 public static CallIconService mMainService;
	 public static SharedPreferences sp;//to prevent concurrent creation of shared pref and editor
		public static Editor e;
		public static int opt;
	private static GoogleAnalytics analytics;

	/**
	 * The default app tracker. The field is from onCreate callback when the application is
	 * initially created.
	 */
	private static Tracker tracker;

	/**
	 * Access to the global Analytics singleton. If this method returns null you forgot to either
	 * set android:name="&lt;this.class.name&gt;" attribute on your application element in
	 * AndroidManifest.xml or you are not setting this.analytics field in onCreate method override.
	 */
	public static GoogleAnalytics analytics() {
		return analytics;
	}

	/**
	 * The default app tracker. If this method returns null you forgot to either set
	 * android:name="&lt;this.class.name&gt;" attribute on your application element in
	 * AndroidManifest.xml or you are not setting this.tracker field in onCreate method override.
	 */
	public static Tracker tracker() {
		return tracker;
	}

	@Override
		public void onCreate() {
			// TODO Auto-generated method stub
			super.onCreate();
			Log.e("application", "created");
			mApplication =this;
			//try{
				sp=getApplicationContext().getSharedPreferences("com.example.call", Context.MODE_PRIVATE);
			
			e=sp.edit();
			/*}catch(Exception e)
			{
				Log.e("shared pref",""+e.toString());
			}*/
			try{
				

			Intent all=new Intent(this,CallRecorderServiceAll.class);
			Intent opt = new Intent(this,CallRecorderServiceOptional.class);
			if(sp.getInt("type", 0)==0)
			{
				startService(all);
			}
			else if(sp.getInt("type", 0)==1)
			{
				stopService(all);
				stopService(opt);
			}
			else if(sp.getInt("type", 0)==2)
			{
				startService(opt);
			}
			}catch(Exception e)
			{
				Log.e("application", "service");
			}

		analytics = GoogleAnalytics.getInstance(this);
		FacebookSdk.sdkInitialize(getApplicationContext());
		// TODO: Replace the tracker-id with your app one from https://www.google.com/analytics/web/
		tracker = analytics.newTracker("UA-67048716-8");

		// Provide unhandled exceptions reports. Do that first after creating the tracker
		tracker.enableExceptionReporting(true);

		// Enable Remarketing, Demographics & Interests reports
		// https://developers.google.com/analytics/devguides/collection/android/display-features
		tracker.enableAdvertisingIdCollection(true);

		// Enable automatic activity tracking for your app
		tracker.enableAutoActivityTracking(true);
		}
	public boolean isBannerVisible()
	{
		SharedPreferences sharedpreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
		return sharedpreferences.getInt("share_count",0)<5;
	}
	/*public CallApplication() {
		// TODO Auto-generated constructor stub
		
		try{
			
			Intent musicservice = new Intent(getApplicationContext(),MusicService.class);
		
		startService(musicservice);
		Intent all=new Intent(this,CallRecorderServiceAll.class);
		Intent opt = new Intent(this,CallRecorderServiceOptional.class);
		if(sp.getInt("type", 0)==0)
		{
			startService(all);
		}
		else if(sp.getInt("type", 0)==1)
		{
			stopService(all);
			stopService(opt);
		}
		else if(sp.getInt("type", 0)==2)
		{
			startService(opt);
		}
		}catch(Exception e)
		{
			Log.e("application", "service");
		}
	}*/
	public static CallApplication getInstance()
	{
		return mApplication;
	}

	 public static void getMainService()
	 {
		 mMainService=CallIconService.getService();
	 }
	 public  void resetService()
	 {
		 try{
				
			Intent all=new Intent(this,CallRecorderServiceAll.class);
			Intent opt = new Intent(this,CallRecorderServiceOptional.class);
			stopService(all);
			stopService(opt);
			if(sp.getInt("type", 0)==0)
			{
				startService(all);
			}
			else if(sp.getInt("type", 0)==1)
			{
				stopService(all);
				stopService(opt);
			}
			else if(sp.getInt("type", 0)==2)
			{
				startService(opt);
			}
			}catch(Exception e)
			{
				Log.e("application", "reset service");
			}
	 }
	 
}
