package com.ktw.bitbit.ui.circle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.ktw.bitbit.FLYAppConfig;
import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.adapter.PublicMessageRecyclerAdapter;
import com.ktw.bitbit.audio1.VoicePlayer;
import com.ktw.bitbit.bean.Friend;
import com.ktw.bitbit.bean.circle.Comment;
import com.ktw.bitbit.bean.circle.PublicMessage;
import com.ktw.bitbit.db.dao.CircleMessageDao;
import com.ktw.bitbit.db.dao.FriendDao;
import com.ktw.bitbit.downloader.Downloader;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.helper.ImageLoadHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.circle.range.NewZanActivity;
import com.ktw.bitbit.ui.circle.range.SendAudioActivity;
import com.ktw.bitbit.ui.circle.range.SendFileActivity;
import com.ktw.bitbit.ui.circle.range.SendShuoshuoActivity;
import com.ktw.bitbit.ui.circle.range.SendVideoActivity;
import com.ktw.bitbit.ui.circle.util.RefreshListImp;
import com.ktw.bitbit.ui.other.BasicInfoActivity;
import com.ktw.bitbit.util.StringUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.view.MergerStatus;
import com.ktw.bitbit.view.PMsgBottomView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fm.jiecao.jcvideoplayer_lib.JCMediaManager;
import fm.jiecao.jcvideoplayer_lib.JVCideoPlayerStandardSecond;
import okhttp3.Call;

/**
 * ???????????????
 */
public class BusinessCircleActivity extends BaseActivity implements showCEView, RefreshListImp {
    private static final int REQUEST_CODE_SEND_MSG = 1;
    // ????????????????????????
    SelectPicPopupWindow menuWindow;
    /**
     * ??????,????????????????????????,???????????????????????????????????????
     */
    ListenerAudio listener;
    CommentReplyCache mCommentReplyCache = null;
    private int mType;
    /* mPageIndex??????????????????????????? */
    private int mPageIndex = 0;
    /* ???????????? */
    private View mMyCoverView;   // ??????root view
    private ImageView mCoverImg; // ????????????ImageView
    private ImageView mAvatarImg;// ????????????
    private TextView tv_user_name;
    private PMsgBottomView mPMsgBottomView;
    private List<PublicMessage> mMessages = new ArrayList<>();
    private SmartRefreshLayout mRefreshLayout;
    private SwipeRecyclerView mPullToRefreshListView;
    private PublicMessageRecyclerAdapter mAdapter;
    private String mLoginUserId;       // ?????????????????????UserId
    private String mLoginNickName;// ???????????????????????????
    private boolean isdongtai;
    private String cricleid;
    private String pinglun;
    private String dianzan;
    /* ?????????????????????????????????????????????,??????????????????????????????????????? */
    private String mUserId;
    private String mNickName;
    private ImageView mIvTitleLeft;
    private TextView mTvTitle;
    private ImageView mIvTitleRight;
    private boolean showTitle = true;

