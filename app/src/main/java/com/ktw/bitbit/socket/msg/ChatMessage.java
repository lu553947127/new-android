package com.ktw.bitbit.socket.msg;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.ktw.bitbit.FLYAppConfig;
import com.ktw.bitbit.socket.EMConnectionManager;
import com.ktw.bitbit.util.DES;
import com.ktw.bitbit.util.Md5Util;
import com.ktw.bitbit.xmpp.listener.ChatMessageListener;

public class ChatMessage extends AbstractMessage {

    private String fromUserId;
    private String toUserId;
    private String fromUserName;
    private String toUserName;
    private short type; // 消息类型;(如：0:text、1:image、2:voice、3:vedio、4:music、5:news)
    private String content;
    private boolean isEncrypt;  // 是否加密传输
    private int encryptType; // 消息加密类型(新)  为int 类型
    private boolean isReadDel;  // 是否阅后即焚
    private String fileName;
    private long fileSize; //  文件大小 单位字节
    private long fileTime; // 文件播放时长  录音时长，视频时长
    private double location_x;// 1.当为地理位置时，有效 2.特殊：当为图片时，该值为图片的宽度
    private double location_y;// 1.当为地理位置时，有效 2.特殊：当为图片时，该值为图片的高度
    private long deleteTime;  // 消息到期时间(当前时间+消息保存天数=到期时间)
    private int messageState;
    private String objectId;
    private long timeSend;

    public ChatMessage() {
    }

