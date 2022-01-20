package com.ktw.bitbit.ui.me.emot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.MyCollectEmotPackageBean;
import com.ktw.bitbit.broadcast.OtherBroadcast;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.util.ToastUtil;
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
 * 设置表情包
 */
public class MyEmotPackageActivity extends BaseActivity {

    private ListView mListView;
    private MyEmotAdapter adapter;
    private List<MyCollectEmotPackageBean> emotBeanList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_emot_package);

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
        tvTitle.setText("我的表情");

    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.lg_lv);
        adapter = new MyEmotAdapter(this, emotBeanList);
        mListView.setAdapter(adapter);

    }

    private void initEvent() {
        findViewById(R.id.rlSingleEmot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyEmotPackageActivity.this, AddSingleEmotPackageActivity.class);
                startActivity(intent);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyCollectEmotPackageBean emotBean = emotBeanList.get(position);
                if (emotBean != null) {
//                    AddEmotPackageActivity.start(MyEmotPackageActivity.this,emotBean.getFaceName(),emotBean.getFaceId(),emotBean.get);
                }
            }
        });

        adapter.setDelMyEmotPackageInterface(new MyEmotAdapter.IDelMyEmotPackageInterface() {
            @Override
            public void delEmot(int position) {
                MyCollectEmotPackageBean emotBean = emotBeanList.get(position);
                if (emotBean.getFace() != null) {
                    delEmotPackage(emotBean.getFace().getName(), position);
                }
            }
        });

        getMyEmotPackage();
    }

    private void delEmotPackage(String name, int position) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("name", name);
        HttpUtils.get().url(coreManager.getConfig().API_FACE_NAME_DELETE)
                .params(params)
                .build()
                .execute(new ListCallback<MyEmotBean>(MyEmotBean.class) {
                    @Override
                    public void onResponse(ArrayResult<MyEmotBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    emotBeanList.remove(position);
                                    adapter.notifyDataSetChanged();
                                    ToastUtil.showToast(MyEmotPackageActivity.this, "移除成功");

                                    //更新缓存
                                    FLYApplication.emotMap.remove(name);

                                    // 发送广播去界面更新
                                    Intent intent = new Intent(OtherBroadcast.SYNC_EMOT_PACKAGE_REMOVE);
                                    intent.putExtra("name", name);
                                    mContext.sendBroadcast(intent);
                                }
                            });
                        }

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(MyEmotPackageActivity.this);
                    }
                });
    }

    private void getMyEmotPackage() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("type", 0 + "");
        HttpUtils.get().url(coreManager.getConfig().API_FACE_COLLECT_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<MyCollectEmotPackageBean>(MyCollectEmotPackageBean.class) {
                    @Override
                    public void onResponse(ArrayResult<MyCollectEmotPackageBean> result) {
                        DialogHelper.dismissProgressDialog();
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

                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(MyEmotPackageActivity.this);
                    }
                });
    }

}