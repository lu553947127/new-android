package com.ktw.bitbit.ui.contacts;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.FLYReporter;
import com.ktw.bitbit.bean.Contact;
import com.ktw.bitbit.bean.Contacts;
import com.ktw.bitbit.db.dao.ContactDao;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.sortlist.BaseComparator;
import com.ktw.bitbit.sortlist.BaseSortModel;
import com.ktw.bitbit.sortlist.SideBar;
import com.ktw.bitbit.sortlist.SortHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.util.AsyncUtils;
import com.ktw.bitbit.util.Constants;
import com.ktw.bitbit.util.ContactsUtil;
import com.ktw.bitbit.util.PermissionUtil;
import com.ktw.bitbit.util.PreferenceUtils;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.util.ViewHolder;
import com.ktw.bitbit.view.PullToRefreshSlideListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SendContactsActivity extends BaseActivity {
    private SideBar mSideBar;
    private TextView mTextDialog;
    private PullToRefreshSlideListView mListView;
    private ContactsAdapter mContactsAdapter;
    private List<Contacts> mContactList;
    private List<BaseSortModel<Contacts>> mSortContactList;
    private BaseComparator<Contacts> mBaseComparator;
    private String mLoginUserId;
    // ??????
    private TextView tvTitleRight;
    private boolean isBatch;
    private Map<String, Contacts> mBatchAddContacts = new HashMap<>();
    private TextView mBatchAddTv;

    private Map<String, Contacts> phoneContacts;

    private int mobilePrefix;

    public static void start(Activity ctx, int requestCode) {
        Intent intent = new Intent(ctx, SendContactsActivity.class);
        ctx.startActivityForResult(intent, requestCode);
    }

    private static void makeResult(Intent intent, List<Contacts> contactsList) {
        intent.putExtra("contactsList", JSON.toJSONString(contactsList));
    }

    @Nullable
    public static List<Contacts> parseResult(Intent intent) {
        if (intent == null) {
            return null;
        }
        String str = intent.getStringExtra("contactsList");
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        try {
            return JSON.parseArray(str, Contacts.class);
        } catch (Exception e) {
            // ????????????????????????????????????????????????
            FLYReporter.unreachable(e);
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_msg_invite);

        mLoginUserId = coreManager.getSelf().getUserId();
        mContactList = new ArrayList<>();
        mSortContactList = new ArrayList<>();
        mBaseComparator = new BaseComparator<>();
        mContactsAdapter = new ContactsAdapter();

        mobilePrefix = PreferenceUtils.getInt(FLYApplication.getContext(), Constants.AREA_CODE_KEY, 86);

        initActionBar();
        boolean isReadContacts = PermissionUtil.checkSelfPermissions(this, new String[]{Manifest.permission.READ_CONTACTS});
        if (!isReadContacts) {
            DialogHelper.tip(this, "????????????????????????");
            return;
        }

        initView();
        dataLayering();
        initEvent();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.phone_contact));

        tvTitleRight = (TextView) findViewById(R.id.tv_title_right);
        tvTitleRight.setText(getString(R.string.select_all));
    }

    public void initView() {
        mListView = (PullToRefreshSlideListView) findViewById(R.id.pull_refresh_list);
        mListView.getRefreshableView().setAdapter(mContactsAdapter);
        mListView.setMode(PullToRefreshBase.Mode.DISABLED);

        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar.setTextView(mTextDialog);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // ??????????????????????????????
                int position = mContactsAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.getRefreshableView().setSelection(position);
                }
            }
        });

        mBatchAddTv = (TextView) findViewById(R.id.sure_add_tv);
    }

    private void dataLayering() {
        phoneContacts = ContactsUtil.getPhoneContacts(this);

        List<Contact> allContacts = ContactDao.getInstance().getAllContacts(mLoginUserId);
        // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        Set<Contact> set = new TreeSet<>(new Comparator<Contact>() {
            @Override
            public int compare(Contact o1, Contact o2) {
                return o1.getToUserId().compareTo(o2.getToUserId());
            }
        });
        set.addAll(allContacts);
        allContacts = new ArrayList<>(set);

        // ???????????????IM??????????????????????????????IM????????????
        for (int i = 0; i < allContacts.size(); i++) {
            phoneContacts.remove(allContacts.get(i).getTelephone());
        }

        Collection<Contacts> values = phoneContacts.values();
        mContactList = new ArrayList<>(values);

        DialogHelper.showDefaulteMessageProgressDialog(this);
        try {
            AsyncUtils.doAsync(this, e -> {
                FLYReporter.post("?????????????????????", e);
                AsyncUtils.runOnUiThread(this, ctx -> {
                    DialogHelper.dismissProgressDialog();
                    ToastUtil.showToast(ctx, R.string.data_exception);
                });
            }, c -> {
                Map<String, Integer> existMap = new HashMap<>();
                List<BaseSortModel<Contacts>> sortedList = SortHelper.toSortedModelList(mContactList, existMap, Contacts::getName);
                c.uiThread(r -> {
                    DialogHelper.dismissProgressDialog();
                    mSideBar.setExistMap(existMap);
                    mSortContactList = sortedList;
                    mContactsAdapter.setData(sortedList);
                });
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initEvent() {
        tvTitleRight.setOnClickListener(v -> isControlBatchStatus(true));

        TextView shareAppTv = findViewById(R.id.share_app_tv);
        shareAppTv.setText(getString(R.string.share_app, getString(R.string.app_name)));
        findViewById(R.id.invited_friend_ll).setVisibility(View.GONE);

        mListView.getRefreshableView().setOnItemClickListener((parent, view, position, id) -> {
            position = (int) id;
            Contacts contact = mSortContactList.get(position).getBean();
            if (contact != null) {
                if (mBatchAddContacts.containsKey(contact.getTelephone())) {
                    mBatchAddContacts.remove(contact.getTelephone());
                    isControlBatchStatus(false);
                } else {
                    mBatchAddContacts.put(contact.getTelephone(), contact);
                }
                mContactsAdapter.notifyDataSetChanged();
            }
        });

        mBatchAddTv.setOnClickListener(v -> {
            Collection<Contacts> values = mBatchAddContacts.values();
            List<Contacts> contactList = new ArrayList<>(values);
            if (contactList.size() == 0) {
                return;
            }
            String telStr = JSON.toJSONString(contactList);
            sendContacts(contactList);
        });
    }

    private void isControlBatchStatus(boolean isChangeAll) {
        if (isChangeAll) {
            isBatch = !isBatch;
            if (isBatch) {
                tvTitleRight.setText(getString(R.string.cancel));
                for (int i = 0; i < mContactList.size(); i++) {
                    mBatchAddContacts.put(mContactList.get(i).getTelephone(), mContactList.get(i));
                }
            } else {
                tvTitleRight.setText(getString(R.string.select_all));
                mBatchAddContacts.clear();
            }
            mContactsAdapter.notifyDataSetChanged();
        } else {
            isBatch = false;
            tvTitleRight.setText(getString(R.string.select_all));
        }
    }

    private void sendContacts(List<Contacts> contactList) {
        Intent intent = new Intent();
        makeResult(intent, contactList);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    class ContactsAdapter extends BaseAdapter implements SectionIndexer {
        List<BaseSortModel<Contacts>> mSortContactList;

        public ContactsAdapter() {
            mSortContactList = new ArrayList<>();
        }

        public void setData(List<BaseSortModel<Contacts>> sortContactList) {
            mSortContactList = sortContactList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mSortContactList.size();
        }

        @Override
        public Object getItem(int position) {
            return mSortContactList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_contacts_msg_invite, parent, false);
            }
            TextView categoryTitleTv = ViewHolder.get(convertView, R.id.catagory_title);
            View view_bg_friend = ViewHolder.get(convertView, R.id.view_bg_friend);
            CheckBox checkBox = ViewHolder.get(convertView, R.id.check_box);
            ImageView avatarImg = ViewHolder.get(convertView, R.id.avatar_img);
            TextView contactNameTv = ViewHolder.get(convertView, R.id.contact_name_tv);
            TextView userNameTv = ViewHolder.get(convertView, R.id.user_name_tv);

            // ??????position???????????????????????????Char ascii???
            int section = getSectionForPosition(position);
            // ?????????????????????????????????????????????Char????????? ??????????????????????????????
            if (position == getPositionForSection(section)) {
                categoryTitleTv.setVisibility(View.VISIBLE);
                categoryTitleTv.setText(mSortContactList.get(position).getFirstLetter());
                view_bg_friend.setVisibility(View.GONE);
            } else {
                categoryTitleTv.setVisibility(View.GONE);
                view_bg_friend.setVisibility(View.VISIBLE);
            }

            final Contacts contact = mSortContactList.get(position).getBean();
            if (contact != null) {
                checkBox.setChecked(mBatchAddContacts.containsKey(contact.getTelephone()));
                AvatarHelper.getInstance().displayAddressAvatar(contact.getName(), avatarImg);
                contactNameTv.setText(contact.getName());
                // ????????????????????????????????????????????????????????????????????????
                String tel = contact.getTelephone().substring(String.valueOf(mobilePrefix).length());
                userNameTv.setText(tel);
            }

            return convertView;
        }

        @Override
        public Object[] getSections() {
            return null;
        }

        @Override
        public int getPositionForSection(int section) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = mSortContactList.get(i).getFirstLetter();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getSectionForPosition(int position) {
            return mSortContactList.get(position).getFirstLetter().charAt(0);
        }
    }
}
