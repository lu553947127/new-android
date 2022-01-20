package com.ktw.bitbit.ui.me.emot;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.broadcast.OtherBroadcast;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.view.MyGridView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 添加表情包
 */
public class AddEmotPackageActivity extends BaseActivity {

    public final static String EMOT_NAME = "emot";

    private List<String> emotBeanList = new ArrayList<>();
    private EmotDetailAdapter adapter;
    private SwipeRefreshLayout srlRefresh;
    private EmotBean bean;
    private MyGridView gvEmot;
    private TextView tvAdd;

    public static void start(Context ctx, EmotBean emotBean) {
        Intent intent = new Intent(ctx, AddEmotPackageActivity.class);
        intent.putExtra(EMOT_NAME, emotBean);
        ctx.startActivity(intent);
    }

    public static void start(Context ctx, String faceName, String faceId, String desc) {
        EmotBean emotBean = new EmotBean();
        emotBean.setId(faceId);
        emotBean.setName(faceName);
        emotBean.setDesc(desc);
        Intent intent = new Intent(ctx, AddEmotPackageActivity.class);
        intent.putExtra(EMOT_NAME, emotBean);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_emot_package);

        bean = getIntent().getParcelableExtra(EMOT_NAME);
        if (bean == null) {
            finish();
        }

        initActionBar();
        initView();
        initEvent();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        if (!TextUtils.isEmpty(bean.getName())) {
            tvTitle.setText(bean.getName());
        }

    }

    private void initView() {
        gvEmot = findViewById(R.id.gvEmot);
        adapter = new EmotDetailAdapter(this, emotBeanList);
        gvEmot.setAdapter(adapter);

        srlRefresh = findViewById(R.id.srlRefresh);
        srlRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary)
                , getResources().getColor(R.color.colorPrimaryDark)
                , getResources().getColor(R.color.colorAccent));
        srlRefresh.post(() -> srlRefresh.setRefreshing(true));

        TextView tvDesc = findViewById(R.id.tvDesc);
        if (!TextUtils.isEmpty(bean.getDesc())) {
            tvDesc.setText(bean.getDesc());
        } else {
            tvDesc.setText("无");
        }

        tvAdd = findViewById(R.id.tvAdd);
    }

    private void initEvent() {
        srlRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadEmotPackageDetail();
            }
        });

        loadEmotPackageDetail();

        gvEmot.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String path = emotBeanList.get(i);
                if (!TextUtils.isEmpty(path)) {
                    SingleEmotPreviewActivity.start(AddEmotPackageActivity.this, path);
                }
            }
        });
        tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEmot();
            }
        });
    }

    private void addEmot() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("faceName", bean.getName());
        params.put("faceId", bean.getId());
        params.put("url", "");
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HttpUtils.get().url(coreManager.getConfig().API_FACE_COLLECT_ADD)
                .params(params)
                .build()
                .execute(new ListCallback<EmotBean>(EmotBean.class) {
                    @Override
                    public void onResponse(ArrayResult<EmotBean> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            ToastUtil.showToast(AddEmotPackageActivity.this, "添加成功");
                            //存入缓存
                            FLYApplication.emotMap.put(bean.getName(), emotBeanList);

                            // 发送广播去界面更新
                            Intent intent = new Intent(OtherBroadcast.SYNC_EMOT_PACKAGE_ADD);
                            intent.putExtra("name", bean.getName());
                            mContext.sendBroadcast(intent);
                        } else {
                            ToastUtil.showToast(AddEmotPackageActivity.this, result.getResultMsg());
                        }
                        DialogHelper.dismissProgressDialog();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(AddEmotPackageActivity.this);
                        DialogHelper.dismissProgressDialog();
                    }
                });
    }

    public void loadEmotPackageDetail() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", 0 + "");
        params.put("name", bean.getName());
        HttpUtils.get().url(coreManager.getConfig().API_FACE_LIST_NAME_GET)
                .params(params)
                .build()
                .execute(new ListCallback<EmotBean>(EmotBean.class) {
                    @Override
                    public void onResponse(ArrayResult<EmotBean> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            if (result.getData() != null && result.getData().size() > 0) {
                                emotBeanList = result.getData().get(0).getPath();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.setData(emotBeanList);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                        hideRefreshView();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showNetError(AddEmotPackageActivity.this);
                        hideRefreshView();
                    }
                });
    }

    public void hideRefreshView() {
        if (srlRefresh.isRefreshing()) {
            srlRefresh.setRefreshing(false);
        }
    }
}