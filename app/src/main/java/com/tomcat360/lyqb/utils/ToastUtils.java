package com.tomcat360.lyqb.utils;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tomcat360.lyqb.R;
import com.tomcat360.lyqb.view.APP;


public class ToastUtils {
	private static Toast toast = null;
	private static ToastParams params;

	public static void toast(String content) {
		ToastParams params = getParams().reset();
		params.content = content;
		toast(params);
	}
	public static void toast(int textResId) {
		toast(APP.getInstance().getString(textResId));
	}
	public static void toast(int textResId, int iconResId) {
		toast(APP.getInstance().getString(textResId), iconResId, Toast.LENGTH_SHORT);
	}
	public static void toast(String content, int iconResId, int duration) {
		ToastParams params = getParams().reset();
		params.iconResId = iconResId;
		params.content = content;
		toast(params);
	}
	
	
	private static final int errorIconResId = 0;
	private static final int successIconResId = 0;
	private static final int defaultIconResId = 0;
	
	public static void toastError(String content) {
		toast(content, errorIconResId, Toast.LENGTH_SHORT);
	}
	public static void toastError(int textResId) {
		toast(APP.getInstance().getString(textResId), errorIconResId, Toast.LENGTH_SHORT);
	}

	public static void toastSuccess(String content) {
		toast(content, successIconResId, Toast.LENGTH_SHORT);
	}
	public static void toastSuccess(int resId) {
		toast(APP.getInstance().getString(resId), successIconResId, Toast.LENGTH_SHORT);
	}

	@SuppressLint("InflateParams")
	public static void toast(ToastParams params) {
		View layout = LayoutInflater.from(APP.getInstance()).inflate(R.layout.layout_my_toast,null);
		ImageView imageView = (ImageView) layout.findViewById(R.id.icon_imageview);
		TextView textView = (TextView) layout.findViewById(R.id.message_textview);
		textView.setText(params.content);
		if (params.iconResId > 0) {
			imageView.setImageResource(params.iconResId);
			imageView.setVisibility(View.VISIBLE);
		}
		else {
			imageView.setVisibility(View.GONE);
		}
		makeToast(params, layout).show();
	}
	
	private static ToastParams getParams() {
		if (params == null) {
			synchronized (ToastParams.class) {
				if (params == null) {
					params = new ToastParams();
				}
			}
		}
		return params;
	}
	
	private static Toast makeToast(ToastParams params, View view) {
		if (toast == null) {
			synchronized (Toast.class) {
				if (toast == null) {
					toast = new Toast(APP.getInstance());
				}
			}
		}
		toast.setGravity(params.gravity, params.xOffset, params.yOffset);
		toast.setDuration(params.duration);
		toast.setView(view);
		return toast;
	}
	
	public static class ToastParams {
		public String content;
		public int gravity;
		public int xOffset;
		public int yOffset;
		public int duration;
		public int iconResId;
		
		public ToastParams() {
			reset();
		}
		public ToastParams reset() {
//			//居中显示
//			gravity = Gravity.CENTER;
//			xOffset = 0;
//			yOffset = 0;
			
			//底部显示
			gravity = Gravity.BOTTOM|Gravity.CENTER;
			xOffset = 0;
			yOffset = 110;
			
			content = null;
			duration = Toast.LENGTH_SHORT;
			iconResId = defaultIconResId;
			return this;
		}
	}
}
