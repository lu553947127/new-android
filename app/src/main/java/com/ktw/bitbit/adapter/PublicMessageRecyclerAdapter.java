package com.ktw.bitbit.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputFilter;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.LogUtils;
import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.FLYReporter;
import com.ktw.bitbit.audio.AudioPalyer;
import com.ktw.bitbit.audio1.VoiceAnimView;
import com.ktw.bitbit.audio1.VoicePlayer;
import com.ktw.bitbit.bean.Friend;
import com.ktw.bitbit.bean.Report;
import com.ktw.bitbit.bean.circle.Comment;
import com.ktw.bitbit.bean.circle.Praise;
import com.ktw.bitbit.bean.circle.PublicMessage;
import com.ktw.bitbit.bean.circle.PublicMessage.Body;
import com.ktw.bitbit.bean.circle.PublicMessage.Resource;
import com.ktw.bitbit.bean.collection.Collectiion;
import com.ktw.bitbit.bean.collection.CollectionEvery;
import com.ktw.bitbit.db.dao.CircleMessageDao;
import com.ktw.bitbit.db.dao.FriendDao;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.helper.ImageLoadHelper;
import com.ktw.bitbit.ui.base.CoreManager;
import com.ktw.bitbit.ui.circle.BusinessCircleActivity;
import com.ktw.bitbit.ui.circle.BusinessCircleActivity.ListenerAudio;
import com.ktw.bitbit.ui.circle.LongTextShowActivity;
import com.ktw.bitbit.ui.circle.MessageEventComment;
import com.ktw.bitbit.ui.circle.MessageEventReply;
import com.ktw.bitbit.ui.circle.range.PraiseListActivity;
import com.ktw.bitbit.ui.map.MapActivity;
import com.ktw.bitbit.ui.me.MyCollection;
import com.ktw.bitbit.ui.mucfile.DownManager;
import com.ktw.bitbit.ui.mucfile.MucFileDetails;
import com.ktw.bitbit.ui.mucfile.XfileUtils;
import com.ktw.bitbit.ui.mucfile.bean.MucFileBean;
import com.ktw.bitbit.ui.other.BasicInfoActivity;
import com.ktw.bitbit.ui.tool.MultiImagePreviewActivity;
import com.ktw.bitbit.ui.tool.SingleImagePreviewActivity;
import com.ktw.bitbit.ui.tool.WebViewActivity;
import com.ktw.bitbit.util.HtmlUtils;
import com.ktw.bitbit.util.LinkMovementClickMethod;
import com.ktw.bitbit.util.StringUtils;
import com.ktw.bitbit.util.SystemUtil;
import com.ktw.bitbit.util.TimeUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.util.UiUtils;
import com.ktw.bitbit.util.UploadCacheUtils;
import com.ktw.bitbit.util.filter.EmojiInputFilter;
import com.ktw.bitbit.util.link.HttpTextView;
import com.ktw.bitbit.view.CheckableImageView;
import com.ktw.bitbit.view.MyGridView;
import com.ktw.bitbit.view.ReportDialog;
import com.ktw.bitbit.view.SelectionFrame;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.BaseCallback;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.ObjectResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import de.greenrobot.event.EventBus;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JVCideoPlayerStandardSecond;
import okhttp3.Call;

/**
 * 1.????????????
 * 2.???????????? adapter
 */
public class PublicMessageRecyclerAdapter extends RecyclerView.Adapter<PublicMessageRecyclerAdapter.ViewHolder> implements ListenerAudio {
    private static final int VIEW_TYPE_NORMAL_TEXT = 0;
    private static final int VIEW_TYPE_NORMAL_SINGLE_IMAGE = 2;
    private static final int VIEW_TYPE_NORMAL_MULTI_IMAGE = 4;
    private static final int VIEW_TYPE_NORMAL_VOICE = 6;
    private static final int VIEW_TYPE_NORMAL_VIDEO = 8;
    private static final int VIEW_TYPE_NORMAL_FILE = 10;
    // ???????????????
    private static final int VIEW_TYPE_NORMAL_LINK = 11;
    private Context mContext;
    private CoreManager coreManager;
    private List<PublicMessage> mMessages;
    private LayoutInflater mInflater;
    private String mLoginUserId;
    private String mLoginNickName;
    private ViewHolder mVoicePlayViewHolder;
    private AudioPalyer mAudioPalyer;
    private String mVoicePlayId = null;
    private Map<String, Boolean> mClickOpenMaps = new HashMap<>();
    private int collectionType;
    private OnItemClickListener onItemClickListener = null;
    /**
     * ??????getShowName???
     * ????????????????????????
     */
    private WeakHashMap<String, String> showNameCache = new WeakHashMap<>();

