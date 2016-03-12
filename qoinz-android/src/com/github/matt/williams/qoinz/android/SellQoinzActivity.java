package com.github.matt.williams.qoinz.android;

import com.realexpayments.hpp.HPPError;
import com.realexpayments.hpp.HPPManager;
import com.realexpayments.hpp.HPPManagerListener;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SellQoinzActivity extends Activity implements HPPManagerListener {

	private static final String TAG = "SellQoinzActivity";
	private HPPManager mHppManager;
	private Fragment mHppManagerFragment = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sell_qoinz);
		
		int numQoinz = QoinCounter.getCount(this);
		((TextView)findViewById(R.id.label)).setText("x" + numQoinz);
		((TextView)findViewById(R.id.sellButton)).setText("Sell for £" + (float)(numQoinz * 0.5));

		mHppManager = new HPPManager();
		Resources res = getResources();
		mHppManager.setHppRequestProducerURL(res.getString(R.string.hpp_request_producer_url));
		mHppManager.setHppURL(res.getString(R.string.hpp_url));
		mHppManager.setHppResponseConsumerURL(res.getString(R.string.hpp_response_consumer_url));
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
	
	public void sell(View v) {
		// TODO: Rebate user
		//if (mHppManagerFragment == null) {
		//	mHppManager.setAmount(cost);
		//	mHppManager.setCurrency("GBP");
		//	mHppManagerFragment = mHppManager.newInstance();
		//	getFragmentManager()
		//	.beginTransaction()       
		//	.add(R.id.fragment_container, mHppManagerFragment)    
		//	.commit();
		//}
		QoinCounter.setCount(this, 0);
		finish();
	}
	
	@Override
	public void hppManagerCompletedWithResult(Object t) {
		Log.e(TAG, "Completed with result: " + t);
		if (mHppManagerFragment != null) {
			getFragmentManager().beginTransaction().remove(mHppManagerFragment).commitAllowingStateLoss();
			mHppManagerFragment = null;
		}
	}

	@Override
	public void hppManagerFailedWithError(HPPError error) {
		Log.e(TAG, "Failed with error: " + error);
		if (mHppManagerFragment != null) {
			getFragmentManager().beginTransaction().remove(mHppManagerFragment).commitAllowingStateLoss();
			mHppManagerFragment = null;
		}
	}

	@Override
	public void hppManagerCancelled() {
		Log.e(TAG, "Cancelled");
		if (mHppManagerFragment != null) {
			//getFragmentManager().beginTransaction().remove(mHppManagerFragment).commitAllowingStateLoss();
			mHppManagerFragment = null;
		}
	}
}
