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
import android.widget.Toast;

public class BuyQoinzActivity extends Activity implements HPPManagerListener {

	private static final String TAG = "BuyQoinzActivity";
	private static final String KEY_NUM_QOINZ = "numQoinz";
	private static final String KEY_QOINZ_ID = "qoinzId";
	private HPPManager mHppManager;
	private Fragment mHppManagerFragment = null;
	private int mNumQoinz;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_buy_qoinz);

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
	
	public void buy1Qoin(View v) {
		buyQoinz(1, "1");
	}

	public void buy3Qoinz(View v) {
		buyQoinz(3, "2.5");
	}

	public void buy5Qoinz(View v) {
		buyQoinz(5, "4");
	}

	public void buy10Qoinz(View v) {
		buyQoinz(10, "7.5");
	}

	public void buy25Qoinz(View v) {
		buyQoinz(25, "18");
	}

	private void buyQoinz(int numQoinz, String cost) {
		if (mHppManagerFragment == null) {
			mHppManager.setAmount(cost);
			mHppManager.setCurrency("GBP");
			mHppManager.setAutoSettleFlag("true");
			mHppManager.setCardStorageEnable("true");
			mHppManager.setOfferSaveCard("true");
			mHppManager.setPayerExists("true");
			mHppManager.setCardPaymentButtonText("Buy " + numQoinz + " for " + "£" + cost);;
			mNumQoinz = numQoinz;
			mHppManager.setSupplementaryData(KEY_NUM_QOINZ, Integer.toString(numQoinz));
			mHppManager.setSupplementaryData(KEY_QOINZ_ID, QoinCounter.getId(this));
			mHppManagerFragment = mHppManager.newInstance();
			getFragmentManager()
			.beginTransaction()       
			.add(R.id.fragment_container, mHppManagerFragment)    
			.commit();
		}
	}

	@Override
	public void hppManagerCompletedWithResult(Object t) {
		Log.e(TAG, "Completed with result: " + t.getClass().getCanonicalName());
		if (mHppManagerFragment != null) {
			getFragmentManager().beginTransaction().remove(mHppManagerFragment).commitAllowingStateLoss();
			mHppManagerFragment = null;
		}
		if (t.toString().equals("{result=00}")) {
			QoinCounter.setCount(this, QoinCounter.getCount(this) + mNumQoinz);
			mNumQoinz = 0;
			finish();
		} else {
			Toast.makeText(this, "Failed with error: " + t, Toast.LENGTH_SHORT).show();			
		}
	}

	@Override
	public void hppManagerFailedWithError(HPPError error) {
		Log.e(TAG, "Failed with error: " + error);
		if (mHppManagerFragment != null) {
			getFragmentManager().beginTransaction().remove(mHppManagerFragment).commitAllowingStateLoss();
			mHppManagerFragment = null;
		}
		Toast.makeText(this, "Failed with error: " + error, Toast.LENGTH_SHORT).show();
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
