package com.ktw.bitbit.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.FLYReporter;
import com.ktw.bitbit.adapter.PublicMessageRecyclerAdapter;
import com.ktw.bitbit.bean.MyZan;
import com.ktw.bitbit.bean.circle.Comment;
import com.ktw.bitbit.bean.circle.PublicMessage;
import com.ktw.bitbit.bean.event.EventAvatarUploadSuccess;
import com.ktw.bitbit.bean.event.MessageEventHongdian;
import com.ktw.bitbit.db.dao.CircleMessageDao;
import com.ktw.bitbit.db.dao.MyZanDao;
import com.ktw.bitbit.db.dao.UserAvatarDao;
import com.ktw.bitbit.db.dao.UserDao;
import com.ktw.bitbit.downloader.Downloader;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.ImageLoadHelper;
import com.ktw.bitbit.ui.base.EasyFragment;
import com.ktw.bitbit.ui.circle.MessageEventComment;
import com.ktw.bitbit.ui.circle.MessageEventNotifyDynamic;
import com.ktw.bitbit.ui.circle.MessageEventReply;
import com.ktw.bitbit.ui.circle.SelectPicPopupWindow;
import com.ktw.bitbit.ui.circle.range.NewZanActivity;
import com.ktw.bitbit.ui.circle.range.SendAudioActivity;
import com.ktw.bitbit.ui.circle.range.SendFileActivity;
import com.ktw.bitbit.ui.circle.range.SendShuoshuoActivity;
import com.ktw.bitbit.ui.circle.range.SendVideoActivity;
import com.ktw.bitbit.ui.mucfile.UploadingHelper;
import com.ktw.bitbit.ui.other.BasicInfoActivity;
import com.ktw.bitbit.util.CameraUtil;
import com.ktw.bitbit.util.LogUtils;
import com.ktw.bitbit.util.StringUtils;
import com.ktw.bitbit.util.TimeUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.util.UiUtils;
import com.ktw.bitbit.view.MergerStatus;
import com.ktw.bitbit.view.TrillCommentInputDialog;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.MessageEvent;
import okhttp3.Call;

/**
 * ????????????Fragment
 * Created by Administrator
 */

