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

public class MainActivity extends Activity implements HPPManagerListener {

	private static final String TAG = "MainActivity";
	private HPPManager mHppManager;
	private Fragment mHppManagerFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mHppManager = new HPPManager();
		Resources res = getResources();
		mHppManager.setHppRequestProducerURL(res.getString(R.string.hpp_request_producer_url));
		mHppManager.setHppURL(res.getString(R.string.hpp_url));
		mHppManager.setHppResponseConsumerURL(res.getString(R.string.hpp_response_consumer_url));
		
		mHppManagerFragment = mHppManager.newInstance();
		getFragmentManager()
		         .beginTransaction()       
		         .add(R.id.fragment_container, mHppManagerFragment)    
		         .commit();		
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
	public void hppManagerCompletedWithResult(Object t) {
		Log.e(TAG, "Completed with result: " + t);
		getFragmentManager().beginTransaction().remove(mHppManagerFragment).commit(); 
	}

	@Override
	public void hppManagerFailedWithError(HPPError error) {
		Log.e(TAG, "Failed with error: " + error);
		getFragmentManager().beginTransaction().remove(mHppManagerFragment).commit(); 
	}

	@Override
	public void hppManagerCancelled() {
		Log.e(TAG, "Cancelled");
		getFragmentManager().beginTransaction().remove(mHppManagerFragment).commit(); 
	}
}
