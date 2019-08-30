package com.megarobo.control.utils;

import android.os.Debug;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * 日志：v d i w e对应系统五种级别
 * verbose debug info warn error
 * 
 * @author huangli
 */
public class Logger {

    public static boolean DEBUG = true;

    private static final int MAX_LOG_COUNT = 3;

    private static final String LOG_PREFIX = "log_";

    private static boolean mInitLogPath = false;
    private static String mLogPath = null;

    public static void setDebug(boolean debug) {
        DEBUG = debug;
    }

    public static void v(String tag, String msg) {
        if (DEBUG) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, msg);
        }
        outPutLog(msg);
    }

    public static void outPutLog(String msg) {
        try {

            clearLogs(false, FileUtil.getFiles(MyCrashHandler
                    .getInstance().getDumpPath(), LOG_PREFIX));

            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmm",Locale.CHINA);
            String s = df.format(new Date());

            File f = new File(getDumpPath());
            if (f != null && !f.exists()) {
                f.mkdir();
            }

            if (msg == null){
                return;
            }
            f = new File(getDumpPath() + LOG_PREFIX + s
                    + ".txt");
            if (f != null) {
                FileWriter fw = new FileWriter(f,true);
                if (fw != null) {

                    msg += "\n";
                    fw.write(msg);
                    fw.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getDumpPath() {
        if (!mInitLogPath) {
            mLogPath = FileUtil.addSlash(FileUtil.getSdcardTmpDir()) + "dump/";
            mInitLogPath = true;
        }
        return mLogPath;
    }

    public static void clearLogs(boolean all, File[] fs) {
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

}
