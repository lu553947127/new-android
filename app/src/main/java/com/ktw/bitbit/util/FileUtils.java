package com.ktw.bitbit.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.text.DecimalFormat;

public class FileUtils {

    public final static String dirPath = "liaocheng";
    public final static String imagePath = dirPath + "/Images";
    public final static String apkPath = dirPath + "/apk";
    public final static String adPath = dirPath + "/ad";

    /**
     * 是否有sd卡
     *
     * @return
     */
    public static boolean hasSDCard() {
        String str = Environment.getExternalStorageState();
        return str.equals(Environment.MEDIA_MOUNTED);
    }

    public static String getRootPath() {
        if (hasSDCard()) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        } else {
            return Environment.getDataDirectory().getAbsolutePath() + "/data/";
        }
    }

    public final static String getImagePath() {
        File appImageDir = new File(getRootPath(), imagePath);
        if (!appImageDir.exists()) {
            appImageDir.mkdirs();
        }
        return appImageDir.getAbsolutePath();
    }

    public final static String getApkPath() {
        File apkDir = new File(getRootPath(), apkPath);
        if (!apkDir.exists()) {
            apkDir.mkdirs();
        }
        return apkDir.getAbsolutePath();
    }

    public final static String getAdPath() {
        File adDir = new File(getRootPath(), adPath);
        if (!adDir.exists()) {
            adDir.mkdirs();
        }
        return adDir.getAbsolutePath();
    }

    /**
     * 创建一个图片文件的全路径
     *
     * @return
     */
    public static String getImageFilePath() {
        return getImagePath() + "/" + System.currentTimeMillis() + ".jpg";
    }


    /**
     * 根据文件路径获取Uri
     *
     * @param filePath
     * @return
     */
    public static Uri getImageStreamFromExternal(String filePath) {
        File picPath = new File(filePath);
        Uri uri = null;
        if (picPath.exists()) {
            uri = Uri.fromFile(picPath);
        }
        return uri;
    }

    /**
     * 获取uri获取图片文件路径
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getRealFilePath(Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public static String FormetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }


}
