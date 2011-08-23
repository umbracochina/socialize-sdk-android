/*
 * Copyright (c) 2011 Socialize Inc. 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.socialize.util;

import java.util.Locale;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;

import com.socialize.Socialize;
import com.socialize.log.SocializeLogger;

/**
 * @author Jason Polites
 */
public class DeviceUtils {
	
	private SocializeLogger logger;
	private String userAgent;
	private float density = 160.0f;
	
	public void init(Context context) {
		if(context instanceof Activity) {
	        DisplayMetrics metrics = new DisplayMetrics();
	        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
	        display.getMetrics(metrics);
	        density = metrics.density;
		}
		else {
			String errroMsg = "Unable to determine device screen density.  Socialize must be intialized from an Activity";
			if(logger != null) {
				logger.warn(errroMsg);
			}
			else {
				System.err.println(errroMsg);
			}
		}
	}
	
	public int getDIP(int pixels) {
		if(pixels != 0) {
			return (int) ((float) pixels * density);
		}
		return pixels;
	}
	
	public String getUDID(Context context) {
		if(hasPermission(context, permission.READ_PHONE_STATE)) {
			TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			
			String deviceId = tManager.getDeviceId();
			
			if(StringUtils.isEmpty(deviceId)) {
				if(logger != null) {
					logger.warn("Unable to determine device UDID, reverting to " + Secure.ANDROID_ID);
				}
				deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
			}
			
			return deviceId;
		}
		else {
			// this is fatal
			if(logger != null) {
				logger.error(SocializeLogger.NO_UDID);
			}
			
			return null;
		}
	}
	
	public String getUserAgentString() {
		if(userAgent == null) {
			userAgent = "Android-" + android.os.Build.VERSION.SDK_INT + "/" + android.os.Build.MODEL + " SocializeSDK/v" + Socialize.VERSION + "; " + Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry();
		}
		return userAgent;
	}
	
	public boolean hasPermission(Context context, String permission) {
		return context.getPackageManager().checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
	}

	public SocializeLogger getLogger() {
		return logger;
	}

	public void setLogger(SocializeLogger logger) {
		this.logger = logger;
	}
}
