package com.ktw.fly.view.chatHolder;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ktw.fly.R;
import com.ktw.fly.adapter.TextImgManyAdapter;
import com.ktw.fly.bean.TextImgBean;
import com.ktw.fly.bean.message.ChatMessage;
import com.ktw.fly.helper.AvatarHelper;
import com.ktw.fly.ui.tool.WebViewActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.ktw.fly.ui.tool.WebViewActivity.EXTRA_URL;

class TextImgManyHolder extends AChatHolderInterface {

    ListView lvList;
    TextView tvTitle;
    ImageView ivImage;

    String mLinkUrl;

    @Override
    public int itemLayoutId(boolean isMysend) {
        return isMysend ? R.layout.chat_from_item_text_img_many : R.layout.chat_to_item_text_img_many;
    }

    @Override
    public void initView(View view) {
        lvList = view.findViewById(R.id.chat_item_content);
        tvTitle = view.findViewById(R.id.chat_title);
        ivImage = view.findViewById(R.id.chat_img);
        mRootView = view.findViewById(R.id.chat_warp_view);
    }

    @Override
    public void fillData(ChatMessage message) {
        try {
            JSONArray jsonArray = new JSONArray(message.getContent());
            if (jsonArray.length() > 0) {
                List<TextImgBean> datas = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = jsonArray.getJSONObject(i);
                    TextImgBean info = new TextImgBean();
                    info.title = json.getString("title");
                    info.img = json.getString("img");
                    info.url = json.getString("url");
                    if (i > 0) {
                        datas.add(info);
                    } else {
                        tvTitle.setText(info.title);
                        AvatarHelper.getInstance().displayUrl(info.img, ivImage);
                        mLinkUrl = info.url;
                    }
                }

                lvList.setAdapter(new TextImgManyAdapter(mContext, datas));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRootClick(View v) {
        Intent intent = new Intent(mContext, WebViewActivity.class);
        intent.putExtra(EXTRA_URL, mLinkUrl);
        mContext.startActivity(intent);
    }

    @Override
    public boolean enableSendRead() {
        return true;
    }
}
