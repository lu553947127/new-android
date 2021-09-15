package com.ktw.fly.ui.systemshare;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ktw.fly.R;
import com.ktw.fly.FLYReporter;
import com.ktw.fly.adapter.FriendSortAdapter;
import com.ktw.fly.bean.Friend;
import com.ktw.fly.bean.message.ChatMessage;
import com.ktw.fly.bean.message.XmppMessage;
import com.ktw.fly.broadcast.MsgBroadcast;
import com.ktw.fly.db.dao.ChatMessageDao;
import com.ktw.fly.db.dao.FriendDao;
import com.ktw.fly.helper.DialogHelper;
import com.ktw.fly.helper.UploadEngine;
import com.ktw.fly.sortlist.BaseComparator;
import com.ktw.fly.sortlist.BaseSortModel;
import com.ktw.fly.sortlist.SideBar;
import com.ktw.fly.sortlist.SortHelper;
import com.ktw.fly.ui.FLYMainActivity;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.ui.message.InstantMessageConfirm;
import com.ktw.fly.util.AsyncUtils;
import com.ktw.fly.util.TimeUtils;
import com.ktw.fly.util.ToastUtil;
import com.ktw.fly.view.LoadFrame;
import com.ktw.fly.xmpp.ListenerManager;
import com.ktw.fly.xmpp.listener.ChatMessageListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 分享 选择 群组
 */
public class ShareNewGroup extends BaseActivity implements ChatMessageListener {
    private PullToRefreshListView mPullToRefreshListView;
    private FriendSortAdapter mAdapter;
    private TextView mTextDialog;
    private SideBar mSideBar;
    private List<BaseSortModel<Friend>> mSortFriends;
    private BaseComparator<Friend> mBaseComparator;
    private String mLoginUserId;

    private InstantMessageConfirm menuWindow;
    private LoadFrame mLoadFrame;

    private ChatMessage mShareChatMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newchat_person_selected);

        mSortFriends = new ArrayList<BaseSortModel<Friend>>();
        mBaseComparator = new BaseComparator<Friend>();
        mLoginUserId = coreManager.getSelf().getUserId();

        mShareChatMessage = new ChatMessage();
        if (ShareUtil.shareInit(this, mShareChatMessage)) return;

        initActionBar();
        initView();
        loadData();

        ListenerManager.getInstance().addChatMessageListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ListenerManager.getInstance().removeChatMessageListener(this);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.select_group_chat_instant));
    }

    private void initView() {
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        mAdapter = new FriendSortAdapter(this, mSortFriends);
        mPullToRefreshListView.setMode(Mode.PULL_FROM_START);
        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                loadData();
            }
        });

        mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Friend friend = mSortFriends.get((int) id).getBean();
                showPopuWindow(view, friend);
            }
        });

        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mSideBar.setTextView(mTextDialog);

        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mPullToRefreshListView.getRefreshableView().setSelection(position);
                }
            }
        });
    }

    private void showPopuWindow(View view, Friend friend) {
        menuWindow = new InstantMessageConfirm(this, new ClickListener(friend), friend);
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
            final List<Friend> friends = FriendDao.getInstance().getAllRooms(mLoginUserId);
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
    public void onMessageSendStateChange(int messageState, String msgId) {
        if (TextUtils.isEmpty(msgId)) {
            return;
        }
        // 更新消息Fragment的广播
        MsgBroadcast.broadcastMsgUiUpdate(mContext);
        if (mShareChatMessage != null && mShareChatMessage.getPacketId().equals(msgId)) {
            if (messageState == ChatMessageListener.MESSAGE_SEND_SUCCESS) {// 发送成功
                if (mLoadFrame != null) {
                    mLoadFrame.change();
                }
            }
        }
    }

    @Override
    public boolean onNewMessage(String fromUserId, ChatMessage message, boolean isGroupMsg) {
        return false;
    }

    /**
     * 事件的监听
     */
    class ClickListener implements OnClickListener {
        private Friend friend;

        public ClickListener(Friend friend) {
            this.friend = friend;
        }

        @Override
        public void onClick(View v) {
            menuWindow.dismiss();
            switch (v.getId()) {
                case R.id.btn_send:// 发送
                    if (friend.getRoomFlag() != 0) {
                        if (friend.getRoomTalkTime() > (System.currentTimeMillis() / 1000)) {// 禁言时间 > 当前时间 禁言还未结束
                            DialogHelper.tip(mContext, getString(R.string.tip_forward_ban));
                            return;
                        } else if (friend.getGroupStatus() == 1) {
                            DialogHelper.tip(mContext, getString(R.string.tip_forward_kick));
                            return;
                        } else if (friend.getGroupStatus() == 2) {
                            DialogHelper.tip(mContext, getString(R.string.tip_forward_disbanded));
                            return;
                        } else if ((friend.getGroupStatus() == 3)) {
                            DialogHelper.tip(mContext, getString(R.string.tip_group_disable_by_service));
                            return;
                        }
                    }

                    mLoadFrame = new LoadFrame(ShareNewGroup.this);
                    mLoadFrame.setSomething(getString(R.string.back_last_page), getString(R.string.open_im, getString(R.string.app_name)), new LoadFrame.OnLoadFrameClickListener() {
                        @Override
                        public void cancelClick() {
                            ShareBroadCast.broadcastFinishActivity(ShareNewGroup.this);
                            finish();
                        }

                        @Override
                        public void confirmClick() {
                            ShareBroadCast.broadcastFinishActivity(ShareNewGroup.this);
                            startActivity(new Intent(ShareNewGroup.this, FLYMainActivity.class));
                            finish();
                        }
                    });
                    mLoadFrame.show();

                    mShareChatMessage.setFromUserId(mLoginUserId);
                    mShareChatMessage.setFromUserName(coreManager.getSelf().getNickName());
                    mShareChatMessage.setToUserId(friend.getUserId());
                    mShareChatMessage.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
                    mShareChatMessage.setTimeSend(TimeUtils.sk_time_current_time());
                    ChatMessageDao.getInstance().saveNewSingleChatMessage(coreManager.getSelf().getUserId(), friend.getUserId(), mShareChatMessage);
                    switch (mShareChatMessage.getType()) {
                        case XmppMessage.TYPE_TEXT:
                            coreManager.sendChatMessage(friend.getUserId(), mShareChatMessage);
                            break;
                        case XmppMessage.TYPE_IMAGE:
                        case XmppMessage.TYPE_VIDEO:
                        case XmppMessage.TYPE_FILE:
                            if (!mShareChatMessage.isUpload()) {// 未上传
                                UploadEngine.uploadImFile(coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), friend.getUserId(), mShareChatMessage, new UploadEngine.ImFileUploadResponse() {
                                    @Override
                                    public void onSuccess(String toUserId, ChatMessage message) {
                                        coreManager.sendChatMessage(friend.getUserId(), mShareChatMessage);
                                    }

                                    @Override
                                    public void onFailure(String toUserId, ChatMessage message) {
                                        mLoadFrame.dismiss();
                                        ToastUtil.showToast(ShareNewGroup.this, getString(R.string.upload_failed));
                                    }
                                });
                            } else {// 已上传 自定义表情默认为已上传
                                coreManager.sendChatMessage(friend.getUserId(), mShareChatMessage);
                            }
                            break;
                        default:
                            FLYReporter.unreachable();
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
