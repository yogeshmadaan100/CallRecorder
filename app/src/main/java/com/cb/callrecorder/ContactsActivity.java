package com.cb.callrecorder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cb.callrecorder.R;
import com.facebook.share.widget.LikeView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.startapp.android.publish.Ad;
import com.startapp.android.publish.AdEventListener;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;


public class ContactsActivity extends AppCompatActivity {

    private Fragment mAllCallsFragment;
    private Fragment mDialledCallsFragment;
    private Fragment mReceivedCallsFragment;

    private final String ALL_CALLS_FRAGMENT_KEY = "all_calls_fragment";
    private final String DIALLED_CALLS_FRAGMENT_KEY = "dialed_calls_fragment";
    private final String RECEIVED_CALLS_FRAGMENT_KEY = "received_calls_fragment";
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager mViewPager = (ViewPager)findViewById(R.id.viewpager);
        if (mViewPager != null) {
            initializeFragments(savedInstanceState);
            setupViewPager(mViewPager);
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(mViewPager);
        }
       // CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        //collapsingToolbarLayout.setTitle("My Title");
        //collapsingToolbarLayout.setCollapsedTitleTextColor(Color.parseColor("#ffffff"));
        toolbar.setTitle("Call Recorder");
        FloatingActionButton actionButton = (FloatingActionButton)findViewById(R.id.fabBtn);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRecording();
            }
        });

        LikeView likeView = (LikeView) findViewById(R.id.like_view);
        likeView.setObjectIdAndType(
                "https://www.facebook.com/zebrawoodlabz",
                LikeView.ObjectType.PAGE);

        if(((CallApplication)getApplication()).isBannerVisible())
            loadBanners();
        loadInterstitial();
        setUpReview();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Sharing App");
                intent.putExtra(Intent.EXTRA_TEXT, "Hello I am using Call Recorder. You can also try at https://play.google.com/store/apps/details?id="+getPackageName());
                SharedPreferences sharedpreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt("share_count", sharedpreferences.getInt("share_count", 0) + 1);
                editor.commit();
                startActivityForResult(intent, 1285);

            }
        });
        if(((CallApplication)getApplication()).isBannerVisible())
            new AlertDialog.Builder(ContactsActivity.this)
                    .setTitle("Share App")
                    .setMessage("Share the app with 5 people to enjoy banner free application.\n Like our Facebook page for more apps and free recharges. Cheers! ")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Sharing App");
                            intent.putExtra(Intent.EXTRA_TEXT, "Hello I am using Astrogyaan. You can also try at https://play.google.com/store/apps/details?id=com.cb.callrecorder");
                            SharedPreferences sharedpreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putInt("share_count", sharedpreferences.getInt("share_count", 0) + 1);
                            editor.commit();
                            startActivityForResult(intent, 1285);                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


        if (mAllCallsFragment != null) {

            /* Bug? -> https://code.google.com/p/android/issues/detail?id=77285 */
            try {
                getFragmentManager().putFragment(outState, ALL_CALLS_FRAGMENT_KEY, mAllCallsFragment);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        if (mReceivedCallsFragment != null) {
try{
    getFragmentManager().putFragment(outState, RECEIVED_CALLS_FRAGMENT_KEY, mReceivedCallsFragment);
} catch (IllegalStateException e) {
    e.printStackTrace();
}
        }

        if (mDialledCallsFragment != null) {
            try{
                getFragmentManager().putFragment(outState, DIALLED_CALLS_FRAGMENT_KEY, mDialledCallsFragment);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeFragments(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Fragment cFragment = getFragmentManager().getFragment(savedInstanceState,
                    ALL_CALLS_FRAGMENT_KEY);

            if (cFragment != null && cFragment instanceof AllCallsFragment) {
                mAllCallsFragment = (AllCallsFragment)cFragment;
            } else {
                mAllCallsFragment = AllCallsFragment.newInstance();
            }

            Fragment sFragment = getFragmentManager().getFragment(savedInstanceState,
                    DIALLED_CALLS_FRAGMENT_KEY);

            if (sFragment != null && sFragment instanceof DialledCallFragment) {
                mDialledCallsFragment = (DialledCallFragment)sFragment;
            } else {
                mDialledCallsFragment = DialledCallFragment.newInstance();
            }

            Fragment rFragment = getFragmentManager().getFragment(savedInstanceState,
                    RECEIVED_CALLS_FRAGMENT_KEY);

            if (rFragment != null && rFragment instanceof ReceivedCallFragment) {
                mReceivedCallsFragment = (ReceivedCallFragment)rFragment;
            } else {
                mReceivedCallsFragment = ReceivedCallFragment.newInstance();
            }
        } else {
            mDialledCallsFragment = DialledCallFragment.newInstance();
            mReceivedCallsFragment = ReceivedCallFragment.newInstance();
            mAllCallsFragment = AllCallsFragment.newInstance();
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ContactsPagerAdapter mAdapter = new ContactsPagerAdapter(getFragmentManager());
        mAdapter.addFragment(mAllCallsFragment, "ALL CALLS");
        mAdapter.addFragment(mDialledCallsFragment, "DIALLED");
        mAdapter.addFragment(mReceivedCallsFragment, "RECEIVED");
        viewPager.setAdapter(mAdapter);
    }
public void setRecording()
{
    CallApplication.sp=getApplicationContext().getSharedPreferences("com.example.call", Context.MODE_PRIVATE);

    CallApplication.e=CallApplication.sp.edit();
    final Dialog dialog=new Dialog(ContactsActivity.this);
    dialog.setContentView(R.layout.layout_dialog);
    dialog.setTitle("Set Your Record Preference");
    RadioGroup group=(RadioGroup)dialog.findViewById(R.id.radioGroup1);
    final RelativeLayout rl =(RelativeLayout)dialog.findViewById(R.id.ask_layout);
    final TextView tv1=(TextView)dialog.findViewById(R.id.r0);
    final TextView tv2=(TextView)dialog.findViewById(R.id.r1);
    switch (CallApplication.sp.getInt("type", 0)) {
        case 0:
            group.check(R.id.radio0);
            break;

        case 1:
            group.check(R.id.radio1);
            break;

        case 2:
            group.check(R.id.radio2);
            break;

        default:
            break;
    }

    group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // TODO Auto-generated method stub
            switch (checkedId) {
                case R.id.radio0:
                    CallApplication.e.putInt("type", 0);
                    rl.setVisibility(View.GONE);
                    tv1.setVisibility(View.VISIBLE);
                    tv2.setVisibility(View.GONE);
                    break;
                case R.id.radio1:
                    CallApplication.e.putInt("type", 1);
                    rl.setVisibility(View.GONE);
                    tv1.setVisibility(View.GONE);
                    tv2.setVisibility(View.VISIBLE);
                    break;
                case R.id.radio2:
                    CallApplication.e.putInt("type", 2);
                    rl.setVisibility(View.VISIBLE);
                    tv1.setVisibility(View.GONE);
                    tv2.setVisibility(View.GONE);
                    break;

                default:
                    break;
            }
        }
    });
    Button save =(Button)dialog.findViewById(R.id.button1);
    save.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            CallApplication.e.commit();
            CallApplication.getInstance().resetService();
            dialog.dismiss();
        }
    });
    dialog.show();
}


    public void setUpReview()
    {

        AppRate.with(this)
                .setInstallDays(0) // default 10, 0 means install day.
                .setLaunchTimes(2) // default 10
                .setRemindInterval(1) // default 1
                .setDebug(false) // default false
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {
                        Log.d(ContactsActivity.class.getName(), Integer.toString(which));
                    }
                })
                .monitor();

        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);

    }
    public void loadBanners()
    {
        AdView mAdView = (AdView) findViewById(R.id.startAppBanner);
        AdRequest adRequest1 = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest1);
    }

    public void loadInterstitial()
    {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        AdRequest adRequest = new AdRequest.Builder()
                .build();

        mInterstitialAd.loadAd(adRequest);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
        });
    }
}
