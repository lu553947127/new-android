package com.ktw.bitbit.view.chatHolder;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.message.ChatMessage;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.ui.tool.WebViewActivity;

import org.json.JSONException;
import org.json.JSONObject;

class TextImgViewHolder extends AChatHolderInterface {

    TextView tvTitle;  // 主标题
    TextView tvText;   // 副标题
    ImageView ivImage; // 图像
    String mLinkUrl;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_text_img : R.layout.chat_to_item_text_img;
    }

    @Override
    public void initView(View view) {
        tvTitle = view.findViewById(R.id.chat_title);
        tvText = view.findViewById(R.id.chat_text);
        ivImage = view.findViewById(R.id.chat_img);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        try {
            JSONObject json = new JSONObject(message.getContent());
            String tile = json.getString("title");
            String sub = json.getString("sub");
            String img = json.getString("img");

            mLinkUrl = json.getString("url");

            tvTitle.setText(tile);
            tvText.setText(sub);
            AvatarHelper.getInstance().displayUrl(img, ivImage);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRootClick(View v) {
        Intent intent = new Intent(mContext, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, mLinkUrl);
        mContext.startActivity(intent);
    }

    @Override
    public boolean enableSendRead() {
        return true;
    }
}
