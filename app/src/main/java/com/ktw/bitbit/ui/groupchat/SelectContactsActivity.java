package com.ktw.bitbit.ui.groupchat;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.drawable.DrawableCompat;

import com.alibaba.fastjson.JSON;
import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.FLYReporter;
import com.ktw.bitbit.bean.Area;
import com.ktw.bitbit.bean.Friend;
import com.ktw.bitbit.bean.message.ChatMessage;
import com.ktw.bitbit.bean.message.MucRoom;
import com.ktw.bitbit.bean.message.XmppMessage;
import com.ktw.bitbit.broadcast.MsgBroadcast;
import com.ktw.bitbit.broadcast.MucgroupUpdateUtil;
import com.ktw.bitbit.call.CallConstants;
import com.ktw.bitbit.call.MessageEventInitiateMeeting;
import com.ktw.bitbit.db.dao.ChatMessageDao;
import com.ktw.bitbit.db.dao.FriendDao;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.sortlist.BaseComparator;
import com.ktw.bitbit.sortlist.BaseSortModel;
import com.ktw.bitbit.sortlist.SideBar;
import com.ktw.bitbit.sortlist.SortHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.dialog.TowInputDialogView;
import com.ktw.bitbit.ui.message.MucChatActivity;
import com.ktw.bitbit.ui.tool.ButtonColorChange;
import com.ktw.bitbit.util.AsyncUtils;
import com.ktw.bitbit.util.CharUtils;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.DisplayUtil;
import com.ktw.bitbit.util.PreferenceUtils;
import com.ktw.bitbit.util.SkinUtils;
import com.ktw.bitbit.util.TimeUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.util.ViewHolder;
import com.ktw.bitbit.view.CircleImageView;
import com.ktw.bitbit.view.HorizontalListView;
import com.ktw.bitbit.view.NoDoubleClickListener;
import com.ktw.bitbit.view.SingleVideoChatToolDialog;
import com.ktw.bitbit.view.TipDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * ??????????????? ????????????
 */
public class SelectContactsActivity extends BaseActivity {
    private static SingleVideoChatToolDialog singleVideoChatToolDialog;
    private EditText mEditText;
    private boolean isSearch;
    private SideBar mSideBar;
    private TextView mTextDialog;
    private ListView mListView;
    private ListViewAdapter mAdapter;
    private List<Friend> mFriendList;
    private List<BaseSortModel<Friend>> mSortFriends;
    private List<BaseSortModel<Friend>> mSearchSortFriends;
    private BaseComparator<Friend> mBaseComparator;
    private HorizontalListView mHorizontalListView;
    private HorListViewAdapter mHorAdapter;
    private List<String> mSelectPositions;
    private Button mOkBtn;
    private String mLoginUserId;
    // ??????????????????
    private boolean mQuicklyInitiateMeeting;
    private int meetType;
    // ????????????????????????????????????????????????
    private boolean mQuicklyCreate;
    // ??????????????????????????????id????????????/??????
    private String mQuicklyId;
    private String mQuicklyName;
    private TowInputDialogView towInputDialogView;

    public static void startQuicklyInitiateMeeting(Context ctx) {
        SingleVideoChatToolDialog.OnSingleVideoChatToolDialog onSingleVideoChatToolDialog = new SingleVideoChatToolDialog.OnSingleVideoChatToolDialog() {
            @Override
            public void videoClick() {
                singleVideoChatToolDialog.dismiss();
                startQuicklyInitiateMeeting(ctx, CallConstants.Video_Meet);
            }

            @Override
            public void voiceClick() {
                singleVideoChatToolDialog.dismiss();
                startQuicklyInitiateMeeting(ctx, CallConstants.Audio_Meet);
            }

            @Override
            public void cancleClick() {
                singleVideoChatToolDialog.dismiss();
            }
        };
        singleVideoChatToolDialog = new SingleVideoChatToolDialog(ctx, onSingleVideoChatToolDialog, false);
        singleVideoChatToolDialog.show();
    }

