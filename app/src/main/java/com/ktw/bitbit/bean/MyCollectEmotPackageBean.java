package com.ktw.bitbit.bean;

import java.util.List;

public class MyCollectEmotPackageBean {

    /**
     * createTime : 1585573230
     * face : {"clollectNumber":0,"createBy":0,"createTime":1585573185,"desc":"three","id":"5e81ed41fa15ebb25c4b9e0a","name":"懒黄人","number":0,"path":["http://47.75.208.70:8089/temp/20200330/9ab3b742-a884-4fa8-9f37-240f179ee081.gif","http://47.75.208.70:8089/temp/20200330/3ff63e0f-2563-41bf-be37-f21d7806a4be.gif","http://47.75.208.70:8089/temp/20200330/8751f76c-43d6-4ee9-9af9-43147ff40d70.gif","http://47.75.208.70:8089/temp/20200330/890b9a7f-be2b-42f9-9604-d132cd417b6c.gif","http://47.75.208.70:8089/temp/20200330/ffff354a-cfee-401c-a935-8b302d84a2f6.gif","http://47.75.208.70:8089/temp/20200330/90ced0f2-2b18-4a99-9217-fc1e62e676de.gif","http://47.75.208.70:8089/temp/20200330/4f04f555-9a29-4e7d-a9df-81ca880134ff.gif","http://47.75.208.70:8089/temp/20200330/999b8d54-995a-4b5d-a89c-5244deff2183.gif","http://47.75.208.70:8089/temp/20200330/1db935df-f1d0-4a25-a204-c12ff0f340a2.gif","http://47.75.208.70:8089/temp/20200330/a3ca0bc1-db66-4ac7-a343-e3cb88f07bd4.gif"],"special":false,"type":0}
     * fileLength : 0
     * fileSize : 0
     * id : 5e81ed6efa15ebb25c4b9e11
     * isOne : 0
     * type : 0
     * userId : 10000002
     */

    private int createTime;
    private FaceBean face;
    private int fileLength;
    private int fileSize;
    private String id;
    private int isOne;
    private int type;
    private int userId;

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public FaceBean getFace() {
        return face;
    }

    public void setFace(FaceBean face) {
        this.face = face;
    }

    public int getFileLength() {
        return fileLength;
    }

    public void setFileLength(int fileLength) {
        this.fileLength = fileLength;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIsOne() {
        return isOne;
    }

    public void setIsOne(int isOne) {
        this.isOne = isOne;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public static class FaceBean {
        /**
         * clollectNumber : 0
         * createBy : 0
         * createTime : 1585573185
         * desc : three
         * id : 5e81ed41fa15ebb25c4b9e0a
         * name : 懒黄人
         * number : 0
         * path : ["http://47.75.208.70:8089/temp/20200330/9ab3b742-a884-4fa8-9f37-240f179ee081.gif","http://47.75.208.70:8089/temp/20200330/3ff63e0f-2563-41bf-be37-f21d7806a4be.gif","http://47.75.208.70:8089/temp/20200330/8751f76c-43d6-4ee9-9af9-43147ff40d70.gif","http://47.75.208.70:8089/temp/20200330/890b9a7f-be2b-42f9-9604-d132cd417b6c.gif","http://47.75.208.70:8089/temp/20200330/ffff354a-cfee-401c-a935-8b302d84a2f6.gif","http://47.75.208.70:8089/temp/20200330/90ced0f2-2b18-4a99-9217-fc1e62e676de.gif","http://47.75.208.70:8089/temp/20200330/4f04f555-9a29-4e7d-a9df-81ca880134ff.gif","http://47.75.208.70:8089/temp/20200330/999b8d54-995a-4b5d-a89c-5244deff2183.gif","http://47.75.208.70:8089/temp/20200330/1db935df-f1d0-4a25-a204-c12ff0f340a2.gif","http://47.75.208.70:8089/temp/20200330/a3ca0bc1-db66-4ac7-a343-e3cb88f07bd4.gif"]
         * special : false
         * type : 0
         */

        private int clollectNumber;
        private int createBy;
        private int createTime;
        private String desc;
        private String id;
        private String name;
        private int number;
        private boolean special;
        private int type;
        private List<String> path;

        public int getClollectNumber() {
            return clollectNumber;
        }

        public void setClollectNumber(int clollectNumber) {
            this.clollectNumber = clollectNumber;
        }

        public int getCreateBy() {
            return createBy;
        }

        public void setCreateBy(int createBy) {
            this.createBy = createBy;
        }

        public int getCreateTime() {
            return createTime;
        }

        public void setCreateTime(int createTime) {
            this.createTime = createTime;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public boolean isSpecial() {
            return special;
        }

        public void setSpecial(boolean special) {
            this.special = special;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public List<String> getPath() {
            return path;
        }

        public void setPath(List<String> path) {
            this.path = path;
        }
    }
}
