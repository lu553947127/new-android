package com.ktw.bitbit.bean.redpacket;

import java.io.Serializable;
import java.util.List;

/**
 * 打开红包的时候返回的信息的实体类
 * Created by 魏正旺 on 2016/9/19.
 */
public class OpenRedpacket implements Serializable {

    private PacketEntity packet;
    private List<ListEntity> list;

    public PacketEntity getPacket() {
        return packet;
    }

    public void setPacket(PacketEntity packet) {
        this.packet = packet;
    }

    public List<ListEntity> getList() {
        return list;
    }

    public void setList(List<ListEntity> list) {
        this.list = list;
    }

    public class PacketEntity implements Serializable {
        /**
         * id : 57e0e8e3f914af8da8c1ffc7
         * over : 0
         * count : 1
         * status : 2
         * userId : 10005948
         * receiveCount : 1
         * greetings : 恭喜发财
         * userIds : [10005921]
         * sendTime : 1474357475
         * money : 23
         * userName : 啦啦啦
         * outTime : 1474443875
         * type : 1
         */
        private String id;
        private double over;
        private int count;
        private int status;
        private String userId;
        private int receiveCount;
        private String greetings;
        private int sendTime;
        private double money;
        private String userName;
        private int outTime;
        private int type;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public double getOver() {
            return over;
        }

        public void setOver(double over) {
            this.over = over;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public int getReceiveCount() {
            return receiveCount;
        }

        public void setReceiveCount(int receiveCount) {
            this.receiveCount = receiveCount;
        }

        public String getGreetings() {
            return greetings;
        }

        public void setGreetings(String greetings) {
            this.greetings = greetings;
        }

        public int getSendTime() {
            return sendTime;
        }

        public void setSendTime(int sendTime) {
            this.sendTime = sendTime;
        }

        public double getMoney() {
            return money;
        }

        public void setMoney(double money) {
            this.money = money;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public int getOutTime() {
            return outTime;
        }

        public void setOutTime(int outTime) {
            this.outTime = outTime;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

    public class ListEntity implements Serializable {
        /**
         * id : 57e0ee6ff914af8da8c1ffcf
         * time : 1474358895
         * redId : 57e0e8e3f914af8da8c1ffc7
         * userId : 10005921
         * money : 23
         * userName : in旅行
         */
        private String id;
        private int time;
        private String redId;
        private String reply;
        private String userId;
        private double money;
        private String userName;

        private String sendName;
        private String sendId;

        public String getSendName() {
            return sendName;
        }

        public void setSendName(String sendName) {
            this.sendName = sendName;
        }

        public String getSendId() {
            return sendId;
        }

        public void setSendId(String sendId) {
            this.sendId = sendId;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public String getRedId() {
            return redId;
        }

        public void setRedId(String redId) {
            this.redId = redId;
        }

        public String getReply() {
            return reply;
        }

        public void setReply(String reply) {
            this.reply = reply;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public double getMoney() {
            return money;
        }

        public void setMoney(double money) {
            this.money = money;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }
}
