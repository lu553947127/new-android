package com.ktw.bitbit.ui.tool;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/18.
 */

public class HtmlFactory {

    // εδΎ
    private static HtmlFactory sington = null;
    private List<String> datas = new ArrayList<>();
    private DataListener mListener;
    private Handler mHandler = new Handler() {

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case -1:
                    mListener.onError(FLYApplication.getContext().getString(R.string.error));
                    break;
                case 0:
                    datas.clear();
                    break;
                case 200:
                    datas.add((String) msg.obj);
                    break;
                case 401:
                    mListener.onResponse(datas, (String) msg.obj);
                    break;
            }
        }
    };

    private HtmlFactory() {
    }

    public static HtmlFactory instance() {
        if (sington == null) {
            synchronized (HtmlFactory.class) {
                if (sington == null) {
                    sington = new HtmlFactory();
                }
            }
        }
        return sington;
    }

    public void queryImage(final String url, DataListener listener) {
        mListener = listener;
        new Thread(new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(0); // εε€
                try {
                    Document document = Jsoup.connect(url).userAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; zh-cn; M032 Build/IML74K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30")
                            .timeout(6000).get();

                    Elements elements = document.select("img[src]");
                    for (Element element : elements) {
                        // εΌε§ε‘«θ£ζ°ζ?
                        String url = element.attr("src");
                        Log.e("xuan", "queryImage: " + url);
                        Message msg = new Message();
                        msg.what = 200;
                        msg.obj = url;
                        mHandler.sendMessage(msg);
                    }

                    Message message = new Message();
                    message.what = 401;
                    message.obj = document.title();
                    mHandler.sendMessage(message); // η»ζοΌε°ζ ι’δΉδΌ θΏε»
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(-1); // εΌεΈΈ
                }
            }
        }).start();
    }

    public interface DataListener<T> {
        void onResponse(List<T> datas, String title);

        void onError(String err);
    }
}