    public static void startQuicklyInitiateMeeting(Context ctx, int meetType) {
        Intent quicklyInitiateMeetingIntent = new Intent(ctx, SelectContactsActivity.class);
        quicklyInitiateMeetingIntent.putExtra("QuicklyInitiateMeeting", true);
        quicklyInitiateMeetingIntent.putExtra("meetType", meetType);
        ctx.startActivity(quicklyInitiateMeetingIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);
        if (getIntent() != null) {
            mQuicklyInitiateMeeting = getIntent().getBooleanExtra("QuicklyInitiateMeeting", false);
            meetType = getIntent().getIntExtra("meetType", CallConstants.Video_Meet);

            mQuicklyCreate = getIntent().getBooleanExtra("QuicklyCreateGroup", false);
            mQuicklyId = getIntent().getStringExtra("ChatObjectId");
            mQuicklyName = getIntent().getStringExtra("ChatObjectName");
        }
        mLoginUserId = coreManager.getSelf().getUserId();

        mFriendList = new ArrayList<>();
        mSortFriends = new ArrayList<>();
        mSearchSortFriends = new ArrayList<>();
        mBaseComparator = new BaseComparator<>();
        mAdapter = new ListViewAdapter();

        mSelectPositions = new ArrayList<>();
        mHorAdapter = new HorListViewAdapter();

        initActionBar();
        initView();

        if (!mQuicklyInitiateMeeting && coreManager.getLimit().cannotCreateGroup()) {
            FLYReporter.unreachable();
            TipDialog tipDialog = new TipDialog(this);
            tipDialog.setTip(getString(R.string.tip_not_allow_create_room));
            tipDialog.setOnDismissListener(dialog -> {
                finish();
            });
            tipDialog.show();
        }

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
        if (mQuicklyInitiateMeeting) {
            tvTitle.setText(getString(R.string.select_contacts));
        } else {
            tvTitle.setText(getString(R.string.select_group_members));
        }
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setAdapter(mAdapter);
        mHorizontalListView = (HorizontalListView) findViewById(R.id.horizontal_list_view);
        mHorizontalListView.setAdapter(mHorAdapter);
        mOkBtn = (Button) findViewById(R.id.ok_btn);
        ButtonColorChange.colorChange(mContext, mOkBtn);
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mSideBar.setVisibility(View.VISIBLE);
        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar.setTextView(mTextDialog);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // ??????????????????????????????
                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }
            }
        });

        /**
         * ????????????????????????????????????
         */
        mEditText = (EditText) findViewById(R.id.search_et);
        mEditText.setHint(getString(R.string.search));
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                isSearch = true;
                mSearchSortFriends.clear();
                String str = mEditText.getText().toString();
                if (TextUtils.isEmpty(str)) {
                    isSearch = false;
                    mAdapter.setData(mSortFriends);
                    return;
                }
                for (int i = 0; i < mSortFriends.size(); i++) {
                    String name = !TextUtils.isEmpty(mSortFriends.get(i).getBean().getRemarkName()) ?
                            mSortFriends.get(i).getBean().getRemarkName() : mSortFriends.get(i).getBean().getNickName();
                    if (name.contains(str)) {
                        // ???????????????????????????
                        mSearchSortFriends.add((mSortFriends.get(i)));
                    }
                }
                mAdapter.setData(mSearchSortFriends);
            }
        });

        mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size()));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Friend friend;
                if (isSearch) {
                    friend = mSearchSortFriends.get(position).bean;
                } else {
                    friend = mSortFriends.get(position).bean;
                }

                if (mQuicklyCreate) {
                    if (friend.getUserId().equals(mLoginUserId)) {
                        ToastUtil.showToast(SelectContactsActivity.this, getString(R.string.tip_cannot_remove_self));
                        return;
                    } else if (friend.getUserId().equals(mQuicklyId)) {
                        ToastUtil.showToast(SelectContactsActivity.this, getString(R.string.tip_quickly_group_cannot_remove) + mQuicklyName);
                        return;
                    }
                }

                for (int i = 0; i < mSortFriends.size(); i++) {
                    if (mSortFriends.get(i).getBean().getUserId().equals(friend.getUserId())) {
                        if (friend.getStatus() != 100) {
                            friend.setStatus(100);
                            mSortFriends.get(i).getBean().setStatus(100);
                            addSelect(friend.getUserId());
                        } else {
                            friend.setStatus(101);
                            mSortFriends.get(i).getBean().setStatus(101);
                            removeSelect(friend.getUserId());
                        }

                        if (isSearch) {
                            mAdapter.setData(mSearchSortFriends);
                        } else {
                            mAdapter.setData(mSortFriends);
                        }
                    }
                }
            }
        });

        mHorizontalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                for (int i = 0; i < mSortFriends.size(); i++) {
                    if (mSortFriends.get(i).getBean().getUserId().equals(mSelectPositions.get(position))) {
                        mSortFriends.get(i).getBean().setStatus(101);
                        mAdapter.setData(mSortFriends);
                    }
                }
                mSelectPositions.remove(position);
                mHorAdapter.notifyDataSetInvalidated();
                mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size()));
            }
        });

        mOkBtn.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                if (mQuicklyInitiateMeeting) {// ???????????????????????????
                    showSelectMeetingTypeDialog();
                    return;
                }

                if (!coreManager.isLogin()) {
                    ToastUtil.showToast(mContext, R.string.service_start_failed);
                    return;
                }

                if (mQuicklyCreate) {
                    // ?????????????????????mSelectPositions????????????????????????,So
                    if (mSelectPositions.size() <= 0) {
                        ToastUtil.showToast(mContext, getString(R.string.tip_create_group_at_lease_one_friend));
                        return;
                    }
                    String sc = coreManager.getSelf().getNickName() + "???" + mQuicklyName + "???";
                    for (int i = 0; i < mSelectPositions.size(); i++) {
                        String name = "";
                        for (int i1 = 0; i1 < mFriendList.size(); i1++) {
                            if (mFriendList.get(i1).getUserId().equals(mSelectPositions.get(i))) {
                                name = !TextUtils.isEmpty(mFriendList.get(i1).getRemarkName()) ? mFriendList.get(i1).getRemarkName() : mFriendList.get(i1).getNickName();
                            }
                        }
                        if (i == mSelectPositions.size() - 1) {
                            sc += name;
                        } else {
                            sc += name + "???";
                        }
                    }
                    createGroupChat(sc, "", 0, 1, 0, 1, 1);
                } else {
                    showCreateGroupChatDialog();
                }
            }
        });

        loadData();
    }

    private void loadData() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        AsyncUtils.doAsync(this, e -> {
            FLYReporter.post("?????????????????????", e);
            AsyncUtils.runOnUiThread(this, ctx -> {
                DialogHelper.dismissProgressDialog();
                ToastUtil.showToast(ctx, R.string.data_exception);
            });
        }, c -> {
            final List<Friend> friends = FriendDao.getInstance().getFriendsGroupChat(mLoginUserId);
            if (friends != null) {
                for (Friend friend : friends) {
                    if (TextUtils.equals(friend.getUserId(), Friend.ID_SYSTEM_MESSAGE)) {
                        // ??????????????????????????????status???????????????????????????
                        friends.remove(friend);
                        break;
                    }
                }
            }
            if (mQuicklyCreate) {
                Friend friend = new Friend();
                friend.setUserId(mLoginUserId);
                friend.setNickName(coreManager.getSelf().getNickName());
                friends.add(0, friend);
            }
            Map<String, Integer> existMap = new HashMap<>();
            List<BaseSortModel<Friend>> sortedList = SortHelper.toSortedModelList(friends, existMap, Friend::getShowName);
            c.uiThread(r -> {
                DialogHelper.dismissProgressDialog();
                mSideBar.setExistMap(existMap);
                mFriendList = friends;
                mSortFriends = sortedList;
                mAdapter.setData(sortedList);
            });
        });
    }

    private void addSelect(String userId) {
        mSelectPositions.add(userId);
        mHorAdapter.notifyDataSetInvalidated();
        mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size()));
    }

    private void removeSelect(String userId) {
        for (int i = 0; i < mSelectPositions.size(); i++) {
            if (mSelectPositions.get(i).equals(userId)) {
                mSelectPositions.remove(i);
            }
        }
        mHorAdapter.notifyDataSetInvalidated();
        mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size()));
    }

    private void showCreateGroupChatDialog() {
        towInputDialogView = DialogHelper.showTowInputDialogAndReturnDialog(this,
                getString(R.string.create_room),
                getString(R.string.jx_inputroomname),
                getString(R.string.jxalert_inputsomething),
                new TowInputDialogView.onSureClickLinsenter() {
                    @Override
                    public void onClick(EditText roomNameEdit, EditText roomDescEdit, int isRead, int isLook, int isNeedVerify, int isShowMember, int isAllowSendCard) {
                        String roomName = roomNameEdit.getText().toString().trim();
                        if (TextUtils.isEmpty(roomName)) {
                            Toast.makeText(SelectContactsActivity.this, getString(R.string.room_name_empty_error), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String roomDesc = roomDescEdit.getText().toString();
//                        if (TextUtils.isEmpty(roomDesc)) {
//                            Toast.makeText(SelectContactsActivity.this, getString(R.string.room_des_empty_error), Toast.LENGTH_SHORT).show();
//                            return;
//                        }

                        int length = 0;
                        for (int i = 0; i < roomName.length(); i++) {
                            String substring = roomName.substring(i, i + 1);
                            if (CharUtils.isChinese(substring)) {  // ?????????????????????
                                length += 2;
                            } else {
                                length += 1;
                            }
                        }
                        if (length > 20) {
                            Toast.makeText(SelectContactsActivity.this, R.string.tip_group_name_too_long, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int length2 = 0;
                        for (int i = 0; i < roomDesc.length(); i++) {
                            String substring = roomDesc.substring(i, i + 1);
                            if (CharUtils.isChinese(substring)) {
                                length2 += 2;
                            } else {
                                length2 += 1;
                            }
                        }
                        if (length2 > 100) {
                            Toast.makeText(SelectContactsActivity.this, R.string.tip_group_description_too_long, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        createGroupChat(roomName, roomDesc, isRead, isLook, isNeedVerify, isShowMember, isAllowSendCard);

                        if (towInputDialogView != null) {
                            towInputDialogView.dismiss();
                        }
                    }
                });

        // ??????????????????
       /* ToastUtil.addEditTextNumChanged(SelectContactsActivity.this, dialogView.getE1(), 20);
        ToastUtil.addEditTextNumChanged(SelectContactsActivity.this, dialogView.getE2(), 100);*/
    }

    private void createGroupChat(final String roomName, final String roomDesc, int isRead,
                                 int isLook, int isNeedVerify, int isShowMember, int isAllowSendCard) {
        final String roomJid = coreManager.createMucRoom(roomName);
        if (TextUtils.isEmpty(roomJid)) {
            ToastUtil.showToast(mContext, getString(R.string.create_room_failed));
            return;
        }
        FLYApplication.mRoomKeyLastCreate = roomJid;
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("jid", roomJid);
        params.put("name", roomName);
        params.put("desc", roomDesc);
        params.put("countryId", String.valueOf(Area.getDefaultCountyId()));

        params.put("showRead", isRead + "");
        // ??????????????????
        PreferenceUtils.putBoolean(mContext, Constants.IS_SHOW_READ + roomJid, isRead == 1);
        // ????????????
        params.put("isLook", isLook + "");
        // ????????????????????????
        params.put("isNeedVerify", isNeedVerify + "");
        // ???????????????
        params.put("showMember", isShowMember + "");
        params.put("allowSendCard", isAllowSendCard + "");

        params.put("allowInviteFriend", "1");
        params.put("allowUploadFile", "1");
        params.put("allowConference", "1");
        params.put("allowSpeakCourse", "1");

        PreferenceUtils.putBoolean(mContext, Constants.IS_SEND_CARD + roomJid, isAllowSendCard == 1);
        PreferenceUtils.putBoolean(mContext, Constants.IS_SHOW_MEMBER + roomJid, isShowMember == 1);

        Area area = Area.getDefaultProvince();
        if (area != null) {
            params.put("provinceId", String.valueOf(area.getId()));    // ??????Id
        }
        area = Area.getDefaultCity();
        if (area != null) {
            params.put("cityId", String.valueOf(area.getId()));            // ??????Id
            area = Area.getDefaultDistrict(area.getId());
            if (area != null) {
                params.put("areaId", String.valueOf(area.getId()));        // ??????Id
            }
        }

        double latitude = FLYApplication.getInstance().getBdLocationHelper().getLatitude();
        double longitude = FLYApplication.getInstance().getBdLocationHelper().getLongitude();
        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));

        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<MucRoom>(MucRoom.class) {

                    @Override
                    public void onResponse(ObjectResult<MucRoom> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            if (mQuicklyCreate) {
                                sendBroadcast(new Intent(com.ktw.bitbit.broadcast.OtherBroadcast.QC_FINISH)); // ????????????????????????????????????????????????????????????
                            }
                            createRoomSuccess(result.getData(), result.getData().getId(), roomJid, roomName, roomDesc);
                        } else {
                            FLYApplication.mRoomKeyLastCreate = "compatible";// ????????????
                            if (!TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(mContext, result.getResultMsg());
                            } else {
                                ToastUtil.showToast(mContext, R.string.tip_server_error);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        FLYApplication.mRoomKeyLastCreate = "compatible";// ????????????
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    // ?????????????????????????????????????????????????????????????????????
    private void createRoomSuccess(MucRoom mucRoom, String roomId, String roomJid, String roomName, String
            roomDesc) {
        Friend friend = new Friend();
        friend.setOwnerId(mLoginUserId);
        friend.setUserId(roomJid);
        friend.setNickName(roomName);
        friend.setDescription(roomDesc);
        friend.setRoomFlag(1);
        friend.setRoomId(roomId);
        friend.setRoomCreateUserId(mLoginUserId);
        // timeSend??????????????????????????????????????????????????????????????????????????????
        friend.setTimeSend(TimeUtils.sk_time_current_time());
        friend.setStatus(Friend.STATUS_FRIEND);
        FriendDao.getInstance().createOrUpdateFriend(friend);

        // ????????????
        MucgroupUpdateUtil.broadcastUpdateUi(this);

        // ????????????????????????????????? ??????????????????????????????????????????????????????
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(XmppMessage.TYPE_TIP);
        chatMessage.setFromUserId(mLoginUserId);
        chatMessage.setFromUserName(coreManager.getSelf().getNickName());
        chatMessage.setToUserId(roomJid);
        chatMessage.setContent(getString(R.string.new_friend_chat));
        chatMessage.setPacketId(coreManager.getSelf().getNickName());
        chatMessage.setTimeSend(TimeUtils.sk_time_current_time());
        if (ChatMessageDao.getInstance().saveNewSingleChatMessage(mLoginUserId, roomJid, chatMessage)) {
            // ??????????????????
            MsgBroadcast.broadcastMsgUiUpdate(SelectContactsActivity.this);
        }

        // ????????????
        String[] noticeFriendList = new String[mSelectPositions.size()];

        // ????????????
        List<String> inviteUsers = new ArrayList<>(mSelectPositions);
        if (mQuicklyCreate) {
            inviteUsers.add(mQuicklyId);
        }

        if (inviteUsers.size() + 1 <= mucRoom.getMaxUserSize()) {
            inviteFriend(JSON.toJSONString(inviteUsers), roomId, roomJid, roomName, noticeFriendList);
        } else {// ????????????????????????
            TipDialog tipDialog = new TipDialog(mContext);
            tipDialog.setmConfirmOnClickListener(getString(R.string.tip_over_member_size, mucRoom.getMaxUserSize()), () -> start(mucRoom.getJid(), mucRoom.getName()));
            tipDialog.show();
            tipDialog.setOnDismissListener(dialog -> start(mucRoom.getJid(), mucRoom.getName()));
        }
    }

    /**
     * ????????????
     */
    private void inviteFriend(String text, String roomId, final String roomJid,
                              final String roomName, final String[] noticeFriendList) {
        if (mSelectPositions.size() <= 0) {
            start(roomJid, roomName);
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", roomId);
        params.put("text", text);

        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_MEMBER_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        setResult(RESULT_OK);
                        start(roomJid, roomName);
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private void start(String jid, String name) {
        // ??????????????????????????????????????????
        Intent intent = new Intent(SelectContactsActivity.this, MucChatActivity.class);
        intent.putExtra(FLYAppConstant.EXTRA_USER_ID, jid);
        intent.putExtra(FLYAppConstant.EXTRA_NICK_NAME, name);
        intent.putExtra(FLYAppConstant.EXTRA_IS_GROUP_CHAT, true);
        startActivity(intent);
        finish();
    }

    private void showSelectMeetingTypeDialog() {
        if (mSelectPositions.size() == 0) {
            DialogHelper.tip(this, getString(R.string.tip_select_at_lease_one_member));
            return;
        }
        EventBus.getDefault().post(new MessageEventInitiateMeeting(meetType, mSelectPositions));
    }

    private class ListViewAdapter extends BaseAdapter implements SectionIndexer {
        List<BaseSortModel<Friend>> mSortFriends;

        public ListViewAdapter() {
            mSortFriends = new ArrayList<>();
        }

        public void setData(List<BaseSortModel<Friend>> sortFriends) {
            mSortFriends = sortFriends;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mSortFriends.size();
        }

        @Override
        public Object getItem(int position) {
            return mSortFriends.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_select_contacts, parent, false);
            }
            TextView catagoryTitleTv = ViewHolder.get(convertView, R.id.catagory_title);
            View view_bg_friend = ViewHolder.get(convertView, R.id.view_bg_friend);
            CheckBox checkBox = ViewHolder.get(convertView, R.id.check_box);
            ImageView avatarImg = ViewHolder.get(convertView, R.id.avatar_img);
            TextView userNameTv = ViewHolder.get(convertView, R.id.user_name_tv);

            // ??????position???????????????????????????Char ascii???
            int section = getSectionForPosition(position);
            // ?????????????????????????????????????????????Char????????? ??????????????????????????????
            if (position == getPositionForSection(section)) {
                catagoryTitleTv.setVisibility(View.VISIBLE);
                view_bg_friend.setVisibility(View.GONE);
                catagoryTitleTv.setText(mSortFriends.get(position).getFirstLetter());
            } else {
                view_bg_friend.setVisibility(View.VISIBLE);
                catagoryTitleTv.setVisibility(View.GONE);
            }
            Friend friend = mSortFriends.get(position).getBean();
            if (friend != null) {
                AvatarHelper.getInstance().displayAvatar(friend.getUserId(), avatarImg, true);
                userNameTv.setText(TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName());
                checkBox.setChecked(false);
                ColorStateList tabColor = SkinUtils.getSkin(SelectContactsActivity.this).getTabColorState();
                if (friend.getStatus() == 100) {
                    checkBox.setChecked(true);
                    Drawable drawable = getResources().getDrawable(R.drawable.sel_check_wx2);
                    drawable = DrawableCompat.wrap(drawable);
                    DrawableCompat.setTintList(drawable, tabColor);
                    checkBox.setButtonDrawable(drawable);
                } else {
                    checkBox.setChecked(false);
                    checkBox.setButtonDrawable(getResources().getDrawable(R.drawable.sel_nor_wx2));
                }

                // ???????????????????????????item?????????????????????????????????
                if (mQuicklyCreate) {
                    if (friend.getUserId().equals(mLoginUserId) || friend.getUserId().equals(mQuicklyId)) {
                        checkBox.setChecked(true);
                        Drawable drawable = getResources().getDrawable(R.drawable.sel_check_wx2);
                        drawable = DrawableCompat.wrap(drawable);
                        DrawableCompat.setTintList(drawable, tabColor);
                        checkBox.setButtonDrawable(drawable);
                    }
                }
            }
            return convertView;
        }

        @Override
        public Object[] getSections() {
            return null;
        }

        @Override
        public int getPositionForSection(int section) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = mSortFriends.get(i).getFirstLetter();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getSectionForPosition(int position) {
            return mSortFriends.get(position).getFirstLetter().charAt(0);
        }
    }

    private class HorListViewAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mSelectPositions.size();
        }

        @Override
        public Object getItem(int position) {
            return mSelectPositions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new CircleImageView(mContext);
                int size = DisplayUtil.dip2px(mContext, 37);
                AbsListView.LayoutParams param = new AbsListView.LayoutParams(size, size);
                convertView.setLayoutParams(param);
            }
            ImageView imageView = (ImageView) convertView;
            String selectPosition = mSelectPositions.get(position);
            AvatarHelper.getInstance().displayAvatar(selectPosition, imageView, true);
            return convertView;
        }
    }
}
