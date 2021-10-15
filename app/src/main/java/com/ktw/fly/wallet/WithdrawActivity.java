package com.ktw.fly.wallet;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.example.qrcode.Constant;
import com.example.qrcode.ScannerActivity;
import com.ktw.fly.FLYAppConfig;
import com.ktw.fly.FLYAppConstant;
import com.ktw.fly.R;
import com.ktw.fly.bean.User;
import com.ktw.fly.helper.RedPacketHelper;
import com.ktw.fly.sp.UserSp;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.me.capital.CapitalPasswordActivity;
import com.ktw.fly.ui.me.redpacket.SendRedPacketActivity;
import com.ktw.fly.util.DisplayUtil;
import com.ktw.fly.util.Md5Util;
import com.ktw.fly.util.ToastUtil;
import com.ktw.fly.util.secure.LoginPassword;
import com.ktw.fly.wallet.adapter.SelectItemAdapter;
import com.ktw.fly.wallet.bean.CoinBean;
import com.ktw.fly.wallet.bean.CurrencyBean;
import com.ktw.fly.wallet.bean.VerifyBean;
import com.ktw.fly.wallet.bean.WalletListBean;
import com.ktw.fly.wallet.dialog.VerifyBottomDialog;
import com.ktw.fly.wallet.dialog.VerifyCenterDialog;
import com.ktw.fly.wallet.utils.BaseTextWatcher;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 提币页面
 */
public class WithdrawActivity extends BaseActivity {

    private TextView mSelectCurrencyTv, mCurrencyTv, mConfirmTv, mEtcTv, mTipTv,
            mTrcTv, mErcTv, mAllTv, mAvailableTv, mFeeTv, mUnitTv;
    private ImageView mImgIv, mScanIv;
    private EditText mNumberEt, mAddressEt;
    private LinearLayoutCompat mDrawLayout, mCurrencyLayout;
    private DrawerLayout mMainLayout;
    private RecyclerView mListView;

    private List<CurrencyBean> mData;
    private String chainName;
    private SelectItemAdapter mAdapter;
    private CurrencyBean item;

    /**
     * 绑定状态 1：手机 2：邮箱  3：都绑定
     */
    private int mBindType = -1;