    public PublicMessageRecyclerAdapter(Context context, CoreManager coreManager, List<PublicMessage> messages) {
        setHasStableIds(true);
        mContext = context;
        this.coreManager = coreManager;
        mMessages = messages;
        setHasStableIds(true);
        mInflater = LayoutInflater.from(mContext);
        mLoginUserId = coreManager.getSelf().getUserId();
        mLoginNickName = coreManager.getSelf().getNickName();
        mAudioPalyer = new AudioPalyer();
        mAudioPalyer.setAudioPlayListener(new AudioPalyer.AudioPlayListener() {
            @Override
            public void onSeekComplete() {
            }

            @Override
            public void onPrepared() {
            }

            @Override
            public void onError() {
                mVoicePlayId = null;
                if (mVoicePlayViewHolder != null) {
                    updateVoiceViewHolderIconStatus(false, mVoicePlayViewHolder);
                }
                mVoicePlayViewHolder = null;
            }

            @Override
            public void onCompletion() {
                mVoicePlayId = null;
                if (mVoicePlayViewHolder != null) {
                    updateVoiceViewHolderIconStatus(false, mVoicePlayViewHolder);
                }
                mVoicePlayViewHolder = null;
            }

            @Override
            public void onBufferingUpdate(int percent) {
            }

            @Override
            public void onPreparing() {
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public long getItemId(int position) {
        return mMessages.get(position).getMessageId().hashCode();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View convertView = mInflater.inflate(R.layout.p_msg_item_main_body, viewGroup, false);
        View innerView = null;
        ViewHolder viewHolder;
        if (viewType == VIEW_TYPE_NORMAL_TEXT) {
            viewHolder = new NormalTextHolder(convertView);
        } else if (viewType == VIEW_TYPE_NORMAL_SINGLE_IMAGE) {
            NormalSingleImageHolder holder = new NormalSingleImageHolder(convertView);
            innerView = mInflater.inflate(R.layout.p_msg_item_normal_single_img, holder.content_fl, false);
            holder.image_view = (ImageView) innerView.findViewById(R.id.image_view);
            viewHolder = holder;
        } else if (viewType == VIEW_TYPE_NORMAL_MULTI_IMAGE) {
            NormalMultiImageHolder holder = new NormalMultiImageHolder(convertView);
            innerView = mInflater.inflate(R.layout.p_msg_item_normal_multi_img, holder.content_fl, false);
            holder.grid_view = (MyGridView) innerView.findViewById(R.id.grid_view);
            viewHolder = holder;
        } else if (viewType == VIEW_TYPE_NORMAL_VOICE) {
            NormalVoiceHolder holder = new NormalVoiceHolder(convertView);
            innerView = mInflater.inflate(R.layout.p_msg_item_normal_voice, holder.content_fl, false);
            holder.img_view = (ImageView) innerView.findViewById(R.id.img_view);
            holder.voice_action_img = (ImageView) innerView.findViewById(R.id.voice_action_img);
            holder.voice_desc_tv = (TextView) innerView.findViewById(R.id.voice_desc_tv);
            holder.chat_to_voice = (VoiceAnimView) innerView.findViewById(R.id.chat_to_voice);
            viewHolder = holder;
        } else if (viewType == VIEW_TYPE_NORMAL_VIDEO) {
            NormalVideoHolder holder = new NormalVideoHolder(convertView);
            innerView = mInflater.inflate(R.layout.p_msg_item_normal_video, holder.content_fl, false);
            holder.gridViewVideoPlayer = (JVCideoPlayerStandardSecond) innerView.findViewById(R.id.preview_video);
            viewHolder = holder;
        } else if (viewType == VIEW_TYPE_NORMAL_FILE) {
            NormalFileHolder holder = new NormalFileHolder(convertView);
            innerView = mInflater.inflate(R.layout.p_msg_item_normal_file, holder.content_fl, false);
            holder.file_click = (RelativeLayout) innerView.findViewById(R.id.collection_file);
            holder.file_image = (ImageView) innerView.findViewById(R.id.file_img);
            holder.text_tv = (TextView) innerView.findViewById(R.id.file_name);
            viewHolder = holder;
        } else if (viewType == VIEW_TYPE_NORMAL_LINK) {
            NormalLinkHolder holder = new NormalLinkHolder(convertView);
            innerView = mInflater.inflate(R.layout.p_msg_item_normal_link, holder.content_fl, false);
            holder.link_click = (LinearLayout) innerView.findViewById(R.id.link_ll);
            holder.link_image = (ImageView) innerView.findViewById(R.id.link_iv);
            holder.link_tv = (TextView) innerView.findViewById(R.id.link_text_tv);
            viewHolder = holder;
        } else {
            throw new IllegalStateException("unkown viewType: " + viewType);
        }

        if (collectionType == 1 || collectionType == 2) {
            // ?????????????????????????????????????????????????????? && ?????????
            viewHolder.llOperator.setVisibility(View.GONE);
        } else {
            viewHolder.llOperator.setVisibility(View.VISIBLE);
        }
        viewHolder.iv_prise = convertView.findViewById(R.id.iv_prise);
        viewHolder.multi_praise_tv = (TextView) convertView.findViewById(R.id.multi_praise_tv);
        viewHolder.tvLoadMore = (TextView) convertView.findViewById(R.id.tvLoadMore);
        viewHolder.line_v = convertView.findViewById(R.id.line_v);
        viewHolder.command_listView = (ListView) convertView.findViewById(R.id.command_listView);
        viewHolder.location_tv = (TextView) convertView.findViewById(R.id.location_tv);
        if (innerView != null) {
            viewHolder.content_fl.addView(innerView);
        }
        viewHolder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(viewHolder);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") int position) {
        int viewType = getItemViewType(position);
        // ???ViewHolder????????????????????????????????????????????????????????????final
        final ViewHolder finalHolder = viewHolder;
        // set data
        final PublicMessage message = mMessages.get(position);
        if (message == null) {
            return;
        }
        /* ???????????? */
        AvatarHelper.getInstance().displayAvatar(message.getUserId(), viewHolder.avatar_img);
        /* ???????????? */
        SpannableStringBuilder nickNamebuilder = new SpannableStringBuilder();
        final String userId = message.getUserId();
        String showName = getShowName(userId, message.getNickName());
        UserClickableSpan.setClickableSpan(mContext, nickNamebuilder, showName, message.getUserId());
        viewHolder.nick_name_tv.setText(nickNamebuilder);
        viewHolder.nick_name_tv.setLinksClickable(true);
        viewHolder.nick_name_tv.setMovementMethod(LinkMovementClickMethod.getInstance());

        // ???????????????????????????
        viewHolder.avatar_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isNormalClick(v)) {
                    return;
                }
                BasicInfoActivity.start(mContext, message.getUserId());
            }
        });

        // ???????????????????????????
        Body body = message.getBody();
        if (body == null) {
            return;
        }

        // ??????????????????
        boolean isForwarding = message.getSource() == PublicMessage.SOURCE_FORWARDING;

        // ??????body_tv
        // todo ????????????????????? ??????/???????????????????????????????????????????????????????????????????????????????????????????????????(????????????6???????????????????????????)
        if (TextUtils.isEmpty(body.getText())) {
            viewHolder.body_tv.setVisibility(View.GONE);
        } else {
            // ??????emoji??????
            viewHolder.body_tv.setFilters(new InputFilter[]{new EmojiInputFilter(mContext)});
            viewHolder.body_tv.setUrlText(body.getText());
            viewHolder.body_tv.setVisibility(View.VISIBLE);
        }
        // ??????????????????6???????????????????????????"??????"
        viewHolder.body_tv.post(() -> {
            Layout layout = viewHolder.body_tv.getLayout();
            if (layout != null) {
                int lines = layout.getLineCount();
                // setText??????setUrlText?????????layout.getEllipsisCount????????????????????????????????????
/*
                if (lines > 0) {
                    if (layout.getEllipsisCount(lines - 1) > 0) {
                        viewHolder.open_tv.setVisibility(View.VISIBLE);
                        viewHolder.open_tv.setOnClickListener(v -> LongTextShowActivity.start(mContext, body.getText()));
                    } else {
                        viewHolder.open_tv.setVisibility(View.GONE);
                        viewHolder.open_tv.setOnClickListener(null);
                    }
                }
*/
                if (lines > 6) {
                    viewHolder.open_tv.setVisibility(View.VISIBLE);
                    viewHolder.open_tv.setOnClickListener(v -> LongTextShowActivity.start(mContext, body.getText()));
                } else {
                    viewHolder.open_tv.setVisibility(View.GONE);
                    viewHolder.open_tv.setOnClickListener(null);
                }
            }
        });

        viewHolder.body_tv.setOnLongClickListener(v -> {
            showBodyTextLongClickDialog(body.getText());
            return false;
        });

        // ?????????????????? MyCollection
        viewHolder.time_tv.setText(TimeUtils.getFriendlyTimeDesc(mContext, (int) message.getTime()));
        if (MyCollection.class.toString().contains(mContext.getClass().toString())) {
            // ????????????????????????
            viewHolder.delete_tv.setText(mContext.getString(R.string.cancel_collection));
            viewHolder.llReport.setVisibility(View.GONE);
        } else {
            viewHolder.llReport.setVisibility(View.VISIBLE);
            viewHolder.delete_tv.setText(mContext.getString(R.string.delete));
        }
        if (collectionType == 1) {
            viewHolder.delete_tv.setVisibility(View.VISIBLE);
            viewHolder.delete_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteMsgDialog(position);
                }
            });
        } else if (collectionType == 2) {
            viewHolder.delete_tv.setVisibility(View.GONE);
        } else {
            if (userId.equals(mLoginUserId)) {
                // ??????????????????
                viewHolder.delete_tv.setVisibility(View.VISIBLE);
                viewHolder.delete_tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDeleteMsgDialog(position);
                    }
                });
            } else {
                viewHolder.delete_tv.setVisibility(View.GONE);
                viewHolder.delete_tv.setOnClickListener(null);
            }
        }

        final ViewHolder vh = viewHolder;
        vh.ivThumb.setChecked(1 == message.getIsPraise());
        vh.tvThumb.setText(String.valueOf(message.getPraise()));
        vh.llThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ?????????????????????
                final boolean isPraise = vh.ivThumb.isChecked();
                // ?????????????????????????????????????????????????????????
                onPraise(position, !isPraise);
                // ?????????????????????????????????????????????
                int praiseCount = message.getPraise();
                if (isPraise) {
                    praiseCount--;
                } else {
                    praiseCount++;
                }
                vh.tvThumb.setText(String.valueOf(praiseCount));
                vh.ivThumb.toggle();
            }
        });
        // ?????????????????????
        // TODO: ?????????????????????????????????
        boolean isComment = false;
        if (message.getComments() != null) {
            for (Comment comment : message.getComments()) {
                if (mLoginUserId.equals(comment.getUserId())) {
                    isComment = true;
                }
            }
        }
        vh.ivComment.setChecked(isComment);
        vh.tvComment.setText(String.valueOf(message.getCommnet()));
        vh.llComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ??????????????????????????????
                onComment(position, vh.command_listView);
                // ?????????????????????????????????????????????
            }
        });
        vh.ivCollection.setChecked(1 == message.getIsCollect());
        vh.llCollection.setOnClickListener(v -> {
            onCollection(position);
        });
        vh.llReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReport(position);
            }
        });

        /* ????????????????????? */
        List<Praise> praises = message.getPraises();
        if (praises != null && praises.size() > 0) {
            viewHolder.multi_praise_tv.setVisibility(View.VISIBLE);
            viewHolder.iv_prise.setVisibility(View.VISIBLE);
            SpannableStringBuilder builder = new SpannableStringBuilder();
            for (int i = 0; i < praises.size(); i++) {
                String praiseName = getShowName(praises.get(i).getUserId(), praises.get(i).getNickName());
                UserClickableSpan.setClickableSpan(mContext, builder, praiseName, praises.get(i).getUserId());
                if (i < praises.size() - 1)
                    builder.append(",");
            }
            if (message.getPraise() > praises.size()) {
                builder.append(mContext.getString(R.string.praise_ending_place_holder, message.getPraise()));
            }
            viewHolder.multi_praise_tv.setText(builder);
        } else {
            viewHolder.iv_prise.setVisibility(View.GONE);
            viewHolder.multi_praise_tv.setVisibility(View.GONE);
            viewHolder.multi_praise_tv.setText("");
        }
        viewHolder.multi_praise_tv.setLinksClickable(true);
        viewHolder.multi_praise_tv.setMovementMethod(LinkMovementClickMethod.getInstance());
        viewHolder.multi_praise_tv.setOnClickListener(v -> {
            PraiseListActivity.start(mContext, message.getMessageId());
        });

        /* ???????????? */
        final List<Comment> comments = message.getComments();
        viewHolder.command_listView.setVisibility(View.VISIBLE);
        CommentAdapter adapter = new CommentAdapter(position, comments);
        viewHolder.command_listView.setAdapter(adapter);
        viewHolder.tvLoadMore.setVisibility(View.GONE);
        if (comments != null && comments.size() > 0) {
            if (message.getCommnet() > comments.size()) {
                // ?????????????????????
                viewHolder.tvLoadMore.setVisibility(View.VISIBLE);
                viewHolder.tvLoadMore.setOnClickListener(v -> {
                    loadCommentsNextPage(vh.tvLoadMore, message.getMessageId(), adapter);
                });
            }
        }

        // ???????????????????????????????????????????????????
        if (praises != null && praises.size() > 0 && comments != null && comments.size() > 0) {
            viewHolder.line_v.setVisibility(View.VISIBLE);
        } else {
            viewHolder.line_v.setVisibility(View.INVISIBLE);
        }

