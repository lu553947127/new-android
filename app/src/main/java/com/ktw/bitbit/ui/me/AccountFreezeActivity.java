package com.ktw.bitbit.ui.me;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.ktw.bitbit.R;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.util.RegexUtils;
import com.ktw.bitbit.util.SkinUtils;
import com.ktw.bitbit.util.ToastUtil;

/**
 * 账号冻结与解冻 步骤一
 * Created by Harvey on 2/6/21.
 **/
public class AccountFreezeActivity extends BaseActivity {
    private int mType;
    private TextView mTitleTv;
    private EditText mPhoneEdt, mRealNameEdt, mIdCardEdt;
    private Button mNextBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_freeze);
        initActionBar();
        initView();
        initData();

    }

    private void initView() {
        mPhoneEdt = findViewById(R.id.edt_phone);
        mRealNameEdt = findViewById(R.id.edt_real_name);
        mIdCardEdt = findViewById(R.id.edt_id_card);
        mNextBtn = findViewById(R.id.btn_next);
        mNextBtn.setOnClickListener(v -> {
            if (regexText()) {
                String phone = mPhoneEdt.getText().toString();
                String realName = mRealNameEdt.getText().toString();
                String idCard = mIdCardEdt.getText().toString();
                Intent intent = new Intent(this, FreezeMessageActivity.class);
                intent.putExtra(FreezeMessageActivity.INTENT_PHONE, phone);
                intent.putExtra(FreezeMessageActivity.INTENT_REAL_NAME, realName);
                intent.putExtra(FreezeMessageActivity.INTENT_ID_CARD, idCard);
                intent.putExtra(AccountOperateActivity.TYPE_OPERATE, mType);
                startActivity(intent);

            }

        });
        ViewCompat.setBackgroundTintList(mNextBtn, ColorStateList.valueOf(SkinUtils.getSkin(this).getAccentColor()));
    }

    private void initData() {
        mType = getIntent().getIntExtra(AccountOperateActivity.TYPE_OPERATE, 0);
        if (mType == AccountOperateActivity.OPERATE_FREEZE_ACCOUNT) {
            mTitleTv.setText(R.string.operate_freeze_title);

        } else if (mType == AccountOperateActivity.OPERATE_UNFREEZE_ACCOUNT) {
            mTitleTv.setText(R.string.operate_unfreeze_title);
        }
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(v -> finish());
        mTitleTv = findViewById(R.id.tv_title_center);


    }

    public boolean regexText() {
        String phone = mPhoneEdt.getText().toString();
        String realName = mRealNameEdt.getText().toString();
        String idCard = mIdCardEdt.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            ToastUtil.showToast(this, "请输入手机号码");
            return false;
        }
        if (TextUtils.isEmpty(realName)) {
            ToastUtil.showToast(this, "真实姓名不能为空");
            return false;
        }
        if (!RegexUtils.checkIdCard(idCard)) {
            ToastUtil.showToast(this, "请输入正确格式的身份证号码");
            return false;
        }
        return true;
    }
}
