package com.ktw.bitbit.ui.me.emot;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.MyCollectEmotPackageBean;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.view.SkinTextView;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;
import com.xuan.xuanhttplibrary.okhttp.result.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 编辑单个个人表情包
 */
public class EditSingleEmotPackageActivity extends BaseActivity {

    private final static int IMAGE_UPLOAD_MAX_COUNT = 300;
    public final static int REQUEST_CODE_EDIT_EMOT = 100;

    private List<MyEmotBean> emotBeanList = FLYApplication.singleEmotList;
    private GridView gvEmot;
    private EditMySingleEmotAdapter adapter;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_single_emot_package);
        initActionBar();
        initView();
        initEvent();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        SkinTextView tvEdit = findViewById(R.id.tv_title_right);
        tvEdit.setText("删除");
        tvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delEmot();
            }
        });
        tvTitle = (TextView) findViewById(R.id.tv_title_center);
        setTitle(0);
    }

    private void delEmot() {
        StringBuffer sb = new StringBuffer();
        for (MyEmotBean myEmotBean : emotBeanList) {
            if (myEmotBean.isCheck()) {
                sb.append(myEmotBean.getId()).append(",");
            }
        }
        String ids = sb.toString();
        if (!TextUtils.isEmpty(ids)) {
            ids = ids.substring(0, ids.length() - 1);
            delEmotNet(ids);
        } else {
            ToastUtil.showToast(this, "请选择");
        }
    }

    private void delEmotNet(String ids) {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("ids", ids);
        HttpUtils.get().url(coreManager.getConfig().API_FACE_ID_DELETE)
                .params(params)
                .build()
                .execute(new ListCallback<MyEmotBean>(MyEmotBean.class) {
                    @Override
                    public void onResponse(ArrayResult<MyEmotBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            setResult(RESULT_OK);

                            loadMySingleEmot();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(EditSingleEmotPackageActivity.this);
                    }
                });
    }

    public void setTitle(int emotCount) {
        String title = String.format(getString(R.string.tips_1), emotCount, IMAGE_UPLOAD_MAX_COUNT);
        tvTitle.setText(title);
    }

    private void initView() {
        gvEmot = findViewById(R.id.gvEmot);
        adapter = new EditMySingleEmotAdapter(this, emotBeanList);
        gvEmot.setAdapter(adapter);
    }

    private void initEvent() {
        adapter.setCheckEmotListener(new EditMySingleEmotAdapter.ICheckEmotListener() {
            @Override
            public void checkEmot(int position, boolean isCheck) {
                MyEmotBean myEmotBean = emotBeanList.get(position);
                if (myEmotBean != null) {
                    myEmotBean.setCheck(isCheck);
                    emotBeanList.set(position, myEmotBean);
                }
            }
        });
    }

    private void loadMySingleEmot() {
        DialogHelper.showDefaulteMessageProgressDialog(this);
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        params.put("type", 1 + "");
        HttpUtils.get().url(coreManager.getConfig().API_FACE_COLLECT_LIST)
                .params(params)
                .build()
                .execute(new ListCallback<MyCollectEmotPackageBean>(MyCollectEmotPackageBean.class) {
                    @Override
                    public void onResponse(ArrayResult<MyCollectEmotPackageBean> result) {
                        DialogHelper.dismissProgressDialog();
                        if (Result.checkSuccess(mContext, result)) {
                            if (result.getData() != null) {
                                FLYApplication.singleEmotList.clear();
                                for (MyCollectEmotPackageBean item : result.getData()) {
                                    MyEmotBean myEmotBean = new MyEmotBean();
                                    myEmotBean.setId(item.getId());
                                    myEmotBean.setUrl(item.getFace().getPath().get(0));
                                    FLYApplication.singleEmotList.add(myEmotBean);
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        emotBeanList = FLYApplication.singleEmotList;
                                        setTitle(emotBeanList.size());
                                        adapter.setData(emotBeanList);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        ToastUtil.showNetError(EditSingleEmotPackageActivity.this);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}