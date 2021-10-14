package com.ktw.fly.wallet;

import android.Manifest;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.example.qrcode.utils.CommonUtils;
import com.j256.ormlite.stmt.query.In;
import com.ktw.fly.FLYApplication;
import com.ktw.fly.R;
import com.ktw.fly.sp.UserSp;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.util.DisplayUtil;
import com.ktw.fly.util.ToastUtil;
import com.ktw.fly.wallet.adapter.SelectItemAdapter;
import com.ktw.fly.wallet.bean.CurrencyBean;
import com.ktw.fly.wallet.bean.WalletListBean;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class CoinActivity extends BaseActivity {

    private TextView mSelectCurrencyTv, mCurrencyTv, mSaveTv,
            mTrcTv, mErcTv, mTitleTv, mEtcTv,
            mAddressTv, mAddress2Tv, mCopyTv, mTipTv, mTip2Tv, mUidTv;
    private ImageView mCodeIv, mCode2Iv, mImgIv;
    private RecyclerView mListView;
    private LinearLayoutCompat mSaveView, mDrawLayout, mCurrencyLayout;
    private DrawerLayout mMainLayout;

    private String chainName;

    private List<CurrencyBean> mData;

    private SelectItemAdapter mAdapter;

    private CurrencyBean item;

    public static void actionStart(Context context, List<CurrencyBean> data, String name) {
        Intent intent = new Intent(context, CoinActivity.class);
        intent.putExtra("data", (Serializable) data);
        intent.putExtra("name", name);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_layout);
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.coin));
        ImageView viewById = findViewById(R.id.iv_title_right);
        viewById.setImageResource(R.mipmap.ic_record);
        viewById.setOnClickListener(v -> {
            //充币记录
            startActivity(new Intent(this, RecordActivity.class));
        });
        initView();
        initBundle();
        initLayout();
        initRv();
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

    /**
     * 获取地址
     *
     * @param currencyName
     */
    private void getAddressData(String currencyName) {
        if (TextUtils.isEmpty(chainName)) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("userId", UserSp.getInstance(this).getUserId(""));
        params.put("protocol", chainName);
        params.put("coinKey", currencyName);
        HttpUtils.post().url(Apis.CURRENCY_ADDRESS)
                .params(params)
                .build()
                .execute(new BaseCallback<WalletListBean>(WalletListBean.class) {

                    @Override
                    public void onResponse(ObjectResult<WalletListBean> result) {
                        if (result == null) {
                            return;
                        }
                        if (result.getResultCode() != 1) {
                            ToastUtil.showToast(CoinActivity.this, result.getMsg());
                            mAddressTv.setText("");
                            mAddress2Tv.setText("");
                            mCodeIv.setImageBitmap(null);
                            mCode2Iv.setImageBitmap(null);
                            return;
                        }

                        WalletListBean data = result.getData();

                        if (data == null) {
                            return;
                        }

                        String address = data.getAddress();
                        if (TextUtils.isEmpty(address)) {
                            return;
                        }

//                        Bitmap bitmap1 = CommonUtils.createQRCode(address,
//                                DisplayUtil.dip2px(FLYApplication.getContext(), 200),
//                                DisplayUtil.dip2px(FLYApplication.getContext(), 200));

                        Bitmap bitmap2 = CommonUtils.createQRCode(address,
                                DisplayUtil.dip2px(FLYApplication.getContext(), 200),
                                DisplayUtil.dip2px(FLYApplication.getContext(), 200));

                        mCodeIv.setImageBitmap(bitmap2);
                        mCode2Iv.setImageBitmap(bitmap2);
                        mAddressTv.setText(address);
                        mAddress2Tv.setText(address);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(CoinActivity.this);
                        mAddressTv.setText("");
                        mAddress2Tv.setText("");
                        mCodeIv.setImageBitmap(null);
                        mCode2Iv.setImageBitmap(null);
                    }
                });

    }


    private void initView() {
        mSelectCurrencyTv = findViewById(R.id.tv_select_currency);
        mCurrencyTv = findViewById(R.id.tv_currency);
        mTrcTv = findViewById(R.id.tv_trc);
        mErcTv = findViewById(R.id.tv_erc);
        mEtcTv = findViewById(R.id.tv_etc);
        mSaveTv = findViewById(R.id.tv_save);
        mAddressTv = findViewById(R.id.tv_address);
        mAddress2Tv = findViewById(R.id.tv_address_2);
        mCopyTv = findViewById(R.id.tv_copy);
        mTipTv = findViewById(R.id.tv_1);
        mUidTv = findViewById(R.id.tv_uid);
        mCodeIv = findViewById(R.id.iv_code);
        mImgIv = findViewById(R.id.iv_img);
        mListView = findViewById(R.id.recycler_view);
        mTip2Tv = findViewById(R.id.tv_2);
        mCode2Iv = findViewById(R.id.iv_code_2);
        mSaveView = findViewById(R.id.ll_save_view);
        mTitleTv = findViewById(R.id.tv_title);
        mDrawLayout = findViewById(R.id.dialog_layout);
        mMainLayout = findViewById(R.id.draw_layout);
        mCurrencyLayout = findViewById(R.id.ll_currency);
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
        mUidTv.setText("UID:" + UserSp.getInstance(this).getUserId("").replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
    }

    private void initDataLayout(CurrencyBean item) {
        Glide.with(CoinActivity.this).load(item.getPath()).into(mImgIv);
        mCurrencyTv.setText(item.getCurrencyName());
        String s = mTip2Tv.getText().toString();
        String s1 = mTipTv.getText().toString();

        mTip2Tv.setText(s.replace(s.substring(s.indexOf(":") + 1, s.indexOf(",")), item.getMinCoinNumber() + " " + item.getCurrencyName()));
        mTipTv.setText(s1.replace(s1.substring(s.indexOf(":") + 1, s1.indexOf(",")), item.getMinCoinNumber() + " " + item.getCurrencyName()));
        mTitleTv.setText(mTitleTv.getText().toString().replace("USDT", item.getCurrencyName()));

        String s2 = mTip2Tv.getText().toString();
        String s3 = mTipTv.getText().toString();

        SpannableString spannableString = new SpannableString(s2);
        spannableString.setSpan(new ForegroundColorSpan(getColor(R.color.home_blue)), s2.indexOf("."), s2.indexOf(","), 0);
        mTip2Tv.setText(spannableString);

        SpannableString spannableString1 = new SpannableString(s3);
        spannableString1.setSpan(new ForegroundColorSpan(getColor(R.color.home_blue)), s3.indexOf("."), s3.indexOf(","), 0);
        mTipTv.setText(spannableString1);

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
        getAddressData(item.getCurrencyName());
    }

    private void initLayout() {

        mSelectCurrencyTv.setOnClickListener(v -> mMainLayout.openDrawer(mDrawLayout));

        mCopyTv.setOnClickListener(v -> {
            if (mAddressTv.getText().length() == 0) {
                return;
            }
            //复制UID
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setText(mAddressTv.getText().toString());
            ToastUtil.showToast(this, getString(R.string.z_tv_copy_success));
        });

        mSaveTv.setOnClickListener(v -> {
            if (TextUtils.isEmpty(mAddressTv.getText().toString())) {
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                savePic();
            }
        });

        mTrcTv.setOnClickListener(v -> {
            chainName = mTrcTv.getText().toString().toUpperCase();
            mTrcTv.setSelected(true);
            mErcTv.setSelected(false);
            mEtcTv.setSelected(false);
            mTrcTv.setBackgroundResource(R.drawable.bg_btn_blue);
            mErcTv.setBackgroundResource(R.drawable.bg_btn_gay);
            mEtcTv.setBackgroundResource(R.drawable.bg_btn_gay);
            getAddressData(item.getCurrencyName());
        });

        mErcTv.setOnClickListener(v -> {
            chainName = mErcTv.getText().toString().toUpperCase();
            mErcTv.setSelected(true);
            mTrcTv.setSelected(false);
            mEtcTv.setSelected(false);
            mErcTv.setBackgroundResource(R.drawable.bg_btn_blue);
            mTrcTv.setBackgroundResource(R.drawable.bg_btn_gay);
            mEtcTv.setBackgroundResource(R.drawable.bg_btn_gay);
            getAddressData(item.getCurrencyName());
        });

        mEtcTv.setOnClickListener(v -> {
            chainName = mEtcTv.getText().toString().toUpperCase();
            mEtcTv.setSelected(true);
            mErcTv.setSelected(false);
            mTrcTv.setSelected(false);
            mEtcTv.setBackgroundResource(R.drawable.bg_btn_blue);
            mErcTv.setBackgroundResource(R.drawable.bg_btn_gay);
            mTrcTv.setBackgroundResource(R.drawable.bg_btn_gay);
            getAddressData(item.getCurrencyName());
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    savePic();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 保存图片
     */
    private void savePic() {
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        saveBitmap(view2Bitmap(mSaveView), format.format(new Date()) + ".JPEG");
    }

    /**
     * view转bitmap
     *
     * @param view 视图
     * @return bitmap
     */
    private Bitmap view2Bitmap(final View view) {
        if (view == null) return null;
        Bitmap ret = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(ret);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return ret;
    }

    /*
     * 保存文件，文件名为当前日期
     */
    private void saveBitmap(Bitmap bitmap, String bitName) {
        String fileName;
        File file;
        if (Build.BRAND.equalsIgnoreCase("xiaomi")) { // 小米手机
            fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + bitName;
        } else { // Meizu 、Oppo
            fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/" + bitName;
        }
        file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            // 格式为 JPEG，照相机拍出的图片为JPEG格式的，PNG格式的不能显示在相册中
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush();
                out.close();
                ToastUtil.showToast(this, getString(R.string.tv_save));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 发送广播，通知刷新图库的显示
        this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + fileName)));
    }


}