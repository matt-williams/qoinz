package com.github.matt.williams.qoinz.android;

import java.util.Arrays;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class QoinzHostApduService extends HostApduService {
	private static final String TAG = "QoinzHostApduService";
	private volatile boolean mGotQoinz;
	private volatile boolean mIsWantPay;

	public static QoinzHostApduService sInstance;
	
	@Override
	public void onCreate() {
		super.onCreate();
    	Log.e(TAG, "onCreate()");
    	sInstance = this;
	}

	@Override
	public void onDestroy() {
    	Log.e(TAG, "onDestroy()");
    	sInstance = null;
		super.onDestroy();
	}
	
	@Override
	public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
    	Log.e(TAG, "processCommandApdu(" + Arrays.toString(apdu) + ")");
    	if (apdu[1] == -92) {
        	return new byte[] { (byte) 0x90, 0x00 };    		
    	} else if (apdu[1] == 1) {
    		if ((QoinState.isAutoPay(this)) && (QoinState.getCount(this) > 0)) {
    			QoinState.setCount(this, QoinState.getCount(this) - 1);
            	QoinzManagerService service = QoinzManagerService.sInstance;
        		if (service != null) {
        			service.notifyPaid();
        		}
        		Toast.makeText(this, "Auto-paid 1 Qoin", Toast.LENGTH_SHORT).show();
            	return new byte[] { (byte) 0x90, 0x01 };
    		} else {
            	mIsWantPay = true;
            	QoinzManagerService service = QoinzManagerService.sInstance;
        		if (service != null) {
        			service.notifyWantPayChange();
        		}
            	return new byte[] { (byte) 0x90, 0x00 };
    		}        	
    	} else if (apdu[1] == 2) {
    		if (mGotQoinz) {
    			QoinState.setCount(this, QoinState.getCount(this) - 1);
    			mGotQoinz = false;
            	mIsWantPay = false;
            	QoinzManagerService service = QoinzManagerService.sInstance;
        		if (service != null) {
        			service.notifyPaid();
        			service.notifyWantPayChange();
        		}
            	return new byte[] { (byte) 0x90, 0x01 };
    		} else {
            	return new byte[] { (byte) 0x90, 0x00 };    			
    		}
    	} else {
        	return new byte[] { (byte) 0x90, 0x00 };    		
    	}
	}

	@Override
	public void onDeactivated(int reason) {
    	Log.e(TAG, "onDeactivated(" + reason + ")");
    	mIsWantPay = false;
    	QoinzManagerService service = QoinzManagerService.sInstance;
		if (service != null) {
			service.notifyWantPayChange();
		}
    }

	public void pay() {
		mGotQoinz = true;
	}

	public boolean isWantPay() {
		return mIsWantPay;
	}
}
