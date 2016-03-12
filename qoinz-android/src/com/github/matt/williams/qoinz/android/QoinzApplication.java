package com.github.matt.williams.qoinz.android;

import java.util.List;
import java.util.UUID;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.github.matt.williams.qoinz.android.QoinzManagerService.LocalBinder;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class QoinzApplication extends Application {
    protected static final String TAG = "QoinzApplication";
	private static final int ID_NOTIFICATION = 1;
	private NotificationManager mNotificationManager;
	private BeaconManager mBeaconManager;
	private ServiceConnection mServiceConnection;
	private LocalBinder mService;
    private Handler mHandler;
	private volatile boolean mIsInRegion = false;
	private volatile boolean mWasInRegion = false;	

    private QoinzManagerService.Listener mListener = new QoinzManagerService.Listener() {
		@Override
		public void wantPayChange() {
            Log.e(TAG, "Listener.wantPayChange()");
			mHandler.post(new Runnable() {
				public void run() {
					maybeNotify();
				}
			});
		}

		@Override
		public void paid() {}
    };
    
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void maybeNotify() {
		if ((mService != null) &&
			(mService.isWantPay())) {
			Notification notification = new Notification.Builder(this)
			.setContentTitle("Qoinz Payment")
			.setContentText("Tap to Pay!")
			.setSmallIcon(R.drawable.ic_notification)
			.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, UseQoinzActivity.class), 0))
			.setOngoing(true)
			.build();
			mNotificationManager.notify(ID_NOTIFICATION, notification);
		} else if (mIsInRegion) {
			Notification notification = new Notification.Builder(this)
			.setContentTitle("Qoinz Payment")
			.setContentText("Buy Qoinz")
			.setSmallIcon(R.drawable.ic_notification)
			.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, BuyQoinzActivity.class), 0))
			.build();
			mNotificationManager.notify(ID_NOTIFICATION, notification);
		} else if (mWasInRegion) {
			Notification notification = new Notification.Builder(this)
			.setContentTitle("Qoinz Payment")
			.setContentText("Sell Qoinz")
			.setSmallIcon(R.drawable.ic_notification)
			.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, SellQoinzActivity.class), 0))
			.build();
			mNotificationManager.notify(ID_NOTIFICATION, notification);
		} else {
			mNotificationManager.cancel(ID_NOTIFICATION);
		}
	}
	
    @Override
    public void onCreate() {
        super.onCreate();
        
		mHandler = new Handler();
    	mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		
        Intent intent = new Intent(this, QoinzManagerService.class);
        mServiceConnection = new ServiceConnection() {
			@Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e(TAG, "onServiceConnected");
                mService = (QoinzManagerService.LocalBinder)service;
                mService.addListener(mListener);
    			mHandler.post(new Runnable() {
    				public void run() {
    					maybeNotify();
    				}
    			});                
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e(TAG, "onServiceDisconnected");
                mService = null;
            }
        };
        Log.e(TAG, "issuing bindService");
        startService(intent);
        if (!bindService(intent, mServiceConnection, 0)) {
            Log.e(TAG, "bindService failed");
            Toast.makeText(this, "Failed to bind to Qoinz service", Toast.LENGTH_SHORT).show();
        }
        
        mBeaconManager = new BeaconManager(getApplicationContext());
        mBeaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
			@Override
			public void onEnteredRegion(Region region, List<Beacon> beacons) {
	            Log.e(TAG, "onEnteredRegion(" + region + ")");
				mIsInRegion = true;
    			mHandler.post(new Runnable() {
    				public void run() {
    					maybeNotify();
    				}
    			});                
			}

			@Override
			public void onExitedRegion(Region region) {
	            Log.e(TAG, "onExitedRegion(" + region + ")");
	            mWasInRegion = mIsInRegion;
				mIsInRegion = false;
    			mHandler.post(new Runnable() {
    				public void run() {
    					maybeNotify();
    				}
    			});				
			}
        });
        mBeaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
	            Log.e(TAG, "onServiceReady()");
            	Resources res = getResources();
                mBeaconManager.startMonitoring(new Region(
                        "monitored region",
                        UUID.fromString(res.getString(R.string.estimote_beacon_uuid)),
                        res.getInteger(R.integer.estimote_beacon_major),
                        res.getInteger(R.integer.estimote_beacon_minor)));
            }
        });
    }

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
        mBeaconManager.disconnect();
        if (mServiceConnection != null) {
        	if (mService != null) {
        		mService.removeListener(mListener);
        	}
            unbindService(mServiceConnection);
            mServiceConnection = null;
            mService = null;
        }
		super.onTerminate();
	}
}