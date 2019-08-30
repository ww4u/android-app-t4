package com.megarobo.control.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Debug;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.zip.CRC32;

public class MyCrashHandler implements UncaughtExceptionHandler {

    private static final boolean DEG = false;
    private static final String TAG = "MyCrashHandler";

    private static final String CRASH_PROFIX = "crash_";

//    private static final String ANR_PROFIX = "anr_";

    private static final int MAX_LOG_COUNT = 3;
    // private static final long CRASH_FEEDBACK_INTERVAL = 1000 * 60 * 60 * 24 *
    // 1;

    private static UncaughtExceptionHandler mOldHandler;
    private static boolean mRegisted = false;
    private static MyCrashHandler mInstance;
    private static String mIMei;
    private static String mAppVersionName;
    private static String mAppVersionCode;
    private static String mAppPackage;
    private static int mAppFlags = 0;
    private static String mDumpKey = "0";
    private static boolean mInitLogPath = false;
    private static String mLogPath = null;
    private static String mChannelId = null;

    public static String getANRPath() {
        return FileUtil.addSlash(FileUtil.getSdcardTmpDir()) + "anr/";
    }

    public String getDumpPath() {
        if (!mInitLogPath) {
            mLogPath = FileUtil.addSlash(FileUtil.getSdcardTmpDir()) + "dump/";
            mInitLogPath = true;
        }
        return mLogPath;
    }

    public synchronized static MyCrashHandler getInstance() {
        if (mInstance == null) {
            mInstance = new MyCrashHandler();
        }

        return mInstance;
    }

    public void register(Context ctx) {
        if (ctx != null && !mRegisted) {
            mRegisted = true;
            mIMei = "";
            mAppFlags = 0;

            try {
                mChannelId = String.valueOf(ManifestUtil.getChannelID(ctx));

                mOldHandler = Thread.getDefaultUncaughtExceptionHandler();
                Thread.setDefaultUncaughtExceptionHandler(this);

                TelephonyManager tm = (TelephonyManager) ctx
                        .getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mIMei = tm.getDeviceId();

                ApplicationInfo ai = ctx.getApplicationInfo();
                if (ai != null) {
                    mAppFlags = ai.flags;
                }
            } catch (Exception e) {
                mIMei = "";
                mChannelId = "";
            }
            PackageInfo packageInfo = AppUtil.getPackageInfo(ctx, ctx.getPackageName());
            if (packageInfo != null) {
                mAppVersionName = packageInfo.versionName;
                mAppVersionCode = packageInfo.versionCode + "";
                mAppPackage = packageInfo.packageName;
            }
            outputCrashLog(null);
        }
    }
    
    public void clearLogs(boolean all, File[] fs) {
        if (fs != null) {
            if (all) {
                for (int i = 0; i < fs.length; i++) {
                    fs[i].delete();
                }
            } else if (fs.length > MAX_LOG_COUNT) {
                
                Arrays.sort(fs, new Comparator<File>() {

                    @Override
                    public int compare(File lhs, File rhs) {
                        
                        return lhs.getName().compareTo(rhs.getName());
                    }
                });

                int t = fs.length - MAX_LOG_COUNT;
                for (int i = 0; i < t; i++) {
                    fs[i].delete();
                }
            }
        }
    }

    private static String ingoreNonImportantMsg(String exceptioncause) {

        String fixed = exceptioncause;

       
        int x = fixed.indexOf("{");
        int y = fixed.indexOf("}");
        if (x != -1 && y != -1) {
            int end = y + 1;
            if (end > fixed.length())
                end = fixed.length();

            String tmp = fixed.substring(x, end);
            fixed = fixed.replace(tmp, " -ADDR- ");
            fixed = fixed.trim();
        }
        
        int z = fixed.indexOf("@");
        if (z != -1) {
            int end = z + 9;
            if (end > fixed.length())
                end = fixed.length();

            String tmp = fixed.substring(z, end);
            fixed = fixed.replace(tmp, " -ADDR- ");
            fixed = fixed.trim();
        }

        // ɾ������к�?
        int m = fixed.indexOf("(");
        if (m != -1) {
            int n = fixed.lastIndexOf(":");
            if (n != -1) {
                fixed = fixed.substring(0, n);
                fixed = fixed.trim();
            }
        }

        return fixed;
    }

