package com.github.matt.williams.qoinz.android;

import com.estimote.sdk.SystemRequirementsChecker;
import com.github.matt.williams.qoinz.android.QoinzManagerService.LocalBinder;

import android.app.Activity;
import android.os.Handler;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class UseQoinzActivity extends Activity {

	private static final String TAG = "UseQoinzActivity";
	private ServiceConnection mServiceConnection;
    private LocalBinder mService;
    private Handler mHandler;
    private QoinzManagerService.Listener mListener = new QoinzManagerService.Listener() {
		@Override
		public void wantPayChange() {
			mHandler.post(new Runnable() {
				public void run() {
					findViewById(R.id.payButton).setEnabled(mService.isWantPay() && (QoinState.getCount(UseQoinzActivity.this) > 0));					
				}
			});
		}

		@Override
		public void paid() {
			mHandler.post(new Runnable() {
				public void run() {
					((TextView)findViewById(R.id.label)).setText("x" + QoinState.getCount(UseQoinzActivity.this));
				}
			});
		}
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SystemRequirementsChecker.checkWithDefaultDialogs(this);
		setContentView(R.layout.activity_use_qoinz);
		
		if (QoinState.getCount(this) == 0) {
			buyMoreQoinz();
		}
		
		mHandler = new Handler();
		
        Intent intent = new Intent(this, QoinzManagerService.class);
        mServiceConnection = new ServiceConnection() {

			@Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e(TAG, "onServiceConnected");
                mService = (QoinzManagerService.LocalBinder)service;
                mService.addListener(mListener);
    			mHandler.post(new Runnable() {
    				public void run() {
    					findViewById(R.id.payButton).setEnabled(mService.isWantPay() && (QoinState.getCount(UseQoinzActivity.this) > 0));					
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
        
        ((ToggleButton)findViewById(R.id.toggleButton)).setChecked(QoinState.isAutoPay(this));
	}

	@Override
	protected void onResume() {
		super.onResume();
		((TextView)findViewById(R.id.label)).setText("x" + QoinState.getCount(this));		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    protected void onDestroy() {
        if (mServiceConnection != null) {
        	if (mService != null) {
        		mService.removeListener(mListener);
        	}
            unbindService(mServiceConnection);
            mServiceConnection = null;
            mService = null;
        }
        super.onDestroy();
    }
	
	public void toggleAutoPay(View v) {
		QoinState.setAutoPay(this, ((ToggleButton)v).isChecked());
	}
	
	public void pay(View v) {
		if (mService != null) {
			mService.pay();
		} else {
			findViewById(R.id.payButton).setEnabled(false);
		}
	}
	
	public void buyMoreQoinz() {
		startActivity(new Intent(this, BuyQoinzActivity.class));
	}

	public void buyMoreQoinz(View v) {
		buyMoreQoinz();
	}
	
	public void sellQoinz(View v) {
		startActivity(new Intent(this, SellQoinzActivity.class));
	}	
}
