package com.ktw.bitbit.bean;

/**
 * @ProjectName: new-android
 * @Package: com.ktw.fly.bean
 * @ClassName: SprayPermissionBean
 * @Description: java类作用描述
 * @Author: 鹿鸿祥
 * @CreateDate: 2021/12/9 15:08
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/12/9 15:08
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class SprayPermissionBean {
    /**
     * id : 61b070ca663f000045002622
     * qiuGou : 1.0
     * sanHua : 2.0
     * zhengHua : 3.0
     */

    private String id;
    private double qiuGou;
    private double sanHua;
    private double zhengHua;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getQiuGou() {
        return qiuGou;
    }

    public void setQiuGou(double qiuGou) {
        this.qiuGou = qiuGou;
    }

    public double getSanHua() {
        return sanHua;
    }

    public void setSanHua(double sanHua) {
        this.sanHua = sanHua;
    }

    public double getZhengHua() {
        return zhengHua;
    }

    public void setZhengHua(double zhengHua) {
        this.zhengHua = zhengHua;
    }
}