/*
        mAdapter = (CommentAdapter) viewHolder.command_listView.getAdapter();
        if (mAdapter == null) {
            mAdapter = new CommentAdapter();
            viewHolder.command_listView.setAdapter(mAdapter);
        }

        if (comments != null && comments.size() > 0) {
            viewHolder.line_v.setVisibility(View.VISIBLE);
            viewHolder.command_listView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.line_v.setVisibility(View.GONE);
            viewHolder.command_listView.setVisibility(View.GONE);
        }
        mAdapter.setData(position, comments);
*/

        if (!TextUtils.isEmpty(message.getLocation())) {
            viewHolder.location_tv.setText(message.getLocation());
            viewHolder.location_tv.setVisibility(View.VISIBLE);
        } else {
            viewHolder.location_tv.setVisibility(View.GONE);
        }

        viewHolder.location_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MapActivity.class);
                intent.putExtra("latitude", message.getLatitude());
                intent.putExtra("longitude", message.getLongitude());
                intent.putExtra("userName", message.getLocation());
                mContext.startActivity(intent);
            }
        });

        // //////////////////??????????????????????????????????????????Type???????????????/////////////////////////////////////////
        // ???????????????????????????????????????text
        SpannableStringBuilder forwardingBuilder = null;
        if (isForwarding) {// ??????????????????????????????
            forwardingBuilder = new SpannableStringBuilder();
            String forwardName = getShowName(message.getFowardUserId(), message.getFowardNickname());
            UserClickableSpan.setClickableSpan(mContext, forwardingBuilder, forwardName, message.getFowardUserId());
            if (!TextUtils.isEmpty(message.getFowardText())) {
                forwardingBuilder.append(" : ");
                forwardingBuilder.append(message.getFowardText());
            }
        }
        if (viewType == VIEW_TYPE_NORMAL_TEXT) {
            viewHolder.content_fl.setVisibility(View.GONE);
        } else if (viewType == VIEW_TYPE_NORMAL_SINGLE_IMAGE) {
            ImageView image_view = ((NormalSingleImageHolder) viewHolder).image_view;
            String url = message.getFirstImageOriginal();
            if (!TextUtils.isEmpty(url)) {
                if (url.endsWith(".gif")) {
                    ImageLoadHelper.showGifWithPlaceHolder(
                            mContext,
                            url,
                            R.drawable.default_gray,
                            R.drawable.image_download_fail_icon,
                            image_view
                    );
                } else {
                    ImageLoadHelper.showImageCenterCrop(
                            mContext,
                            url,
                            R.drawable.default_gray,
                            R.drawable.image_download_fail_icon,
                            image_view
                    );
                }
                image_view.setOnClickListener(new SingleImageClickListener(url));
                image_view.setVisibility(View.VISIBLE);
            } else {
                image_view.setImageBitmap(null);
                image_view.setVisibility(View.GONE);
            }
        } else if (viewType == VIEW_TYPE_NORMAL_MULTI_IMAGE) {
            MyGridView grid_view = ((NormalMultiImageHolder) viewHolder).grid_view;
            if (body.getImages() != null) {
                grid_view.setAdapter(new ImagesInnerGridViewAdapter(mContext, body.getImages()));
                grid_view.setOnItemClickListener(new MultipleImagesClickListener(body.getImages()));
            } else {
                grid_view.setAdapter(null);
            }
        } else if (viewType == VIEW_TYPE_NORMAL_VOICE) {
            // ???????????????????????????????????????????????????
            // ????????????????????????????????????????????????
            final NormalVoiceHolder holder = (NormalVoiceHolder) viewHolder;
            holder.chat_to_voice.fillData(message);
            holder.chat_to_voice.setOnClickListener(v -> {
                VoicePlayer.instance().playVoice(holder.chat_to_voice);
            });
        } else if (viewType == VIEW_TYPE_NORMAL_VIDEO) {
            NormalVideoHolder holder = (NormalVideoHolder) viewHolder;
            String imageUrl = message.getFirstImageOriginal();
            // ???????????????????????????????????????????????????
            String videoUrl = UploadCacheUtils.get(mContext, message.getFirstVideo());
            if (!TextUtils.isEmpty(videoUrl)) {
                if (videoUrl.equals(message.getFirstVideo())) {
                    // ?????????????????????????????????????????????????????????????????????
                    videoUrl = FLYApplication.getProxy(mContext).getProxyUrl(message.getFirstVideo());
                }
                holder.gridViewVideoPlayer.setUp(videoUrl,
                        JVCideoPlayerStandardSecond.SCREEN_LAYOUT_NORMAL, "");
            }
            if (TextUtils.isEmpty(imageUrl)) {
                AvatarHelper.getInstance().asyncDisplayOnlineVideoThumb(videoUrl, holder.gridViewVideoPlayer.thumbImageView);
            } else {
                ImageLoadHelper.showImageWithPlaceHolder(
                        mContext,
                        imageUrl,
                        R.drawable.default_gray,
                        R.drawable.default_gray,
                        holder.gridViewVideoPlayer.thumbImageView
                );
            }
        } else if (viewType == VIEW_TYPE_NORMAL_FILE) {
            // ??????
            NormalFileHolder holder = (NormalFileHolder) viewHolder;
            final String mFileUrl = message.getFirstFile();

            if (TextUtils.isEmpty(mFileUrl)) {
                return;
            }
            // ??????????????????????????????fileName,
            if (!TextUtils.isEmpty(message.getFileName())) {
                holder.text_tv.setText(mContext.getString(R.string.msg_file) + message.getFileName());
            } else {
                try {
                    message.setFileName(mFileUrl.substring(mFileUrl.lastIndexOf('/') + 1));
                    holder.text_tv.setText(mContext.getString(R.string.msg_file) + message.getFileName());
                } catch (Exception ignored) {
                    // ??????url????????????????????????/???????????????????????????url,
                    holder.text_tv.setText(mContext.getString(R.string.msg_file) + mFileUrl);
                }
            }

            String suffix = "";
            int index = mFileUrl.lastIndexOf(".");
            if (index != -1) {
                suffix = mFileUrl.substring(index + 1).toLowerCase();
                if (suffix.equals("png") || suffix.equals("jpg")) {
                    ImageLoadHelper.showImageWithSize(
                            mContext,
                            mFileUrl,
                            100, 100,
                            holder.file_image
                    );
                } else {
                    AvatarHelper.getInstance().fillFileView(suffix, holder.file_image);
                }
            }

            final long size = message.getBody().getFiles().get(0).getSize();
            Log.e("xuan", "setOnClickListener: " + size);

            holder.file_click.setOnClickListener(v -> intentPreviewFile(mFileUrl, message.getFileName(), message.getNickName(), size));
        } else if (viewType == VIEW_TYPE_NORMAL_LINK) {
            NormalLinkHolder holder = (NormalLinkHolder) viewHolder;
            if (TextUtils.isEmpty(message.getBody().getSdkIcon())) {
                holder.link_image.setImageResource(R.drawable.browser);
            } else {
                AvatarHelper.getInstance().displayUrl(message.getBody().getSdkIcon(), holder.link_image);
            }
            holder.link_tv.setText(message.getBody().getSdkTitle());

            holder.link_click.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, message.getBody().getSdkUrl());
                mContext.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    /**
     * @see PublicMessage#getType() <br/>
     * 1=???????????????2=???????????????3=??????????????? 4=???????????????5?????????<br/>
     * ???????????????????????? <br/>
     * {@link #VIEW_TYPE_NORMAL_TEXT}0???????????????????????????<br/>
     * {@link #VIEW_TYPE_NORMAL_SINGLE_IMAGE} 2??????????????????????????????<br/>
     * {@link #VIEW_TYPE_NORMAL_MULTI_IMAGE}4??????????????????????????????<br/>
     * {@link #VIEW_TYPE_NORMAL_VOICE} 6?????????????????????<br/>
     * {@link #VIEW_TYPE_NORMAL_VIDEO}8?????????????????????<br/>
     */

    public void reset() {
        stopVoice();
    }

    @Override
    public int getItemViewType(int position) {
        PublicMessage message = mMessages.get(position);
//        boolean fromSelf = message.getSource() == PublicMessage.SOURCE_SELF;
        if (message == null || message.getBody() == null) {
            // ????????????????????????????????????????????????????????????????????????????????????
            return VIEW_TYPE_NORMAL_TEXT;
        }
        Body body = message.getBody();
        if (message.getIsAllowComment() == 1) {
            message.setIsAllowComment(1);
        } else {
            message.setIsAllowComment(0);
        }
        if (body.getType() == PublicMessage.TYPE_TEXT) {
            // ????????????
            return VIEW_TYPE_NORMAL_TEXT;
        } else if (body.getType() == PublicMessage.TYPE_IMG) {
            if (body.getImages() == null || body.getImages().size() == 0) {
                // ????????????????????????????????????????????????????????????????????????????????????
                body.setType(PublicMessage.TYPE_TEXT);
                // ????????????
                return VIEW_TYPE_NORMAL_TEXT;
            } else if (body.getImages().size() <= 1) {
                // ??????????????????????????????
                return VIEW_TYPE_NORMAL_SINGLE_IMAGE;
            } else {// ???????????????????????????
                return VIEW_TYPE_NORMAL_MULTI_IMAGE;
            }
        } else if (body.getType() == PublicMessage.TYPE_VOICE) {// ????????????
            return VIEW_TYPE_NORMAL_VOICE;
        } else if (body.getType() == PublicMessage.TYPE_VIDEO) {// ????????????
            return VIEW_TYPE_NORMAL_VIDEO;
        } else if (body.getType() == PublicMessage.TYPE_FILE) {
            // ??????
            return VIEW_TYPE_NORMAL_FILE;
        } else if (body.getType() == PublicMessage.TYPE_LINK) {
            // ??????
            return VIEW_TYPE_NORMAL_LINK;
        } else {
            // ?????????????????????
            return VIEW_TYPE_NORMAL_TEXT;
        }
    }

    /**
     * ?????????????????????
     *
     * @param filePath
     */
    private void intentPreviewFile(String filePath, String fileName, String fromName, long size) {
        MucFileBean data = new MucFileBean();

        // ??????????????????
        int start = filePath.lastIndexOf(".");
        String suffix = start > -1 ? filePath.substring(start + 1).toLowerCase() : "";

        int fileType = XfileUtils.getFileType(suffix);
        data.setNickname(fromName);
        data.setUrl(filePath);
        data.setName(fileName);
        data.setSize(size);
        data.setState(DownManager.STATE_UNDOWNLOAD);
        data.setType(fileType);
        Intent intent = new Intent(mContext, MucFileDetails.class);
        intent.putExtra("data", data);
        mContext.startActivity(intent);
    }

    /**
     * ????????????????????????????????????????????????????????????????????????
     */
    private void loadCommentsNextPage(TextView view, String messageId, CommentAdapter adapter) {
        // isLoading?????????noMore?????????????????????????????????????????????isLoading???false,
        if (adapter.isLoading()) {
            return;
        }
        adapter.setLoading(true);
        // ?????????20??? ???????????????????????????????????????????????????????????????20??????
        int pageSize = 20;
        // ???20??????????????????????????????index==1, 21????????????????????????????????????????????????????????????
        int index = (adapter.getCount() + (pageSize - 1)) / pageSize;
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("pageIndex", String.valueOf(index));
        params.put("pageSize", String.valueOf(pageSize));
        params.put("messageId", messageId);

        String url = coreManager.getConfig().MSG_COMMENT_LIST;

        view.setTag(messageId);
        HttpUtils.get().url(url)
                .params(params)
                .build()
                .execute(new ListCallback<Comment>(Comment.class) {
                    @Override
                    public void onResponse(ArrayResult<Comment> result) {
                        List<Comment> data = result.getData();
                        if (data.size() > 0) {
                            adapter.addAll(data);
                            adapter.setLoading(false);
                        } else {
                            ToastUtil.showToast(mContext, R.string.tip_no_more);
                            if (view.getTag() == messageId) {
                                // ?????????????????????
                                view.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        FLYReporter.post("???????????????????????????", e);
                        ToastUtil.showToast(mContext, mContext.getString(R.string.tip_comment_load_error));
                    }
                });

    }

    private String getShowName(String userId, String defaultName) {
        String cache = showNameCache.get(userId);
        if (!TextUtils.isEmpty(cache)) {
            return cache;
        }
        String showName = "";

        if (userId.equals(mLoginUserId)) {
            showName = coreManager.getSelf().getNickName();
        } else {
            Friend friend = FriendDao.getInstance().getFriend(mLoginUserId, userId);
            if (friend != null) {
                showName = TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName();
            }
        }

        if (TextUtils.isEmpty(showName)) {
            showName = defaultName;
        }
        showNameCache.put(userId, showName);
        return showName;
    }

    /* ???????????? */
    private void showDeleteMsgDialog(final int position) {
        SelectionFrame selectionFrame = new SelectionFrame(mContext);
        int tip;
        if (mContext instanceof MyCollection) {
            tip = R.string.sure_cancel_collection;
        } else {
            tip = R.string.delete_prompt;
        }
        selectionFrame.setSomething(null, mContext.getString(tip), new SelectionFrame.OnSelectionFrameClickListener() {
            @Override
            public void cancelClick() {

            }

            @Override
            public void confirmClick() {
                if (collectionType == 1 || collectionType == 2) {
                    // ????????????
                    deleteCollection(position);
                } else {
                    // ????????????
                    deleteMsg(position);
                }
            }
        });
        selectionFrame.show();
    }

    private void deleteMsg(final int position) {
        final PublicMessage message = mMessages.get(position);
        if (message == null) {
            return;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", message.getMessageId());
        DialogHelper.showDefaulteMessageProgressDialog((Activity) mContext);

        HttpUtils.get().url(CoreManager.requireConfig(FLYApplication.getInstance()).CIRCLE_MSG_DELETE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        DialogHelper.dismissProgressDialog();
                        CircleMessageDao.getInstance().deleteMessage(message.getMessageId());// ????????????????????????????????????????????????
                        mMessages.remove(position);
                        notifyDataSetChanged();

                        // ???????????????????????????????????????????????????
                        JCVideoPlayer.releaseAllVideos();
                        stopVoice();
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    public void deleteCollection(final int position) {
        final PublicMessage message = mMessages.get(position);
        if (message == null) {
            return;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("emojiId", message.getEmojiId());
        DialogHelper.showDefaulteMessageProgressDialog((Activity) mContext);

        HttpUtils.get().url(CoreManager.requireConfig(FLYApplication.getInstance()).Collection_REMOVE)
                .params(params)
                .build()
                .execute(new BaseCallback<Collectiion>(Collectiion.class) {

                    @Override
                    public void onResponse(ObjectResult<Collectiion> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            mMessages.remove(position);
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private void showBodyTextLongClickDialog(final String text) {
        CharSequence[] items = new CharSequence[]{mContext.getString(R.string.copy)};
        new AlertDialog.Builder(mContext).setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // ????????????
                        SystemUtil.copyText(mContext, text);
                        break;
                }
            }
        }).setCancelable(true).create().show();
    }

    private void showCommentLongClickDialog(final int messagePosition, final int commentPosition,
                                            final CommentAdapter adapter) {
        if (messagePosition < 0 || messagePosition >= mMessages.size()) {
            return;
        }
        final PublicMessage message = mMessages.get(messagePosition);
        if (message == null) {
            return;
        }
        final List<Comment> comments = message.getComments();
        if (comments == null) {
            return;
        }
        if (commentPosition < 0 || commentPosition >= comments.size()) {
            return;
        }
        final Comment comment = comments.get(commentPosition);

        CharSequence[] items;
        if (comment.getUserId().equals(mLoginUserId) || message.getUserId().equals(mLoginUserId)) {
            // ???????????? || ???????????????????????????????????????
            items = new CharSequence[]{mContext.getString(R.string.copy), mContext.getString(R.string.delete)};
        } else {
            items = new CharSequence[]{mContext.getString(R.string.copy)};
        }
        new AlertDialog.Builder(mContext).setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:// ????????????
                        if (TextUtils.isEmpty(comment.getBody())) {
                            return;
                        }
                        SystemUtil.copyText(mContext, comment.getBody());
                        break;
                    case 1:
                        deleteComment(message, messagePosition, comment.getCommentId(), comments, commentPosition, adapter);
                        break;
                }
            }
        }).setCancelable(true).create().show();
    }

    /**
     * ??????????????????
     */
    private void deleteComment(PublicMessage message, int messagePosition, String commentId, final List<Comment> comments,
                               final int commentPosition, final CommentAdapter adapter) {
        String messageId = message.getMessageId();
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", messageId);
        params.put("commentId", commentId);
        HttpUtils.get().url(CoreManager.requireConfig(FLYApplication.getInstance()).MSG_COMMENT_DELETE)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            message.setCommnet(message.getCommnet() - 1);
                            comments.remove(commentPosition);
                            notifyItemChanged(messagePosition);
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    public void onPraise(int messagePosition, boolean isPraise) {
        praiseOrCancel(messagePosition, isPraise);
    }

    public void onComment(int messagePosition, ListView view) {
        if (mContext instanceof BusinessCircleActivity) {
            ((BusinessCircleActivity) mContext).showCommentEnterView(messagePosition, null, null, null);
        } else {
            PublicMessage message = mMessages.get(messagePosition);
            String path = "";
            if (message.getType() == 3) {
                //??????
                path = message.getFirstAudio();
            } else if (message.getType() == 2) {
                //??????
                path = message.getFirstImageOriginal();
            } else if (message.getType() == 6) {
                //??????
                path = message.getFirstVideo();
            }
            view.setTag(message);
            EventBus.getDefault().post(new MessageEventComment("Comment", message.getMessageId(), message.getIsAllowComment(),
                    message.getType(), path, message, view));
        }
    }

    private <T> T firstOrNull(List<T> list) {
        if (list == null) {
            return null;
        }
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    private String collectionParam(PublicMessage message) {
        com.alibaba.fastjson.JSONArray array = new com.alibaba.fastjson.JSONArray();
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        int type = message.getCollectionType();
        String msg = "";
        String collectContent = "";
        String fileName = "";
        long fileLength = 0;
        long fileSize = 0;
        String id = message.getMessageId();
        Resource res = null;
        if (message.getBody() != null) {
            collectContent = message.getBody().getText();
            switch (type) {
                case CollectionEvery.TYPE_TEXT:
                    msg = message.getBody().getText();
                    break;
                case CollectionEvery.TYPE_IMAGE:
                    List<Resource> images = message.getBody().getImages();
                    // ????????????????????????????????????????????????????????????????????????????????????
                    if (images == null || images.isEmpty()) {
                        type = CollectionEvery.TYPE_TEXT;
                        msg = message.getBody().getText();
                        break;
                    }
                    StringBuilder sb = new StringBuilder();
                    boolean firstTime = true;
                    for (Resource token : images) {
                        String url = token.getOriginalUrl();
                        if (TextUtils.isEmpty(url)) {
                            continue;
                        }
                        if (firstTime) {
                            firstTime = false;
                        } else {
                            sb.append(',');
                        }
                        sb.append(url);
                    }
                    msg = sb.toString();
                    break;
                case CollectionEvery.TYPE_FILE:
                    res = firstOrNull(message.getBody().getFiles());
                    break;
                case CollectionEvery.TYPE_VIDEO:
                    res = firstOrNull(message.getBody().getVideos());
                    break;
                case CollectionEvery.TYPE_VOICE:
                    res = firstOrNull(message.getBody().getAudios());
                    break;
                default:
                    throw new IllegalStateException("??????<" + type + ">????????????");
            }
        }

        if (res != null) {
            if (!TextUtils.isEmpty(res.getOriginalUrl())) {
                msg = res.getOriginalUrl();
            }
            fileLength = res.getLength();
            fileSize = res.getSize();
        }
        if (!TextUtils.isEmpty(message.getFileName())) {
            fileName = message.getFileName();
        }

        json.put("type", String.valueOf(type));
        json.put("msg", msg);
        json.put("fileName", fileName);
        json.put("fileSize", fileSize);
        json.put("fileLength", fileLength);
        json.put("collectContent", collectContent);
        json.put("collectType", 1);
        json.put("collectMsgId", id);
        array.add(json);
        return JSON.toJSONString(array);
    }

    private void onCollection(final int messagePosition) {
        PublicMessage message = mMessages.get(messagePosition);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        if (1 == message.getIsCollect()) {
            params.put("messageId", message.getMessageId());

            HttpUtils.get().url(coreManager.getConfig().MSG_COLLECT_DELETE)
                    .params(params)
                    .build()
                    .execute(new BaseCallback<Void>(Void.class) {

                        @Override
                        public void onResponse(ObjectResult<Void> result) {
                            if (result.getResultCode() == 1) {
                                ToastUtil.showToast(mContext, R.string.tip_collection_canceled);
                                message.setIsCollect(0);
                                notifyDataSetChanged();
                            } else if (!TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(mContext, result.getResultMsg());
                            } else {
                                ToastUtil.showToast(mContext, R.string.tip_server_error);
                            }
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            ToastUtil.showNetError(mContext);
                        }
                    });
        } else {
            params.put("emoji", collectionParam(message));

            HttpUtils.post().url(coreManager.getConfig().Collection_ADD)
                    .params(params)
                    .build()
                    .execute(new BaseCallback<Void>(Void.class) {

                        @Override
                        public void onResponse(ObjectResult<Void> result) {
                            if (result.getResultCode() == 1) {
                                Toast.makeText(mContext, mContext.getString(R.string.collection_success), Toast.LENGTH_SHORT).show();
                                message.setIsCollect(1);
                                notifyDataSetChanged();
                            } else if (!TextUtils.isEmpty(result.getResultMsg())) {
                                ToastUtil.showToast(mContext, result.getResultMsg());
                            } else {
                                ToastUtil.showToast(mContext, R.string.tip_server_error);
                            }
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            ToastUtil.showNetError(mContext);
                        }
                    });
        }
    }

    public void onReport(final int messagePosition) {
        ReportDialog mReportDialog = new ReportDialog(mContext, false, new ReportDialog.OnReportListItemClickListener() {
            @Override
            public void onReportItemClick(Report report) {
                report(messagePosition, report);
            }
        });
        mReportDialog.show();
    }

    /**
     * ??? || ?????????
     */
    private void praiseOrCancel(final int position, final boolean isPraise) {
        final PublicMessage message = mMessages.get(position);
        if (message == null) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("messageId", message.getMessageId());
        String requestUrl;
        if (isPraise) {
            requestUrl = CoreManager.requireConfig(FLYApplication.getInstance()).MSG_PRAISE_ADD;
        } else {
            requestUrl = CoreManager.requireConfig(FLYApplication.getInstance()).MSG_PRAISE_DELETE;
        }
        HttpUtils.get().url(requestUrl)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            message.setIsPraise(isPraise ? 1 : 0);
                            List<Praise> praises = message.getPraises();
                            if (praises == null) {
                                praises = new ArrayList<>();
                                message.setPraises(praises);
                            }
                            int praiseCount = message.getPraise();
                            if (isPraise) {
                                // ???????????????
                                // ?????????????????????
                                Praise praise = new Praise();
                                praise.setUserId(mLoginUserId);
                                praise.setNickName(mLoginNickName);
                                // praises.add(0, praise);
                                praises.add(praise);// ??????????????????????????????????????????????????????????????????????????????????????????????????????
                                praiseCount++;
                                message.setPraise(praiseCount);
                            } else {
                                // ???????????????
                                // ?????????????????????
                                for (int i = 0; i < praises.size(); i++) {
                                    if (mLoginUserId.equals(praises.get(i).getUserId())) {
                                        praises.remove(i);
                                        praiseCount--;
                                        message.setPraise(praiseCount);
                                        break;
                                    }
                                }
                            }
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        ToastUtil.showErrorNet(mContext);
                    }
                });
    }

    private void report(int position, Report report) {
        final PublicMessage message = mMessages.get(position);
        if (message == null) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("toUserId", message.getUserId());
        params.put("reason", String.valueOf(report.getReportId()));

        HttpUtils.get().url(CoreManager.requireConfig(FLYApplication.getInstance()).USER_REPORT)
                .params(params)
                .build()
                .execute(new BaseCallback<Void>(Void.class) {

                    @Override
                    public void onResponse(ObjectResult<Void> result) {
                        if (Result.checkSuccess(mContext, result)) {
                            ToastUtil.showToast(mContext, mContext.getString(R.string.report_success));
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {

                    }
                });
    }

    /**
     * ??????????????????
     */
    public void stopVoice() {
        if (mAudioPalyer != null) {
            mAudioPalyer.stop();
        }
        VoicePlayer.instance().stop();
    }

    /**
     * @param viewHolder
     */
    private void play(ViewHolder viewHolder, PublicMessage message) {
        JCVideoPlayer.releaseAllVideos();

        String voiceUrl = message.getFirstAudio();
        if (mVoicePlayId == null) {
            // ???????????????
            try {
                mAudioPalyer.play(voiceUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mVoicePlayId = message.getMessageId();
            updateVoiceViewHolderIconStatus(true, viewHolder);
            mVoicePlayViewHolder = viewHolder;
        } else {
            if (mVoicePlayId == message.getMessageId()) {
                mAudioPalyer.stop();
                mVoicePlayId = null;
                updateVoiceViewHolderIconStatus(false, viewHolder);
                mVoicePlayViewHolder = null;
            } else {
                // ????????????????????? ???????????????
                mAudioPalyer.stop();
                mVoicePlayId = null;
                if (mVoicePlayViewHolder != null) {
                    updateVoiceViewHolderIconStatus(false, mVoicePlayViewHolder);
                }
                try {
                    mAudioPalyer.play(voiceUrl);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mVoicePlayId = message.getMessageId();
                updateVoiceViewHolderIconStatus(true, viewHolder);
                mVoicePlayViewHolder = viewHolder;
            }
        }
    }

    private void updateVoiceViewHolderIconStatus(boolean play, ViewHolder viewHolder) {
        if (viewHolder instanceof NormalVoiceHolder) {
            // ????????????
            if (play) {
                ((NormalVoiceHolder) viewHolder).voice_action_img.setImageResource(R.drawable.feed_main_player_pause);
            } else {
                ((NormalVoiceHolder) viewHolder).voice_action_img.setImageResource(R.drawable.feed_main_player_play);
            }
        } else {
            // ????????????
            if (play) {
                ((FwVoiceHolder) viewHolder).voice_action_img.setImageResource(R.drawable.feed_main_player_pause);
            } else {
                ((FwVoiceHolder) viewHolder).voice_action_img.setImageResource(R.drawable.feed_main_player_play);
            }
        }
    }

    /**
     * ?????????????????????
     */
    @Override
    public void ideChange() {
        stopVoice();
    }

    /**
     * ???????????????
     **/
    public void setData(List<PublicMessage> mMessages) {
        this.mMessages = mMessages;
        this.notifyDataSetChanged();
    }

    /**
     * 0:?????? default==0
     * 1:?????????????????? ???????????????????????? ????????????????????????
     * 2.?????????????????? ???????????????????????? ????????????????????????
     */
    public void setCollectionType(int collectionType) {
        this.collectionType = collectionType;
    }

    public interface OnItemClickListener {
        void onItemClick(PublicMessageRecyclerAdapter.ViewHolder vh);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar_img;
        TextView nick_name_tv;
        TextView time_tv;
        HttpTextView body_tv;
        //  HttpTextView body_tvS;
        TextView open_tv;
        FrameLayout content_fl;
        TextView delete_tv;
        TextView multi_praise_tv;
        View line_v;
        ListView command_listView;
        TextView tvLoadMore;
        TextView location_tv;

        View llOperator;
        View llThumb;
        CheckableImageView ivThumb;
        TextView tvThumb;
        View llComment;
        CheckableImageView ivComment;
        TextView tvComment;
        View llCollection;
        CheckableImageView ivCollection;
        View llReport;
        ImageView iv_prise;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar_img = (ImageView) itemView.findViewById(R.id.avatar_img);
            nick_name_tv = (TextView) itemView.findViewById(R.id.nick_name_tv);
            time_tv = (TextView) itemView.findViewById(R.id.time_tv);
            body_tv = itemView.findViewById(R.id.body_tv);
            // body_tvS = itemView.findViewById(R.id.body_tvS);
            open_tv = (TextView) itemView.findViewById(R.id.open_tv);
            content_fl = (FrameLayout) itemView.findViewById(R.id.content_fl);
            delete_tv = (TextView) itemView.findViewById(R.id.delete_tv);

            llOperator = itemView.findViewById(R.id.llOperator);
            llThumb = itemView.findViewById(R.id.llThumb);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            tvThumb = itemView.findViewById(R.id.tvThumb);
            llComment = itemView.findViewById(R.id.llComment);
            ivComment = itemView.findViewById(R.id.ivComment);
            tvComment = itemView.findViewById(R.id.tvComment);
            llCollection = itemView.findViewById(R.id.llCollection);
            ivCollection = itemView.findViewById(R.id.ivCollection);
            llReport = itemView.findViewById(R.id.llReport);
        }
    }

    /* ?????????Text */
    static class NormalTextHolder extends ViewHolder {

        public NormalTextHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /* ?????????Text */
    static class FwTextHolder extends ViewHolder {
        TextView text_tv;

        public FwTextHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /* ????????????????????? */
    static class NormalSingleImageHolder extends ViewHolder {
        ImageView image_view;

        public NormalSingleImageHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /* ????????????????????? */
    static class FwSingleImageHolder extends ViewHolder {
        TextView text_tv;
        ImageView image_view;

        public FwSingleImageHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /* ????????????????????? */
    static class NormalMultiImageHolder extends ViewHolder {
        MyGridView grid_view;

        public NormalMultiImageHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /* ????????????????????? */
    static class FwMultiImageHolder extends ViewHolder {
        TextView text_tv;
        MyGridView grid_view;

        public FwMultiImageHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /* ??????????????? */
    static class NormalVoiceHolder extends ViewHolder {
        ImageView img_view;
        ImageView voice_action_img;
        TextView voice_desc_tv;
        VoiceAnimView chat_to_voice;

        public NormalVoiceHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /* ??????????????? */
    static class FwVoiceHolder extends ViewHolder {
        TextView text_tv;
        ImageView img_view;
        ImageView voice_action_img;
        TextView voice_desc_tv;

        public FwVoiceHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /* ??????????????? */
    static class NormalVideoHolder extends ViewHolder {
        JVCideoPlayerStandardSecond gridViewVideoPlayer;

        public NormalVideoHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /* ??????????????? */
    static class FwVideoHolder extends ViewHolder {
        TextView text_tv;
        ImageView video_thumb_img;
        TextView video_desc_tv;

        public FwVideoHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /************************* ???????????? ******************************/

    /* ??????????????? */
    static class NormalFileHolder extends ViewHolder {
        RelativeLayout file_click;
        ImageView file_image;
        TextView text_tv;

        public NormalFileHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /* ??????????????? */
    static class NormalLinkHolder extends ViewHolder {
        LinearLayout link_click;
        ImageView link_image;
        TextView link_tv;

        public NormalLinkHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class CommentViewHolder {
        TextView text_view;
    }

    public class CommentAdapter extends BaseAdapter {
        private int messagePosition;
        private boolean loading;
        private List<Comment> datas;

        CommentAdapter(int messagePosition, List<Comment> data) {
            this.messagePosition = messagePosition;
            if (data == null) {
                datas = new ArrayList<>();
            } else {
                this.datas = data;
            }
        }

/*
        public void setData(int messagePosition, List<Comment> data) {
            this.messagePosition = messagePosition;
            this.datas = data;
            notifyDataSetChanged();
        }
*/

        public void addAll(List<Comment> data) {
            this.datas.addAll(data);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            CommentViewHolder holder;
            if (convertView == null) {
                holder = new CommentViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.p_msg_comment_list_item, null);
                holder.text_view = (TextView) convertView.findViewById(R.id.text_view);
                convertView.setTag(holder);
            } else {
                holder = (CommentViewHolder) convertView.getTag();
            }
            final Comment comment = datas.get(position);
            SpannableStringBuilder builder = new SpannableStringBuilder();
            String showName = getShowName(comment.getUserId(), comment.getNickName());
            UserClickableSpan.setClickableSpan(mContext, builder, showName, comment.getUserId());            // ??????????????????ClickSpanned
            if (!TextUtils.isEmpty(comment.getToUserId()) && !TextUtils.isEmpty(comment.getToNickname())) {
                builder.append(mContext.getString(R.string.replay_infix_comment));
                String toShowName = getShowName(comment.getToUserId(), comment.getToNickname());
                UserClickableSpan.setClickableSpan(mContext, builder, toShowName, comment.getToUserId());// ?????????????????????ClickSpanned
            }
            builder.append(":");
            // ??????????????????
            String commentBody = comment.getBody();
            if (!TextUtils.isEmpty(commentBody)) {
                commentBody = StringUtils.replaceSpecialChar(comment.getBody());
                CharSequence charSequence = HtmlUtils.transform200SpanString(commentBody, true);
                builder.append(charSequence);
            }
            holder.text_view.setText(builder);
            holder.text_view.setLinksClickable(true);
            holder.text_view.setMovementMethod(LinkMovementClickMethod.getInstance());

            holder.text_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (comment.getUserId().equals(mLoginUserId)) {
                        // ?????????????????????????????????????????????????????????????????????
                        showCommentLongClickDialog(messagePosition, position, CommentAdapter.this);
                    } else {
                        // ??????????????????
                        String toShowName = getShowName(comment.getUserId(), comment.getNickName());
                        if (mContext instanceof BusinessCircleActivity) {
                            ((BusinessCircleActivity) mContext).showCommentEnterView(messagePosition, comment.getUserId(), comment.getNickName(), toShowName);
                        } else {
                            EventBus.getDefault().post(new MessageEventReply("Reply", comment, messagePosition, toShowName,
                                    (ListView) parent));
                        }
                    }
                }
            });

            holder.text_view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showCommentLongClickDialog(messagePosition, position, CommentAdapter.this);
                    return true;
                }
            });

            return convertView;
        }

        public boolean isLoading() {
            return loading;
        }

        public void setLoading(boolean loading) {
            this.loading = loading;
        }

        public void addComment(Comment comment) {
            this.datas.add(0, comment);
            notifyDataSetChanged();
        }
    }

    private class SingleImageClickListener implements View.OnClickListener {
        private String url;

        SingleImageClickListener(String url) {
            this.url = url;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, SingleImagePreviewActivity.class);
            intent.putExtra(FLYAppConstant.EXTRA_IMAGE_URI, url);
            mContext.startActivity(intent);
        }
    }

    private class MultipleImagesClickListener implements AdapterView.OnItemClickListener {
        private List<Resource> images;

        MultipleImagesClickListener(List<Resource> images) {
            this.images = images;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (images == null || images.size() <= 0) {
                return;
            }
            ArrayList<String> lists = new ArrayList<String>();
            for (int i = 0; i < images.size(); i++) {
                lists.add(images.get(i).getOriginalUrl());
                LogUtils.e(images.get(i).getOriginalUrl());
            }
            Intent intent = new Intent(mContext, MultiImagePreviewActivity.class);
            intent.putExtra(FLYAppConstant.EXTRA_IMAGES, lists);
            intent.putExtra(FLYAppConstant.EXTRA_POSITION, position);
            intent.putExtra(FLYAppConstant.EXTRA_CHANGE_SELECTED, false);
            mContext.startActivity(intent);
        }
    }
}
