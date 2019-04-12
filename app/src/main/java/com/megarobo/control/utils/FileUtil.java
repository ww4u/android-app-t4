package com.megarobo.control.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;

import com.megarobo.control.MegaApplication;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

public class FileUtil {

    public static final String EXTERNAL_DIR = "MegaRobo";
    public static final String INTERNAL_DIR = "Temp";

    public static final String TMP_FILE_SUFFIX = ".tmp";
    public static final String UNZIP_FILE_SUFFIX = "_"; // 防止解压时，dst ,src相同

    public static final String ROOT_PATH = Environment
            .getExternalStorageDirectory()
            + File.separator
            + "Mega"
            + File.separator;

    public static final String PATH_UPDATE = ROOT_PATH + "APK" + File.separator;
    public static final String FILE_UPDATE_APK = PATH_UPDATE + "Mega.apk";

    public static File getUpdateDir() {
        return new File(PATH_UPDATE);
    }

    public static File getUpdateApk() {
        return new File(FILE_UPDATE_APK);
    }

    public static File getImgFile(Context context, String fileName) {
        return new File(getDiskCacheDir(context, "img"), fileName);
    }

    public static File getSplashFile(Context context) {
        return new File(getDiskCacheDir(context, "splash"), "splash.jpg");
    }

    public static File getRegionFile(Context context) {
        return new File(getDiskCacheDir(context, "region"), "region.txt");
    }

    public static String getRegionFilePath(Context context) {
        return getDiskCachePath(context, "region") + File.separator + "region.txt";
    }

    public static File getRegionZip(Context context) {
        return new File(getDiskCacheDir(context, "region"), "region.zip");
    }

    public static String getRegionZipPath(Context context) {
        return getDiskCachePath(context, "region") + File.separator + "region.zip";
    }

    public static File getRegionZipTemp(Context context) {
        return new File(getDiskCacheDir(context, "region"), "region.zip.temp");
    }

    // 获取cache路径，不存在则创建
    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            if (context.getExternalCacheDir() != null) {
                cachePath = context.getExternalCacheDir().getPath();
            } else {
                cachePath = context.getCacheDir().getPath();
            }
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        File dir = new File(cachePath + File.separator + uniqueName);
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static String getDiskCachePath(Context context, String uniqueName) {
        return getDiskCacheDir(context, uniqueName).getPath();
    }