    private static long getDumpKey(Throwable ex, StackTraceElement[] stacks) {
        if (ex == null || stacks == null)
            return 0L;

        if (stacks.length == 0)
            return 0L;

        String exceptionCause = ex.toString();
        if (DEG) {
            Logger.i(TAG, "Throwable.toString:" + exceptionCause);
            for (int i = 0; i < stacks.length; i++) {
                Logger.i(TAG, "StackTraceElement.index:" + i + ",StackTraceElement.toString:"
                        + stacks[i].toString());
            }
        }
        String useCurrLine[] = {
                "java.lang.RuntimeException",
                "java.lang.UnsatisfiedLinkError",
                "java.lang.IllegalArgumentException",
                "java.lang.AssertionError", "java.lang.SecurityException",
                "java.lang.UnsupportedOperationException",
                "android.database.sqlite.SQLiteException",
                "java.lang.NoClassDefFoundError",
                "android.content.ActivityNotFoundException",
                "android.app.RemoteServiceException",
                "java.lang.IllegalStateException",
                "java.lang.IncompatibleClassChangeError",
                "android.view.WindowManager$BadTokenException",
                "android.database.CursorWindowAllocationException",
                "android.database.sqlite.SQLiteDiskIOException",
                "android.database.sqlite.SQLiteDatabaseLockedException",
                "android.content.res.Resources$NotFoundException"
        };

        String useNextLine[] = {
                "java.lang.OutOfMemoryError",
                "java.lang.NullPointerException",
                "java.lang.IllegalArgumentException",
                "java.util.NoSuchElementException",
                "java.util.ConcurrentModificationException",
                "java.lang.IndexOutOfBoundsException",
                "java.lang.ArrayIndexOutOfBoundsException",
                "java.util.NoSuchElementException",
                "java.lang.StackOverflowError",
                "java.lang.ExceptionInInitializerError"
        };

        
        for (String xx : useCurrLine) {
            if (exceptionCause.indexOf(xx) != -1) {

                String ParsedException = ingoreNonImportantMsg(exceptionCause);
                CRC32 crc1 = new CRC32();
                crc1.update(ParsedException.getBytes());

                CRC32 crc2 = new CRC32();
                crc2.update(xx.getBytes());

                long key = (crc1.getValue() + crc2.getValue());

                return key;
            }
        }

        for (String xx : useNextLine) {
            if (exceptionCause.indexOf(xx) != -1) {
                String stack0 = stacks[0].toString();

                String ParsedException = ingoreNonImportantMsg(stack0);
                CRC32 crc1 = new CRC32();
                crc1.update(ParsedException.getBytes());

                CRC32 crc2 = new CRC32();
                crc2.update(xx.getBytes());

                long key = (crc1.getValue() + crc2.getValue());

                return key;
            }
        }

        return 0L;
    }

    private static String GetMemoryInfoString() {

        Debug.MemoryInfo meminfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(meminfo);

        return meminfo.getTotalPss() + "/" + meminfo.dalvikPss + "/"
                + meminfo.nativePss + "/" + meminfo.otherPss;
    }

    private static int GetNativeFdCnt() {
        try {
            File x = new File("/proc/self/fd");
            if (x.exists() && x.isDirectory()) {
                return x.listFiles().length;
            }
        } catch (Exception e) {
        }
        return 0;
    }

    private String getCommonsInfo() {
//    	mAppVersionName=
        int root = 0;
//        if (SuKit.getInstance().isMobileRoot()) {
//            ++root;
//        }

        String dump = "-----infromation----\n";
        dump += "package="
                + mAppPackage
                + "\nvername="//versionName
                + mAppVersionName
                + "\nvercode="//versionCode
                + mAppVersionCode
                + "\nappflags="
                + String.valueOf(mAppFlags)
                + "\ndebug="
                + String.valueOf(0 != (mAppFlags & ApplicationInfo.FLAG_DEBUGGABLE))
                + "\nimei="
                + mIMei
                + "\nboard="
                + "\nlanguage=" + Locale.getDefault().getLanguage()
                + "\nchannel=" + mChannelId + "\nmeminfo="
                + MyCrashHandler.GetMemoryInfoString() + "\nnativefd="
                + MyCrashHandler.GetNativeFdCnt() + "\nRoot="
                + Integer.toString(root);

        return dump;
    }

    private String outputCrashLog(Throwable ex) {
        String dump = "";
        try {

            clearLogs(false, FileUtil.getFiles(MyCrashHandler
                    .getInstance().getDumpPath(), CRASH_PROFIX));

            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.CHINA);
            String s = df.format(new Date());

            File f = new File(getDumpPath());
            if (f != null && !f.exists()) {
                f.mkdir();
            }

            if (ex == null){
                return null;
            }
            f = new File(getDumpPath() + CRASH_PROFIX + s
                    + ".txt");
            if (f != null) {
                FileWriter fw = new FileWriter(f);
                if (fw != null) {
                    dump = getCommonsInfo();
                    dump += "\n\n----exception localized message----\n";

                    s = ex.getLocalizedMessage();
                    if (s != null) {
                        dump += s;
                    }

                    dump += "\n\n----exception stack trace----\n";
                    fw.write(dump);

                    PrintWriter pw = new PrintWriter(fw);
                    if (pw != null) {
                        Throwable c = ex;
                        boolean checked = false;
                        while (c != null) {
                            c.printStackTrace(pw);

                            ByteArrayOutputStream bous = new ByteArrayOutputStream();
                            PrintWriter pwbyte = new PrintWriter(bous);
                            c.printStackTrace(pwbyte);
                            pwbyte.flush();
                            String stackStr = new String(bous.toByteArray());
                            dump += stackStr;
                            dump += "\n";

                            StackTraceElement[] elements = c.getStackTrace();
                            if (null != elements && !checked) {
                                mDumpKey = Long.toString(getDumpKey(ex,
                                        elements));

                                String tempDump = "-----dumpkey----";
                                tempDump += "\ndumpkey=" + mDumpKey + "\n\n";
                                dump += tempDump;
                                fw.write(tempDump);

                                checked = true;
                            }

                            c = c.getCause();
                        }

                        pw.close();
                    }

                    fw.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dump;
    }

    public boolean isDebug() {
        return 0 != (mAppFlags & ApplicationInfo.FLAG_DEBUGGABLE);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        try {
            if (ex != null) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        outputCrashLog(ex);
        if (null != mOldHandler && null != thread) {
           
            mOldHandler.uncaughtException(thread, ex);
        }
    }

}
