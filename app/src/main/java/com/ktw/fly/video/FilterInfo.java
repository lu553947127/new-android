package com.ktw.fly.video;

import com.ktw.fly.luo.camfilter.GPUCamImgOperator;

public class FilterInfo {

    private String name;
    private GPUCamImgOperator.GPUImgFilterType type;
    private int rid;

    public FilterInfo(GPUCamImgOperator.GPUImgFilterType type, String name, int rid) {
        this.name = name;
        this.type = type;
        this.rid = rid;
    }

    public String getName() {
        return name;
    }


    public GPUCamImgOperator.GPUImgFilterType getType() {
        return type;
    }


    public int getRid() {
        return rid;
    }
}
