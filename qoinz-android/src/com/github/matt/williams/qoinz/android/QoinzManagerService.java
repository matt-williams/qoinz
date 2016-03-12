package com.github.matt.williams.qoinz.android;

import java.util.ArrayList;
import java.util.List;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class QoinzManagerService extends Service {
	private static final String TAG = "QoinzManagerService";
	public final Binder mBinder = new LocalBinder();
	public List<Listener> mListeners = new ArrayList<Listener>();
	public static QoinzManagerService sInstance;
	
    public class LocalBinder extends Binder {
        void addListener(Listener listener) {
        	mListeners.add(listener);
        }
        
        void removeListener(Listener listener) {
        	mListeners.remove(listener);
        }
        
        void pay() {
        	QoinzHostApduService service = QoinzHostApduService.sInstance;
        	if (service != null) {
        		service.pay();
        	}
        }
        
        boolean isWantPay() {
        	QoinzHostApduService service = QoinzHostApduService.sInstance;
        	if (service != null) {
        		return service.isWantPay();
        	}
        	return false;
        }
    }

    public interface Listener {
    	public void wantPayChange();
    	public void paid();
    }
    
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
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	void notifyWantPayChange() {
		for (Listener listener : mListeners) {
			listener.wantPayChange();
		}
	}
	
	void notifyPaid() {
		for (Listener listener : mListeners) {
			listener.paid();
		}
	}	
}
