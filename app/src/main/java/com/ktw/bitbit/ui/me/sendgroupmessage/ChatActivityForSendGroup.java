package com.ktw.bitbit.ui.me.sendgroupmessage;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.FLYReporter;
import com.ktw.bitbit.audio1.VoicePlayer;
import com.ktw.bitbit.bean.Friend;
import com.ktw.bitbit.bean.PrivacySetting;
import com.ktw.bitbit.bean.SelectFriendItem;
import com.ktw.bitbit.bean.VideoFile;
import com.ktw.bitbit.bean.message.ChatMessage;
import com.ktw.bitbit.bean.message.XmppMessage;
import com.ktw.bitbit.broadcast.MsgBroadcast;
import com.ktw.bitbit.broadcast.OtherBroadcast;
import com.ktw.bitbit.db.dao.ChatMessageDao;
import com.ktw.bitbit.downloader.Downloader;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.PrivacySettingHelper;
import com.ktw.bitbit.helper.UploadEngine;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.map.MapPickerActivity;
import com.ktw.bitbit.ui.me.LocalVideoActivity;
import com.ktw.bitbit.ui.me.SelectFriendsActivity;
import com.ktw.bitbit.ui.me.sendgroupmessage.ChatBottomForSendGroup.ChatBottomListener;
import com.ktw.bitbit.util.BitmapUtil;
import com.ktw.bitbit.util.RecorderUtils;
import com.ktw.bitbit.util.TimeUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.video.EasyCameraActivity;
import com.ktw.bitbit.video.MessageEventGpu;
import com.ktw.bitbit.view.SelectCardPopupWindow;
import com.ktw.bitbit.view.SelectFileDialog;
import com.ktw.bitbit.view.photopicker.PhotoPickerActivity;
import com.ktw.bitbit.view.photopicker.SelectModel;
import com.ktw.bitbit.view.photopicker.intent.PhotoPickerIntent;
import com.ktw.bitbit.xmpp.CoreService;
import com.ktw.bitbit.xmpp.CoreService.CoreServiceBinder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import Jni.FFmpegCmd;
import Jni.VideoUitls;
import VideoHandle.OnEditorListener;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import fm.jiecao.jcvideoplayer_lib.MessageEvent;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;


/**
 * ????????????
 */
