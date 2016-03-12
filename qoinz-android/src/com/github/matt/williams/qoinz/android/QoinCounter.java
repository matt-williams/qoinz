package com.github.matt.williams.qoinz.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class QoinCounter {
	private static final String PREFS_NAME = "QoinCounter";
	private static final String KEY_NUM_QOINZ = "numQoinz";

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