    // ??????????????????????????????
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        public void onClick(View v) {
            menuWindow.dismiss();
            Intent intent = new Intent();
            switch (v.getId()) {
                case R.id.btn_send_picture:// ???????????????
                    intent.setClass(getApplicationContext(), SendShuoshuoActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.btn_send_voice:  // ????????????
                    intent.setClass(getApplicationContext(), SendAudioActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.btn_send_video:  // ????????????
                    intent.setClass(getApplicationContext(), SendVideoActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.btn_send_file:   // ????????????
                    intent.setClass(getApplicationContext(), SendFileActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.new_comment:     // ????????????
                    Intent intent2 = new Intent(getApplicationContext(), NewZanActivity.class);
                    intent2.putExtra("OpenALL", true);
                    startActivity(intent2);
                    break;
                default:
                    break;
            }
        }
    };
    private boolean more;
    private RelativeLayout rl_title;
    private MergerStatus mergerStatus;
    private View actionBar;
    private Friend mFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_circle);
        mLoginUserId = coreManager.getSelf().getUserId();
        mLoginNickName = coreManager.getSelf().getNickName();

        if (getIntent() != null) {
            mType = getIntent().getIntExtra(FLYAppConstant.EXTRA_CIRCLE_TYPE, FLYAppConstant.CIRCLE_TYPE_MY_BUSINESS);// ?????????????????????????????????
            mUserId = getIntent().getStringExtra(FLYAppConstant.EXTRA_USER_ID);
            mNickName = getIntent().getStringExtra(FLYAppConstant.EXTRA_NICK_NAME);
            mFriend = FriendDao.getInstance().getFriend(mLoginUserId, mUserId);

            pinglun = getIntent().getStringExtra("pinglun");
            dianzan = getIntent().getStringExtra("dianzan");
            isdongtai = getIntent().getBooleanExtra("isdongtai", false);
            cricleid = getIntent().getStringExtra("messageid");
        }

        if (!isMyBusiness()) {//?????????????????????????????????????????????mUserId??????????????????
            if (TextUtils.isEmpty(mUserId)) {// ?????????userId????????????????????????????????????????????????
                mUserId = mLoginUserId;
                mNickName = mLoginNickName;
            }
        }

       /* if (mUserId != null && mUserId.equals(mLoginUserId)) {
            String mLastMessage = PreferenceUtils.getString(this, "BUSINESS_CIRCLE_DATA");
            if (!TextUtils.isEmpty(mLastMessage)) {
                mMessages = JSON.parseArray(mLastMessage, PublicMessage.class);
            }
        }*/

        initActionBar();
        Downloader.getInstance().init(FLYApplication.getInstance().mAppDir + File.separator + coreManager.getSelf().getUserId()
                + File.separator + Environment.DIRECTORY_MOVIES);// ???????????????????????????
        initView();
    }

    private boolean isMyBusiness() {
        return mType == FLYAppConstant.CIRCLE_TYPE_MY_BUSINESS;
    }

    private boolean isMySpace() {
        return mLoginUserId.equals(mUserId);
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTvTitle = (TextView) findViewById(R.id.tv_title_center);
        mTvTitle.setText(mNickName);
        mIvTitleRight = (ImageView) findViewById(R.id.iv_title_right);
        if (mUserId.equals(mLoginUserId)) {// ???????????????????????????????????????
            mIvTitleRight.setImageResource(R.mipmap.more_icon);
            mIvTitleRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuWindow = new SelectPicPopupWindow(BusinessCircleActivity.this, itemsOnClick);
                    // ????????????????????????????????????????????????????????????
                    menuWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    // +x???,-x???,+y???,-y???
                    // pop??????????????????
                    menuWindow.showAsDropDown(v,
                            -(menuWindow.getContentView().getMeasuredWidth() - v.getWidth() / 2 - 40),
                            0);
                }
            });
        } else {
            findViewById(R.id.iv_title_add).setVisibility(View.GONE);
        }
    }

    private void initView() {
        mergerStatus = findViewById(R.id.mergerStatus);
        rl_title = findViewById(R.id.rl_title);
        mPullToRefreshListView = findViewById(R.id.recyclerView);
        mPullToRefreshListView.setLayoutManager(new LinearLayoutManager(this));
        initCoverView();
        mRefreshLayout = findViewById(R.id.refreshLayout);
        mPMsgBottomView = (PMsgBottomView) findViewById(R.id.bottom_view);
       /* mResizeLayout.setOnResizeListener(new ResizeLayout.OnResizeListener() {
            @Override
            public void OnResize(int w, int h, int oldw, int oldh) {
                if (oldh < h) {// ???????????????
                    mCommentReplyCache = null;
                    mPMsgBottomView.setHintText("");
                    mPMsgBottomView.reset();
                }
            }
        });*/

        mPMsgBottomView.setPMsgBottomListener(new PMsgBottomView.PMsgBottomListener() {
            @Override
            public void sendText(String text) {
                if (mCommentReplyCache != null) {
                    mCommentReplyCache.text = text;
                    addComment(mCommentReplyCache);
                    mPMsgBottomView.hide();
                }
            }
        });


        if (isdongtai) {
            // ???????????????????????????HeadView
            mPullToRefreshListView.addHeaderView(actionBar);

        } else {
            actionBar.setVisibility(View.INVISIBLE);
            mPullToRefreshListView.addHeaderView(actionBar);
        }

        mAdapter = new PublicMessageRecyclerAdapter(this, coreManager, mMessages);
        setListenerAudio(mAdapter);
        mPullToRefreshListView.setAdapter(mAdapter);

        if (isdongtai) {
            mRefreshLayout.setEnableRefresh(false);
            mRefreshLayout.setEnableLoadMore(false);
        }
        mRefreshLayout.setOnRefreshListener(refreshLayout -> {
            requestData(true);
        });
        mRefreshLayout.setOnLoadMoreListener(refreshLayout -> {
            requestData(false);
        });

        mPullToRefreshListView.addOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    int totalScroll;

                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int scrollState) {
                        if (mPMsgBottomView.getVisibility() != View.GONE) {
                            mPMsgBottomView.hide();
                        }
                    }

                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