    public static boolean replaceFile(String srcPath, String dstPath) {
        File dstFile = new File(dstPath);
        if (dstFile.exists()) {
            int retry = 3;
            do {
                if (dstFile.delete()) {
                    return (new File(srcPath)).renameTo(dstFile);
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            } while (--retry > 0);
            return false;
        }

        return (new File(srcPath)).renameTo(dstFile);
    }

    public static void copyFile(String srcPath, String dstPath) {
        if (TextUtils.isEmpty(srcPath) || TextUtils.isEmpty(dstPath)) {
            return;
        }
        File srcFile = new File(srcPath);
        File dstFile = new File(dstPath);
        if (srcFile.exists() == false) {
            return;
        }
        try {
            FileInputStream fis = new FileInputStream(srcFile);
            FileOutputStream fos = new FileOutputStream(dstFile);
            byte[] bytes = new byte[512];
            do {
                int size = fis.read(bytes);
                if (size >= 0) {
                    fos.write(bytes, 0, size);
                } else {
                    break;
                }
            } while (true);
            fis.close();
            fos.close();
        } catch (Exception e) {
        }

    }

    // 添加斜杠
    public static String addSlash(String path) {
        if (TextUtils.isEmpty(path)) {
            return File.separator;
        }

        if (path.charAt(path.length() - 1) != File.separatorChar) {
            return path + File.separatorChar;
        }

        return path;
    }

    // 获取sdcard上临时目�? 能创建就创建，不能创建返回null
    public static String getSdcardTmpDir() {
        File f = new File(addSlash(Environment.getExternalStorageDirectory().getAbsolutePath()) + EXTERNAL_DIR);
        if (!f.exists() || f.isFile()) {
            f.delete();
            f.mkdir();
        }

        if (f.exists() && f.isDirectory()) {
            return f.getAbsolutePath();
        } else {
            return null;
        }
    }

    public static String getSdcardDcimDir() {
        File f = new File(addSlash(Environment.getExternalStorageDirectory().getAbsolutePath()) + "DCIM/Camera");
        if (!f.exists() || f.isFile()) {
            f.delete();
            f.mkdir();
        }

        if (f.exists() && f.isDirectory()) {
            return f.getAbsolutePath();
        } else {
            return null;
        }
    }

    public static String getInternalTmpDir() {
        File f = new File(addSlash(MegaApplication.getInstance().getDir(INTERNAL_DIR, 0).getAbsolutePath()));
        if (!f.exists() || f.isFile()) {
            f.delete();
            f.mkdir();
        }

        if (f.exists() && f.isDirectory()) {
            return f.getAbsolutePath();
        } else {
            return null;
        }
    }

    public static boolean exportAssetFile(Context context, String relativeFileName, String targetFile) {
        InputStream is = null;
        FileOutputStream os = null;

        boolean bOk = false;

        try {
            is = context.getAssets().open(relativeFileName);
            os = new FileOutputStream(targetFile);

            if (is.available() == 0) {
                os.close();
                return false;
            }

            byte[] buffer = new byte[1024 * 100];
            int currentReaded = 0;
            while (true) {
                currentReaded = is.read(buffer, 0, buffer.length);
                if (-1 == currentReaded) {
                    break;
                }

                os.write(buffer, 0, currentReaded);
            }

            is.close();
            os.flush();
            os.close();

            bOk = true;
        } catch (IOException e) {
        }

        return bOk;
    }

    /**
     * Perform an fsync on the given FileOutputStream.  The stream at this
     * point must be flushed but not yet closed.
     */
    public static boolean sync(FileOutputStream stream) {
        try {
            if (stream != null) {
                stream.getFD().sync();
            }
            return true;
        } catch (IOException e) {
        }
        return false;
    }

//    public static void chmodFile(String path, String mod) {
//        try {
//            int mode = 0;
//
//            for (int i = 0; i < mod.length(); i++) {
//                byte c = (byte) (mod.charAt(i) - '0');
//                mode = (mode << 3) | c;
//            }
//
//            FileUtils.setPermissions(path, mode, -1, -1);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 得到某个路径下，以某个字符串�?��的所有文�?
     *
     * @param filePath
     * @param filePrefix 为null则返回所有文�?
     * @return
     */
    public static File[] getFiles(String filePath, final String filePrefix) {
        if (!TextUtils.isEmpty(filePath)) {

            File dir = new File(filePath);
            if (dir != null) {
                if (dir.exists()) {
                    if (TextUtils.isEmpty(filePrefix)) {
                        return dir.listFiles();
                    } else {
                        String fnames[] = dir.list(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String filename) {
                                return filename.startsWith(filePrefix);
                            }
                        });

                        if (fnames != null && fnames.length != 0) {
                            Arrays.sort(fnames, new Comparator<String>() {
                                @Override
                                public int compare(String str1, String str2) {
                                    return str2.compareTo(str1);
                                }
                            });

                            File[] fs = new File[fnames.length];
                            for (int i = 0; i < fnames.length; i++) {
                                fs[i] = new File(addSlash(filePath) + fnames[i]);
                            }

                            return fs;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Runtime.getRuntime().exec("rm -rf "+file.getAbsolutePath());//到时候改成这个试�?
     * delete file or directory
     * <ul>
     * <li>if path is null or empty, return true</li>
     * <li>if path not exist, return true</li>
     * <li>if path exist, delete recursion. return true</li>
     * <ul>
     *
     * @param path
     * @return
     */
    public synchronized static boolean deleteFileWithFolder(String path) {
        try {
            if (TextUtils.isEmpty(path)) {
                return true;
            }

            File file = new File(path);
            if (!file.exists()) {
                return true;
            }
            if (file.isFile()) {
                return file.delete();
            }
            if (!file.isDirectory()) {
                return false;
            }

            File[] listFiles = file.listFiles();// 删除目录
            if (listFiles != null) {
                for (File f : listFiles) {
                    if (f != null && f.isFile()) {
                        f.delete();
                    } else if (f != null && f.isDirectory()) {
                        deleteFileWithFolder(f.getAbsolutePath());
                    }
                }
            }
            return file.delete();// 这里删除的是dir
        } catch (Exception e) {
            return false;
        }
    }

    public static void copyMp3() {
        boolean success = false;
        int retry = 0;

        while (!success) {
            if (retry >= 3) {
                break;
            }
            retry++;

            String path;
//            if(SdcardUtil.isValidExternalStorage()){
//                File file = HaoHuanApplication.getInstance().getExternalCacheDir();
//                if(file != null){
//                    path = file.getAbsolutePath();
//                    path = FileUtil.addSlash(path);
//                    path = FileUtil.addSlash(path + MP3_DIR);
//                }else{
//                    path = FileUtil.addSlash(Environment.getExternalStorageDirectory().getPath())
//                            + FileUtil.EXTERNAL_DIR;
//                    file = new File(path);
//                    if(!file.exists()){
//                        file.mkdirs();
//                    }
//                    path = FileUtil.addSlash(path);
//                    path = FileUtil.addSlash(path + MP3_DIR);
//                }
//            }else
//            {
//                File file = HaoHuanApplication.getInstance().getFilesDir();
//                path = file.getAbsolutePath();
//                path = FileUtil.addSlash(path);
//                path = FileUtil.addSlash(path + MP3_DIR);
//            }
//            if (path != null) {
//                File file = new File(path);
//                if (!file.exists()) {
//                    file.mkdirs();
//                }
//                file = new File(path + new String(new byte[]{108,105,98,107,120,104,121,46,115,111}));

//                if (ResourceUtil.copyFileFromRaw(R.raw.voice1, file, HaoHuanApplication.getInstance(), true)){
//                    success = true;
//                    sMp3Path = file.getAbsolutePath();
//                    chmodFile(sMp3Path,"755");
//                }
//            }

        }
    }

    public static boolean saveBitmapToFile(Bitmap bitmap, File file) {
        if (file == null) {
            return false;
        } else if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(file));
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)) {
                    bos.flush();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
