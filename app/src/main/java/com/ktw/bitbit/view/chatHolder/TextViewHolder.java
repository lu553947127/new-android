package com.ktw.bitbit.view.chatHolder;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.AutoAnswerBean;
import com.ktw.bitbit.bean.event.EventClickProblem;
import com.ktw.bitbit.bean.message.ChatMessage;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.HtmlUtils;
import com.ktw.bitbit.util.PreferenceUtils;
import com.ktw.bitbit.util.StringUtils;
import com.ktw.bitbit.util.link.HttpTextView;
import com.ktw.bitbit.view.MyClickSpan;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;

public class TextViewHolder extends AChatHolderInterface {

    public HttpTextView mTvContent;
    public TextView tvFireTime;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_text : R.layout.chat_to_item_text;
    }

    @Override
    public void initView(View view) {
        mTvContent = view.findViewById(R.id.chat_text);
        mRootView = view.findViewById(R.id.chat_warp_view);
        if (!isMysend) {
            tvFireTime = view.findViewById(R.id.tv_fire_time);
        }
    }

    @Override
    public void fillData(ChatMessage message) {
        // 修改字体功能
        int size = PreferenceUtils.getInt(mContext, Constants.FONT_SIZE) + 16;
        mTvContent.setTextSize(size);
        mTvContent.setTextColor(mContext.getResources().getColor(R.color.black));

        String content = StringUtils.replaceSpecialChar(message.getContent());
        CharSequence charSequence = HtmlUtils.transform200SpanString(content, true);
        Log.e("zx", "fillData: " + charSequence.toString() + "  length: " + charSequence.length() + " split: " + charSequence.toString().split("\\[").length);
        if (message.getIsReadDel() && !isMysend) {// 阅后即焚
            if (!message.isGroup() && !message.isSendRead()) {
                mTvContent.setText(R.string.tip_click_to_read);
                mTvContent.setTextColor(mContext.getResources().getColor(R.color.redpacket_bg));
            } else {
                // 已经查看了，当适配器再次刷新的时候，不需要重新赋值
                mTvContent.setText(charSequence);
            }
        } else {
            mTvContent.setText(charSequence);
        }

        mTvContent.setUrlText(mTvContent.getText());

        if (!TextUtils.isEmpty(message.getObjectId())
                && message.getObjectId().contains("isRobotMsg")) {
            try {
                JSONObject json = new JSONObject(message.getObjectId());
                boolean isRobotMsg = json.getBoolean("isRobotMsg");
                if (isRobotMsg) {
                    //是机器人消息
                    setTextHighLightWithClick(mTvContent, message.getContent());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        mTvContent.setOnClickListener(v -> mHolderListener.onItemClick(mRootView, TextViewHolder.this, mdata));
        mTvContent.setOnLongClickListener(v -> {
            mHolderListener.onItemLongClick(v, TextViewHolder.this, mdata);
            return true;
        });
    }

    @Override
    protected void onRootClick(View v) {

    }

    @Override
    public boolean enableFire() {
        return true;
    }

    @Override
    public boolean enableSendRead() {
        return true;
    }

    public void showFireTime(boolean show) {
        if (tvFireTime != null) {
            tvFireTime.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 设置高亮文本以及点击事件
     *
     * @param tv
     * @param text
     */
    public void setTextHighLightWithClick(TextView tv, String text) {
        tv.setClickable(true);
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        SpannableString s = new SpannableString(text);

        List<AutoAnswerBean.AnswerBean> answerBeanList = FLYApplication.autoAnswerBean.getAutoAnswerList();
        if (answerBeanList != null && answerBeanList.size() > 0) {
            for (AutoAnswerBean.AnswerBean item : answerBeanList) {
                String keyword = "【" + item.getSort() + "】  " + item.getIssue();
                Pattern p = Pattern.compile(keyword);
                Matcher m = p.matcher(s);
                if (m.find()) {
                    int start = m.start();
                    int end = m.end();
                    s.setSpan(new MyClickSpan(R.color.button_text, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            EventBus.getDefault().post(new EventClickProblem(item.getSort()));
                        }
                    }), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        tv.setText(s);
    }
}