    /**
     * 将业务逻辑使用的ChatMessage转换为传输的ChatMessage
     *
     * @param message
     * @return
     */
    public static ChatMessage toSocketMessage(com.ktw.bitbit.bean.message.ChatMessage message) {
        com.ktw.bitbit.bean.message.ChatMessage chatMessage = message.clone(false);
        chatMessage.setGroup(message.isGroup());

        // 给消息进行加密
        if (chatMessage.getIsEncrypt() == 1) {
            try {
                // 生成encryptKey
                String encryptKey = Md5Util.toMD5(FLYAppConfig.apiKey + chatMessage.getTimeSend() + chatMessage.getPacketId());
                // 通过DES 对content进行加密
                String x = DES.encryptDES(chatMessage.getContent(), encryptKey);
                chatMessage.setContent(x);
            } catch (Exception e) {
                // 加密失败，将该字段置为不加密，以防接收方收到后去解密
                chatMessage.setIsEncrypt(0);
            }
        }

        ChatMessage chat = new ChatMessage();
        chat.setFromUserId(chatMessage.getFromUserId());
        chat.setToUserId(chatMessage.getToUserId());
        chat.setFromUserName(chatMessage.getFromUserName());
        chat.setToUserName(chatMessage.getToUserName());
        chat.setType((short) chatMessage.getType());
        chat.setContent(chatMessage.getContent());
        chat.setEncrypt(chatMessage.getIsEncrypt() == 1);
        //  todo 替换掉isEncrypt字段 需要服务端支持
        // chat.setEncryptType(chatMessage.getIsEncrypt());
        chat.setReadDel(chatMessage.getIsReadDel());
        chat.setFileName(chatMessage.getFilePath());
        chat.setFileSize(chatMessage.getFileSize());
        chat.setFileTime(chatMessage.getTimeLen());
        if (!TextUtils.isEmpty(chatMessage.getLocation_x())) {
            double x = Double.parseDouble(chatMessage.getLocation_x());
            chat.setLocation_x(x);
        }
        if (!TextUtils.isEmpty(chatMessage.getLocation_y())) {
            double y = Double.parseDouble(chatMessage.getLocation_y());
            chat.setLocation_y(y);
        }
        chat.setDeleteTime(chatMessage.getDeleteTime());
        chat.setObjectId(chatMessage.getObjectId());
        chat.setTimeSend(chatMessage.getTimeSend());

        MessageHead head = new MessageHead();
        head.setChatType((byte) (chatMessage.isGroup() ? 2 : 1)); // 标记是否单聊
        head.setFrom(chatMessage.getFromUserId() + "/" + EMConnectionManager.CURRENT_DEVICE);
        if (chat.getFromUserId().equals(chat.getToUserId())) {
            head.setTo(chatMessage.getToUserId() + "/" + chat.getToUserName());
        } else {
            head.setTo(chatMessage.getToUserId());
        }
        head.setMessageId(chatMessage.getPacketId());
        chat.setMessageHead(head);

        return chat;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isEncrypt() {
        return isEncrypt;
    }

    public void setEncrypt(boolean encrypt) {
        isEncrypt = encrypt;
    }

    public int getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(int encryptType) {
        this.encryptType = encryptType;
    }

    public boolean isReadDel() {
        return isReadDel;
    }

    public void setReadDel(boolean readDel) {
        isReadDel = readDel;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getFileTime() {
        return fileTime;
    }

    public void setFileTime(long fileTime) {
        this.fileTime = fileTime;
    }

    public double getLocation_x() {
        return location_x;
    }

    public void setLocation_x(double location_x) {
        this.location_x = location_x;
    }

    public double getLocation_y() {
        return location_y;
    }

    public void setLocation_y(double location_y) {
        this.location_y = location_y;
    }

    public long getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(long deleteTime) {
        this.deleteTime = deleteTime;
    }

    public int getMessageState() {
        return messageState;
    }

    public void setMessageState(int messageState) {
        this.messageState = messageState;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public long getTimeSend() {
        return timeSend;
    }

    public void setTimeSend(long timeSend) {
        this.timeSend = timeSend;
    }

    public String getMessageId() {
        return messageHead.getMessageId();
    }

    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        object.put("fromUserId", this.fromUserId);
        object.put("toUserId", this.toUserId);
        object.put("toUserName", this.toUserName);
        object.put("fromUserName", this.fromUserName);
        object.put("type", this.type);
        object.put("content", this.content);
        object.put("isEncrypt", this.isEncrypt);
        object.put("encryptType", this.encryptType);
        object.put("isReadDel", this.isReadDel);
        object.put("fileName", this.fileName);
        object.put("fileSize", this.fileSize);
        object.put("fileTime", this.fileTime);
        object.put("location_x", this.location_x);
        object.put("location_y", this.location_y);
        object.put("deleteTime", this.deleteTime);
        object.put("objectId", this.objectId);
        object.put("timeSend", this.timeSend);
        object.put("messageHead", this.messageHead);
        String msg = object.toString();
        return msg;
    }

    /**
     * 将传输过程中的ChatMessage转换为业务逻辑使用的ChatMessage
     *
     * @param loginId
     * @return
     */
    public com.ktw.bitbit.bean.message.ChatMessage toSkMessage(String loginId) {
        com.ktw.bitbit.bean.message.ChatMessage chat = new com.ktw.bitbit.bean.message.ChatMessage();

        chat.setFromUserId(this.fromUserId);
        chat.setToUserId(this.toUserId);
        chat.setFromUserName(this.fromUserName);
        chat.setToUserName(this.toUserName);
        chat.setType(this.type);
        chat.setContent(this.content);
        chat.setIsEncrypt(this.isEncrypt ? 1 : 0);
        //  todo 替换掉isEncrypt字段 需要服务端支持
        // chat.setIsEncrypt(this.encryptType);
        chat.setIsReadDel(this.isReadDel ? 1 : 0);
        chat.setFilePath(this.fileName);
        chat.setFileSize((int) this.fileSize);
        chat.setTimeLen((int) this.fileTime);
        chat.setLocation_x(String.valueOf(this.location_x));
        chat.setLocation_y(String.valueOf(this.location_y));
        chat.setDeleteTime(this.deleteTime);
        chat.setObjectId(this.objectId);
        chat.setTimeSend(this.timeSend);

        chat.setPacketId(this.messageHead.getMessageId());
        chat.setGroup(this.messageHead.chatType == 2);
        chat.setDelayMsg(messageHead.offline);

        chat.setMessageState(ChatMessageListener.MESSAGE_SEND_SUCCESS); // 收到的消息默认成功

        String from = this.messageHead.getFrom();
        from = from.replaceAll(this.fromUserId + "/", "");
        chat.setFromId(from);

        if (fromUserId.equals(toUserId)) {
            chat.setMySend(EMConnectionManager.CURRENT_DEVICE.equals(from));
        } else {
            chat.setMySend(loginId.equals(this.fromUserId));
        }
        return chat;
    }
}
