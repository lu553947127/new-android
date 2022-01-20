package com.ktw.bitbit.ui.message.multi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.RoomMember;
import com.ktw.bitbit.broadcast.MsgBroadcast;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.PreferenceUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.view.SwitchButton;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * 群管理
 */
public class GroupManager extends BaseActivity {
    /**
     * 更新群组 是否显示已读人数、私密群组、是否开启进群验证、是否对普通成员开放群成员列表、群成员是否可在群组内发送名片
     */
    String authority;
    private String mRoomId;
    private String mRoomJid;
    SwitchButton.OnCheckedChangeListener mOnCheckedChangeListener = new SwitchButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(SwitchButton view, boolean isChecked) {
            switch (view.getId()) {
                case R.id.sb_read:
                    updateGroupHostAuthority(0, isChecked);
                    break;
                case R.id.sb_look:
                    updateGroupHostAuthority(1, isChecked);
                    break;
                case R.id.sb_verify:
                    updateGroupHostAuthority(2, isChecked);
                    break;
                case R.id.sb_show_member:
                    updateGroupHostAuthority(3, isChecked);
                    break;
                case R.id.sb_allow_chat:
                    updateGroupHostAuthority(4, isChecked);
                    break;
                case R.id.sb_allow_invite:
                    updateGroupHostAuthority(5, isChecked);
                    break;
                case R.id.sb_allow_upload:
                    updateGroupHostAuthority(6, isChecked);
                    break;
                case R.id.sb_allow_conference:
                    updateGroupHostAuthority(7, isChecked);
                    break;
                case R.id.sb_allow_send_course:
                    updateGroupHostAuthority(8, isChecked);
                    break;
                case R.id.sb_notify:
                    updateGroupHostAuthority(9, isChecked);
                    break;
            }
        }
    };
    private int[] status_lists;
    private SwitchButton mSbRead;
    private SwitchButton mSbLook;
    private SwitchButton mSbVerify;
    private SwitchButton mSbShowMember;
    private SwitchButton mSbAllowChat;
    private SwitchButton mSbAllowInvite;
    private SwitchButton mSbAllowUpload;
    private SwitchButton mSbAllowConference;
    private SwitchButton mSbAllowSendCourse;
    private SwitchButton mSbNotify;
    private int roomRole;
    private String mRoomName;
    private int mMemberSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manager);
        mRoomId = getIntent().getStringExtra("roomId");
        mRoomJid = getIntent().getStringExtra("roomJid");
        roomRole = getIntent().getIntExtra("roomRole", 0);
        status_lists = getIntent().getIntArrayExtra("GROUP_STATUS_LIST");
        mRoomName = getIntent().getStringExtra("copy_name");
        mMemberSize = getIntent().getIntExtra("copy_size", 0);
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
        tvTitle.setText(getString(R.string.group_management));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
