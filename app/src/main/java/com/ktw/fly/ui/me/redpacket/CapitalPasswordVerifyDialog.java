package com.ktw.fly.ui.me.redpacket;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ktw.fly.FLYAppConstant;
import com.ktw.fly.R;
import com.ktw.fly.util.ScreenUtil;
import com.ktw.fly.view.PasswordInputView;

/**
 * 资金密码验证Dialog
 */
public class CapitalPasswordVerifyDialog extends Dialog {
    private TextView tvAction;
    private View llMoney;
    private TextView tvMoney;


    private String action;
    private String money;

    private OnInputFinishListener onInputFinishListener;
    private TextView capitalNameText;
    private String capitalName;

    public CapitalPasswordVerifyDialog(@NonNull Context context) {
        super(context, R.style.MyDialog);
    }

    public CapitalPasswordVerifyDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CapitalPasswordVerifyDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capital_password_verify_dialog);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView() {
        findViewById(R.id.ivClose).setOnClickListener(v -> {
            cancel();
        });
        tvAction = findViewById(R.id.tvAction);
        if (action != null) {
            tvAction.setText(action);
        }
        llMoney = findViewById(R.id.llMoney);
        tvMoney = findViewById(R.id.tvMoney);
        if (!TextUtils.isEmpty(money)) {
            tvMoney.setText(formatMoney(money));
            llMoney.setVisibility(View.VISIBLE);
        } else {
            llMoney.setVisibility(View.GONE);
        }

        capitalNameText = findViewById(R.id.tv_capital_name);

        if (capitalName != null) {
            capitalNameText.setText(capitalName);
        }

        EditText passwordEdit = findViewById(R.id.et_password);

        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });


        findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordEdit.getText().toString();
                onInputFinishListener.onInputFinish(password);
                dismiss();
            }
    });

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = (int) (ScreenUtil.getScreenWidth(getContext()) * 0.9);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public void setAction(String action) {
        this.action = action;
        if (tvAction != null) {
            tvAction.setText(action);
        }
    }

    public void setMoney(String money) {
        this.money = money;
        if (tvMoney != null) {
            tvMoney.setText(formatMoney(money));
        }
        if (llMoney != null) {
            if (!TextUtils.isEmpty(money)) {
                llMoney.setVisibility(View.VISIBLE);
            } else {
                llMoney.setVisibility(View.GONE);
            }
        }
    }

    public void setCapitalName(String capitalName) {
        this.capitalName = capitalName;
        if (capitalNameText != null) {
            capitalNameText.setText(capitalName);
        }
    }

    public void setOnInputFinishListener(OnInputFinishListener onInputFinishListener) {
        this.onInputFinishListener = onInputFinishListener;
    }


    public interface OnInputFinishListener {
        void onInputFinish(String password);
    }


    private String formatMoney(String money) {
        try {
            if (money.contains(".")) {
                for (int i = 0; i < money.length(); i++) {
                    int indMinPrice = money.indexOf(".");
                    String subMinPrice = money.substring(indMinPrice);
                    if (subMinPrice.length() - 1 == 1) {
                        return money + "0";
                    } else {
                        return money;
                    }
                }
            } else {
                return money + ".00";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0.00";
    }
}
