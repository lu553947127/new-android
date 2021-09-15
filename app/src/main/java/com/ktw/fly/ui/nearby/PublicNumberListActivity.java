package com.ktw.fly.ui.nearby;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ktw.fly.FLYAppConfig;
import com.ktw.fly.FLYAppConstant;
import com.ktw.fly.R;
import com.ktw.fly.adapter.UserAdapter;
import com.ktw.fly.bean.User;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.other.BasicInfoActivity;
import com.ktw.fly.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

public class PublicNumberListActivity extends BaseActivity {
    private PullToRefreshListView mPullToRefreshListView;
    private List<User> mUsers;
    private UserAdapter mAdapter;
    private int mPageIndex = 0;
    private String mKeyWord;// 关键字(keyword)

    public static void start(Context ctx, String keyWord) {
        Intent intent = new Intent(ctx, PublicNumberListActivity.class);
        intent.putExtra("keyWord", keyWord);
        ctx.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_pullrefresh_list);
        mKeyWord = getIntent().getStringExtra("keyWord");
        mUsers = new ArrayList<User>();
        mAdapter = new UserAdapter(mUsers, this);
        initAction();
        initView();
    }

    private void initAction() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.search));
    }

    private void initView() {
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        View emptyView = LayoutInflater.from(mContext).inflate(R.layout.layout_list_empty_view, null);
        mPullToRefreshListView.setEmptyView(emptyView);
        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);
        mPullToRefreshListView.setShowIndicator(false);

        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestData(true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestData(false);
            }
        });

        mPullToRefreshListView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, BasicInfoActivity.class);
                intent.putExtra(FLYAppConstant.EXTRA_USER_ID, mUsers.get((int) id).getUserId());
                startActivity(intent);
            }
        });
        requestData(true);
    }

    private void requestData(final boolean isPullDwonToRefersh) {
        if (isPullDwonToRefersh) {
            mPageIndex = 0;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("page", String.valueOf(mPageIndex));
        params.put("limit", String.valueOf(FLYAppConfig.PAGE_SIZE));
        params.put("keyWorld", mKeyWord);

        DialogHelper.showDefaulteMessageProgressDialogAddCancel(this, null);

        HttpUtils.get().url(coreManager.getConfig().PUBLIC_SEARCH)
                .params(params)
                .build()
                .execute(new ListCallback<User>(User.class) {
                    @Override
                    public void onResponse(ArrayResult<User> result) {
                        DialogHelper.dismissProgressDialog();
                        mPageIndex++;
                        if (isPullDwonToRefersh) {
                            mUsers.clear();
                        }
                        List<User> datas = result.getData();
                        if (datas != null && datas.size() > 0) {
                            mUsers.addAll(datas);
                        }
                        mAdapter.notifyDataSetChanged();
                        mPullToRefreshListView.onRefreshComplete();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Log.e("zx", "onError: " + e.toString());
                        ToastUtil.showNetError(PublicNumberListActivity.this);
                    }
                });
    }
}
