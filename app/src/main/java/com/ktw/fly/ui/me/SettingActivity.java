package com.ktw.fly.ui.me;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.ktw.fly.FLYApplication;
import com.ktw.fly.R;
import com.ktw.fly.bean.Friend;
import com.ktw.fly.broadcast.MsgBroadcast;
import com.ktw.fly.db.dao.ChatMessageDao;
import com.ktw.fly.db.dao.FriendDao;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.helper.LoginHelper;
import com.ktw.fly.sp.UserSp;
import com.ktw.fly.ui.account.ChangePasswordActivity;
import com.ktw.fly.ui.account.LoginHistoryActivity;
import com.ktw.fly.ui.backup.BackupHistoryActivity;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.lock.DeviceLockHelper;
import com.ktw.fly.ui.me.emot.EmotPackageActivity;
import com.ktw.fly.util.AsyncUtils;
import com.ktw.fly.util.Constants;
import com.ktw.fly.util.GetFileSizeUtil;
import com.ktw.fly.util.LocaleHelper;
import com.ktw.fly.util.Md5Util;
import com.ktw.fly.util.PreferenceUtils;
import com.ktw.fly.util.SkinUtils;
import com.ktw.fly.util.ToastUtil;
import com.ktw.fly.util.UiUtils;
import com.ktw.fly.view.SelectionFrame;
import com.ktw.fly.xmpp.helloDemon.IntentWrapper;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 设置
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener {
    private TextView mCacheTv;
    private TextView cacheTv, clearRecordsTv, changeTv, privateTv, aboutTv;
    private Button mExitBtn;
    private String mLoginUserId;
    private My_BroadcastReceiver mMyBroadcastReceiver = new My_BroadcastReceiver();
    private String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.settings));

        mLoginUserId = coreManager.getSelf().getUserId();
        initView();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(com.ktw.fly.broadcast.OtherBroadcast.SEND_MULTI_NOTIFY);
        intentFilter.addAction(com.ktw.fly.broadcast.OtherBroadcast.NO_EXECUTABLE_INTENT);
        registerReceiver(mMyBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMyBroadcastReceiver != null) {
            unregisterReceiver(mMyBroadcastReceiver);
        }
    }

    private void initView() {
        cacheTv = (TextView) findViewById(R.id.cache_text);
        mCacheTv = (TextView) findViewById(R.id.cache_tv);
        clearRecordsTv = (TextView) findViewById(R.id.tv_cencel_chat);
        changeTv = (TextView) findViewById(R.id.passwoedtv);
        privateTv = (TextView) findViewById(R.id.privacySetting_text);
        aboutTv = (TextView) findViewById(R.id.aboutUs_text);
        mExitBtn = (Button) findViewById(R.id.exit_btn);
//        mExitBtn.setBackground(new ColorDrawable(MyApplication.getContext().getResources().getColor(R.color.redpacket_bg)));
        ViewCompat.setBackgroundTintList(mExitBtn, ColorStateList.valueOf(SkinUtils.getSkin(this).getAccentColor()));
        mExitBtn.setText(getString(R.string.setting_logout));

        cacheTv.setText(getString(R.string.clear_cache));
        long cacheSize = GetFileSizeUtil.getFileSize(new File(FLYApplication.getInstance().mAppDir));
        mCacheTv.setText(GetFileSizeUtil.formatFileSize(cacheSize));
        clearRecordsTv.setText(getString(R.string.clean_all_chat_history));
        changeTv.setText(getString(R.string.change_password));
        privateTv.setText(getString(R.string.privacy_setting));
        aboutTv.setText(getString(R.string.about_us));
        TextView mSwitchL = (TextView) findViewById(R.id.switch_language_tv);
        TextView mSwitchS = (TextView) findViewById(R.id.switch_skin_tv);
        mSwitchL.setText(getString(R.string.switch_language));
        mSwitchS.setText(getString(R.string.change_skin));
        TextView tv_language_scan = findViewById(R.id.tv_language_scan);
        String lan = LocaleHelper.getLanguage(this);
        if (lan.equals("zh")) {
            language = getResources().getString(R.string.simplified_chinese);
        } else if (lan.equals("en")) {
            language = getResources().getString(R.string.english);
        } else {
            language = getResources().getString(R.string.traditional_chinese);
        }
        tv_language_scan.setText(language);
        TextView tv_skin_scan = findViewById(R.id.tv_skin_scan);
        SkinUtils.Skin skin = SkinUtils.getSkin(this);
        tv_skin_scan.setText(skin.getColorName());
        findViewById(R.id.clear_cache_rl).setOnClickListener(this);
        findViewById(R.id.rl_cencel_chat).setOnClickListener(this);
        findViewById(R.id.rl_backup_chat).setOnClickListener(this);
        findViewById(R.id.change_password_rl).setOnClickListener(this);
        findViewById(R.id.switch_language).setOnClickListener(this);
        findViewById(R.id.skin_rl).setOnClickListener(this);
        findViewById(R.id.chat_font_size_rl).setOnClickListener(this);
        findViewById(R.id.send_gMessage_rl).setOnClickListener(this);
        findViewById(R.id.privacy_settting_rl).setOnClickListener(this);
        findViewById(R.id.secure_setting_rl).setOnClickListener(this);
        findViewById(R.id.chat_face_rl).setOnClickListener(this);
        if (coreManager.getConfig().thirdLogin) {
            findViewById(R.id.bind_account_rl).setOnClickListener(this);
        } else {
            findViewById(R.id.bind_account_rl).setVisibility(View.GONE);
        }
        findViewById(R.id.tuisongmsg).setOnClickListener(this);
        findViewById(R.id.about_us_rl).setOnClickListener(this);
//        if (!AppConfig.isShiku()) {
//            findViewById(R.id.about_us_rl).setVisibility(View.GONE);
//        }
        mExitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExitDialog();
            }
        });

        List<IntentWrapper> intentWrapperList = IntentWrapper.getWhiteListMatters(this, "");
        if (intentWrapperList.size() == 0) {
            findViewById(R.id.tuisongmsg).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        switch (v.getId()) {
            case R.id.clear_cache_rl:
                // 清除缓存
                clearCache();
                break;
            case R.id.rl_cencel_chat:
                SelectionFrame selectionFrame = new SelectionFrame(this);
                selectionFrame.setSomething(null, getString(R.string.is_empty_all_chat), new SelectionFrame.OnSelectionFrameClickListener() {
                    @Override
                    public void cancelClick() {

                    }

                    @Override
                    public void confirmClick() {
                        emptyServerMessage();

                        // 清除所有聊天记录
                        delAllChatRecord();

                        PreferenceUtils.putLong(FLYApplication.getContext(), Constants.CHAT_CLEAR_ALL_TIME, System.currentTimeMillis()/1000);
                    }
                });
                selectionFrame.show();
                break;
            case R.id.rl_backup_chat:
                BackupHistoryActivity.start(this);
                break;
            case R.id.change_password_rl:
                // 修改密码
                startActivity(new Intent(mContext, ChangePasswordActivity.class));
                break;
            case R.id.switch_language:
                // 切换语言
                startActivity(new Intent(this, SwitchLanguage.class));
                break;
            case R.id.skin_rl:
                // 更换皮肤
                startActivity(new Intent(this, SkinStore.class));
                break;
            case R.id.chat_font_size_rl:
                // 更换聊天字体
                startActivity(new Intent(this, FontSizeActivity.class));
                break;
            case R.id.send_gMessage_rl:
                // 群发消息
                startActivity(new Intent(this, SelectFriendsActivity.class));
                break;
            case R.id.privacy_settting_rl:
                // 开启验证
                startActivity(new Intent(mContext, PrivacySettingActivity.class));
                break;
            case R.id.secure_setting_rl:
                // 安全设置，
                startActivity(new Intent(mContext, DeviceMangerActivity.class));
                break;
            case R.id.bind_account_rl:
                // 绑定第三方
                startActivity(new Intent(mContext, BandAccountActivity.class));
                break;
            case R.id.tuisongmsg:
                IntentWrapper.whiteListMatters(this, "");
                break;
            case R.id.about_us_rl:
                // 关于我们
                startActivity(new Intent(mContext, AboutActivity.class));
                break;
            case R.id.chat_face_rl:
                // 聊天表情
                startActivity(new Intent(mContext, EmotPackageActivity.class));
                break;
        }
    }

    /**
     * 清楚缓存
     */
    private void clearCache() {
        String filePath = FLYApplication.getInstance().mAppDir;
        new ClearCacheAsyncTaska(filePath).execute(true);
    }

    // 服务器上所有的单人聊天记录也需要删除
    private void emptyServerMessage() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", String.valueOf(1));// 0 清空单人 1 清空所有

        HttpUtils.get().url(coreManager.getConfig().EMPTY_SERVER_MESSAGE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {

                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    /**
     * 清空所有聊天记录
     */
    private void delAllChatRecord() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        AsyncUtils.doAsync(this, settingActivityAsyncContext -> {
            // 不需要查询出所有好友，只需查询出最近聊天的好友即可
/*
            List<Friend> mAllFriend = new ArrayList<>();
            // 我的设备
            List<Friend> allDevices = FriendDao.getInstance().getDevice(mLoginUserId);
            mAllFriend.addAll(allDevices);
            // 公众号
            List<Friend> allSystems = FriendDao.getInstance().getAllSystems(mLoginUserId);
            mAllFriend.addAll(allSystems);
            // 我的好友
            List<Friend> allFriends = FriendDao.getInstance().getAllFriends(mLoginUserId);
            mAllFriend.addAll(allFriends);
            // 我的群组
            List<Friend> allRooms = FriendDao.getInstance().getAllRooms(mLoginUserId);
            mAllFriend.addAll(allRooms);
*/
            List<Friend> mNearChatFriendList = FriendDao.getInstance().getNearlyFriendMsg(mLoginUserId);
            for (int i = 0; i < mNearChatFriendList.size(); i++) {
                FriendDao.getInstance().resetFriendMessage(mLoginUserId, mNearChatFriendList.get(i).getUserId());
                ChatMessageDao.getInstance().deleteMessageTable(mLoginUserId, mNearChatFriendList.get(i).getUserId());
            }

            runOnUiThread(() -> {
                // 更新消息界面
                DialogHelper.dismissProgressDialog();
                MsgBroadcast.broadcastMsgUiUpdate(SettingActivity.this);
                MsgBroadcast.broadcastMsgNumReset(SettingActivity.this);
                ToastUtil.showToast(SettingActivity.this, getString(R.string.delete_success));
            });
        });
    }

    // 退出当前账号
    private void showExitDialog() {
        SelectionFrame mSF = new SelectionFrame(this);
        mSF.setSomething(null, getString(R.string.sure_exit_account), new SelectionFrame.OnSelectionFrameClickListener() {
            @Override
            public void cancelClick() {

            }

            @Override
            public void confirmClick() {
                logout();
                // 退出时清除设备锁密码，
                DeviceLockHelper.clearPassword();
                UserSp.getInstance(mContext).clearUserInfo();
                FLYApplication.getInstance().mUserStatus = LoginHelper.STATUS_USER_SIMPLE_TELPHONE;
                coreManager.logout();
                LoginHelper.broadcastLogout(mContext);
                LoginHistoryActivity.start(SettingActivity.this);
                finish();
            }
        });
        mSF.show();
    }

    private void logout() {
        HashMap<String, String> params = new HashMap<String, String>();
        // 得到电话
        String phoneNumber = coreManager.getSelf().getTelephone();
        String digestTelephone = Md5Util.toMD5(phoneNumber);
        params.put("telephone", digestTelephone);
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        // 默认为86
        params.put("areaCode", String.valueOf(86));
        params.put("deviceKey", "android");

        HttpUtils.get().url(coreManager.getConfig().USER_LOGOUT)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {

                    @Override
                    public void onResponse(ObjectResult<String> result) {
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                    }
                });
    }

    private class ClearCacheAsyncTaska extends AsyncTask<Boolean, String, Integer> {

        private File rootFile;
        private ProgressDialog progressDialog;

        private int filesNumber = 0;
        private boolean canceled = false;
        private long notifyTime = 0;

        public ClearCacheAsyncTaska(String filePath) {
            this.rootFile = new File(filePath);
        }

        @Override
        protected void onPreExecute() {
            filesNumber = GetFileSizeUtil.getFolderSubFilesNumber(rootFile);
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getString(R.string.deleteing));
            progressDialog.setMax(filesNumber);
            progressDialog.setProgress(0);
            // 设置取消按钮
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int i) {
                    canceled = true;
                }
            });
            progressDialog.show();
        }

        /**
         * 返回true代表删除完成，false表示取消了删除
         */
        @Override
        protected Integer doInBackground(Boolean... params) {
            if (filesNumber == 0) {
                return 0;
            }
            // 是否删除已清空的子文件夹
            boolean deleteSubFolder = params[0];
            return deleteFolder(rootFile, true, deleteSubFolder, 0);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            // String filePath = values[0];
            int progress = Integer.parseInt(values[1]);
            // progressDialog.setMessage(filePath);
            progressDialog.setProgress(progress);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (!canceled && result == filesNumber) {
                ToastUtil.showToast(mContext, R.string.clear_completed);
            }
            long cacheSize = GetFileSizeUtil.getFileSize(rootFile);
            mCacheTv.setText(GetFileSizeUtil.formatFileSize(cacheSize));
        }

        /**
         * 是否删除完毕
         *
         * @param file
         * @param deleteSubFolder
         * @return
         */
        private int deleteFolder(File file, boolean rootFolder, boolean deleteSubFolder, int progress) {
            if (file == null || !file.exists() || !file.isDirectory()) {
                return 0;
            }
            File flist[] = file.listFiles();
            for (File subFile : flist) {
                if (canceled) {
                    return progress;
                }
                if (subFile.isFile()) {
                    subFile.delete();
                    progress++;
                    long current = System.currentTimeMillis();
                    if (current - notifyTime > 200) {// 200毫秒更新一次界面
                        notifyTime = current;
                        publishProgress(subFile.getAbsolutePath(), String.valueOf(progress));
                    }
                } else {
                    progress = deleteFolder(subFile, false, deleteSubFolder, progress);
                    if (deleteSubFolder) {
                        subFile.delete();
                    }
                }
            }
            return progress;
        }
    }

    private class My_BroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                if (action.equals(com.ktw.fly.broadcast.OtherBroadcast.SEND_MULTI_NOTIFY)) {// 群发消息结束，关闭当前界面
                    finish();
                } else if (action.equals(com.ktw.fly.broadcast.OtherBroadcast.NO_EXECUTABLE_INTENT)) {// 无可执行的intent 需提醒用户
                    DialogHelper.tip(SettingActivity.this, getString(R.string.no_executable_intent));
                }
            }
        }
    }
}
