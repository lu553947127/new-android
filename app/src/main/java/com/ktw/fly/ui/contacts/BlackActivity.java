package com.ktw.fly.ui.contacts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.ktw.fly.FLYAppConstant;
import com.ktw.fly.FLYApplication;
import com.ktw.fly.R;
import com.ktw.fly.FLYReporter;
import com.ktw.fly.adapter.FriendSortAdapter;
import com.ktw.fly.bean.AttentionUser;
import com.ktw.fly.bean.Friend;
import com.ktw.fly.broadcast.CardcastUiUpdateUtil;
import com.ktw.fly.db.dao.FriendDao;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.sortlist.BaseComparator;
import com.ktw.fly.sortlist.BaseSortModel;
import com.ktw.fly.sortlist.SideBar;
import com.ktw.fly.sortlist.SortHelper;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.base.CoreManager;
import com.ktw.fly.ui.other.BasicInfoActivity;
import com.ktw.fly.util.AsyncUtils;
import com.ktw.fly.util.Constants;
import com.ktw.fly.util.PreferenceUtils;
import com.ktw.fly.util.ToastUtil;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 黑名单列表
 */
public class BlackActivity extends BaseActivity {
    private ListView mPullToRefreshListView;
    private FriendSortAdapter mAdapter;
    private List<BaseSortModel<Friend>> mSortFriends;
    private BaseComparator<Friend> mBaseComparator;
    private SideBar mSideBar;
    private TextView mTextDialog;
    private String mLoginUserId;
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CardcastUiUpdateUtil.ACTION_UPDATE_UI)) {
                loadData();
                mAdapter.setData(mSortFriends);
            }
        }
    };

    public BlackActivity() {
        mSortFriends = new ArrayList<BaseSortModel<Friend>>();
        mBaseComparator = new BaseComparator<Friend>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black);
        mLoginUserId = coreManager.getSelf().getUserId();
        initActionBar();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUpdateReceiver);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(R.string.black_list);
    }

    private void initView() {
        mPullToRefreshListView = (ListView) findViewById(R.id.pull_refresh_list);
        mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Friend friend = mSortFriends.get(position).getBean();
                if (friend != null) {
                    Intent intent = new Intent(BlackActivity.this, BasicInfoActivity.class);
                    intent.putExtra(FLYAppConstant.EXTRA_USER_ID, friend.getUserId());
                    startActivity(intent);
                }
            }
        });

        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar.setTextView(mTextDialog);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mPullToRefreshListView.setSelection(position);
                }
            }
        });

        mAdapter = new FriendSortAdapter(this, mSortFriends);
        mPullToRefreshListView.setAdapter(mAdapter);
        getBlackList();

        registerReceiver(mUpdateReceiver, CardcastUiUpdateUtil.getUpdateActionFilter());
    }

    private void loadData() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        AsyncUtils.doAsync(this, e -> {
            FLYReporter.post("加载数据失败，", e);
            AsyncUtils.runOnUiThread(this, ctx -> {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(ctx, R.string.data_exception);
            });
        }, c -> {
            final List<Friend> friends = FriendDao.getInstance().getAllBlacklists(mLoginUserId);
            Map<String, Integer> existMap = new HashMap<>();
            List<BaseSortModel<Friend>> sortedList = SortHelper.toSortedModelList(friends, existMap, Friend::getShowName);
            c.uiThread(r -> {
                DialogHelper.dismissProgressDialog();
                mSideBar.setExistMap(existMap);
                mSortFriends = sortedList;
                mAdapter.setData(sortedList);
                if (friends.size() == 0) {
                    findViewById(R.id.fl_empty).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.fl_empty).setVisibility(View.GONE);
                }
            });
        });
    }

    /**
     * 获取黑名单列表
     */
    private void getBlackList() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        HashMap<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);

        HttpUtils.get().url(coreManager.getConfig().FRIENDS_BLACK_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<AttentionUser>(AttentionUser.class) {
                    @Override
                    public void onResponse(ArrayResult<AttentionUser> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            List<AttentionUser> attentionUsers = result.getData();
                            if (attentionUsers != null && attentionUsers.size() > 0) {
                                for (int i = 0; i < attentionUsers.size(); i++) {
                                    AttentionUser attentionUser = attentionUsers.get(i);
                                    if (attentionUser == null) {
                                        continue;
                                    }
                                    String userId = attentionUser.getToUserId();// 好友的Id
                                    Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, userId);
                                    if (friend == null) {
                                        friend = new Friend();
                                        friend.setOwnerId(attentionUser.getUserId());
                                        friend.setUserId(attentionUser.getToUserId());
                                        friend.setNickName(attentionUser.getToNickName());
                                        friend.setRemarkName(attentionUser.getRemarkName());
                                        friend.setTimeCreate(attentionUser.getCreateTime());
                                        friend.setStatus(Friend.STATUS_BLACKLIST);

                                        friend.setOfflineNoPushMsg(attentionUser.getOfflineNoPushMsg());
                                        friend.setTopTime(attentionUser.getOpenTopChatTime());
                                        PreferenceUtils.putInt(FLYApplication.getContext(), Constants.MESSAGE_READ_FIRE + attentionUser.getUserId() + CoreManager.requireSelf(FLYApplication.getContext()).getUserId(),
                                                attentionUser.getIsOpenSnapchat());
                                        friend.setChatRecordTimeOut(attentionUser.getChatRecordTimeOut());// 消息保存天数 -1/0 永久

                                        friend.setCompanyId(attentionUser.getCompanyId());
                                        friend.setRoomFlag(0);
                                        FriendDao.getInstance().createOrUpdateFriend(friend);
                                    } else {
                                        FriendDao.getInstance().updateFriendStatus(mLoginUserId, userId, Friend.STATUS_BLACKLIST);
                                    }
                                }
                            }
                            loadData();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

}
