package com.ktw.fly.view.chatHolder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.ktw.fly.FLYAppConstant;
import com.ktw.fly.R;
import com.ktw.fly.bean.message.ChatMessage;
import com.ktw.fly.bean.message.XmppMessage;
import com.ktw.fly.db.dao.ChatMessageDao;
import com.ktw.fly.downloader.DownloadListener;
import com.ktw.fly.downloader.Downloader;
import com.ktw.fly.downloader.FailReason;
import com.ktw.fly.helper.AvatarHelper;
import com.ktw.fly.ui.message.ChatOverviewActivity;
import com.ktw.fly.ui.tool.SingleImagePreviewActivity;
import com.ktw.fly.util.FileUtil;
import com.ktw.fly.view.XuanProgressPar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fm.jiecao.jcvideoplayer_lib.ChatImageView;
import pl.droidsonroids.gif.GifDrawable;

public class ImageViewHolder extends AChatHolderInterface {

    private static final int IMAGE_MIN_SIZE = 70;
    private static final int IMAGE_MAX_SIZE = 105;
    ChatImageView mImageView;
    XuanProgressPar progressPar;
    private int width, height;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_image : R.layout.chat_to_item_image;
    }

    @Override
    public void initView(View view) {
        mImageView = view.findViewById(R.id.chat_image);
        progressPar = view.findViewById(R.id.img_progress);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        // 修改image布局大小，解决不能滑动到底部的问题
        changeImageLayaoutSize(message, IMAGE_MIN_SIZE, IMAGE_MAX_SIZE);

        String filePath = message.getFilePath();
        if (FileUtil.isExist(filePath)) { // 本地存在
            if (filePath.endsWith(".gif")) { // 加载gif
                fillImageGif(filePath);
            } else {
                if (mHolderListener != null) {
                    Bitmap bitmap = mHolderListener.onLoadBitmap(filePath, width, height);
                    if (bitmap != null && !bitmap.isRecycled()) {
                        mImageView.setImageBitmap(bitmap);
                    } else {
                        mImageView.setImageBitmap(null);
                    }
                }
                // 这种加载 notify 会导致 图片不出来，一直处于加载的状态
                // AvatarHelper.getInstance().displayChatImage(filePath,mImageView);
            }
        } else {
            if (TextUtils.isEmpty(message.getContent())) {
                mImageView.setImageResource(R.drawable.fez);
            } else {
                Downloader.getInstance().addDownload(message.getContent(), mSendingBar, new FileDownloadListener(message));
            }
        }

        // 判断是否为阅后即焚类型的图片，如果是 模糊显示该图片
        if (!isGounp) {
            mImageView.setAlpha(message.getIsReadDel() ? 0.1f : 1f);
        }

        // 上传进度条 我的消息才有进度条
        if (message.isUpload() || !isMysend || message.getUploadSchedule() >= 100) {
            progressPar.setVisibility(View.GONE);
        } else {
            progressPar.setVisibility(View.VISIBLE);
        }
        progressPar.update(message.getUploadSchedule());
    }

    private void fillImage(String filePath) {
        AvatarHelper.getInstance().displayUrl(filePath, mImageView, R.drawable.fez);
    }

    private void fillImageGif(String filePath) {
        try {
            GifDrawable gifFromFile = new GifDrawable(new File(filePath));
            mImageView.setImageGifDrawable(gifFromFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void changeImageLayaoutSize(ChatMessage message, int mindp, int maxdp) {
        ViewGroup.LayoutParams mLayoutParams = mImageView.getLayoutParams();

        if (TextUtils.isEmpty(message.getLocation_x()) || TextUtils.isEmpty(message.getLocation_y())) {
            mLayoutParams.width = dp2px(maxdp);
            mLayoutParams.height = dp2px(maxdp);

            width = mLayoutParams.width;
            height = mLayoutParams.height;
            Downloader.getInstance().addDownload(message.getContent(), mSendingBar, new FileDownloadListener(message));
        } else {
            float image_width = Float.parseFloat(message.getLocation_x());
            float image_height = Float.parseFloat(message.getLocation_y());

            // 基于宽度进行缩放,三挡:宽图 55/100,窄图100/55
            float width = image_width / image_height < 0.4 ? mindp : maxdp;
            float height = width == maxdp ? Math.max(width / image_width * image_height, mindp) : maxdp;

            mLayoutParams.width = dp2px(width);
            mLayoutParams.height = dp2px(height);

            this.width = mLayoutParams.width;
            this.height = mLayoutParams.height;
        }

        mImageView.setLayoutParams(mLayoutParams);
    }

    @Override
    public void onRootClick(View v) {
        if (mdata.getIsReadDel()) { // 阅后即焚图片跳转至单张图片预览类
            Intent intent = new Intent(mContext, SingleImagePreviewActivity.class);
            intent.putExtra(FLYAppConstant.EXTRA_IMAGE_URI, mdata.getContent());
            intent.putExtra("image_path", mdata.getFilePath());
            intent.putExtra("isReadDel", mdata.getIsReadDel());
            if (!isGounp && !isMysend && mdata.getIsReadDel()) {
                intent.putExtra("DEL_PACKEDID", mdata.getPacketId());
            }
            mContext.startActivity(intent);
        } else {
            int imageChatMessageList_current_position = 0;
            List<ChatMessage> imageChatMessageList = new ArrayList<>();
            for (int i = 0; i < chatMessages.size(); i++) {
                if ((chatMessages.get(i).getType() == XmppMessage.TYPE_IMAGE
                        || chatMessages.get(i).getType() == XmppMessage.TYPE_EMOT_PACKAGE
                        || chatMessages.get(i).getType() == XmppMessage.TYPE_CUSTOM_EMOT)
                        && !chatMessages.get(i).getIsReadDel()
                ) {
                    if (chatMessages.get(i).getPacketId().equals(mdata.getPacketId())) {
                        imageChatMessageList_current_position = imageChatMessageList.size();
                    }
                    imageChatMessageList.add(chatMessages.get(i));
                }
            }
            Intent intent = new Intent(mContext, ChatOverviewActivity.class);
//            intent.putExtra("imageChatMessageList", JSON.toJSONString(imageChatMessageList));
            ChatOverviewActivity.imageChatMessageListStr = JSON.toJSONString(imageChatMessageList);
            intent.putExtra("imageChatMessageList_current_position", imageChatMessageList_current_position);
            mContext.startActivity(intent);
        }
    }

    @Override
    public boolean enableSendRead() {
        return true;
    }

    // 启用阅后即焚
    @Override
    public boolean enableFire() {
        return true;
    }

    class FileDownloadListener implements DownloadListener {
        private ChatMessage message;

        public FileDownloadListener(ChatMessage message) {
            this.message = message;
        }

        @Override
        public void onStarted(String uri, View view) {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFailed(String uri, FailReason failReason, View view) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }

        @Override
        public void onComplete(String uri, String filePath, View view) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
            message.setFilePath(filePath);
            ChatMessageDao.getInstance().updateMessageDownloadState(mLoginUserId, mToUserId, message.get_id(), true, filePath);
            // 保存图片尺寸到数据库
            saveImageSize(filePath);

            if (filePath.endsWith(".gif")) { // 加载gif
                fillImageGif(filePath);
            } else { // 加载图片
                fillImage(filePath);
            }
        }

        @Override
        public void onCancelled(String uri, View view) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }

        private void saveImageSize(String filePath) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options); // 此时返回的bitmap为null

            message.setLocation_x(String.valueOf(options.outWidth));
            message.setLocation_y(String.valueOf(options.outHeight));

            // 重绘图片尺寸
            changeImageLayaoutSize(message, IMAGE_MIN_SIZE, IMAGE_MAX_SIZE);
            // 保存下载到数据库
            ChatMessageDao.getInstance().updateMessageLocationXY(message, mLoginUserId);
        }
    }
}
