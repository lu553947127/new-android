package com.ktw.bitbit.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.Friend;
import com.ktw.bitbit.bean.event.MessageEventHongdian;
import com.ktw.bitbit.broadcast.OtherBroadcast;
import com.ktw.bitbit.course.LocalCourseActivity;
import com.ktw.bitbit.db.dao.FriendDao;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.sp.UserSp;
import com.ktw.bitbit.ui.FLYMainActivity;
import com.ktw.bitbit.ui.base.EasyFragment;
import com.ktw.bitbit.ui.circle.BusinessCircleActivity;
import com.ktw.bitbit.ui.circle.SelectPicPopupWindow;
import com.ktw.bitbit.ui.circle.range.NewZanActivity;
import com.ktw.bitbit.ui.circle.range.SendAudioActivity;
import com.ktw.bitbit.ui.circle.range.SendFileActivity;
import com.ktw.bitbit.ui.circle.range.SendShuoshuoActivity;
import com.ktw.bitbit.ui.circle.range.SendVideoActivity;
import com.ktw.bitbit.ui.contacts.PublishNumberActivity;
import com.ktw.bitbit.ui.contacts.label.LabelActivityNewUI;
import com.ktw.bitbit.ui.groupchat.SelectContactsActivity;
import com.ktw.bitbit.ui.life.LifeCircleActivity;
import com.ktw.bitbit.ui.me.AboutActivity;
import com.ktw.bitbit.ui.me.AccountAndSafeActivity;
import com.ktw.bitbit.ui.me.BasicInfoEditActivity;
import com.ktw.bitbit.ui.me.MyCollection;
import com.ktw.bitbit.ui.me.NearPersonActivity;
import com.ktw.bitbit.ui.me.OfferApplyActivity;
import com.ktw.bitbit.ui.me.SettingActivity;
import com.ktw.bitbit.ui.me.ThirdServiceActivity;
import com.ktw.bitbit.ui.me.redpacket.RedPacketListActivity;
import com.ktw.bitbit.ui.me.redpacket.WxPayBlance;
import com.ktw.bitbit.ui.message.ChatActivity;
import com.ktw.bitbit.ui.other.QRcodeActivity;
import com.ktw.bitbit.ui.tool.SingleImagePreviewActivity;
import com.ktw.bitbit.ui.tool.WebViewActivity;
import com.ktw.bitbit.util.UiUtils;

import de.greenrobot.event.EventBus;

public class MeFragment extends EasyFragment implements View.OnClickListener {

