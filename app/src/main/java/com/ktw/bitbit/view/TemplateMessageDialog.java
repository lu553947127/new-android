package com.ktw.bitbit.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.SprayPermissionBean;
import com.ktw.bitbit.ui.base.CoreManager;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import okhttp3.Call;

/**
 * @ProjectName: new-android
 * @Package: com.ktw.fly.view
 * @ClassName: TemplateMessageDialog
 * @Description: 模版消息弹出窗
 * @Author: 鹿鸿祥
 * @CreateDate: 2021/12/6 15:17
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/12/6 15:17
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TemplateMessageDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    private TextView tv_title;
    private EditText et_number;
    private EditText et_price;
    private TextView tv_standard;
    private EditText et_standard;
    private LinearLayout ll_two;
    private EditText et_two;
    private EditText et_two2;
    private static CoreManager sCoreManager;
    private double qiugou = 0;
    private double sanhua = 0;
    private double zhenghua = 0;

    public static TemplateMessageDialog newInstance(CoreManager coreManager) {
        sCoreManager = coreManager;
        return new TemplateMessageDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(STYLE_NO_TITLE, R.style.BottomSheetDialog);
        super.onCreate(savedInstanceState);
        getSprayPermission();
    }

    /**
     * 获取设置浪费权限
     */
    private void getSprayPermission() {
        HttpUtils.get().url(sCoreManager.getConfig().SPRAY_PERMISSION)
                .build()
                .execute(new BaseCallback<SprayPermissionBean>(SprayPermissionBean.class) {

                    @Override
                    public void onResponse(ObjectResult<SprayPermissionBean> result) {
                        if (result == null) {
                            return;
                        }

                        qiugou = result.getData().getQiuGou();
                        sanhua = result.getData().getSanHua();
                        zhenghua = result.getData().getZhengHua();
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_template_message, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        Button btnConfirm = view.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);
        tv_title = view.findViewById(R.id.tv_title);
        tv_title.setOnClickListener(this);
        et_number = view.findViewById(R.id.et_number);
        et_price = view.findViewById(R.id.et_price);
        tv_standard = view.findViewById(R.id.tv_standard);
        et_standard = view.findViewById(R.id.et_standard);
        ll_two = view.findViewById(R.id.ll_two);
        et_two = view.findViewById(R.id.et_two);
        et_two2 = view.findViewById(R.id.et_two2);
    }

    private OnClickCustomButtonListener onClickCustomButtonListener;

    public interface OnClickCustomButtonListener {
        void onClick(String message);
    }

    public void setClickListener(OnClickCustomButtonListener onClickCustomButtonListener) {
        this.onClickCustomButtonListener = onClickCustomButtonListener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_title:
                if (tv_title.getText().toString().equals("模版一")){
                    ll_two.setVisibility(View.VISIBLE);
                    tv_standard.setText("起收标准：");
                    et_standard.setVisibility(View.VISIBLE);
                    tv_title.setText("模版二");
                }else {
                    ll_two.setVisibility(View.GONE);
                    tv_standard.setText("起收标准：1");
                    et_standard.setVisibility(View.GONE);
                    tv_title.setText("模版一");
                }
                break;
            case R.id.btn_confirm:
                if (tv_title.getText().toString().equals("模版一")){
                    if (TextUtils.isEmpty(et_number.getText().toString())){
                        ToastUtils.showShort("求购数量不能为空");
                        return;
                    }

                    if (TextUtils.isEmpty(et_price.getText().toString())){
                        ToastUtils.showShort("求购单价不能为空");
                        return;
                    }

                    String text = "\uD83D\uDD25海浪必\uD83D\uDD25\n" +
                            "\uD83C\uDF38诚信收散浪花\uD83C\uDF38\n" +
                            "求购数量：" + et_number.getText().toString() + "\n" +
                            "求购单价：" + et_price.getText().toString() + "\n" +
                            "起收标准：1\n" +
                            "\uD83D\uDCA5地球不灭\uD83D\uDE08海浪到老\uD83D\uDCA5";

                    if (onClickCustomButtonListener != null) {
                        onClickCustomButtonListener.onClick(text);
                        dismiss();
                    }
                }else {
                    if (TextUtils.isEmpty(et_number.getText().toString())){
                        ToastUtils.showShort("求购数量不能为空");
                        return;
                    }

                    if (TextUtils.isEmpty(et_price.getText().toString())){
                        ToastUtils.showShort("求购单价不能为空");
                        return;
                    }

                    if (Double.parseDouble(et_price.getText().toString()) > qiugou){
                        ToastUtils.showShort("求购单价不能超出" + qiugou + "的限制");
                        return;
                    }

                    if (TextUtils.isEmpty(et_standard.getText().toString())){
                        ToastUtils.showShort("起收标准不能为空");
                        return;
                    }

                    if (TextUtils.isEmpty(et_two.getText().toString())){
                        ToastUtils.showShort("散花单价不能为空");
                        return;
                    }

                    if (Double.parseDouble(et_two.getText().toString()) > sanhua){
                        ToastUtils.showShort("散花单价不能超出" + sanhua + "的限制");
                        return;
                    }

                    if (TextUtils.isEmpty(et_two2.getText().toString())){
                        ToastUtils.showShort("整花单价不能为空");
                        return;
                    }

                    if (Double.parseDouble(et_two2.getText().toString()) > zhenghua){
                        ToastUtils.showShort("整花单价不能超出" + zhenghua + "的限制");
                        return;
                    }

                    String text2 = "\uD83D\uDD25海浪必\uD83D\uDD25\n" +
                            "\uD83C\uDF38诚信收浪花\uD83C\uDF38\n" +
                            "求购数量：" + et_number.getText().toString() + "\n" +
                            "求购单价：" + et_price.getText().toString() + "\n" +
                            "起收标准：" + et_standard.getText().toString() + "\n" +
                            "散花单价：" + et_two.getText().toString() + "\n" +
                            "整花单价：" + et_two2.getText().toString() + "\n" +
                            "\uD83D\uDCA5地球不灭\uD83D\uDE08海浪到老\uD83D\uDCA5";

                    if (onClickCustomButtonListener != null) {
                        onClickCustomButtonListener.onClick(text2);
                        dismiss();
                    }
                }
                break;
        }
    }
}