    public static void actionStart(Context context, List<CurrencyBean> data,String name) {
        Intent intent = new Intent(context, WithdrawActivity.class);
        intent.putExtra("data", (Serializable) data);
        intent.putExtra("name", name);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw_layout);
        initTitle();
        initView();
        initBundle();
        initLayout();
        initRv();
        getBindType();
    }

    private void initLayout() {
        mSelectCurrencyTv.setOnClickListener(v -> mMainLayout.openDrawer(mDrawLayout));

        mTrcTv.setOnClickListener(v -> {
            chainName = mTrcTv.getText().toString().toUpperCase();
            mTrcTv.setSelected(true);
            mErcTv.setSelected(false);
            mEtcTv.setSelected(false);
            mTrcTv.setBackgroundResource(R.drawable.bg_btn_blue);
            mErcTv.setBackgroundResource(R.drawable.bg_btn_gay);
            mEtcTv.setBackgroundResource(R.drawable.bg_btn_gay);
        });

        mErcTv.setOnClickListener(v -> {
            chainName = mErcTv.getText().toString().toUpperCase();
            mErcTv.setSelected(true);
            mTrcTv.setSelected(false);
            mEtcTv.setSelected(false);
            mErcTv.setBackgroundResource(R.drawable.bg_btn_blue);
            mTrcTv.setBackgroundResource(R.drawable.bg_btn_gay);
            mEtcTv.setBackgroundResource(R.drawable.bg_btn_gay);
        });

        mEtcTv.setOnClickListener(v -> {
            chainName = mEtcTv.getText().toString().toUpperCase();
            mEtcTv.setSelected(true);
            mErcTv.setSelected(false);
            mTrcTv.setSelected(false);
            mEtcTv.setBackgroundResource(R.drawable.bg_btn_blue);
            mErcTv.setBackgroundResource(R.drawable.bg_btn_gay);
            mTrcTv.setBackgroundResource(R.drawable.bg_btn_gay);
        });

        mScanIv.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
//                Intent intent = new Intent(this, CaptureActivity.class);
//                startActivityForResult(intent, 200);
                startSan();
            }
        });

        mAllTv.setOnClickListener(v -> {
            //全部
            mNumberEt.setText(item.getFreezeAssets());
            mNumberEt.setSelection(mNumberEt.getText().length());
        });

        mNumberEt.addTextChangedListener(new BaseTextWatcher());

        mConfirmTv.setOnClickListener(v -> {
            queryPwd();
        });
    }

    /**
     * 查询是否设置资金密码
     */
    private void queryPwd() {
        String userId = coreManager.getSelf().getUserId();
        RedPacketHelper.detectionCapitalPassword(getApplication(), coreManager, userId,
                error -> {
                    Intent intent = new Intent(this, CapitalPasswordActivity.class);
                    startActivity(intent);
                },
                success -> {
                    verifyPwd();
                });
    }

    /**
     * 一设置资金密码
     */
    private void verifyPwd() {
        if (mBindType == -1) {
            getBindType();
            return;
        }
        //提币
        if (mBindType != 3) {
            //没有双绑，直接发验证码
            VerifyBottomDialog.newInstance(mBindType)
                    .setCallBack(new VerifyBottomDialog.VerifyCallBack() {
                        @Override
                        public void onVerifyCallBackClicked(String pwd, String code) {
                            //开始提币
                            verifyCode(mBindType, code, pwd);
                        }

                        @Override
                        public void onSendMsgClicked(VerifyBottomDialog dialog) {
                            //发送验证码
                            sendCode(mBindType, dialog);
                        }
                    }).show(getSupportFragmentManager(), "Verify");
        } else {
            //双绑定 给出选择机会
            VerifyCenterDialog.newInstance().setCallBack(type -> VerifyBottomDialog.newInstance(type)
                    .setCallBack(new VerifyBottomDialog.VerifyCallBack() {
                        @Override
                        public void onVerifyCallBackClicked(String pwd, String code) {
                            //开始提币
                            verifyCode(type, code, pwd);
                        }

                        @Override
                        public void onSendMsgClicked(VerifyBottomDialog dialog) {
                            //发送验证码
                            sendCode(type, dialog);
                        }
                    }).show(getSupportFragmentManager(), "Verify"));

        }
    }


    private void startSan() {
        Intent intent = new Intent(this, ScannerActivity.class);
        // 设置扫码框的宽
        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_WIDTH, DisplayUtil.dip2px(this, 200));
        // 设置扫码框的高
        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_HEIGHT, DisplayUtil.dip2px(this, 200));
        // 设置扫码框距顶部的位置
        intent.putExtra(Constant.EXTRA_SCANNER_FRAME_TOP_PADDING, DisplayUtil.dip2px(this, 100));
        // 可以从相册获取
        intent.putExtra(Constant.EXTRA_IS_ENABLE_SCAN_FROM_PIC, true);
        startActivityForResult(intent, 200);
    }

    private void initBundle() {
        mData = (List<CurrencyBean>) getIntent().getSerializableExtra("data");
        if (TextUtils.isEmpty(getIntent().getStringExtra("name"))) {
            item = mData.get(0);
        } else {
            String name = getIntent().getStringExtra("name");

            for (int i = 0; i < mData.size(); i++) {
                CurrencyBean currencyBean = mData.get(i);
                if (currencyBean.getCurrencyName().equalsIgnoreCase(name)) {
                    item = currencyBean;
                }
            }
        }
        item.setSelect(true);
        initDataLayout(item);
    }

    private void initView() {
        mConfirmTv = findViewById(R.id.tv_confirm);
        mAllTv = findViewById(R.id.tv_all);
        mTipTv = findViewById(R.id.tv_1);
        mAvailableTv = findViewById(R.id.tv_available);
        mFeeTv = findViewById(R.id.tv_fee);
        mUnitTv = findViewById(R.id.tv_unit);
        mScanIv = findViewById(R.id.iv_scan);
        mNumberEt = findViewById(R.id.et_number);
        mAddressEt = findViewById(R.id.et_address);

        mSelectCurrencyTv = findViewById(R.id.tv_select_currency);
        mCurrencyTv = findViewById(R.id.tv_currency);
        mTrcTv = findViewById(R.id.tv_trc);
        mErcTv = findViewById(R.id.tv_erc);
        mEtcTv = findViewById(R.id.tv_etc);
        mImgIv = findViewById(R.id.iv_img);
        mListView = findViewById(R.id.recycler_view);

        mDrawLayout = findViewById(R.id.dialog_layout);
        mMainLayout = findViewById(R.id.draw_layout);
        mCurrencyLayout = findViewById(R.id.ll_currency);
    }

    private void initRv() {
        findViewById(R.id.iv_cancel)
                .setOnClickListener(v -> mMainLayout.closeDrawer(mDrawLayout));
        mAdapter = new SelectItemAdapter();
        mListView.setLayoutManager(new LinearLayoutManager(this));
        mListView.setAdapter(mAdapter);
        mAdapter.setNewInstance(mData);

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull @NotNull BaseQuickAdapter<?, ?> adapter, @NonNull @NotNull View view, int position) {
                mMainLayout.closeDrawer(mDrawLayout);
                for (CurrencyBean data : mAdapter.getData()) {
                    data.setSelect(false);
                }
                item = mAdapter.getItem(position);
                item.setSelect(true);
                initDataLayout(item);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initDataLayout(CurrencyBean item) {
        Glide.with(this).load(item.getPath()).into(mImgIv);
        mCurrencyTv.setText(item.getCurrencyName());
        String s1 = mTipTv.getText().toString();
        mNumberEt.setHint(getString(R.string.tv_withdraw_number) + " " + item.getMinWithdrawNumber() + " " + item.getCurrencyName());
        //提现数量
        mTipTv.setText(s1.replace(s1.substring(s1.indexOf(":") + 1), item.getMinWithdrawNumber() + " " + item.getCurrencyName()));
        mUnitTv.setText(item.getCurrencyName());
        //可用
        String available = mAvailableTv.getText().toString();
        mAvailableTv.setText(available.replace(available.substring(available.indexOf(":") + 1), item.getFreezeAssets()) + item.getCurrencyName());
        //手续费
        String fee = mFeeTv.getText().toString();
        mFeeTv.setText(fee.replace(fee.substring(fee.indexOf(":") + 1), item.getF11()));

        String[] split = item.getType().split(",");

        if (split.length == 1) {
            mCurrencyLayout.setVisibility(View.GONE);
        } else if (split.length == 2) {
            mCurrencyLayout.setVisibility(View.VISIBLE);
            mEtcTv.setVisibility(View.GONE);
            mTrcTv.setText(split[0]);
            mErcTv.setText(split[1]);
        } else {
            mCurrencyLayout.setVisibility(View.VISIBLE);
            mEtcTv.setVisibility(View.VISIBLE);
            mEtcTv.setText(split[2]);
            mTrcTv.setText(split[0]);
            mErcTv.setText(split[1]);
        }
        chainName = split[0];
        mTrcTv.setSelected(true);
        mErcTv.setSelected(false);
        mEtcTv.setSelected(false);
        mTrcTv.setBackgroundResource(R.drawable.bg_btn_blue);
        mErcTv.setBackgroundResource(R.drawable.bg_btn_gay);
        mEtcTv.setBackgroundResource(R.drawable.bg_btn_gay);
    }

    private void initTitle() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.tv_withdraw));
        ImageView viewById = findViewById(R.id.iv_title_right);
        viewById.setImageResource(R.mipmap.ic_record);
        viewById.setOnClickListener(v -> {
            //提币记录
            startActivity(new Intent(this, RecordActivity.class));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null) {
//            String result = data.getStringExtra("result");
            String result = data.getExtras().getString(Constant.EXTRA_RESULT_CONTENT);
            if (!TextUtils.isEmpty(result)) {
                mAddressEt.setText(result);
                mAddressEt.setSelection(result.length());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, CaptureActivity.class);
                    startActivityForResult(intent, 200);
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 提币
     */
    private void requestData() {
        String number = mNumberEt.getText().toString();
        String address = mAddressEt.getText().toString();
        if (TextUtils.isEmpty(chainName)) {
            return;
        }
        if (TextUtils.isEmpty(number) || TextUtils.isEmpty(address)) {
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", UserSp.getInstance(this).getUserId(""));
        params.put("toAddress", address);
        params.put("toCurrencyNumber", number);
        params.put("currencyId", item.getF01());
        params.put("currencyTypeName", chainName);
        HttpUtils.post().url(Apis.WITHDRAW_OP)
                .params(params)
                .build()
                .execute(new BaseCallback<Object>(Object.class) {

                    @Override
                    public void onResponse(ObjectResult<Object> result) {
                        if (result == null) {
                            return;
                        }
                        ToastUtil.showToast(WithdrawActivity.this, result.getMsg());
                        if (result.getResultCode() == 1) {
                            requestWalletListData();
                            mNumberEt.setText("");
                            mAddressEt.setText("");
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(WithdrawActivity.this);
                    }
                });
    }

    /**
     * 获取币种信息
     */
    private void requestWalletListData() {
        Map<String, String> params = new HashMap<>();
        params.put("userId", UserSp.getInstance(this).getUserId(""));
        HttpUtils.post().url(Apis.USER_ASSET)
                .params(params)
                .build()
                .execute(new BaseCallback<WalletListBean>(WalletListBean.class) {

                    @Override
                    public void onResponse(ObjectResult<WalletListBean> result) {
                        if (result == null) {
                            return;
                        }
                        if (result.getResultCode() != 1) {
                            ToastUtil.showToast(WithdrawActivity.this, result.getMsg());
                            return;
                        }

                        WalletListBean data = result.getData();

                        if (data == null) {
                            return;
                        }


                        List<CurrencyBean> list = data.getList();

                        if (list == null || list.size() == 0) {
                            return;
                        }
                        mData.clear();
                        mData.addAll(list);
                        for (CurrencyBean c : mData) {
                            if (item.getCurrencyName().equalsIgnoreCase(c.getCurrencyName())) {
                                item = c;
                                initDataLayout(item);
                                return;
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(WithdrawActivity.this);
                    }
                });
    }

    /**
     * 提币
     *
     * @param type
     * @param dialog
     */
    public void sendCode(int type, VerifyBottomDialog dialog) {
        Map<String, String> params = new HashMap<>();
        params.put("userId", UserSp.getInstance(this).getUserId(""));
        params.put("type", type + "");
        HttpUtils.post().url(Apis.SEND_CODE)
                .params(params)
                .build()
                .execute(new BaseCallback<Object>(Object.class) {

                    @Override
                    public void onResponse(ObjectResult<Object> result) {
                        if (result == null) {
                            return;
                        }
                        if (result.getResultCode() != 1) {
                            ToastUtil.showToast(WithdrawActivity.this, result.getResultMsg());
                            return;
                        }
                        ToastUtil.showToast(WithdrawActivity.this, getString(R.string.verification_code_send_success));
                        dialog.onSuccessSend();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(WithdrawActivity.this);
                    }
                });
    }

    /**
     * 验证
     */
    private void verifyCode(int type, String code, String pwd) {
        String number = mNumberEt.getText().toString();
        String address = mAddressEt.getText().toString();
        if (TextUtils.isEmpty(chainName)) {
            return;
        }
        if (TextUtils.isEmpty(number) || TextUtils.isEmpty(address)) {
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", UserSp.getInstance(this).getUserId(""));
        params.put("type", type + "");
        params.put("code", code);
        params.put("password", LoginPassword.encodeMd5(pwd));
        HttpUtils.post().url(Apis.VERIFY_CODE)
                .params(params)
                .build()
                .execute(new BaseCallback<Object>(Object.class) {

                    @Override
                    public void onResponse(ObjectResult<Object> result) {
                        if (result == null) {
                            return;
                        }
                        if (result.getResultCode() != 1) {
                            ToastUtil.showToast(WithdrawActivity.this, result.getResultMsg());
                            return;
                        }
                        requestData();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(WithdrawActivity.this);
                    }
                });
    }

    /**
     * 获取绑定接口状态
     */
    private void getBindType() {
        Map<String, String> params = new HashMap<>();
        params.put("userId", UserSp.getInstance(this).getUserId(""));
        HttpUtils.post().url(FLYAppConfig.HOST + "user/querybindnumber")
                .params(params)
                .build()
                .execute(new BaseCallback<VerifyBean>(VerifyBean.class) {

                    @Override
                    public void onResponse(ObjectResult<VerifyBean> result) {
                        if (result == null) {
                            return;
                        }
                        if (result.getResultCode() != 1) {
                            ToastUtil.showToast(WithdrawActivity.this, result.getResultMsg());
                            return;
                        }
                        VerifyBean data = result.getData();
                        if (data == null) {
                            return;
                        }
                        if (data.getMailbox() == 1 && data.getPhone() == 1) {
                            mBindType = 3;
                        } else {
                            mBindType = data.getPhone() == 1 ? 1 : 2;
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(WithdrawActivity.this);
                    }
                });
    }

}