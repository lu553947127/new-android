package com.ktw.fly.ui.message;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ktw.fly.R;
import com.ktw.fly.bean.message.ChatMessage;
import com.ktw.fly.ui.base.BaseActivity;
import com.ktw.fly.util.HtmlUtils;
import com.ktw.fly.util.StringUtils;
import com.ktw.fly.view.ChatContentView;

public class MessageRemindActivity extends BaseActivity implements View.OnClickListener {

    private TextView tv_content_message;
    private ChatMessage chatMessage;

    public static void start(Context ctx, String body, String mToUserId) {
        Intent intent = new Intent(ctx, MessageRemindActivity.class);
        intent.putExtra("body", body);
        intent.putExtra("mToUserId", mToUserId);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_remind);
        String body = getIntent().getStringExtra("body");
        chatMessage = new ChatMessage(body);
        initView();
    }

    private void initView() {
        tv_content_message = (TextView) findViewById(R.id.tv_content_message);
        String content = StringUtils.replaceSpecialChar(chatMessage.getContent());
        CharSequence charSequence = HtmlUtils.transform200SpanString(content, true);
        tv_content_message.setText(charSequence);
        tv_content_message.setMovementMethod(ScrollingMovementMethod.getInstance());

        findViewById(R.id.iv_forward).setOnClickListener(this);
        findViewById(R.id.iv_enshrine).setOnClickListener(this);
        findViewById(R.id.iv_timing).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_forward:
                if (chatMessage.getIsReadDel()) {
                    // 为阅后即焚类型的消息，不可转发
                    Toast.makeText(mContext, getString(R.string.cannot_forwarded), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(mContext, InstantMessageActivity.class);
                intent.putExtra("fromUserId", getIntent().getStringExtra("mToUserId"));
                intent.putExtra("messageId", chatMessage.getPacketId());
                mContext.startActivity(intent);
                ((Activity) mContext).finish();
                break;
            case R.id.iv_enshrine:
                // 收藏
                if (chatMessage.getIsReadDel()) {
                    // 为阅后即焚类型的消息，不可收藏
                    Toast.makeText(mContext, getString(R.string.tip_cannot_collect_burn), Toast.LENGTH_SHORT).show();
                    return;
                }
                new ChatContentView(MessageRemindActivity.this)
                        .collectionTypeMessage(chatMessage);
                break;
            case R.id.iv_timing:
                //TODO 定时器设置
                break;
        }
    }
}
