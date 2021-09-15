package com.ktw.fly.ui.message;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ktw.fly.R;
import com.ktw.fly.FLYReporter;
import com.ktw.fly.adapter.FriendSortAdapter;
import com.ktw.fly.bean.Friend;
import com.ktw.fly.bean.message.ChatMessage;
import com.ktw.fly.broadcast.MsgBroadcast;
import com.ktw.fly.db.dao.ChatMessageDao;
import com.ktw.fly.db.dao.FriendDao;
import com.ktw.fly.helper.TrillStatisticsHelper;
import com.ktw.fly.sortlist.BaseComparator;
import com.ktw.fly.sortlist.BaseSortModel;
import com.ktw.fly.sortlist.SideBar;
import com.ktw.fly.sortlist.SortHelper;
import com.ktw.fly.ui.FLYMainActivity;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.util.AsyncUtils;
import com.ktw.fly.util.Constants;
import com.ktw.fly.util.SkinUtils;
import com.ktw.fly.util.TimeUtils;
import com.ktw.fly.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.greenrobot.event.EventBus;

/**
 * 转发 选择 好友
 */
public class SelectNewContactsActivity extends BaseActivity implements OnClickListener {
    private PullToRefreshListView mPullToRefreshListView;
    private FriendSortAdapter mAdapter;
    private TextView mTextDialog;
    private SideBar mSideBar;
    private List<BaseSortModel<Friend>> mSortFriends;
    private BaseComparator<Friend> mBaseComparator;
    private String mLoginUserId;

    private boolean isMoreSelected;// 是否为多选转发
    private boolean isSingleOrMerge;// 逐条还是合并转发
    private String toUserId;
    private String messageId;