//                        totalScroll += dy;
//                        if (totalScroll < 0) {
//                            totalScroll = 0;
//                        }
//                        rl_title.setAlpha(totalScroll > 500 ? 0 : 1f - (Float.valueOf(totalScroll) / 500.0f));
//                        mergerStatus.setAlpha(totalScroll > 500 ? 1f : Float.valueOf(totalScroll) / 500.0f);
//                        if (dy > 2) {
//                            startTranslateAnim(false);
//                        }
//
//                        //&& mMyCoverView.getTop() == 0
//                        if (dy < -4 && mMyCoverView.getTop() == 0) {
//                            startTranslateAnim(true);
//                        }
                    }
                });

        requestData(true);
    }

    private void initCoverView() {
        actionBar = LayoutInflater.from(this).inflate(R.layout.a_view_actionbar, mPullToRefreshListView, false);
        mMyCoverView = LayoutInflater.from(this).inflate(R.layout.space_cover_view, mPullToRefreshListView, false);
        mMyCoverView.findViewById(R.id.ll_btn_send).setVisibility(View.GONE);
        mCoverImg = (ImageView) mMyCoverView.findViewById(R.id.cover_img);
        mAvatarImg = (ImageView) mMyCoverView.findViewById(R.id.avatar_img);
        tv_user_name = (TextView) mMyCoverView.findViewById(R.id.tv_user_name);

        // ??????
        if (isMyBusiness() || isMySpace()) {
            AvatarHelper.getInstance().displayAvatar(mLoginNickName, mLoginUserId, mAvatarImg, true);
            // ????????????user???????????????????????????????????????????????????
            String bg = coreManager.getSelf().getMsgBackGroundUrl();

            tv_user_name.setText(mLoginNickName);
            if (!TextUtils.isEmpty(bg)) {
                ImageLoadHelper.loadImageDontAnimateWithPlaceholder(
                        this,
                        bg,
                        R.drawable.avatar_normal,
                        d -> {
                            mCoverImg.setImageDrawable(d);
                        }, e -> {
                            AvatarHelper.getInstance().displayAvatar(mLoginNickName, mLoginUserId, mCoverImg, false);
                        }
                );
            } else {
                AvatarHelper.getInstance().displayRoundAvatar(mLoginNickName, mLoginUserId, mCoverImg, false);
            }
        } else {
            if (mFriend != null && !TextUtils.isEmpty(mFriend.getRemarkName())) {
                tv_user_name.setText(mFriend.getRemarkName());
            } else {
                tv_user_name.setText(mNickName);
            }
            AvatarHelper.getInstance().displayAvatar(mNickName, mUserId, mAvatarImg, true);
            AvatarHelper.getInstance().displayRoundAvatar(mNickName, mUserId, mCoverImg, false);
        }
        mAvatarImg.setOnClickListener(v -> {// ?????????????????????
            if (isMyBusiness() || isMySpace()) {
                BasicInfoActivity.start(mContext, mLoginUserId);
            } else {
                BasicInfoActivity.start(mContext, mUserId);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (mPMsgBottomView != null && mPMsgBottomView.getVisibility() == View.VISIBLE) {
            mPMsgBottomView.hide();
        } else {
            // ?????????????????????????????????
            // ??????PublicMessageAdapter????????????activity, ???????????????
            if (JVCideoPlayerStandardSecond.backPress()) {
                JCMediaManager.instance().recoverMediaPlayer();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (listener != null) {
            listener.ideChange();
        }
        listener = null;
    }

    @Override
    public void finish() {
        VoicePlayer.instance().stop();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       /* if (mUserId.equals(mLoginUserId)) {
            if (mMessages != null && mMessages.size() > 0) {
                PreferenceUtils.putString(this, "BUSINESS_CIRCLE_DATA", JSON.toJSONString(mMessages));
            }
        }*/
    }

    public void startTranslateAnim(boolean show) {
        if (showTitle == show) {
            return;
        }
        showTitle = show;
        float fromy = -300;
        float toy = 0;

        if (!show) {
            fromy = 0;
            toy = -300;
        }
        TranslateAnimation animation = new TranslateAnimation(0, 0, fromy, toy);
        animation.setDuration(500);
        animation.setFillAfter(true);
    }

    public void setListenerAudio(ListenerAudio listener) {
        this.listener = listener;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SEND_MSG) {
            if (resultCode == Activity.RESULT_OK) {// ???????????????
                String messageId = data.getStringExtra(FLYAppConstant.EXTRA_MSG_ID);
                CircleMessageDao.getInstance().addMessage(mLoginUserId, messageId);
                requestData(true);
                removeNullTV();
            }
        }
    }

    /********** ????????????????????????????????? *********/

    /**
     * ??????????????????
     *
     * @param isPullDwonToRefersh ????????????????????????????????????
     */
    private void requestData(boolean isPullDwonToRefersh) {
        if (isMyBusiness()) {
            requestMyBusiness(isPullDwonToRefersh);
        } else {
            if (isdongtai) {
                if (isPullDwonToRefersh) {
                    more = true;
                }
                if (!more) {
                    // ToastUtil.showToast(getContext(), getString(R.string.tip_last_item));
                    mRefreshLayout.setNoMoreData(true);
                    refreshComplete();
                } else {
                    requestSpacedongtai(isPullDwonToRefersh);
                }
            } else {
                requestSpace(isPullDwonToRefersh);
            }
        }
    }

    /**
     * ??????????????????
     */
    private void refreshComplete() {
        mPullToRefreshListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.finishRefresh();
                mRefreshLayout.finishLoadMore();
            }
        }, 200);
    }

    private void requestMyBusiness(final boolean isPullDwonToRefersh) {
        if (isPullDwonToRefersh) {
            mPageIndex = 0;
        }
        List<String> msgIds = CircleMessageDao.getInstance().getCircleMessageIds(mLoginUserId, mPageIndex, FLYAppConfig.PAGE_SIZE);

        if (msgIds == null || msgIds.size() <= 0) {
            refreshComplete();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("ids", JSON.toJSONString(msgIds));

        HttpUtils.get().url(coreManager.getConfig().MSG_GETS)
                .params(params)
                .build()
                .execute(new ListCallback<PublicMessage>(PublicMessage.class) {
                    @Override
                    public void onResponse(com.xuan.xuanhttplibrary.okhttp.result.ArrayResult<PublicMessage> result) {
                        List<PublicMessage> data = result.getData();
                        if (isPullDwonToRefersh) {
                            mMessages.clear();
                        }
                        if (data != null && data.size() > 0) {// ??????????????????
                            mPageIndex++;
                            mMessages.addAll(data);
                        }
                        mAdapter.notifyDataSetChanged();

                        refreshComplete();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getApplicationContext());
                        refreshComplete();
                    }
                });
    }

    private void requestSpace(final boolean isPullDwonToRefersh) {
        String messageId = null;
        if (!isPullDwonToRefersh && mMessages.size() > 0) {
            messageId = mMessages.get(mMessages.size() - 1).getMessageId();
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", mUserId);
        params.put("flag", PublicMessage.FLAG_NORMAL + "");

        if (!TextUtils.isEmpty(messageId)) {
            if (isdongtai) {
                params.put("messageId", cricleid);
            } else {
                params.put("messageId", messageId);
            }
        }
        params.put("pageSize", String.valueOf(FLYAppConfig.PAGE_SIZE));

        HttpUtils.get().url(coreManager.getConfig().MSG_USER_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<PublicMessage>(PublicMessage.class) {
                    @Override
                    public void onResponse(ArrayResult<PublicMessage> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            List<PublicMessage> data = result.getData();
                            if (isPullDwonToRefersh) {
                                mMessages.clear();
                            }
                            if (data != null && data.size() > 0) {
                                mMessages.addAll(data);
                            }
                            more = !(data == null || data.size() < FLYAppConfig.PAGE_SIZE);
                            mAdapter.notifyDataSetChanged();

                            if (more) {
                                mRefreshLayout.resetNoMoreData();
                            } else {
                                mRefreshLayout.setNoMoreData(true);
                            }
                            refreshComplete();
                            if (mAdapter.getItemCount() == 0)
                                addNullTV2LV();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getApplicationContext());
                        refreshComplete();
                    }
                });
    }

    // ????????????&?????????
    private void requestSpacedongtai(final boolean isPullDwonToRefersh) {
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", cricleid);

        HttpUtils.get().url(coreManager.getConfig().MSG_GET)
                .params(params)
                .build()
                .execute(new BaseCallback<PublicMessage>(PublicMessage.class) {
                    @Override
                    public void onResponse(com.xuan.xuanhttplibrary.okhttp.result.ObjectResult<PublicMessage> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            PublicMessage datas = result.getData();
                            if (datas == null) {
                                // ????????????????????????????????????????????????????????????????????????????????????
                                ToastUtil.showToast(mContext, R.string.message_not_found);
                                finish();
                                return;
                            }
                            List<PublicMessage> datass = new ArrayList<>();
                            datass.add(datas);
                            if (isPullDwonToRefersh) {
                                mMessages.clear();
                            }
                            mMessages.addAll(datass);
                            mAdapter.notifyDataSetChanged();

                            refreshComplete();
                            if (mAdapter.getItemCount() == 0)
                                addNullTV2LV();
                        } else if (result.getResultCode() == 101002) {
                            finish();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getApplicationContext());
                        refreshComplete();
                    }
                });
    }

    public void showCommentEnterView(int messagePosition, String toUserId, String toNickname, String toShowName) {
        PublicMessage message = mMessages.get(messagePosition);
        if (message != null && message.getIsAllowComment() == 1) {
            Toast.makeText(mContext, getString(R.string.ban_comment), Toast.LENGTH_SHORT).show();
            return;
        }
        mCommentReplyCache = new CommentReplyCache();
        mCommentReplyCache.messagePosition = messagePosition;
        mCommentReplyCache.toUserId = toUserId;
        mCommentReplyCache.toNickname = toNickname;
        if (TextUtils.isEmpty(toUserId) || TextUtils.isEmpty(toNickname) || TextUtils.isEmpty(toShowName)) {
            mPMsgBottomView.setHintText("");
        } else {
            mPMsgBottomView.setHintText(getString(R.string.replay_text, toShowName));
        }
        mPMsgBottomView.show();
    }

    private void addComment(CommentReplyCache cache) {
        Comment comment = new Comment();
        comment.setUserId(mLoginUserId);
        comment.setNickName(mLoginNickName);
        comment.setToUserId(cache.toUserId);
        comment.setToNickname(cache.toNickname);
        comment.setBody(cache.text);
        addComment(cache.messagePosition, comment);
    }

    private void addComment(final int position, final Comment comment) {
        final PublicMessage message = mMessages.get(position);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", message.getMessageId());
        if (!TextUtils.isEmpty(comment.getToUserId())) {
            params.put("toUserId", comment.getToUserId());
        }
        if (!TextUtils.isEmpty(comment.getToNickname())) {
            params.put("toNickname", comment.getToNickname());
        }
        params.put("body", comment.getBody());

        HttpUtils.post().url(coreManager.getConfig().MSG_COMMENT_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(com.xuan.xuanhttplibrary.okhttp.result.ObjectResult<String> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            List<Comment> comments = message.getComments();
                            if (comments == null) {
                                comments = new ArrayList<>();
                                message.setComments(comments);
                            }
                            comment.setCommentId(result.getData());
                            comments.add(0, comment);
                            message.getCount().setComment(message.getCount().getComment() + 1);
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getApplicationContext());
                    }
                });
    }

    @Override
    public void showView(int messagePosition, String toUserId, String toNickname, String toShowName) {
        showCommentEnterView(messagePosition, toUserId, toNickname, toShowName);
    }

    @Override
    public void refreshAfterOperation(PublicMessage message) {
        int size = mMessages.size();
        for (int i = 0; i < size; i++) {
            if (StringUtils.strEquals(mMessages.get(i).getMessageId(), message.getMessageId())) {
                mMessages.set(i, message);
                mAdapter.setData(mMessages);
            }
        }
    }

    public void addNullTV2LV() {
        TextView nullTextView = new TextView(this);
        nullTextView.setTag("NullTV");
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int paddingSize = getResources().getDimensionPixelSize(R.dimen.NormalPadding);
        nullTextView.setPadding(0, paddingSize, 0, paddingSize);
        nullTextView.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        nullTextView.setGravity(Gravity.CENTER);

        nullTextView.setLayoutParams(lp);
        nullTextView.setText(getString(R.string.no_data_now));
        mPullToRefreshListView.addFooterView(nullTextView);
        mRefreshLayout.setEnableRefresh(false);
    }

    public void removeNullTV() {
        mPullToRefreshListView.removeFooterView(mPullToRefreshListView.findViewWithTag("NullTV"));
        mRefreshLayout.setEnableRefresh(true);
    }

    public interface ListenerAudio {
        void ideChange();
    }

    class CommentReplyCache {
        int messagePosition;// ?????????Position
        String toUserId;
        String toNickname;
        String text;
    }
}
