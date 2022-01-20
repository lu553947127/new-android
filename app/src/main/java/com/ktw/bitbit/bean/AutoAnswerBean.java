package com.ktw.bitbit.bean;


import java.util.List;

public class AutoAnswerBean {
    private String answer;
    private String endAnswer;
    private String startAnswer;
    private List<AnswerBean> autoAnswerList;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getEndAnswer() {
        return endAnswer;
    }

    public void setEndAnswer(String endAnswer) {
        this.endAnswer = endAnswer;
    }

    public String getStartAnswer() {
        return startAnswer;
    }

    public void setStartAnswer(String startAnswer) {
        this.startAnswer = startAnswer;
    }

    public List<AnswerBean> getAutoAnswerList() {
        return autoAnswerList;
    }

    public void setAutoAnswerList(List<AnswerBean> autoAnswerList) {
        this.autoAnswerList = autoAnswerList;
    }

    public static class AnswerBean{
        private String answer;
        private String id;
        private String issue;
        private int sort;
        private long updateTime;

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getIssue() {
            return issue;
        }

        public void setIssue(String issue) {
            this.issue = issue;
        }

        public int getSort() {
            return sort;
        }

        public void setSort(int sort) {
            this.sort = sort;
        }

        public long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(long updateTime) {
            this.updateTime = updateTime;
        }
    }
}
