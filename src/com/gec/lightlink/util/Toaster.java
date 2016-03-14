package com.gec.lightlink.util;

import android.content.Context;
import android.widget.Toast;

/**
 * @author sig
 * @version 1.0
 */
public class Toaster {

	public static void longToastShow(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
	}

	public static void longToastShow(Context context, int resId) {
		Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
	}

	public static void shortToastShow(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	public static void shortToastShow(Context context, int resId) {
		Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
	}
}