    private ImageView mAvatarImg;
    private TextView mNickNameTv;
    private TextView mPhoneNumTv;
    private TextView skyTv, setTv;
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, OtherBroadcast.SYNC_SELF_DATE_NOTIFY)) {
                updateUI();
            }
        }
    };
    private SelectPicPopupWindow menuWindow;
    // ??????????????????????????????
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            if (menuWindow != null) {
                // ??????????????????????????????listener, ??????menuWindow,
                menuWindow.dismiss();
            }
            Intent intent = new Intent();
            switch (v.getId()) {
                case R.id.btn_send_picture:
                    // ???????????????
                    intent.setClass(getActivity(), SendShuoshuoActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_send_voice:
                    // ????????????
                    intent.setClass(getActivity(), SendAudioActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_send_video:
                    // ????????????
                    intent.setClass(getActivity(), SendVideoActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_send_file:
                    // ????????????
                    intent.setClass(getActivity(), SendFileActivity.class);
                    startActivity(intent);
                    break;
                case R.id.new_comment:
                    // ????????????&???
                    Intent intent2 = new Intent(getActivity(), NewZanActivity.class);
                    intent2.putExtra("OpenALL", true);
                    startActivity(intent2);
                    EventBus.getDefault().post(new MessageEventHongdian(0));
                    break;
                default:
                    break;
            }
        }
    };

    public MeFragment() {
    }

    @Override
//    protected int inflateLayoutId() {
//        return R.layout.fragment_me;
//    }
    protected int inflateLayoutId() {
        return R.layout.fragment_new_me_layout;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            initView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mUpdateReceiver);
    }

    private void initView() {
        skyTv = findViewById(R.id.MySky);
        setTv = findViewById(R.id.SettingTv);
        skyTv.setText(getString(R.string.my_moments));
        setTv.setText(getString(R.string.settings));
        findViewById(R.id.info_rl).setOnClickListener(this);
        findViewById(R.id.rlt_safe_settings).setOnClickListener(this);

        findViewById(R.id.meeting_rl).setOnClickListener(this);
        // ??????????????????ui??????????????????????????????????????????????????????????????????
        if (coreManager.getConfig().newUi) {
            findViewById(R.id.ll_more).setVisibility(View.GONE);
        }

        findViewById(R.id.my_monry).setOnClickListener(this);
        // ???????????????????????????????????????
        if (!coreManager.getConfig().enablePayModule) {
            findViewById(R.id.my_monry).setVisibility(View.GONE);
        }
        findViewById(R.id.my_space_rl).setOnClickListener(this);
        findViewById(R.id.my_collection_rl).setOnClickListener(this);
        findViewById(R.id.local_course_rl).setOnClickListener(this);
        findViewById(R.id.setting_rl).setOnClickListener(this);
        findViewById(R.id.rlt_qr_code).setOnClickListener(this);
        findViewById(R.id.rlt_customer_service).setOnClickListener(this);
        findViewById(R.id.rlt_third_service).setOnClickListener(this);
        findViewById(R.id.rlt_about_us).setOnClickListener(this);
        findViewById(R.id.rlt_read_envelope).setOnClickListener(this);

        mAvatarImg = (ImageView) findViewById(R.id.avatar_img);
        mNickNameTv = (TextView) findViewById(R.id.nick_name_tv);
        mPhoneNumTv = (TextView) findViewById(R.id.phone_number_tv);
        String loginUserId = coreManager.getSelf().getUserId();
        AvatarHelper.getInstance().displayAvatar(coreManager.getSelf().getNickName(), loginUserId, mAvatarImg, false);
        mNickNameTv.setText(coreManager.getSelf().getNickName());

        mAvatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SingleImagePreviewActivity.class);
                intent.putExtra(FLYAppConstant.EXTRA_IMAGE_URI, coreManager.getSelf().getUserId());
                startActivity(intent);
            }
        });

        initTitleBackground();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(OtherBroadcast.SYNC_SELF_DATE_NOTIFY);
        getActivity().registerReceiver(mUpdateReceiver, intentFilter);
        findViewById(R.id.iv_title_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuWindow = new SelectPicPopupWindow(getActivity(), itemsOnClick);
                menuWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                menuWindow.showAsDropDown(v,
                        -(menuWindow.getContentView().getMeasuredWidth() - v.getWidth() / 2 - 40),
                        0);
            }
        });

        //??????
        if (coreManager.readConfigBean().getIsOpenActivity() == 0) {
            RelativeLayout rlActivity = findViewById(R.id.local_activity_rl);
//            rlActivity.setVisibility(View.VISIBLE);
            rlActivity.setVisibility(View.GONE);
            rlActivity.setOnClickListener(this);
            TextView tvActivity = findViewById(R.id.my_activity_tv);
            tvActivity.setText(coreManager.readConfigBean().getActivityName());
        }

        //????????????
        if (coreManager.readConfigBean().getIsOpenWithdrawlApply() == 0) {
            RelativeLayout rlWithdrawal = findViewById(R.id.local_withdrawal_rl);
            rlWithdrawal.setVisibility(View.VISIBLE);
            rlWithdrawal.setVisibility(View.GONE);
            rlWithdrawal.setOnClickListener(this);
            TextView tvWithdrawal = findViewById(R.id.my_withdrawal_tv);
            tvWithdrawal.setText("????????????");
        }

        //?????????
        findViewById(R.id.rlt_discover).setOnClickListener(this);
        findViewById(R.id.rlt_scan_qr_code).setOnClickListener(this);
        findViewById(R.id.rlt_tag).setOnClickListener(this);
        findViewById(R.id.rlt_official_accounts).setOnClickListener(this);
        findViewById(R.id.rlt_neary).setOnClickListener(this);
    }


    private void initTitleBackground() {

//        findViewById(R.id.tool_bar).setBackgroundColor(skin.getAccentColor());
//        SkinUtils.Skin skin = SkinUtils.getSkin(getActivity());
//        ((FLYMainActivity) getActivity()).setStatusBarLight(skin.isLight());
    }

    @Override
    public void onClick(View v) {
        if (!UiUtils.isNormalClick(v)) {
            return;
        }
        int id = v.getId();
        switch (id) {
            case R.id.info_rl:
                // ????????????
                startActivityForResult(new Intent(getActivity(), BasicInfoEditActivity.class), 1);
                break;
            case R.id.meeting_rl:
                // ????????????
                SelectContactsActivity.startQuicklyInitiateMeeting(requireContext());
                break;

            case R.id.my_monry:
                // ????????????
                startActivity(new Intent(getActivity(), WxPayBlance.class));
                break;
            case R.id.my_space_rl:
                // ????????????
                Intent intent = new Intent(getActivity(), BusinessCircleActivity.class);
                intent.putExtra(FLYAppConstant.EXTRA_CIRCLE_TYPE, FLYAppConstant.CIRCLE_TYPE_PERSONAL_SPACE);
                startActivity(intent);
                break;
            case R.id.my_collection_rl:
                // ????????????
                startActivity(new Intent(getActivity(), MyCollection.class));
                break;
            case R.id.local_course_rl:
                // ????????????
                startActivity(new Intent(getActivity(), LocalCourseActivity.class));
                break;
            case R.id.setting_rl:
                // ??????
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
            case R.id.local_activity_rl:
                // ??????
                Intent activityIntent = new Intent(getActivity(), WebViewActivity.class);
                activityIntent.putExtra(WebViewActivity.EXTRA_URL, coreManager.readConfigBean().getActivityUrl());
                startActivity(activityIntent);
                break;
            case R.id.local_withdrawal_rl:
                // ????????????
                startActivity(new Intent(getActivity(), OfferApplyActivity.class));
                break;
            case R.id.rlt_qr_code:
                Intent qrIntent = new Intent(getContext(), QRcodeActivity.class);
                qrIntent.putExtra("isgroup", false);
                if (!TextUtils.isEmpty(coreManager.getSelf().getAccount())) {
                    qrIntent.putExtra("userid", coreManager.getSelf().getAccount());
                } else {
                    qrIntent.putExtra("userid", coreManager.getSelf().getUserId());
                }
                qrIntent.putExtra("userAvatar", coreManager.getSelf().getUserId());
                qrIntent.putExtra("nickName", coreManager.getSelf().getNickName());
                qrIntent.putExtra("sex", coreManager.getSelf().getSex());
                startActivity(qrIntent);
                break;
            case R.id.rlt_safe_settings:
                startActivity(new Intent(getContext(), AccountAndSafeActivity.class));
                break;
            case R.id.rlt_customer_service:
                Friend friend = FriendDao.getInstance().getFriend(coreManager.getSelf().getUserId(), Friend.ID_SYSTEM_NOTIFICATION);
                if (friend != null) {
                    ChatActivity.start(getContext(), friend);
                }
                break;
            case R.id.rlt_third_service:
                startActivity(new Intent(getContext(), ThirdServiceActivity.class));
                break;
            case R.id.rlt_about_us:
                startActivity(new Intent(getContext(), AboutActivity.class));
                break;
            case R.id.rlt_read_envelope:
                RedPacketListActivity.actionStart(getContext());
                break;
        }

        if (v.getId() == R.id.rlt_scan_qr_code) {
            FLYMainActivity.requestQrCodeScan(getActivity());
        } else if (v.getId() == R.id.rlt_discover) {//?????????
            startActivity(new Intent(getContext(), LifeCircleActivity.class));
        } else if (v.getId() == R.id.rlt_tag) {//??????
            LabelActivityNewUI.start(requireContext());
        } else if (v.getId() == R.id.rlt_official_accounts) {//?????????
            Intent intentNotice = new Intent(getActivity(), PublishNumberActivity.class);
            getActivity().startActivity(intentNotice);
//        } else if (v.getId() == R.id.rlt_money) {//?????????
//            PaymentOrReceiptActivity.start(getActivity(), coreManager.getSelf().getUserId());
        } else if (v.getId() == R.id.rlt_neary) {
            startActivity(new Intent(getActivity(), NearPersonActivity.class));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 || resultCode == Activity.RESULT_OK) {// ?????????????????????
            updateUI();
        }
    }

    /**
     * ?????????????????????????????????ui??????
     */
    private void updateUI() {
        if (mAvatarImg != null) {
            AvatarHelper.getInstance().displayAvatar(coreManager.getSelf().getUserId(), mAvatarImg, true);
        }
        if (mNickNameTv != null) {
            mNickNameTv.setText(coreManager.getSelf().getNickName());
        }

        if (mPhoneNumTv != null) {
            String phoneNumber = UserSp.getInstance(getContext()).getValue("account", "");
            mPhoneNumTv.setText(phoneNumber);
        }
    }


}