//        if (coreManager.getConfig().isOpenRoomSearch) {
//            findViewById(R.id.rl_look).setVisibility(View.VISIBLE);
//            findViewById(R.id.rl_look_summer).setVisibility(View.VISIBLE);
//        }
        mSbRead = (SwitchButton) findViewById(R.id.sb_read);
        mSbLook = (SwitchButton) findViewById(R.id.sb_look);
        mSbVerify = (SwitchButton) findViewById(R.id.sb_verify);
        mSbShowMember = (SwitchButton) findViewById(R.id.sb_show_member);
        mSbAllowChat = (SwitchButton) findViewById(R.id.sb_allow_chat);
        mSbAllowInvite = (SwitchButton) findViewById(R.id.sb_allow_invite);
        mSbAllowUpload = (SwitchButton) findViewById(R.id.sb_allow_upload);
        mSbAllowConference = (SwitchButton) findViewById(R.id.sb_allow_conference);
        mSbAllowSendCourse = (SwitchButton) findViewById(R.id.sb_allow_send_course);
        mSbNotify = (SwitchButton) findViewById(R.id.sb_notify);
        mSbRead.setChecked(status_lists[0] == 1);
        mSbLook.setChecked(status_lists[1] == 1);
        mSbVerify.setChecked(status_lists[2] == 1);
        mSbShowMember.setChecked(status_lists[3] == 1);
        mSbAllowChat.setChecked(status_lists[4] == 1);
        mSbAllowInvite.setChecked(status_lists[5] == 1);
        mSbAllowUpload.setChecked(status_lists[6] == 1);
        mSbAllowConference.setChecked(status_lists[7] == 1);
        mSbAllowSendCourse.setChecked(status_lists[8] == 1);
        mSbNotify.setChecked(status_lists[9] == 1);
        mSbRead.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbLook.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbVerify.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbShowMember.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbAllowChat.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbAllowInvite.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbAllowUpload.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbAllowConference.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbAllowSendCourse.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mSbNotify.setOnCheckedChangeListener(mOnCheckedChangeListener);

        // 设置 &&  取消 管理员，隐身人，监控人的按钮设置监听，
        @SuppressLint("UseSparseArrays")
        Map<Integer, Integer> setRoleIdMap = new HashMap<>();
        setRoleIdMap.put(R.id.set_manager_rl, RoomMember.ROLE_MANAGER);
        setRoleIdMap.put(R.id.set_invisible_rl, RoomMember.ROLE_INVISIBLE);
        setRoleIdMap.put(R.id.set_guardian_rl, RoomMember.ROLE_GUARDIAN);
        for (Integer id : setRoleIdMap.keySet()) {
            findViewById(id).setOnClickListener(v -> {
                SetManagerActivity.start(this, mRoomId, mRoomJid, setRoleIdMap.get(id));
            });
        }

        findViewById(R.id.set_remarks_rl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupManager.this, GroupMoreFeaturesActivity.class);
                intent.putExtra("roomId", mRoomId);
                intent.putExtra("isSetRemark", true);
                startActivity(intent);
            }
        });

        findViewById(R.id.transfer_group_rl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupManager.this, GroupTransferActivity.class);
                intent.putExtra("roomId", mRoomId);
                intent.putExtra("roomJid", mRoomJid);
                startActivity(intent);
                finish();
            }
        });

        //群复制
        findViewById(R.id.copy_rl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRoomId == null) {
                    return;
                }
                if (roomRole == 1 || roomRole == 2) {
                    Intent intent = new Intent(GroupManager.this, RoomCopyActivity.class);
                    intent.putExtra("roomId", mRoomId);
                    intent.putExtra("copy_name", mRoomName);
                    intent.putExtra("copy_size", mMemberSize);
                    startActivity(intent);
                    finish();
                } else tip(getString(R.string.copy_group_manager));
            }
        });

        findViewById(R.id.set_invisible_rl).setVisibility(View.GONE);
        findViewById(R.id.copy_rl).setVisibility(View.GONE);
    }

    private void updateGroupHostAuthority(final int type, final boolean isChecked) {
        authority = isChecked ? "1" : "0";
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("roomId", mRoomId);
        if (type == 0) {
            params.put("showRead", authority);
        } else if (type == 1) {
            params.put("isLook", authority);
        } else if (type == 2) {
            params.put("isNeedVerify", authority);
        } else if (type == 3) {
            params.put("showMember", authority);
        } else if (type == 4) {
            params.put("allowSendCard", authority);
        } else if (type == 5) {
            params.put("allowInviteFriend", authority);
        } else if (type == 6) {
            params.put("allowUploadFile", authority);
        } else if (type == 7) {
            params.put("allowConference", authority);
        } else if (type == 8) {
            params.put("allowSpeakCourse", authority);
        } else if (type == 9) {
            params.put("isAttritionNotice", authority);
        }
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().ROOM_UPDATE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            EventBus.getDefault().post(new EventGroupStatus(type, Integer.valueOf(authority)));// 更新群组信息页面
                            String str;
                            if (isChecked) {
                                str = getString(R.string.is_open);
                            } else {
                                str = getString(R.string.is_close);
                            }
                            if (type == 0) {
                                PreferenceUtils.putBoolean(mContext, Constants.IS_SHOW_READ + mRoomJid, isChecked);
                                MsgBroadcast.broadcastMsgRoomUpdate(mContext);// 服务端不会给调用接口者推送对应的XMPP协议，所以需要通知聊天界面刷新
                            } else if (type == 3) {
                                PreferenceUtils.putBoolean(mContext, Constants.IS_SHOW_MEMBER + mRoomJid, isChecked);
                            } else if (type == 4) {
                                PreferenceUtils.putBoolean(mContext, Constants.IS_SEND_CARD + mRoomJid, isChecked);
                            } else if (type == 7) {
                                PreferenceUtils.putBoolean(mContext, Constants.IS_ALLOW_NORMAL_CONFERENCE + mRoomJid, isChecked);
                            } else if (type == 8) {
                                PreferenceUtils.putBoolean(mContext, Constants.IS_ALLOW_NORMAL_SEND_COURSE + mRoomJid, isChecked);
                            }
                            tip(str);
                        } else {
                            ToastUtil.showErrorData(mContext);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(mContext);
                    }
                });
    }

    public void tip(String tip) {
        ToastUtil.showToast(getBaseContext(), tip);
    }
}