public class DiscoverFragment extends EasyFragment {
    private static final int REQUEST_CODE_SEND_MSG = 1;
    private static final int REQUEST_CODE_PICK_PHOTO = 2;
    private static int PAGER_SIZE = 10;
    private String mUserId;
    private String mUserName;
    private TextView mTvTitle;
    private ImageView mIvTitleRight;
    private SelectPicPopupWindow menuWindow;
    // ??????
    private View mHeadView;
    private ImageView ivHeadBg, ivHead;
    // ??????...
    private LinearLayout mTipLl;
    private ImageView mTipIv;
    private TextView mTipTv;
    // ??????
    private SmartRefreshLayout mRefreshLayout;
    private SwipeRecyclerView mListView;
    private PublicMessageRecyclerAdapter mAdapter;
    private List<PublicMessage> mMessages = new ArrayList<>();
    private boolean more;
    private String messageId;
    private boolean showTitle = true;

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
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.btn_send_voice:
                    // ????????????
                    intent.setClass(getActivity(), SendAudioActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.btn_send_video:
                    // ????????????
                    intent.setClass(getActivity(), SendVideoActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.btn_send_file:
                    // ????????????
                    intent.setClass(getActivity(), SendFileActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SEND_MSG);
                    break;
                case R.id.new_comment:
                    // ????????????&???
                    Intent intent2 = new Intent(getActivity(), NewZanActivity.class);
                    intent2.putExtra("OpenALL", true);
                    startActivity(intent2);
                    mTipLl.setVisibility(View.GONE);
                    EventBus.getDefault().post(new MessageEventHongdian(0));
                    break;
                default:
                    break;
            }
        }
    };
    private MergerStatus mergerStatus;
    private RelativeLayout rl_title;

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_discover;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        initActionBar();
        Downloader.getInstance().init(FLYApplication.getInstance().mAppDir + File.separator + coreManager.getSelf().getUserId()
                + File.separator + Environment.DIRECTORY_MOVIES);// ???????????????????????????
        initViews();
        initData();
    }

    public void onDestroy() {
        super.onDestroy();
        // ???????????????????????????????????????
        JCVideoPlayer.releaseAllVideos();
        if (mAdapter != null) {
            mAdapter.stopVoice();
        }
        EventBus.getDefault().unregister(this);
    }


    private void initActionBar() {
//        if (coreManager.getConfig().newUi) {
//            findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    requireActivity().finish();
//                }
//            });
//            findViewById(R.id.iv_title_left_first).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    requireActivity().finish();
//                }
//            });
//        } else {
//            findViewById(R.id.iv_title_left).setVisibility(View.GONE);
//            findViewById(R.id.iv_title_left_first).setVisibility(View.GONE);
//        }

        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().finish();
            }
        });
        findViewById(R.id.iv_title_left_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().finish();
            }
        });

        mIvTitleRight = (ImageView) findViewById(R.id.iv_title_right);
        mIvTitleRight.setImageResource(R.mipmap.more_icon);
        mIvTitleRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuWindow = new SelectPicPopupWindow(getActivity(), itemsOnClick);
                menuWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                menuWindow.showAsDropDown(v,
                        -(menuWindow.getContentView().getMeasuredWidth() - v.getWidth() / 2 - 40),
                        0);
            }
        });
    }

    public void initViews() {
        more = true;
        mUserId = coreManager.getSelf().getUserId();
        mUserName = coreManager.getSelf().getNickName();
        mergerStatus = findViewById(R.id.mergerStatus);
        rl_title = findViewById(R.id.rl_title);
        // ---------------------------???????????????-----------------------
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mListView = findViewById(R.id.recyclerView);
        mListView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mHeadView = inflater.inflate(R.layout.space_cover_view, mListView, false);
        TextView tv_user_name = mHeadView.findViewById(R.id.tv_user_name);
        tv_user_name.setText(coreManager.getSelf().getNickName());
        ivHeadBg = (ImageView) mHeadView.findViewById(R.id.cover_img);
        ivHeadBg.setOnClickListener(v -> {
            if (UiUtils.isNormalClick(v)) {
                changeBackgroundImage();
            }
        });
        ivHead = (ImageView) mHeadView.findViewById(R.id.avatar_img);
        ivHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UiUtils.isNormalClick(v)) {
                    Intent intent = new Intent(getActivity(), BasicInfoActivity.class);
                    intent.putExtra(FLYAppConstant.EXTRA_USER_ID, mUserId);
                    startActivity(intent);
                }
            }
        });

        displayAvatar();

        mTipLl = (LinearLayout) mHeadView.findViewById(R.id.tip_ll);
        mTipIv = (ImageView) mHeadView.findViewById(R.id.tip_avatar);
        mTipTv = (TextView) mHeadView.findViewById(R.id.tip_content);
        mTipLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTipLl.setVisibility(View.GONE);
                EventBus.getDefault().post(new MessageEventHongdian(0));

                Intent intent = new Intent(getActivity(), NewZanActivity.class);
                intent.putExtra("OpenALL", false); // ??????????????????????????????
                startActivity(intent);
            }
        });

        // ---------------------------??????????????????-----------------------
        mRefreshLayout = findViewById(R.id.refreshLayout);
        mListView.addHeaderView(mHeadView);

        mRefreshLayout.setOnRefreshListener(refreshLayout -> {
            requestData(true);
        });
        mRefreshLayout.setOnLoadMoreListener(refreshLayout -> {
            requestData(false);
        });

        EventBus.getDefault().register(this);

        mHeadView.findViewById(R.id.btn_send_picture).setOnClickListener(itemsOnClick);
        mHeadView.findViewById(R.id.btn_send_voice).setOnClickListener(itemsOnClick);
        mHeadView.findViewById(R.id.btn_send_video).setOnClickListener(itemsOnClick);
        mHeadView.findViewById(R.id.btn_send_file).setOnClickListener(itemsOnClick);
        mHeadView.findViewById(R.id.new_comment).setOnClickListener(itemsOnClick);

        mListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int totalScroll;

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalScroll += dy;
                if (totalScroll < 0) {
                    totalScroll = 0;
                }
                rl_title.setAlpha(totalScroll > 500 ? 0 : 1f - (Float.valueOf(totalScroll) / 500.0f));
                mergerStatus.setAlpha(totalScroll > 500 ? 1f : Float.valueOf(totalScroll) / 500.0f);
                if (dy > 2) {
                    startTranslateAnim(false);
                }

                if (dy < -4 && mHeadView.getTop() == 0) {
                    startTranslateAnim(true);
                }
            }
        });
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

    private void changeBackgroundImage() {
        CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_PHOTO);
    }

    private void updateBackgroundImage(String path) {
        File bg = new File(path);
        if (!bg.exists()) {
            LogUtils.log(path);
            FLYReporter.unreachable();
            ToastUtil.showToast(requireContext(), R.string.image_not_found);
            return;
        }
        DialogHelper.showDefaulteMessageProgressDialog(requireActivity());
        UploadingHelper.upfile(coreManager.getSelfStatus().accessToken, coreManager.getSelf().getUserId(), new File(path), new UploadingHelper.OnUpFileListener() {
            @Override
            public void onSuccess(String url, String filePath) {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", coreManager.getSelfStatus().accessToken);
                params.put("msgBackGroundUrl", url);

                HttpUtils.get().url(coreManager.getConfig().USER_UPDATE)
                        .params(params)
                        .build()
                        .execute(new BaseCallback<Void>(Void.class) {

                            @Override
                            public void onResponse(ObjectResult<Void> result) {
                                DialogHelper.dismissProgressDialog();
                                coreManager.getSelf().setMsgBackGroundUrl(url);
                                UserDao.getInstance().updateMsgBackGroundUrl(coreManager.getSelf().getUserId(), url);
                                if (getContext() == null) {
                                    return;
                                }
                                displayAvatar();
                            }

                            @Override
                            public void onError(Call call, Exception e) {
                                DialogHelper.dismissProgressDialog();
                                if (getContext() == null) {
                                    return;
                                }
                                ToastUtil.showErrorNet(requireContext());
                            }
                        });
            }

            @Override
            public void onFailure(String err, String filePath) {
                DialogHelper.dismissProgressDialog();
                if (getContext() == null) {
                    return;
                }
                ToastUtil.showErrorNet(requireContext());
            }
        });

    }

    public void initData() {
        mAdapter = new PublicMessageRecyclerAdapter(getActivity(), coreManager, mMessages);
        mListView.setAdapter(mAdapter);
        requestData(true);
    }

    private void requestData(boolean isPullDownToRefresh) {
        if (isPullDownToRefresh) {// ????????????
            updateTip();
            messageId = null;
            more = true;
        }

        if (!more) {
            // ToastUtil.showToast(getContext(), getString(R.string.tip_last_item));
            mRefreshLayout.setNoMoreData(true);
            refreshComplete();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("pageSize", String.valueOf(PAGER_SIZE));
        if (messageId != null) {
            params.put("messageId", messageId);
        }

        HttpUtils.get().url(coreManager.getConfig().MSG_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<PublicMessage>(PublicMessage.class) {
                    @Override
                    public void onResponse(ArrayResult<PublicMessage> result) {
                        if (getContext() != null && Result.checkSuccess(requireContext(), result)) {
                            List<PublicMessage> data = result.getData();
                            if (isPullDownToRefresh) {
                                mMessages.clear();
                            }
                            if (data != null && data.size() > 0) {
                                mMessages.addAll(data);
                                // ???????????????????????????id
                                messageId = data.get(data.size() - 1).getMessageId();
                                if (data.size() == PAGER_SIZE) {
                                    more = true;
                                    mRefreshLayout.resetNoMoreData();
                                } else {
                                    // ?????????????????????10???????????????????????????
                                    more = false;
                                }
                            } else {
                                // ????????????????????????????????????????????????
                                more = false;
                            }
                            mAdapter.notifyDataSetChanged();
                            refreshComplete();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        if (getContext() != null) {
                            ToastUtil.showNetError(requireContext());
                            refreshComplete();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_SEND_MSG) {
            // ??????????????????,??????Fragment
            String messageId = data.getStringExtra(FLYAppConstant.EXTRA_MSG_ID);
            CircleMessageDao.getInstance().addMessage(mUserId, messageId);
            requestData(true);
        } else if (requestCode == REQUEST_CODE_PICK_PHOTO) {
            if (data != null && data.getData() != null) {
                String path = CameraUtil.getImagePathFromUri(requireContext(), data.getData());
                updateBackgroundImage(path);
            } else {
                ToastUtil.showToast(requireContext(), R.string.c_photo_album_failed);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(EventAvatarUploadSuccess message) {
        if (message.event) {// ??????????????????????????????????????????????????????????????????????????????????????????????????????
            displayAvatar();
        }
    }

    public void displayAvatar() {
        // ??????????????????
        AvatarHelper.getInstance().displayAvatar(mUserId, ivHead, true);
        // ????????????user???????????????????????????????????????????????????
        String bg = coreManager.getSelf().getMsgBackGroundUrl();
        if (TextUtils.isEmpty(bg)) {
            realDisplayAvatar();
        }
        ImageLoadHelper.loadImageDontAnimateWithPlaceholder(
                requireContext().getApplicationContext(),
                bg,
                R.drawable.avatar_normal,
                d -> {
                    ivHeadBg.setImageDrawable(d);
                }, e -> {
                    realDisplayAvatar();
                }
        );
    }

    private void realDisplayAvatar() {
        final String mOriginalUrl = AvatarHelper.getAvatarUrl(mUserId, false);
        if (!TextUtils.isEmpty(mOriginalUrl)) {
            String time = UserAvatarDao.getInstance().getUpdateTime(mUserId);
            ImageLoadHelper.loadImageSignatureDontAnimateWithPlaceHolder(
                    FLYApplication.getContext(),
                    mOriginalUrl,
                    R.drawable.avatar_normal,
                    time,
                    d -> {
                        ivHeadBg.setImageDrawable(d);
                    }, e -> {
                        Log.e("zq", "?????????????????????" + mOriginalUrl);
                    }
            );
        } else {
            Log.e("zq", "????????????????????????");// ????????????????????????
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEvent message) {
        if (message.message.equals("prepare")) {// ???????????????????????????????????????
            mAdapter.stopVoice();
        }
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(MessageEventNotifyDynamic message) {
        // ????????? || ?????? || ???????????? ?????? ????????????
        requestData(true);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventComment message) {
        if (message.event.equals("Comment") && message.pbmessage.getIsAllowComment() == 0) {
            TrillCommentInputDialog trillCommentInputDialog = new TrillCommentInputDialog(getActivity(),
                    getString(R.string.enter_pinlunt),
                    str -> {
                        Comment mComment = new Comment();
                        Comment comment = mComment.clone();
                        if (comment == null)
                            comment = new Comment();
                        comment.setBody(str);
                        comment.setUserId(mUserId);
                        comment.setNickName(mUserName);
                        comment.setTime(TimeUtils.sk_time_current_time());
                        addComment(message, comment);
                    });
            Window window = trillCommentInputDialog.getWindow();
            if (window != null) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);// ???????????????
                trillCommentInputDialog.show();
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.ban_comment), Toast.LENGTH_SHORT).show();
        }

    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEventReply message) {
        if (message.event.equals("Reply")) {
            TrillCommentInputDialog trillCommentInputDialog = new TrillCommentInputDialog(getActivity(), getString(R.string.replay) + "???" + message.comment.getNickName(),
                    str -> {
                        Comment mComment = new Comment();
                        Comment comment = mComment.clone();
                        if (comment == null)
                            comment = new Comment();
                        comment.setToUserId(message.comment.getUserId());
                        comment.setToNickname(message.comment.getNickName());
                        comment.setToBody(message.comment.getToBody());
                        comment.setBody(str);
                        comment.setUserId(mUserId);
                        comment.setNickName(mUserId);
                        comment.setTime(TimeUtils.sk_time_current_time());
                        Reply(message, comment);
                    });
            Window window = trillCommentInputDialog.getWindow();
            if (window != null) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);// ???????????????
                trillCommentInputDialog.show();
            }
        }
    }

    /**
     * ??????????????????
     */
    private void refreshComplete() {
        mListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.finishRefresh();
                mRefreshLayout.finishLoadMore();
            }
        }, 200);
    }

    public void updateTip() {
        int tipCount = MyZanDao.getInstance().getZanSize(coreManager.getSelf().getUserId());
        if (tipCount == 0) {
            mTipLl.setVisibility(View.GONE);
            EventBus.getDefault().post(new MessageEventHongdian(0));
        } else {
            List<MyZan> zanList = MyZanDao.getInstance().queryZan(coreManager.getSelf().getUserId());
            if (zanList == null || zanList.size() == 0) {
                return;
            }
            MyZan zan = zanList.get(zanList.size() - 1);
            AvatarHelper.getInstance().displayAvatar(zan.getFromUsername(), zan.getFromUserId(), mTipIv, true);
            mTipTv.setText(tipCount + getString(R.string.piece_new_message));
            mTipLl.setVisibility(View.VISIBLE);
            EventBus.getDefault().post(new MessageEventHongdian(tipCount));
        }
    }

    private void addComment(MessageEventComment message, final Comment comment) {
        String messageId = message.id;
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", messageId);
        if (comment.isReplaySomeBody()) {
            params.put("toUserId", comment.getToUserId() + "");
            params.put("toNickname", comment.getToNickname());
            params.put("toBody", comment.getToBody());
        }
        params.put("body", comment.getBody());

        HttpUtils.post().url(coreManager.getConfig().MSG_COMMENT_ADD)
                .params(params)
                .build()
                .execute(new BaseCallback<String>(String.class) {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        // ????????????
                        if (getContext() != null && Result.checkSuccess(requireContext(), result)) {
                            comment.setCommentId(result.getData());
                            message.pbmessage.setCommnet(message.pbmessage.getCommnet() + 1);
                            PublicMessageRecyclerAdapter.CommentAdapter adapter = (PublicMessageRecyclerAdapter.CommentAdapter) message.view.getAdapter();
                            adapter.addComment(comment);
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }

    /**
     * ??????
     */
    private void Reply(MessageEventReply event, final Comment comment) {
        final int position = event.id;
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
                    public void onResponse(ObjectResult<String> result) {
                        // ????????????
                        if (getContext() != null && Result.checkSuccess(requireContext(), result)) {
                            comment.setCommentId(result.getData());
                            message.setCommnet(message.getCommnet() + 1);
                            PublicMessageRecyclerAdapter.CommentAdapter adapter = (PublicMessageRecyclerAdapter.CommentAdapter) event.view.getAdapter();
                            adapter.addComment(comment);
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(getActivity());
                    }
                });
    }

    /**
     * ?????????????????????
     */
    public void showToCurrent(String mCommentId) {
        int pos = -1;
        for (int i = 0; i < mMessages.size(); i++) {
            if (StringUtils.strEquals(mCommentId, mMessages.get(i).getMessageId())) {
                pos = i + 2;
                break;
            }
        }
        // ????????????????????????????????????
        if (pos != -1) {
            mListView.scrollToPosition(pos);
        }
    }
}
