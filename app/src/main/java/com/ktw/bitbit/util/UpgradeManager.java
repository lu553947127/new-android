package com.ktw.bitbit.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ktw.bitbit.BuildConfig;
import com.ktw.bitbit.R;
import com.ktw.bitbit.view.cjt2325.cameralibrary.util.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpgradeManager {

    private Context context;
    private String apkUrl;
    private String versionName;
    // 下载中
    private static final int UPDATEING = 1;
    // 下载完成
    private static final int UPDATE_FINISH = 2;
    // 下载失败
    private static final int UPDATE_FIAL = 3;

    // 下载文件的存放目录
    private static final String savePath = FileUtils.getApkPath();

    private ProgressBar progressBar;//进度条
    private TextView titleTextView;//标题
    private TextView progressView; //下载进度百分比
    private TextView fileSizeTextView;//文件总大小
    private Dialog downloadDialog;// 下载对话框
    private int progress;// 下载的数量
    private Thread downLoadThread;// 下载的Thread

    private boolean interceptFlag = false;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATEING:
                    progressView.setText(progress + "%");
                    progressBar.setProgress(progress);
                    break;
                case UPDATE_FINISH:
                    downloadDialog.dismiss();
                    ToastUtil.showToast(context, "下载成功");
                    installAPK();
                    break;
                case UPDATE_FIAL:
                    downloadDialog.dismiss();
                    ToastUtil.showToast(context, "下载失败");
                    break;
                default:
                    break;
            }
        }
    };

    public UpgradeManager(Activity context, String apkUrl, String versionName) {
        this.context = context;
        this.apkUrl = apkUrl;
        this.versionName = versionName;
    }

    private void showDowloadProgress() {
        downloadDialog = new Dialog(context, R.style.VersionDialog);
        View view = LayoutInflater.from(context).inflate(
                R.layout.download_progress, null);
        progressBar = view.findViewById(R.id.loading_progress);
        titleTextView = view.findViewById(R.id.dialog_title);
        progressView = view.findViewById(R.id.progress);
        fileSizeTextView = view.findViewById(R.id.tv_file_size);

        titleTextView.setText(String.format("更新应用版本：%s", versionName));

        Window dialogWindow = downloadDialog.getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        downloadDialog.setCanceledOnTouchOutside(false);
        downloadDialog.setContentView(view);
        downloadDialog.setCancelable(false);
        downloadDialog.show();
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

    // 下载apk的Runnable
    private Runnable downRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(apkUrl);
                Request request = new Request.Builder().url(url).build();
                OkHttpClient okHttpClient = new OkHttpClient();
                Call call = okHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        handler.sendEmptyMessage(UPDATE_FIAL);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        InputStream is = response.body().byteStream();
                        long length = response.body().contentLength();
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fileSizeTextView.setText(String.format("更新文件大小：%s", FormetFileSize(length)));
                            }
                        });
                        File file = new File(savePath);
                        //android创建目录有先创建父目录，再创建子目录（默认只能创建一层目录）
                        if (!file.exists()) {
                            file.mkdir();
                        }
                        String apkFile = savePath + "/" + getUrlApk(apkUrl);
                        File ApkFile = new File(apkFile);
                        // 判断文件是否存在，不存在则创建
                        if (!ApkFile.exists()) {
                            ApkFile.createNewFile();
                        }
                        FileOutputStream fos = new FileOutputStream(ApkFile);
                        int count = 0;
                        byte buf[] = new byte[64];

                        do {
                            int numread = is.read(buf);
                            count += numread;
                            progress = (int) (((float) count / length) * 100);
                            // 更新进度
                            handler.sendEmptyMessage(UPDATEING);
                            if (numread <= 0) {
                                // 下载完成通知安装
                                handler.sendEmptyMessage(UPDATE_FINISH);
                                break;
                            }
                            fos.write(buf, 0, numread);
                        } while (!interceptFlag);// 点击取消就停止下载.
                        fos.close();
                        is.close();
                    }
                });

            } catch (MalformedURLException e) {
                LogUtil.d(e.toString());
                handler.sendEmptyMessage(UPDATE_FIAL);
            }
        }
    };


    public void downloadApk() {
        showDowloadProgress();
        downLoadThread = new Thread(downRunnable);
        downLoadThread.start();
    }

    private void installAPK() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File apkFile = new File(savePath + "/" + getUrlApk(apkUrl));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = GenericFileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static String getUrlApk(String path) {
        String name = path.substring(path.lastIndexOf("/") + 1);
        return name;
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }
}
