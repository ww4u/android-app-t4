package com.megarobo.control.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * 
* @Description:用于获取AndroidManifest中相关参数信息，如版本信息，metaData
*
 */
public class ManifestUtil {
	
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public static String getPackageName(Context context) {
		PackageInfo pkg = null;
        try {
            pkg = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      
     
        } catch (NameNotFoundException e) {

        } 
        return (pkg == null) ? null : pkg.applicationInfo.loadLabel(context.getPackageManager()).toString(); 
	}
	/**
	 * 获取 AndroidManifest.xml中metaData中的CHANNEL_ID信息
	 * 
	 * @param context
	 * @return
	 */
	public static int getChannelID(Context context) {
		ApplicationInfo info;

		int channelId = 0;
		try {
			info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			channelId = info.metaData.getInt("CHANNEL_ID");
		} catch (Exception e) {
		}
		return channelId;
	}

	/**
	 * 获取 AndroidManifest.xml中metaData中的CHANNEL_NAME信息
	 * 
	 * @param context
	 * @return
	 */
	public static String getChannelName(Context context) {
		ApplicationInfo info;

		String channelName = "xx";
		try {
			info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			channelName = info.metaData.getString("CHANNEL_NAME");
		} catch (Exception e) {
		}
		return channelName;
	}
	
	/**
	 * 获取当前版本名称
	 * @param context
	 * @return
	 */
	public static String getVersionName(Context context) {
		PackageInfo pkg = null;
		try {
			pkg = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
		}
		return (pkg == null) ? null : pkg.versionName;
	}

	/**
	 * @param context
	 * @return
	 */
	public static int getVersionCode(Context context) {
		PackageInfo pkg = null;
		try {
			pkg = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
		}
		return (pkg == null) ? 0 : pkg.versionCode;
	}
}
