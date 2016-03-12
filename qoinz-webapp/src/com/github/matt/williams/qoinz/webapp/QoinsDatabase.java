package com.github.matt.williams.qoinz.webapp;

import java.util.HashMap;
import java.util.Map;

public class QoinsDatabase {
	private static QoinsDatabase sInstance;
	private Map<String, Integer> mDatabase = new HashMap<String, Integer>();
	
	public synchronized static QoinsDatabase getInstance() {
		if (sInstance == null) {
			sInstance = new QoinsDatabase();
		}
		return sInstance;
	}

	public synchronized void add(String id, int deltaQoinz) {
		int numQoinz = 0;
		if (mDatabase.get(id) != null) {
			numQoinz = mDatabase.get(id);
		}
		numQoinz += deltaQoinz;
		mDatabase.put(id, numQoinz);
	}

	public synchronized boolean decrement(String id) {
		int numQoinz = 0;
		if (mDatabase.get(id) != null) {
			numQoinz = mDatabase.get(id);
		}
		if (numQoinz > 0) {
			numQoinz--;
			mDatabase.put(id, numQoinz);
			return true;
		} else {
			return false;
		}
	}	
}
