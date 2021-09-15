package com.ktw.fly.ui.me.emot;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ktw.fly.R;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.util.ToastUtil;
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
 * 新的推荐表情包
 */
public class NewEmotPackageActivity extends BaseActivity {

    private List<EmotBean> emotBeanList = new ArrayList<>();
    private EmotAdapter adapter;
    private SwipeRefreshLayout srlRefresh;
    private GridView gvEmot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_emot_package);
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
        tvTitle.setText("新品推荐");

    }

    private void initView() {
        gvEmot = findViewById(R.id.gvEmot);
        adapter = new EmotAdapter(this, emotBeanList);
        gvEmot.setAdapter(adapter);

        srlRefresh = findViewById(R.id.srlRefresh);
        srlRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary)
                , getResources().getColor(R.color.colorPrimaryDark)
                , getResources().getColor(R.color.colorAccent));
        srlRefresh.post(() -> srlRefresh.setRefreshing(true));
    }

    private void initEvent() {
        srlRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadEmotPackage();
            }
        });
        loadEmotPackage();

        gvEmot.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                EmotBean emotBean = emotBeanList.get(i);
                if (emotBean != null) {
                    AddEmotPackageActivity.start(NewEmotPackageActivity.this, emotBean);
                }
            }
        });
    }

    public void loadEmotPackage() {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("type", 1 + "");
        HttpUtils.get().url(coreManager.getConfig().API_FACE_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<EmotBean>(EmotBean.class) {
                    @Override
                    public void onResponse(ArrayResult<EmotBean> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            if (result.getData() != null && result.getData().size() > 0) {
                                emotBeanList = result.getData();
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
                        ToastUtil.showNetError(NewEmotPackageActivity.this);
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