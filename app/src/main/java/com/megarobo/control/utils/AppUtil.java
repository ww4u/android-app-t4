package com.megarobo.control.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.animation.Animation;

import com.megarobo.control.MegaApplication;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;


public class AppUtil {

    static final int FLAG_STOPPED = 1 << 21;
    private static final String SCHEME = "package";
    /**
     * 调用系统InstalledAppDetails界面�?��的Extra名称(用于Android 2.1及之前版�?
     */
    private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
    /**
     * 调用系统InstalledAppDetails界面�?��的Extra名称(用于Android 2.2)
     */
    private static final String APP_PKG_NAME_22 = "pkg";
    private static final String ACTION_APPLICATION_DETAILS_SETTINGS_23 = "android.settings.APPLICATION_DETAILS_SETTINGS";
    /**
     * InstalledAppDetails�?��包名
     */
    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
    /**
     * InstalledAppDetails类名
     */
    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

    /**
     * 调用系统InstalledAppDetails界面显示已安装应用程序的详细信息�?对于Android 2.3（Api Level
     * 9）以上，使用SDK提供的接口； 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）�?
     *
     * @param packageName 应用程序的包�?
     */
    public static Intent getPackageDetailsIntent(String packageName) {
        Intent intent = new Intent();
        int apiLevel = 0;
        try {
            apiLevel = Build.VERSION.SDK_INT;
        } catch (Exception ex) {
        }
        if (apiLevel >= 9) { // 2.3（ApiLevel 9）以上，使用SDK提供的接�?
            intent.setAction(ACTION_APPLICATION_DETAILS_SETTINGS_23);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
        } else { // 2.3以下，使用非公开的接口（查看InstalledAppDetails源码�?
            // 2.2�?.1中，InstalledAppDetails使用的APP_PKG_NAME不同�?
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
                    : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME,
                    APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        return intent;
    }

    public static boolean isSystemApp(String pkgName) {
        Context ctx = MegaApplication.getInstance();
        PackageManager pm = ctx.getPackageManager();

        try {
            ApplicationInfo app;
            app = pm.getApplicationInfo(pkgName, 0);
            return (app.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
        } catch (Exception e) {
        }

        return false;
    }

    public static boolean isAppInstalled(String pkg) {
        boolean ret = true;
        if (TextUtils.isEmpty(pkg)) {
            ret = false;
        }
        try {
            MegaApplication
                    .getInstance().getPackageManager().getPackageGids(pkg);
        } catch (Exception e) {
            ret = false;
        }
        return ret;
    }

    public static String getPkgPath(String pkg) {
        String ret = null;
        try {
            ret = MegaApplication.getInstance().
                    getPackageManager().
                    getApplicationInfo(pkg, 0).
                    sourceDir;
        } catch (NameNotFoundException e) {
        }
        return ret;
    }

    /**
     * @param pkgName
     * @return 通过包名获取应用�?
     */
    public static String getLabelByPkgName(String pkgName) {
        Context ctx = MegaApplication.getInstance();
        String lable = "";
        PackageManager pm = ctx.getPackageManager();
        try {
            int num = pkgName.indexOf(":");
            if (num != -1) {
                pkgName = pkgName.substring(0, num);
            }
            ApplicationInfo app;
            app = pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
            lable = pm.getApplicationLabel(app).toString();
            if (TextUtils.isEmpty(lable))
                lable = pkgName;
            return lable;
        } catch (Exception e) {
            lable = pkgName;
        }
        return lable;
    }

//    public static void showNoNetDialog(final Context context) {
//        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(context);
////        builder.setTitle(context.getString(R.string.tip));
//        builder.setMessage(context.getString(R.string.no_net_tip));
//        builder.setPositiveButton(
//                context.getString(R.string.set),
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        startNetworkSetting(context);
//                    }
//                }).setNegativeButton(R.string.cancel, null);
//        builder.build().show();
//    }

    public static void startNetworkSetting(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

//    public static void showNoPermissionDialog(final Context context, String permissionName) {
//        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(context);
//        builder.setMessage(context.getString(R.string.no_permission_tip, permissionName));
//        builder.setPositiveButton(
//                context.getString(R.string.goto_settings),
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        startAppsSettings(context);
//                    }
//                }).setNegativeButton(R.string.cancel, null);
//        builder.build().show();
//    }

    public static void startSystemLocationSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startAppsSettings(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            String packageName = "com.ucredit.haihuan";
            intent.setData(Uri.parse("package:" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception ignored) {
            }
        }
    }

    public static boolean validateMicAvailability(){
        Boolean available = true;
        AudioRecord recorder =
                new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_DEFAULT, 44100);
        try{
            if(recorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED ){
                available = false;
            }

            recorder.startRecording();
            if(recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING){
                recorder.stop();
                available = false;
            }
            recorder.stop();
        } finally{
            recorder.release();
            recorder = null;
        }

        return available;
    }

    public static boolean isMusicPlayedInBackground(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return am.isMusicActive();
    }

    public static boolean requestAudioFocus(Context context, AudioManager.OnAudioFocusChangeListener listener) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int result = am.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    public static void abandonAudioFocus(Context context, AudioManager.OnAudioFocusChangeListener listener) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        am.abandonAudioFocus(listener);
    }

    public static boolean isGPAvailable(Context ctx) {
        if (!isAppInstalled("com.android.vending")) {
            return false;
        }

        //判断GP服务�?
        PackageInfo gsfInfo = getPackageInfo(ctx, "com.google.android.gsf");
        if (null == gsfInfo || ((gsfInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1)) {
            return false;
        }

        PackageInfo gsfLoginInfo = getPackageInfo(ctx, "com.google.android.gsf.login");
        return !(null == gsfLoginInfo || ((gsfLoginInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1));

    }

    public static boolean isGPlusAvailable(Context ctx) {
        return isAppInstalled("com.google.android.apps.plus");
    }

    public static boolean isFBAvailable(Context ctx) {
        return isAppInstalled("com.facebook.katana");
    }

    public static PackageInfo getPackageInfo(Context c, String packageName) {
        if (null == c || TextUtils.isEmpty(packageName))
            return null;

        PackageInfo info = null;
        try {
            info = c.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_GIDS);
        } catch (/*NameNotFoundException*/Exception e) {
            // 抛出找不到的异常，说明该程序已经被卸�?
            return null;
        }
        return info;
    }

    public static void installPkg(String path) {
        File file = new File(path);
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        final String type = "application/vnd.android.package-archive";
        intent.setDataAndType(Uri.fromFile(file), type);
        MegaApplication.getInstance().startActivity(intent);
    }

    public static boolean isPkgStopped(Context c, String pkgName) {
        if (12 <= Build.VERSION.SDK_INT) {
            try {
                ApplicationInfo ai = c.getApplicationContext()
                        .getPackageManager()
                        .getApplicationInfo(pkgName,
                                0);

                return (ai.flags & FLAG_STOPPED) != 0;
            } catch (NameNotFoundException e) {
            }
        }

        return false;
    }

    // 关闭activity切换时的过渡动画，只能在startActivity()和finish()之后调用
    public static void cancelActivityTransition(Context context) {
        if (Build.VERSION.SDK_INT >= 5) {
            Method overridePendingTransitionMethod = null;
            try {
                overridePendingTransitionMethod = context.getClass().getMethod("overridePendingTransition",
                        int.class, int.class);
                if (null == overridePendingTransitionMethod) {
                    return;
                }

                overridePendingTransitionMethod.invoke(context, Integer.valueOf(Animation.INFINITE),
                        Integer.valueOf(Animation.INFINITE));
            } catch (Exception e) {

            }
        }
    }

    public static List<ResolveInfo> queryActivitys(PackageManager pm,
                                                   Intent intent) {
        List<ResolveInfo> infoList = null;
        if (pm != null && intent != null) {
            infoList = pm.queryIntentActivities(intent, 0);
        }
        return infoList;
    }

    public static int getCurrentVersionCode() {
        PackageManager manager = MegaApplication.getInstance().getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(MegaApplication.getInstance().getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return info == null ? 0 : info.versionCode;
    }

    public static String getCurrentVersionName() {
        PackageManager manager = MegaApplication.getInstance().getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(MegaApplication.getInstance().getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return info == null ? null : info.versionName;
    }



    /**
     * 检查相机权限是否被禁
     * @return true if Camera permission is granted
     */
    public static boolean checkCameraPermission() {
        Camera c = null;
        try {
            // attempt to get a Camera instance
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }

        boolean granted = c != null;

        // release
        if (c != null) {
            try {
                c.release(); // release the camera for other applications
                c = null;
            } catch (Exception e) {

            }
        }

        return granted;
    }


}