    private InstantMessageConfirmNew menuWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newchat_person_selected);
        isMoreSelected = getIntent().getBooleanExtra(Constants.IS_MORE_SELECTED_INSTANT, false);
        isSingleOrMerge = getIntent().getBooleanExtra(Constants.IS_SINGLE_OR_MERGE, false);
        // 在ChatContentView内长按转发才需要以下参数
        toUserId = getIntent().getStringExtra("fromUserId");
        messageId = getIntent().getStringExtra("messageId");

        mSortFriends = new ArrayList<BaseSortModel<Friend>>();
        mBaseComparator = new BaseComparator<Friend>();
        mLoginUserId = coreManager.getSelf().getUserId();

        initActionBar();
        initView();
        loadData();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.select_contacts));
        TextView tvRight = (TextView) findViewById(R.id.tv_title_right);
        tvRight.setTextColor(getResources().getColor(R.color.white));
        tvRight.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_grey_circle));
        ViewCompat.setBackgroundTintList(tvRight, ColorStateList.valueOf(SkinUtils.getSkin(this).getAccentColor()));
        tvRight.setText(R.string.finish);
        tvRight.setOnClickListener(v -> {
            List<Friend> friends = new ArrayList<>();
            for (int i = 0; i < mSortFriends.size(); i++) {
                if (mSortFriends.get(i).getBean().isCheck()) {
                    friends.add(mSortFriends.get(i).getBean());
                }
            }
            if (friends.size() > 0) {
                showPopuWindow(v, friends);
            }
        });
    }

    private void initView() {
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        View headView = View.inflate(this, R.layout.item_headview_creategroup_chat, null);
        mPullToRefreshListView.getRefreshableView().addHeaderView(headView);
        headView.setOnClickListener(this);
        mPullToRefreshListView.setMode(Mode.PULL_FROM_START);
        mAdapter = new FriendSortAdapter(this, mSortFriends);
        mAdapter.showCheckBox();
        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);
        mPullToRefreshListView.setOnRefreshListener(refreshView -> loadData());

        mPullToRefreshListView.setOnItemClickListener((parent, view, position, id) -> {
            Friend friend = mSortFriends.get((int) id).getBean();
            mSortFriends.get((int) id).getBean().setCheck(!friend.isCheck());
            mAdapter.notifyDataSetChanged();
        });

        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mSideBar.setTextView(mTextDialog);

        mSideBar.setOnTouchingLetterChangedListener(s -> {
            // 该字母首次出现的位置
            int position = mAdapter.getPositionForSection(s.charAt(0));
            if (position != -1) {
                mPullToRefreshListView.getRefreshableView().setSelection(position);
            }
        });
    }

    private void showPopuWindow(View view, List<Friend> friends) {
        menuWindow = new InstantMessageConfirmNew(SelectNewContactsActivity.this, new ClickListener(friends),
                friends);
        menuWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void loadData() {
        AsyncUtils.doAsync(this, e -> {
            FLYReporter.post("加载数据失败，", e);
            AsyncUtils.runOnUiThread(this, ctx -> {
                ToastUtil.showToast(ctx, R.string.data_exception);
            });
        }, c -> {
            long startTime = System.currentTimeMillis();
            final List<Friend> friends = FriendDao.getInstance().getAllFriends(mLoginUserId);
            Map<String, Integer> existMap = new HashMap<>();
            List<BaseSortModel<Friend>> sortedList = SortHelper.toSortedModelList(friends, existMap, Friend::getShowName);

            long delayTime = 200 - (startTime - System.currentTimeMillis());// 保证至少200ms的刷新过程
            if (delayTime < 0) {
                delayTime = 0;
            }
            c.postDelayed(r -> {
                mSideBar.setExistMap(existMap);
                mSortFriends = sortedList;
                mAdapter.setData(sortedList);
                mPullToRefreshListView.onRefreshComplete();
            }, delayTime);
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_headview_instant_group:
                Intent intent = new Intent(SelectNewContactsActivity.this, SelectNewGroupInstantActivity.class);
                intent.putExtra(Constants.IS_MORE_SELECTED_INSTANT, isMoreSelected);
                intent.putExtra(Constants.IS_SINGLE_OR_MERGE, isSingleOrMerge);
                intent.putExtra("fromUserId", toUserId);
                intent.putExtra("messageId", messageId);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 事件的监听
     */
    class ClickListener implements OnClickListener {
        private List<Friend> friends;

        public ClickListener(List<Friend> friends) {
            this.friends = friends;
        }

        @Override
        public void onClick(View v) {
            menuWindow.dismiss();
            switch (v.getId()) {
                case R.id.btn_send:
                    for (int i = 0; i < friends.size(); i++) {
                        Friend friend = friends.get(i);
                        if (isMoreSelected) {// 多选转发 通知多选页面(即多选消息的单聊 || 群聊页面，在该页面获取选中的消息在发送出去)
                            EventBus.getDefault().post(new EventMoreSelected(friend.getUserId(), isSingleOrMerge, false));
                            finish();
                        } else {
                            ChatMessage chatMessage = ChatMessageDao.getInstance().findMsgById(mLoginUserId, toUserId, messageId);
                            TrillStatisticsHelper.share(mContext, coreManager, chatMessage);
                            chatMessage.setFromUserId(mLoginUserId);
                            chatMessage.setFromUserName(coreManager.getSelf().getNickName());
                            chatMessage.setToUserId(friend.getUserId());
                            chatMessage.setUpload(true);
                            chatMessage.setMySend(true);
                            chatMessage.setReSendCount(5);
                            chatMessage.setSendRead(false);
                            // 因为该消息的原主人可能开启了消息传输加密，我们对于content字段解密后存入了数据库，但是isEncrypt字段并未改变
                            // 如果我们将此消息转发给另一人，对方可能会对我方已解密的消息再次进行解密
                            chatMessage.setIsEncrypt(0);
                            chatMessage.setTimeSend(TimeUtils.sk_time_current_time());
                            chatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                            ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, friend.getUserId(), chatMessage);
                            coreManager.sendChatMessage(friend.getUserId(), chatMessage);

                            MsgBroadcast.broadcastMsgUiUpdate(mContext);
                            Intent intent = new Intent(mContext, FLYMainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                    break;
                case R.id.btn_cancle:// 取消
                    break;
                default:
                    break;
            }
        }
    }
}