public class ChatActivityForSendGroup extends BaseActivity implements
        ChatBottomListener, SelectCardPopupWindow.SendCardS {
    // ????????????????????????
    private static final int REQUEST_CODE_PICK_PHOTO = 2;
    private static final int REQUEST_CODE_SELECT_VIDE0 = 3;
    private static final int REQUEST_CODE_SELECT_Locate = 5;
    public static boolean isAlive;
    private boolean isSending;// ??????????????????????????????
    private TextView mCountTv;
    private TextView mNameTv;
    private ChatBottomForSendGroup mChatBottomView;
    private CoreService mService;
    private String mLoginUserId;
    private String mLoginNickName;
    private List<SelectFriendItem> friendItemList;
    private List<SelectFriendItem> mCloneFriendItemList;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((CoreServiceBinder) service).getService();
        }
    };
    private UploadEngine.ImFileUploadResponse mUploadResponse = new UploadEngine.ImFileUploadResponse() {

        @Override
        public void onSuccess(String toUserId, ChatMessage message) {
            message.setUpload(true);
            message.setUploadSchedule(100);
            send(message);
        }

        @Override
        public void onFailure(String toUserId, ChatMessage message) {
            DialogHelper.dismissProgressDialog();
            Toast.makeText(mContext, getString(R.string.upload_failed), Toast.LENGTH_SHORT).show();
        }
    };

    public static void start(Context ctx, Collection<SelectFriendItem> items) {
        Intent intent = new Intent(ctx, ChatActivityForSendGroup.class);
        // ???????????????????????????TransactionTooLargeException?????????????????????
        // intent.putExtra("items", JSON.toJSONString(items));
        SelectFriendsActivity.tempData = JSON.toJSONString(items);
        ctx.startActivity(intent);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_for_sg);

        isAlive = true;

        // String items = getIntent().getStringExtra("items");
        friendItemList = JSON.parseArray(SelectFriendsActivity.tempData, SelectFriendItem.class);
        mCloneFriendItemList = new ArrayList<>(friendItemList);

        mLoginUserId = coreManager.getSelf().getUserId();
        mLoginNickName = coreManager.getSelf().getNickName();

        bindService(CoreService.getIntent(), mConnection, BIND_AUTO_CREATE);
        EventBus.getDefault().register(this);
        Downloader.getInstance().init(FLYApplication.getInstance().mAppDir + File.separator + mLoginUserId
                + File.separator + Environment.DIRECTORY_MUSIC);

        initActionBar();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAlive = false;
        // unbindService(mConnection);
        EventBus.getDefault().unregister(this);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // finish??????tempData?????????
                SelectFriendsActivity.tempData = "";
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.mass));
    }

    private void initView() {
        mCountTv = (TextView) findViewById(R.id.send_size_tv);
        mNameTv = (TextView) findViewById(R.id.send_name_tv);
        mCountTv.setText(getString(R.string.you_will_send_a_message_to) + friendItemList.size() + getString(R.string.bit) + getString(R.string.friend));
        final StringBuilder userNames = new StringBuilder();
        userNames.append(friendItemList.get(0).getName());
        for (int i = 1; i < friendItemList.size(); i++) {
            userNames.append(",");
            userNames.append(friendItemList.get(i).getName());
        }
        mNameTv.setText(userNames);

        mChatBottomView = (ChatBottomForSendGroup) findViewById(R.id.chat_bottom_view);
        mChatBottomView.setChatBottomListener(this);
    }

    private void setSameParams(ChatMessage message) {
        DialogHelper.showDefaulteMessageProgressDialogAddCancel(this, dialog -> dialog.dismiss());

        message.setFromUserId(mLoginUserId);
        message.setFromUserName(mLoginNickName);
        message.setIsReadDel(0);

        PrivacySetting privacySetting = PrivacySettingHelper.getPrivacySettings(this);
        boolean isEncrypt = privacySetting.getIsEncrypt() == 1;
        if (isEncrypt) {
            message.setIsEncrypt(1);
        } else {
            message.setIsEncrypt(0);
        }
        message.setReSendCount(ChatMessageDao.fillReCount(message.getType()));
        sendMessage(message);
    }

    private void sendMessage(ChatMessage message) {
        if (message.getType() == XmppMessage.TYPE_VOICE || message.getType() == XmppMessage.TYPE_IMAGE
                || message.getType() == XmppMessage.TYPE_VIDEO || message.getType() == XmppMessage.TYPE_FILE
                || message.getType() == XmppMessage.TYPE_LOCATION) {
            if (!message.isUpload()) {
                UploadEngine.uploadImFile(coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), message.getToUserId(), message, mUploadResponse);
            } else {
                message.setUpload(true);
                message.setUploadSchedule(100);
                send(message);
            }
        } else {
            send(message);
        }
    }

    private void send(ChatMessage oMessage) {
        new Thread(() -> {
            for (int i = 0; i < friendItemList.size(); i++) {
                try {
                    Thread.sleep(100);// ????????????????????????100ms???????????????
                    // ???????????????????????????????????????????????????????????????
                    ChatMessage message = oMessage.clone(false);
                    SelectFriendItem item = friendItemList.get(i);
                    String userId = item.getUserId();
                    message.setToUserId(userId);
                    message.setUploadSchedule(oMessage.getUploadSchedule());
                    message.setUpload(oMessage.isUpload());
                    message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                    message.setTimeSend(TimeUtils.sk_time_current_time());
                    if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, userId, message)) {
                        if (item.getIsRoom() == 1) {
                            coreManager.sendMucChatMessage(userId, message);
                        } else {
                            coreManager.sendChatMessage(userId, message);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void stopVoicePlay() {
        VoicePlayer.instance().stop();
    }

    @Override
    public void sendVoice(String filePath, int timeLen) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        long fileSize = file.length();
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_VOICE);
        message.setContent("");
        message.setFilePath(filePath);
        message.setFileSize((int) fileSize);
        message.setTimeLen(timeLen);
        setSameParams(message);
    }

    @Override
    public void sendText(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_TEXT);
        message.setContent(text);
        setSameParams(message);
    }

    @Override
    public void sendGif(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        // ??????????????????
        if (isSending) {
            return;
        }
        isSending = true;

        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_GIF);
        message.setContent(text);
        setSameParams(message);
    }

    @Override
    public void sendCollection(String collection) {
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_IMAGE);
        message.setContent(collection);
        message.setUpload(true);// ??????????????????
        setSameParams(message);
    }

    public void sendImage(File file) {
        if (!file.exists()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_IMAGE);
        message.setContent("");
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        long fileSize = file.length();
        message.setFileSize((int) fileSize);
        int[] imageParam = BitmapUtil.getImageParamByIntsFile(filePath);
        message.setLocation_x(String.valueOf(imageParam[0]));
        message.setLocation_y(String.valueOf(imageParam[1]));
        setSameParams(message);
    }

    public void sendVideo(File file) {
        if (!file.exists()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_VIDEO);
        message.setContent("");
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        long fileSize = file.length();
        message.setFileSize((int) fileSize);
        setSameParams(message);
    }

    public void sendFile(File file) {
        if (!file.exists()) {
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_FILE);
        message.setContent("");
        String filePath = file.getAbsolutePath();
        message.setFilePath(filePath);
        long fileSize = file.length();
        message.setFileSize((int) fileSize);
        setSameParams(message);
    }

    public void sendLocate(double latitude, double longitude, String address, String snapshot) {
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_LOCATION);
        message.setContent("");
        message.setFilePath(snapshot);
        message.setLocation_x(latitude + "");
        message.setLocation_y(longitude + "");
        message.setObjectId(address);
        setSameParams(message);
    }

    public void sendCard(Friend friend) {
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_CARD);
        message.setContent(friend.getNickName());
        message.setObjectId(friend.getUserId());
        setSameParams(message);
    }

    @Override
    public void clickPhoto() {
        ArrayList<String> imagePaths = new ArrayList<>();
        PhotoPickerIntent intent = new PhotoPickerIntent(ChatActivityForSendGroup.this);
        intent.setSelectModel(SelectModel.SINGLE);
        intent.setSelectedPaths(imagePaths);
        startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
        mChatBottomView.reset();
    }

    @Override
    public void clickCamera() {
        Intent intent = new Intent(this, EasyCameraActivity.class);
        startActivity(intent);
        mChatBottomView.reset();
    }

    @Override
    public void clickVideo() {
        Intent intent = new Intent(mContext, LocalVideoActivity.class);
        intent.putExtra(FLYAppConstant.EXTRA_ACTION, FLYAppConstant.ACTION_SELECT);
        intent.putExtra(FLYAppConstant.EXTRA_MULTI_SELECT, false);
        startActivityForResult(intent, REQUEST_CODE_SELECT_VIDE0);
    }

    @Override
    public void clickFile() {
        SelectFileDialog dialog = new SelectFileDialog(this, new SelectFileDialog.OptionFileListener() {
            @Override
            public void option(List<File> files) {
                if (files != null && files.size() > 0) {
                    for (int i = 0; i < files.size(); i++) {
                        sendFile(files.get(i));
                    }
                }
            }

            @Override
            public void intent() {

            }
        });
        dialog.show();
    }

    @Override
    public void clickLocation() {
        Intent intent = new Intent(mContext, MapPickerActivity.class);
        intent.putExtra(FLYAppConstant.EXTRA_FORM_CAHT_ACTIVITY, true);
        startActivityForResult(intent, REQUEST_CODE_SELECT_Locate);
    }

    @Override
    public void clickCard() {
        SelectCardPopupWindow mSelectCardPopupWindow = new SelectCardPopupWindow(this, this);
        mSelectCardPopupWindow.showAtLocation(findViewById(R.id.root_view),
                Gravity.CENTER, 0, 0);
    }

    @Override
    public void sendCardS(List<Friend> friends) {
        for (int i = 0; i < friends.size(); i++) {
            sendCard(friends.get(i));
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventGpu message) {// ????????????
        photograph(new File(message.event));
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEvent message) {
        Iterator<SelectFriendItem> iterator = mCloneFriendItemList.iterator();
        while (iterator.hasNext()) {
            SelectFriendItem item = iterator.next();
            String s = item.getUserId();
            if (message.message.equals(s)) {// ????????????????????????
                iterator.remove();
                if (mCloneFriendItemList.size() == 0) {// ????????????????????????????????? ??????????????????
                    Log.e("TAG", "over: " + s);
                    MsgBroadcast.broadcastMsgUiUpdate(FLYApplication.getInstance());
                    DialogHelper.dismissProgressDialog();

                    sendBroadcast(new Intent(OtherBroadcast.SEND_MULTI_NOTIFY));
                    // finish??????tempData?????????
                    SelectFriendsActivity.tempData = "";
                    finish();
                }
                break;
            }
        }
    }

    private void compress(File file) {
        String path = file.getPath();
        DialogHelper.showMessageProgressDialog(this, FLYApplication.getContext().getString(R.string.compressed));
        final String out = RecorderUtils.getVideoFileByTime();
        String[] cmds = RecorderUtils.ffmpegComprerssCmd(path, out);
        long duration = VideoUitls.getDuration(path);

        FFmpegCmd.exec(cmds, duration, new OnEditorListener() {
            public void onSuccess() {
                DialogHelper.dismissProgressDialog();
                File outFile = new File(out);
                runOnUiThread(() -> {
                    if (outFile.exists()) {
                        sendVideo(outFile);
                    } else {
                        sendVideo(file);
                    }
                });
            }

            public void onFailure() {
                DialogHelper.dismissProgressDialog();
                runOnUiThread(() -> {
                    sendVideo(file);
                });
            }

            public void onProgress(float progress) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_PHOTO && resultCode == RESULT_OK) {// ????????????
            if (data != null) {
                boolean isOriginal = data.getBooleanExtra(PhotoPickerActivity.EXTRA_RESULT_ORIGINAL, false);
                album(data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT), isOriginal);
            } else {
                ToastUtil.showToast(this, R.string.c_photo_album_failed);
            }
        } else if (requestCode == REQUEST_CODE_SELECT_VIDE0 && resultCode == RESULT_OK) {// ??????????????????
            if (data == null) {
                return;
            }
            String json = data.getStringExtra(FLYAppConstant.EXTRA_VIDEO_LIST);
            List<VideoFile> fileList = JSON.parseArray(json, VideoFile.class);
            if (fileList == null || fileList.size() == 0) {
                // ???????????????????????????????????????
                FLYReporter.unreachable();
            } else {
                for (VideoFile videoFile : fileList) {
                    String filePath = videoFile.getFilePath();
                    if (TextUtils.isEmpty(filePath)) {
                        // ???????????????????????????????????????
                        FLYReporter.unreachable();
                    } else {
                        File file = new File(filePath);
                        if (!file.exists()) {
                            // ???????????????????????????????????????
                            FLYReporter.unreachable();
                        } else {
                            compress(file);
                        }
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_SELECT_Locate && resultCode == RESULT_OK) {// ?????????????????????
            double latitude = data.getDoubleExtra(FLYAppConstant.EXTRA_LATITUDE, 0);
            double longitude = data.getDoubleExtra(FLYAppConstant.EXTRA_LONGITUDE, 0);
            String address = data.getStringExtra(FLYAppConstant.EXTRA_ADDRESS);
            String snapshot = data.getStringExtra(FLYAppConstant.EXTRA_SNAPSHOT);

            if (latitude != 0 && longitude != 0 && !TextUtils.isEmpty(address)
                    && !TextUtils.isEmpty(snapshot)) {
                sendLocate(latitude, longitude, address, snapshot);
            } else {
                ToastUtil.showToast(mContext, getString(R.string.loc_startlocnotice));
            }
        }
    }

    // ?????????????????? ??????
    private void photograph(final File file) {
        Log.e("zq", "?????????????????????:" + file.getPath() + "?????????????????????:" + file.length() / 1024 + "KB");
        // ?????????????????????Luban???????????????
        Luban.with(this)
                .load(file)
                .ignoreBy(100)     // ????????????100kb ?????????
                // .putGear(2)     // ?????????????????????????????????
                // .setTargetDir() // ??????????????????????????????
                .setCompressListener(new OnCompressListener() { // ????????????
                    @Override
                    public void onStart() {
                        Log.e("zq", "????????????");
                    }

                    @Override
                    public void onSuccess(File file) {
                        Log.e("zq", "????????????????????????????????????:" + file.getPath() + "?????????????????????:" + file.length() / 1024 + "KB");
                        sendImage(file);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("zq", "????????????,????????????");
                        sendImage(file);
                    }
                }).launch();// ????????????
    }

    // ?????????????????? ??????
    private void album(ArrayList<String> stringArrayListExtra, boolean isOriginal) {
        if (isOriginal) {// ????????????????????????
            Log.e("zq", "???????????????????????????????????????");
            for (int i = 0; i < stringArrayListExtra.size(); i++) {
                sendImage(new File(stringArrayListExtra.get(i)));
            }
            Log.e("zq", "???????????????????????????????????????");
            return;
        }

        List<String> list = new ArrayList<>();
        List<File> fileList = new ArrayList<>();
        for (int i = 0; i < stringArrayListExtra.size(); i++) {
            // Luban????????????????????????????????????????????????????????????????????????
            // ???????????????????????????
            List<String> lubanSupportFormatList = Arrays.asList("jpg", "jpeg", "png", "webp");
            boolean support = false;
            for (int j = 0; j < lubanSupportFormatList.size(); j++) {
                if (stringArrayListExtra.get(i).endsWith(lubanSupportFormatList.get(j))) {
                    support = true;
                    break;
                }
            }
            if (!support) {
                list.add(stringArrayListExtra.get(i));
                fileList.add(new File(stringArrayListExtra.get(i)));
            }
        }

        if (fileList.size() > 0) {
            for (File file : fileList) {// ?????????????????????????????????
                sendImage(file);
            }
        }

        // ???????????????????????????
        stringArrayListExtra.removeAll(list);

        Luban.with(this)
                .load(stringArrayListExtra)
                .ignoreBy(100)// ????????????100kb ?????????
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(File file) {
                        sendImage(file);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }).launch();// ????????????
    }
}
