package com.ktw.bitbit.ui.dialog;

import android.app.Activity;
import android.text.InputFilter;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ktw.bitbit.R;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.ui.dialog.base.BaseDialog;
import com.ktw.bitbit.ui.tool.ButtonColorChange;
import com.ktw.bitbit.view.NoDoubleClickListener;
import com.suke.widget.SwitchButton;

/**
 * Created by Administrator on 2016/4/21.
 */
public class TowInputDialogView extends BaseDialog {

    private TextView mTitleTv;
    private AutoCompleteTextView mContentEt;
    private AutoCompleteTextView mSecondEt;
    private Button mCommitBtn;

    // 显示群消息已读人数、私密群组、是否开启进群验证、是否显示群成员列表、允许普通群成员私聊
    /*
    暂且全都隐藏，设置到群组信息-群管理内设置
     */
   /* private RelativeLayout mRelativeLayout;
    private SwitchButton mSwitchButton;*/
    private int isRead = 0; // 0不显示 1显示(default - 不显示)
    private RelativeLayout mRlPublic;
    private SwitchButton mSbPublic;
    private int isLook = 1;// 0公开 1不公开(default - 不公开)
    /* private RelativeLayout mRlVerify;
     private SwitchButton mSbVerify;*/
    private int isNeedVerify = 0;    // 0不需要 1需要(default - 不需要)
    /* private RelativeLayout mRlShowMember;
     private SwitchButton mSbShowMember;*/
    private int isShowMember = 1;    // 0不显示 1显示(default - 显示)
    /* private RelativeLayout mRlSendCard;
     private SwitchButton mSbSendCard;*/
    private int isAllowSendCard = 1; // 0不允许 1允许(default - 公开)
    private onSureClickLinsenter mOnClickListener;

    {
        RID = R.layout.dialog_double_input;
    }

    public TowInputDialogView(Activity activity) {
        this(activity, "", "", "", null);
    }

    public TowInputDialogView(Activity activity, String title, String hint, String hint2, onSureClickLinsenter onClickListener) {
        mActivity = activity;
        initView();
        setView(title, hint, hint2);
        mOnClickListener = onClickListener;
    }

    public TowInputDialogView(Activity activity, String title,
                              String hint, String hint2, String text, String text2, onSureClickLinsenter onClickListener) {
        mActivity = activity;
        initView();
        setView(title, hint, hint2, text, text2);
        mOnClickListener = onClickListener;
    }

    protected void initView() {
        super.initView();
        mTitleTv = (TextView) mView.findViewById(R.id.title);
        mContentEt = (AutoCompleteTextView) mView.findViewById(R.id.content);
        // mContentEt.setCompletionHint(getString(R.string.please_input_room_name));
        mContentEt.setFilters(new InputFilter[]{DialogHelper.mExpressionFilter, DialogHelper.mChineseEnglishNumberFilter});
        mSecondEt = (AutoCompleteTextView) mView.findViewById(R.id.second_et);
        // mSecondEt.setCompletionHint(getString(R.string.please_input_room_desc));
        mSecondEt.setFilters(new InputFilter[]{DialogHelper.mExpressionFilter, DialogHelper.mChineseEnglishNumberFilter});

        mCommitBtn = (Button) mView.findViewById(R.id.sure_btn);
        ButtonColorChange.textChange(mActivity, mView.findViewById(R.id.tv_input_room_name));
        ButtonColorChange.textChange(mActivity, mView.findViewById(R.id.tv_input_room_desc));
        ButtonColorChange.colorChange(mActivity, mCommitBtn);
        // 显示已读人数、隐私群组、是否开启进群验证、是否对普通成员开放群成员列表、群成员是否可在群组内发送名片
        /*mRelativeLayout = (RelativeLayout) mView.findViewById(R.id.read_rl);
        mSwitchButton = (SwitchButton) mView.findViewById(R.id.switch_read);*/
        mRlPublic = (RelativeLayout) mView.findViewById(R.id.public_rl);
        mSbPublic = (SwitchButton) mView.findViewById(R.id.switch_look);
        /*mRlVerify = (RelativeLayout) mView.findViewById(R.id.verify_rl);
        mSbVerify = (SwitchButton) mView.findViewById(R.id.switch_verify);
        mRlShowMember = (RelativeLayout) mView.findViewById(R.id.show_member_rl);
        mSbShowMember = (SwitchButton) mView.findViewById(R.id.switch_show_member);
        mRlSendCard = (RelativeLayout) mView.findViewById(R.id.send_card_rl);
        mSbSendCard = (SwitchButton) mView.findViewById(R.id.switch_send_card);*/
    }

    private void setView(String title, String hint, String hint2) {
        mTitleTv.setText(title);
        // mContentEt.setHint(hint);
        mSecondEt.setVisibility(View.VISIBLE);
        //  mSecondEt.setHint(hint2);

        /*mSwitchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {// 显示已读人数
                    isRead = 1;
                } else {// 不显示已读人数
                    isRead = 0;
                }
            }
        });*/

/*
        mSbPublic.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {// 私密群组
                    isLook = 1;
                } else {// 公开群组
                    isLook = 0;
                }
            }
        });
*/

/*
        // 可见、不可编辑
        mSbPublic.setEnabled(false);
        mSbPublic.setAlpha(0.4f);
*/

       /* mSbVerify.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {// 需要群主验证
                    isNeedVerify = 1;
                } else {// 不需要群主验证
                    isNeedVerify = 0;
                }
            }
        });

        mSbShowMember.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {// 显示
                    isShowMember = 1;
                } else {// 不显示
                    isShowMember = 0;
                }
            }
        });

        mSbSendCard.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {// 允许私聊
                    isAllowSendCard = 1;
                } else {// 不允许私聊
                    isAllowSendCard = 0;
                }
            }
        });*/

        mCommitBtn.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                // mDialog.dismiss();
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(mContentEt, mSecondEt, isRead, isLook, isNeedVerify, isShowMember, isAllowSendCard);
                }
            }
        });
    }

    private void setView(String title, String hint, String hint2, String text, String text2) {
        mTitleTv.setText(title);
        mContentEt.setHint(hint);
        mContentEt.setText(text);
        mSecondEt.setVisibility(View.VISIBLE);
        mSecondEt.setHint(hint2);
        mSecondEt.setText(text2);

        mRlPublic.setVisibility(View.VISIBLE);

        mSbPublic.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {// 私密群组
                    isLook = 1;
                } else {// 公开群组
                    isLook = 0;
                }
            }
        });

        // 可见、不可编辑
        mSbPublic.setEnabled(false);
        mSbPublic.setAlpha(0.4f);

        mCommitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(mContentEt, mSecondEt, isRead, isLook, isNeedVerify, isShowMember, isAllowSendCard);
                }
            }
        });
    }

    public void setSureClick(onSureClickLinsenter onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void setTitle(String title) {
        mTitleTv.setText(title);
    }

    public void setHint(String hint) {
        mContentEt.setHint(hint);
    }

    public void setMaxLines(int maxLines) {
        mContentEt.setMaxLines(maxLines);
    }

    public String getContent() {
        return mContentEt.getText().toString();
    }

    // 外面需要对两个EditText做操作，给获取方法
    public EditText getE1() {
        return mContentEt;
    }

    public EditText getE2() {
        return mSecondEt;
    }

    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    // 这里有两个EditText，比较特殊，所以单击事件监听器也需要传两个EditText过去
    public interface onSureClickLinsenter {
        void onClick(EditText e1, EditText e2, int isRead, int isLook, int isNeedVerify, int isShowMember, int isAllowSendCard);
    }
}
