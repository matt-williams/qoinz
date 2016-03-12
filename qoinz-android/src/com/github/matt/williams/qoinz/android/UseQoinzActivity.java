package com.github.matt.williams.qoinz.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class UseQoinzActivity extends Activity {

	private static final String TAG = "UseQoinzActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_use_qoinz);
		
		if (QoinCounter.getCount(this) == 0) {
			buyMoreQoinz();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		((TextView)findViewById(R.id.label)).setText("x" + QoinCounter.getCount(this));		
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
	
	public void pay(View v) {
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
