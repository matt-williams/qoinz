package com.github.matt.williams.qoinz.android;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class QoinCounter {
	private static final String PREFS_NAME = "QoinCounter";
	private static final String KEY_NUM_QOINZ = "numQoinz";
	private static final String KEY_ID = "id";
	private static final String TAG = "QoinCounter";

	public static String getId(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		String id = prefs.getString(KEY_ID, null);
		if (id == null) {
			id = UUID.randomUUID().toString();
			Editor editor = prefs.edit();
			editor.putString(KEY_ID, id);
			editor.commit();
		}
		Log.e(TAG, "id is " + id);
		return id;
	}
	
	public static int getCount(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		return prefs.getInt(KEY_NUM_QOINZ, 0);
	}
	
	public static void setCount(Context context, int count) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		Editor editor = prefs.edit();
		editor.putInt(KEY_NUM_QOINZ, count);
		editor.commit();
	}
}
