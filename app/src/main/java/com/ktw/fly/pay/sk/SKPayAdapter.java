package com.ktw.fly.pay.sk;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.ktw.fly.FLYApplication;
import com.ktw.fly.R;
import com.ktw.fly.bean.CodePay;
import com.ktw.fly.bean.Friend;
import com.ktw.fly.bean.PayCertificate;
import com.ktw.fly.bean.Transfer;
import com.ktw.fly.bean.message.ChatMessage;
import com.ktw.fly.bean.message.XmppMessage;
import com.ktw.fly.db.dao.FriendDao;
import com.ktw.fly.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;


public class SKPayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> mChatMessageSource;

    public SKPayAdapter(List<ChatMessage> chatMessages) {
        this.mChatMessageSource = chatMessages;
        if (mChatMessageSource == null) {
            mChatMessageSource = new ArrayList<>();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int resource;
        if (viewType == SKPayType.TRANSFER_BACK) {
            resource = R.layout.item_sk_pay_transfer_back;
            return new TransferBackHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        } else if (viewType == SKPayType.PAYMENT_SUCCESS) {
            resource = R.layout.item_sk_pay_payment;
            return new PaymentHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        } else if (viewType == SKPayType.RECEIPT_SUCCESS) {
            resource = R.layout.item_sk_pay_receipt;
            return new ReceiptHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        } else if (viewType == SKPayType.PAY_CERTIFICATE) {
            resource = R.layout.item_sk_pay_certificate;
            return new PayCertificateHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        } else {
            resource = R.layout.item_sk_pay_unkonw;
            return new SystemViewHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = mChatMessageSource.get(position);
        if (holder instanceof TransferBackHolder) {
            Transfer transfer = JSON.parseObject(chatMessage.getContent(), Transfer.class);
            ((TransferBackHolder) holder).mNotifyTimeTv.setText(TimeUtils.f_long_2_str(chatMessage.getTimeSend() * 1000));
            ((TransferBackHolder) holder).mMoneyTv.setText("￥" + transfer.getMoney());
            Friend friend = FriendDao.getInstance().getFriend(transfer.getUserId(), transfer.getToUserId());
            if (friend != null) {
                ((TransferBackHolder) holder).mBackReasonTv.setText(FLYApplication.getContext().getString(R.string.transfer_back_reason_out_time,
                        TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName()));
            }
            ((TransferBackHolder) holder).mBackTimeTv.setText(TimeUtils.f_long_2_str(transfer.getOutTime() * 1000));
            ((TransferBackHolder) holder).mTransferTimeTv.setText(TimeUtils.f_long_2_str(transfer.getCreateTime() * 1000));
        } else if (holder instanceof PaymentHolder) {
            CodePay codePay = JSON.parseObject(chatMessage.getContent(), CodePay.class);
            ((PaymentHolder) holder).mMoneyTv.setText("￥" + codePay.getMoney());
            if (codePay.getType() == 1) {// 付款码
                ((PaymentHolder) holder).mReceiptUserTv.setText(codePay.getToUserName());
            } else {// 二维码收款
                ((PaymentHolder) holder).mReceiptUserTv.setText(codePay.getUserName());
            }
        } else if (holder instanceof ReceiptHolder) {
            CodePay codePay = JSON.parseObject(chatMessage.getContent(), CodePay.class);
            ((ReceiptHolder) holder).mMoneyTv.setText("￥" + codePay.getMoney());
            if (codePay.getType() == 1) {// 付款码
                ((ReceiptHolder) holder).mPaymentUserTv.setText(codePay.getUserName());
            } else {// 二维码收款
                ((ReceiptHolder) holder).mPaymentUserTv.setText(codePay.getToUserName());
            }
        } else if (holder instanceof PayCertificateHolder) {
            PayCertificate payCertificate = JSON.parseObject(chatMessage.getContent(), PayCertificate.class);
            ((PayCertificateHolder) holder).mMoneyTv.setText("￥" + payCertificate.getMoney());
            ((PayCertificateHolder) holder).mReceiptUserTv.setText(payCertificate.getName());
        } else {
            ((SystemViewHolder) holder).mSystemTv.setText(chatMessage.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return mChatMessageSource.size();
    }

    @Override
    public int getItemViewType(int position) {
        int type = mChatMessageSource.get(position).getType();
        if (type == XmppMessage.TYPE_TRANSFER_BACK) {
            return SKPayType.TRANSFER_BACK;
        } else if (type == XmppMessage.TYPE_PAYMENT_OUT || type == XmppMessage.TYPE_RECEIPT_OUT) {
            return SKPayType.PAYMENT_SUCCESS;
        } else if (type == XmppMessage.TYPE_PAYMENT_GET || type == XmppMessage.TYPE_RECEIPT_GET) {
            return SKPayType.RECEIPT_SUCCESS;
        } else if (type == XmppMessage.TYPE_PAY_CERTIFICATE) {
            return SKPayType.PAY_CERTIFICATE;
        } else {
            return SKPayType.UN_KNOW;
        }
    }

    class TransferBackHolder extends RecyclerView.ViewHolder {
        TextView mNotifyTimeTv;
        TextView mMoneyTv;
        TextView mBackReasonTv;
        TextView mBackTimeTv;
        TextView mTransferTimeTv;

        public TransferBackHolder(View itemView) {
            super(itemView);
            mNotifyTimeTv = itemView.findViewById(R.id.sk_pay_transfer_notify_time_tv);
            mMoneyTv = itemView.findViewById(R.id.sk_pay_transfer_money_tv);
            mBackReasonTv = itemView.findViewById(R.id.sk_pay_transfer_reason);
            mBackTimeTv = itemView.findViewById(R.id.sk_pay_transfer_back_time_tv);
            mTransferTimeTv = itemView.findViewById(R.id.sk_pay_transfer_transfer_time);
        }
    }

    class PaymentHolder extends RecyclerView.ViewHolder {

        TextView mMoneyTv;
        TextView mReceiptUserTv;

        public PaymentHolder(View itemView) {
            super(itemView);
            mMoneyTv = itemView.findViewById(R.id.sk_pay_payment_money_tv);
            mReceiptUserTv = itemView.findViewById(R.id.sk_pay_payment_receipt_user_tv);
        }
    }

    class ReceiptHolder extends RecyclerView.ViewHolder {

        TextView mMoneyTv;
        TextView mPaymentUserTv;

        public ReceiptHolder(View itemView) {
            super(itemView);
            mMoneyTv = itemView.findViewById(R.id.sk_pay_receipt_money_tv);
            mPaymentUserTv = itemView.findViewById(R.id.sk_pay_receipt_payment_user_tv);
        }
    }

    class PayCertificateHolder extends RecyclerView.ViewHolder {

        TextView mMoneyTv;
        TextView mReceiptUserTv;

        public PayCertificateHolder(View itemView) {
            super(itemView);
            mMoneyTv = itemView.findViewById(R.id.sk_pay_payment_money_tv);
            mReceiptUserTv = itemView.findViewById(R.id.sk_pay_payment_receipt_user_tv);
        }
    }

    class SystemViewHolder extends RecyclerView.ViewHolder {

        TextView mSystemTv;

        public SystemViewHolder(View itemView) {
            super(itemView);
            mSystemTv = itemView.findViewById(R.id.chat_content_tv);
        }
    }

}
