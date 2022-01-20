package com.ktw.bitbit.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ktw.bitbit.R;
import com.ktw.bitbit.pay.new_ui.PaymentOrReceiptActivity;
import com.ktw.bitbit.ui.FLYMainActivity;
import com.ktw.bitbit.ui.base.EasyFragment;
import com.ktw.bitbit.ui.contacts.PublishNumberActivity;
import com.ktw.bitbit.ui.contacts.label.LabelActivityNewUI;
import com.ktw.bitbit.ui.life.LifeCircleActivity;
import com.ktw.bitbit.ui.me.NearPersonActivity;


/**
 * Created by XionghuiJi on 2020/12/03
 */
public class FindFragment extends EasyFragment {


    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_find;
    }

    @Override
    protected void onActivityCreated(Bundle savedInstanceState, boolean createView) {
        if (createView){
            initView();
        }
    }

    private void  initView(){
        findViewById(R.id.iv_title_left).setVisibility(View.GONE);
        TextView titleTv = findViewById(R.id.tv_title_center);
        titleTv.setText("发现" );
        findViewById(R.id.rlt_discover).setOnClickListener(this);
        findViewById(R.id.rlt_money).setOnClickListener(this);
        findViewById(R.id.rlt_scan_qr_code).setOnClickListener(this);
        findViewById(R.id.rlt_tag).setOnClickListener(this);
        findViewById(R.id.rlt_official_accounts).setOnClickListener(this);
        findViewById(R.id.rlt_neary).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
       if (v.getId() ==R.id.rlt_scan_qr_code){
           FLYMainActivity.requestQrCodeScan(getActivity());
       }else if (v.getId()== R.id.rlt_discover){//朋友圈
            startActivity(new Intent(getContext(), LifeCircleActivity.class));
       }else if (v.getId()== R.id.rlt_tag){//标签
           LabelActivityNewUI.start(requireContext());
       }else if (v.getId()== R.id.rlt_official_accounts){//公众号
           Intent intentNotice = new Intent(getActivity(), PublishNumberActivity.class);
           getActivity().startActivity(intentNotice);
       }else if (v.getId()== R.id.rlt_money){//收付款
           PaymentOrReceiptActivity.start(getActivity(), coreManager.getSelf().getUserId());
       }else if (v.getId()== R.id.rlt_neary){
           startActivity(new Intent(getActivity(), NearPersonActivity.class));
       }
    }
}
