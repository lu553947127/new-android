package com.ktw.bitbit.ui.me.select;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.ktw.bitbit.R;
import com.ktw.bitbit.bean.SelectFriendItem;
import com.ktw.bitbit.bean.company.Department;
import com.ktw.bitbit.bean.company.StructBean;
import com.ktw.bitbit.bean.company.StructBeanNetInfo;
import com.ktw.bitbit.helper.AvatarHelper;
import com.ktw.bitbit.helper.DialogHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.util.DisplayUtil;
import com.ktw.bitbit.util.SkinUtils;
import com.ktw.bitbit.view.MarqueeTextView;
import com.ktw.bitbit.view.SelectCpyPopupWindow;
import com.xuan.xuanhttplibrary.okhttp.HttpUtils;
import com.xuan.xuanhttplibrary.okhttp.callback.ListCallback;
import com.xuan.xuanhttplibrary.okhttp.result.ArrayResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import fm.jiecao.jcvideoplayer_lib.MessageEvent;
import okhttp3.Call;

/**
 * ζηεδΊ
 */
public class SelectColleagueActivity extends BaseActivity {
    private static Context mContext;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private Set<SelectFriendItem> mSelectedList = new HashSet<>();
    private List<StructBeanNetInfo> mStructData;// ζε‘ε¨θΏεηε?ζ΄ζ°ζ?
    private List<StructBean> mStructCloneData;
    private List<Department> mDepartments;
    private List<String> userList;
    private List<String> forCurrentSonDepart;
    private List<String> forCurrenttwoSonDepart;
    private List<String> forCurrentthrSonDepart;
    private SelectCpyPopupWindow mSelectCpyPopupWindow;
    private String mLoginUserId;
    private String mCompanyCreater;// ε¬εΈεε»Ίθ
    private String mCompanyId;     // ε¬εΈid
    private String rootDepartment;
    private Map<String, StructBean> structBeanMap = new HashMap<>();

    public static void start(Activity ctx, int requestCode, List<SelectFriendItem> mSelectedList) {
        mContext = ctx;
        Intent intent = new Intent(ctx, SelectColleagueActivity.class);
        if (mSelectedList != null && mSelectedList.size() > 0) {
            intent.putExtra("SELECTED_ITEMS", JSON.toJSONString(mSelectedList));
        }
        ctx.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_colleague);
        mLoginUserId = coreManager.getSelf().getUserId();
        String sSelectedList = getIntent().getStringExtra("SELECTED_ITEMS");
        if (!TextUtils.isEmpty(sSelectedList)) {
            mSelectedList.addAll(JSON.parseArray(sSelectedList, SelectFriendItem.class));
        }
        initActionBar();
        initView();
        initData();
    }

    private void initActionBar() {
        getSupportActionBar().hide();
        TextView tvTitle = findViewById(R.id.tv_title_center);
        tvTitle.setText(getString(R.string.select_colleague));
        TextView tv_title_right = findViewById(R.id.tv_title_right);
        tv_title_right.setText(getString(R.string.finish));
        tv_title_right.setOnClickListener(v -> {
            result();
        });
        findViewById(R.id.iv_title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.companyRecycle);
        mAdapter = new MyAdapter(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        EventBus.getDefault().register(this);
    }

    private void initData() {
        mStructData = new ArrayList<>();
        mStructCloneData = new ArrayList<>();

        mDepartments = new ArrayList<>();
        userList = new ArrayList<>();
        forCurrentSonDepart = new ArrayList<>();
        forCurrenttwoSonDepart = new ArrayList<>();
        forCurrentthrSonDepart = new ArrayList<>();
        loadData();
    }

    private void result() {
        Set<SelectFriendItem> items = new HashSet<>();
        for (StructBean bean : mStructCloneData) {
            if (bean.isEmployee() && bean.getSelected()) {
                items.add(new SelectFriendItem(bean.getUserId(), bean.getText(), 0));
            }
        }
        Intent intent = new Intent();
        intent.putExtra("SELECTED_ITEMS", JSON.toJSONString(items));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void helloEventBus(final MessageEvent message) {
        if (message.message.equals("Update")) {// ζ΄ζ°
            initData();
        }
    }

    private void loadData() {
        // ζ Ήζ?userIdζ₯θ―’ζε±ε¬εΈ
        Map<String, String> params = new HashMap<>();
        params.put("access_token", coreManager.getSelfStatus().accessToken);
        params.put("userId", coreManager.getSelf().getUserId());
        DialogHelper.showDefaulteMessageProgressDialog(this);

        HttpUtils.get().url(coreManager.getConfig().AUTOMATIC_SEARCH_COMPANY)
                .params(params)
                .build()
                .execute(new ListCallback<StructBeanNetInfo>(StructBeanNetInfo.class) {
                    @Override
                    public void onResponse(ArrayResult<StructBeanNetInfo> result) {
                        DialogHelper.dismissProgressDialog();
                        if (result.getResultCode() == 1) {
                            // ζ°ζ?ε·²ζ­£η‘?θΏε
                            mStructData = result.getData();
                            if (mStructData == null || mStructData.size() == 0) {
                                // ζ°ζ?δΈΊnull
                                Toast.makeText(SelectColleagueActivity.this, R.string.tip_no_data, Toast.LENGTH_SHORT).show();
                            } else {
                                // θ?Ύη½?ζ°ζ?
                                setData(mStructData);
                            }
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e) {
                        DialogHelper.dismissProgressDialog();
                        Toast.makeText(SelectColleagueActivity.this, R.string.check_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setData(List<StructBeanNetInfo> data) {
        StructBean structBean;
        for (int i = 0; i < data.size(); i++) {
            structBean = new StructBean();
            // ε¬εΈδΈΊζι«ηΊ§οΌpIdη»δΈδ»₯1εδΈΊζ θ―
            structBean.setParent_id("1");
            // ε¬εΈid
            mCompanyId = data.get(i).getId();
            structBean.setId(mCompanyId);
            mCompanyCreater = String.valueOf(data.get(i).getCreateUserId());
            structBean.setCreateUserId(mCompanyCreater);
            structBean.setCompanyId(mCompanyId);
            // ζ΅θ―εη°ε₯½εζ―iosη«―ε°androidη«―ζε₯ε¬εΈοΌandroidη«―ζεΌεδΊε΄©ζΊ
            // ζ₯εΏζΎη€ΊδΈΊgetRootDpartIdθΏεηζ―null
            // ηζε‘η«―θΏεηζ°ζ?rootDepartmentIdε―δ»₯ε¨departmentεθ·εε°
            // εζΆrootDepartmentIdε₯½εζ―ιθΏcompanyId+1ηζηοΌεεΌε?ΉδΈδΈε§
            if (data.get(i).getRootDpartId() != null && data.get(i).getRootDpartId().size() > 0) {
                rootDepartment = data.get(i).getRootDpartId().get(0);
            } else {
                rootDepartment = data.get(i).getDepartments().get(1).getParentId();
            }
            // ε¬εΈεη§°
            structBean.setText(data.get(i).getCompanyName());
            // ε¬εΈε¬ε
            if (TextUtils.isEmpty(data.get(i).getNoticeContent())) {
                structBean.setNotificationDes(getString(R.string.no_notice));
            } else {
                structBean.setNotificationDes(data.get(i).getNoticeContent());
            }
            /*
            ζ°ζ?ε±η€Ί
             */
            // ζ―ε¦ε±εΌ
            structBean.setExpand(false);
            // ε±ηΊ§
            structBean.setIndex(0);
            structBean.setCompany(true);
            structBean.setDepartment(false);
            structBean.setEmployee(false);
            //
            mStructCloneData.add(structBean);
            boolean companySelected = true;
            StructBean companyStructBean = structBean;
            /**
             * θ―₯ε¬εΈδΈζε±ι¨ι¨ζ°ζ?
             */
            List<StructBeanNetInfo.DepartmentsBean> dps = data.get(i).getDepartments();
            for (int j = 0; j < dps.size(); j++) {
                // ι¨ι¨ζ°ζ?
                Department department = new Department();
                department.setDepartmentId(dps.get(j).getId());
                department.setDepartmentName(dps.get(j).getDepartName());
                department.setBelongToCompany(dps.get(j).getCompanyId());
                mDepartments.add(department);

                structBean = new StructBean();
                // εε·₯ηΊ§ε«ζ θ―
                int employeeIndex = 2;
                // ζ―ε¦δΈΊεηΊ§ι¨ι¨δΉδΈηι¨ι¨
                boolean otherSon = false;
                // ζ θ―:εδΈΊζζι¨ι¨ηparentidθ?Ύη½?δΈΊε¬εΈid,ζ Ήι¨ι¨δΈζΎη€Ί
                if (!dps.get(j).getId().equals(rootDepartment)) {
                    structBean.setParent_id(dps.get(j).getCompanyId());
                }
                // ι»θ?€ι¨ι¨parentidδΈΊζ Ήι¨ι¨id,εεε»ΊδΈηΊ§ι¨ι¨parentidδΈΊε¬εΈid
                if (rootDepartment.equals(dps.get(j).getParentId()) || mCompanyId.equals(dps.get(j).getParentId())) {
                    // δΈηΊ§ι¨ι¨
                    forCurrentSonDepart.add(dps.get(j).getId());
                    structBean.setIndex(1);
                    // θ―₯ι¨ι¨δΈεε·₯δΈζ δΈΊ2
                    employeeIndex = 2;
                    otherSon = true;
                }
                /*
                ε¨ζ­€θΏθ‘δΊγδΈγεηΊ§ι¨ι¨ηε€ζ­
                 */
                for (int k = 0; k < forCurrentSonDepart.size(); k++) {
                    // ιει¨ι¨ιε,ε¦ζζδΈͺι¨ι¨ηparentIdη­δΊδΈηΊ§ι¨ι¨ηζδΈͺι¨ι¨IdοΌθ―΄ζζ­€ι¨ι¨δΈΊδΊηΊ§η?ε½
                    if (forCurrentSonDepart.get(k).equals(dps.get(j).getParentId())) {
                        // δΈΊδΈηΊ§ι¨ι¨εε€ζ°ζ?
                        forCurrenttwoSonDepart.add(dps.get(j).getId());
                        // ιζ°θ?Ύη½?θ―₯ι¨ι¨ηparent_id
                        structBean.setParent_id(dps.get(j).getParentId());
                        // θ?Ύη½?δΈζ 
                        structBean.setIndex(2);
                        employeeIndex = 3;
                        otherSon = true;
                    }
                }
                for (int k = 0; k < forCurrenttwoSonDepart.size(); k++) {
                    // ιει¨ι¨ιε,ε¦ζζδΈͺι¨ι¨ηparentIdη­δΊδΊηΊ§ι¨ι¨ηζδΈͺι¨ι¨IdοΌθ―΄ζζ­€ι¨ι¨δΈΊδΈηΊ§η?ε½
                    if (forCurrenttwoSonDepart.get(k).equals(dps.get(j).getParentId())) {
                        forCurrentthrSonDepart.add(dps.get(j).getId());
                        // ιζ°θ?Ύη½?θ―₯ι¨ι¨ηparent_id
                        structBean.setParent_id(dps.get(j).getParentId());
                        // θ?Ύη½?δΈζ 
                        structBean.setIndex(3);
                        employeeIndex = 4;
                        otherSon = true;
                    }
                }
                for (int k = 0; k < forCurrentthrSonDepart.size(); k++) {
                    // ιει¨ι¨ιε,ε¦ζζδΈͺι¨ι¨ηparentIdη­δΊδΈηΊ§ι¨ι¨ηζδΈͺι¨ι¨IdοΌθ―΄ζζ­€ι¨ι¨δΈΊεηΊ§η?ε½
                    if (forCurrentthrSonDepart.get(k).equals(dps.get(j).getParentId())) {
                        // ιζ°θ?Ύη½?θ―₯ι¨ι¨ηparent_id
                        structBean.setParent_id(dps.get(j).getParentId());
                        // θ?Ύη½?δΈζ 
                        structBean.setIndex(4);
                        employeeIndex = 5;
                        otherSon = true;
                    }
                }
                if (!otherSon) {
                    // δ»₯δΈι¨ι¨εδΈε­ε¨θ―₯ι¨ι¨οΌθ³ε°δΈΊδΊηΊ§ι¨ι¨οΌδΈζ θ?Ύη½?δΈΊ5ε§
                    // ιζ°θ?Ύη½?θ―₯ι¨ι¨ηparent_id
                    structBean.setParent_id(dps.get(j).getParentId());
                    // θ?Ύη½?δΈζ 
                    structBean.setIndex(5);
                    employeeIndex = 6;
                }
                // ι¨ι¨id
                structBean.setId(dps.get(j).getId());
                // ε¬εΈid
                structBean.setCompanyId(dps.get(j).getCompanyId());

                // ε¬εΈεε»ΊθidοΌε€ζ­ζ―ε¦ζ₯ζζδ½ζι
                structBean.setCreateUserId(mCompanyCreater);
                // ι¨ι¨εη§°
                structBean.setText(dps.get(j).getDepartName());
                // ι¨ι¨δΈζζεε·₯userId,δΈΊδΊιΏεζ·»ε εε·₯ζΆεΊη°ιε€
                List<StructBeanNetInfo.DepartmentsBean.EmployeesBean> empList = dps.get(j).getEmployees();

                for (StructBeanNetInfo.DepartmentsBean.EmployeesBean employeesBean : empList) {
                    int userId = employeesBean.getUserId();
                    userList.add(String.valueOf(userId));
                }
                /*
                ζ°ζ?ζΎη€Ί
                 */
                structBean.setExpand(false);
                /*
                ζ°ζ?ζδ½
                 */
                structBean.setCompany(false);
                structBean.setDepartment(true);
                structBean.setEmployee(false);
                mStructCloneData.add(structBean);
                StructBean departmentStructBean = structBean;
                /**
                 * θ―₯ι¨ι¨δΈζε±εε·₯ζ°ζ?
                 */
                List<StructBeanNetInfo.DepartmentsBean.EmployeesBean> eps = dps.get(j).getEmployees();
                boolean departmentSelected = true;
                for (int z = 0; z < eps.size(); z++) {
                    structBean = new StructBean();
                    // ζ θ―:ι¨ι¨id
                    structBean.setParent_id(eps.get(z).getDepartmentId());
                    // εε·₯id
                    structBean.setId(eps.get(z).getId());
                    // ι¨ι¨id
                    structBean.setDepartmentId(eps.get(z).getDepartmentId());
                    // ε¬εΈid
                    structBean.setCompanyId(eps.get(z).getCompanyId());

                    // ε¬εΈεε»Ίθ
                    structBean.setCreateUserId(mCompanyCreater);
                    structBean.setEmployeeToCompanyId(eps.get(z).getCompanyId());
                    // employee name/id/role
                    structBean.setText(eps.get(z).getNickname());
                    structBean.setUserId(String.valueOf(eps.get(z).getUserId()));
                    structBean.setIdentity(eps.get(z).getPosition());
                    structBean.setRole(eps.get(z).getRole());
                    // ζε±ζ Ήι¨ι¨
                    structBean.setRootDepartmentId(rootDepartment);
                    structBean.setExpand(false);
                    if (employeeIndex == 2) {
                        structBean.setIndex(2);
                    } else if (employeeIndex == 3) {
                        structBean.setIndex(3);
                    } else if (employeeIndex == 4) {
                        structBean.setIndex(4);
                    } else if (employeeIndex == 5) {
                        structBean.setIndex(5);
                    } else {
                        structBean.setIndex(6);
                    }
                    structBean.setCompany(false);
                    structBean.setDepartment(false);
                    structBean.setEmployee(true);
                    if (mSelectedList.contains(new SelectFriendItem(structBean.getUserId(), structBean.getText(), 0))) {
                        structBean.setSelected(true);
                    } else {
                        departmentSelected = false;
                        companySelected = false;
                    }
                    mStructCloneData.add(structBean);
                }
                departmentStructBean.setSelected(departmentSelected);
            }
            companyStructBean.setSelected(companySelected);
        }
        // δΈΊδΊθͺε¨εΎιε·²η»ιδΈ­ζζζεηι¨ι¨εε¬εΈοΌ
        // δ½ζ―ζ²‘ζ³η΄ζ₯θ·εδΈδΈͺι¨ι¨ηηΆι¨ι¨εε­ι¨ι¨οΌδΊζ―εͺθ½ιεοΌ
        Map<String, StructBean> existsMemberMap = new HashMap<>();
        for (StructBean s : mStructCloneData) {
            structBeanMap.put(s.getId(), s);
        }
        for (StructBean s : mStructCloneData) {
            StructBean parent = structBeanMap.get(s.getParent_id());
            if (parent != null) {
                existsMemberMap.put(parent.getId(), parent);
            }
            // ε­ι¨ι¨ζ²‘ιδΈ­ε°ε―Όθ΄ηΆι¨ι¨ζ²‘ιδΈ­οΌ
            if (!s.getSelected()) {
                while (parent != null) {
                    parent.setSelected(false);
                    parent = structBeanMap.get(parent.getParent_id());
                }
            }
        }
        for (StructBean s : mStructCloneData) {
            if (!s.isEmployee() && !existsMemberMap.containsKey(s.getId())) {
                // ζ²‘ζζεηι¨ι¨δΈθ¦θͺε¨ιδΈ­οΌ
                s.setSelected(false);
            }
        }
        mAdapter.setData(mStructCloneData);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    interface ItemClickListener {
        void onItemClick(int layoutPosition);

        void onItemSelectChange(int layoutPosition, boolean selected);
    }

    class MyAdapter extends RecyclerView.Adapter<StructHolder> {
        // ε·²η»θ?Ύη½?ε₯½ηε?ζ΄ζ°ζ?
        List<StructBean> mData;
        // ηζ­£ε±η€Ίηζ°ζ?
        List<StructBean> currData;
        LayoutInflater mInflater;
        Context mContext;
        ItemClickListener mListener;

        public MyAdapter(Context context) {
            mData = new ArrayList<>();
            currData = new ArrayList<>();
            mInflater = LayoutInflater.from(context);
            this.mContext = context;
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            StructBean bean = currData.get(position);
            return bean.getId().hashCode();
        }

        public void setOnItemClickListener(ItemClickListener listener) {
            mListener = listener;
        }

        public void setData(List<StructBean> data) {
            mData = data;
            currData.clear();
            for (int i = 0; i < mData.size(); i++) {
                StructBean info = mData.get(i);
                if (info.getParent_id() != null) {
                    if (info.getParent_id().equals("1")) {
                        // ι»θ?€ε±εΌη¬¬δΈδΈͺε¬εΈδΈι’ηι¨ι¨
                        currData.add(info);
                        if (i == 0) {
                            info.setExpand(true);
                            openItemData(info.getId(), 0, info.getIndex());
                        }
                    }
                }
            }
            notifyDataSetChanged();
        }

        /**
         * ε±η€Ίε¬εΈ(ι¨ι¨)εΈε±ζθζεεΈε±
         */
        private void showView(boolean group, StructHolder holder) {
            if (group) {
                holder.rlGroup.setVisibility(View.VISIBLE);
                holder.rlPersonal.setVisibility(View.GONE);
            } else {
                holder.rlGroup.setVisibility(View.GONE);
                holder.rlPersonal.setVisibility(View.VISIBLE);
            }
        }

        /**
         * ε±εΌitem
         */
        private void openItemData(String id, int position, int index) {
            for (int i = mData.size() - 1; i > -1; i--) {
                StructBean data = mData.get(i);
                // ζ Ήζ?parent_idδΈΊcurrDataζ·»ε ζ°ζ?
                if (id.equals(data.getParent_id())) {
                    data.setExpand(false);// ι»θ?€ζΆθ΅·
                    data.setIndex(index + 1);
                    currData.add(position + 1, data);
                }
            }
            notifyDataSetChanged();
        }

        /**
         * ```````
         */
        private void closeItemData(String id, int position) {
            StructBean structBean = currData.get(position);
            if (structBean.isCompany()) {
                for (int i = currData.size() - 1; i >= 0; i--) {
                    StructBean data = currData.get(i);
                    if (data.getId().equals(structBean.getId()) || data.getCompanyId().equals(structBean.getId())) { // ε¬εΈ || ε¬εΈδΈηι¨ι¨&εε·₯
                        if (data.isCompany()) { // ε¬εΈ
                            data.setExpand(false);
                        } else if (data.isDepartment()) { // ι¨ι¨
                            data.setExpand(false);
                            data.setIndex(data.getIndex() - 1);
                            currData.remove(i);
                        } else if (data.isEmployee()) { // εε·₯
                            data.setIndex(data.getIndex() - 1);
                            currData.remove(i);
                        }
                    }
                }
            } else if (structBean.isDepartment()) {
                Map<String, String> expandMap = new HashMap<>();
                expandMap.put(structBean.getId(), structBean.getId());
                List<StructBean> structBeans = new ArrayList<>();
                for (int i = 0; i < currData.size(); i++) {
                    StructBean data = currData.get(i);
                    if (expandMap.containsKey(data.getId())
                            || expandMap.containsKey(data.getParent_id())
                            || (data.isEmployee() && expandMap.containsKey(data.getDepartmentId()))) {
                        if (data.getId().equals(structBean.getId())) {
                            data.setExpand(false);// ε½εηΉε»ηι¨ι¨
                        } else {
                            if (data.isDepartment()) {// ε­ι¨ι¨θΏζδΊΊοΌιθ¦ε°ε­ι¨ι¨ηidδΉθ?°ε½δΈ
                                expandMap.put(data.getId(), data.getId());
                            }
                            structBeans.add(data);
                        }
                    }
                }
                currData.removeAll(structBeans);
            }
            notifyDataSetChanged();
        }

        @Override
        public StructHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = mInflater.inflate(R.layout.manager_company_item_select, null);
            final View add = view.findViewById(R.id.iv_group_add);
            final View add2 = view.findViewById(R.id.iv_group_add2);
            StructHolder holder = new StructHolder(view, new ItemClickListener() {
                // item click
                @Override
                public void onItemClick(int layoutPosition) {
                    StructBean bean = currData.get(layoutPosition);
                    // company/department click
                    if (bean.isExpand()) {
                        bean.setExpand(false);
                        closeItemData(bean.getId(), layoutPosition);
                    } else {
                        bean.setExpand(true);
                        openItemData(bean.getId(), layoutPosition, bean.getIndex());
                    }
                    // employee click
                    if (bean.isEmployee()) {
                        showEmployeeInfo(add2, layoutPosition);
                    }
                }

                @Override
                public void onItemSelectChange(int layoutPosition, boolean selected) {
                    StructBean bean = currData.get(layoutPosition);
                    if (bean.getSelected() == selected) {
                        return;
                    }
                    bean.setSelected(selected);
                    updateChild(bean);
                    updateParent(bean);

                    notifyDataSetChanged();
                }

                // ιδΈ­ε¬εΈζθι¨ι¨ε°±ε¨ιοΌ
                // ι‘ΊεΊε―θ½δΈεοΌζδ»₯ζ²‘εΆδ»ζΉζ³θ·εΎι¨ι¨ζεοΌεͺθ½ιεοΌ
                private void updateChild(StructBean bean) {
                    if (bean == null) {
                        return;
                    }
                    if (bean.isEmployee()) {
                        return;
                    }
                    for (StructBean s : mData) {
                        if (!TextUtils.equals(s.getParent_id(), bean.getId())) {
                            continue;
                        }
                        boolean selected = bean.getSelected();
                        if (s.getSelected() == selected) {
                            continue;
                        }
                        s.setSelected(selected);
                        updateChild(s);
                    }
                }

                private void updateParent(StructBean bean) {
                    boolean selected = bean.getSelected();
                    StructBean parent = structBeanMap.get(bean.getParent_id());
                    if (parent == null) {
                        return;
                    }
                    if (parent.getSelected() == selected) {
                        return;
                    }
                    if (parent.getSelected() && !selected) {
                        parent.setSelected(false);
                        updateParent(parent);
                        return;
                    }
                    // δ»₯δΈζ―ηΆι¨ι¨ζͺιδΈ­ζε΅δΈιδΈ­ε­ι¨ι¨οΌιθ¦ε€ζ­ζ―ε¦θ·ηιδΈ­ηΆι¨ι¨οΌ
                    // ζ°ζ?η»ζθ?Ύθ?‘ηζ―ζ²‘εζ³η΄ζ₯ζΏε°ηΆι¨ι¨εε­ι¨ι¨οΌε ζ­€εͺθ½δΈζ¬‘ζ¬‘ιεοΌ
                    for (StructBean s : mData) {
                        if (TextUtils.equals(s.getParent_id(), parent.getId())) {
                            if (!s.getSelected()) {
                                return;
                            }
                        }
                    }
                    // ε°θΏθ‘¨η€ΊηΆι¨ι¨ζζεΆδ»ε­ι¨ι¨ι½ζ―ιδΈ­δΊηοΌζδ»₯ηΆι¨ι¨θ·ηιδΈ­οΌ
                    parent.setSelected(true);
                    updateParent(parent);
                }

                private int type(StructBean o) {
                    return o.getIndex();
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(StructHolder holder, int position) {
            StructBean bean = currData.get(position);
            showView(bean.isCompany() || bean.isDepartment(), holder);
            if (bean.isCompany() || bean.isDepartment()) {
                if (bean.isExpand()) {
                    holder.ivGroup.setImageResource(R.mipmap.ex);
                    holder.ivGroupAdd.setVisibility(View.INVISIBLE);
                } else {
                    holder.ivGroup.setImageResource(R.mipmap.ec);
                    holder.ivGroupAdd.setVisibility(View.INVISIBLE);
                }
                ViewGroup.LayoutParams lp = holder.ivGroup.getLayoutParams();
                if (bean.getIndex() > 0) {
                    lp.width = DisplayUtil.dip2px(mContext, 10);
                    lp.height = DisplayUtil.dip2px(mContext, 10);
                } else {
                    lp.width = DisplayUtil.dip2px(mContext, 14);
                    lp.height = DisplayUtil.dip2px(mContext, 14);
                }
                holder.ivGroup.setLayoutParams(lp);
/*
                lp = holder.ivGroupAdd.getLayoutParams();
                if (bean.getIndex() > 0) {
                    lp.width = DisplayUtil.dip2px(mContext, 8);
                    lp.height = DisplayUtil.dip2px(mContext, 2);
                } else {
                    lp.width = DisplayUtil.dip2px(mContext, 12);
                    lp.height = DisplayUtil.dip2px(mContext, 3);
                }
                holder.ivGroupAdd.setLayoutParams(lp);
*/
                if (bean.isCompany()) {
                    // ζΎη€Ίε¬ε
                    holder.tvNotificationDes.setText(bean.getNotificationDes());
                    holder.rlNotification.setVisibility(View.VISIBLE);
                    // θ?Ύη½?θζ―ι’θ²
                    // holder.rlGroup.setBackgroundColor(getResources().getAccentColor(R.color.department_item));
                } else if (bean.isDepartment()) {
                    // ιθε¬ε
                    holder.rlNotification.setVisibility(View.GONE);
                    // holder.rlGroup.setBackgroundColor(getResources().getAccentColor(R.color.person_item));
                }
                holder.tvGroupText.setText(bean.getText());
                // ζ Ήζ?δΈζ θ?Ύη½?padding
                holder.rlGroup.setPadding(DisplayUtil.dip2px(mContext, 14 + 9 * bean.getIndex()), 0, 0, 0);
                holder.cbSelect.setChecked(bean.getSelected());
            } else {
                // ζε
                AvatarHelper.getInstance().displayAvatar(bean.getText(), bean.getUserId(), holder.ivInco, true);
                holder.tvTextName.setText(bean.getText());
                holder.tvIdentity.setText(bean.getIdentity());
                holder.rlPersonal.setPadding(DisplayUtil.dip2px(mContext, 14 + 9 * bean.getIndex() + 12), 0, 0, 0);
                holder.cbSelect2.setChecked(bean.getSelected());
            }
        }

        @Override
        public int getItemCount() {
            return currData.size();
        }

        @SuppressLint("ResourceAsColor")
        private void showEmployeeInfo(final View asView, final int layoutPosition) {
        }

    }

    class StructHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // ε¬εΈ/ι¨ι¨εη§°
        TextView tvGroupText;
        // εε·₯εη§°
        TextView tvTextName;
        // εε·₯θΊ«δ»½
        TextView tvIdentity;
        // δΈδΈ
        ImageView ivGroup;
        // ζ·»ε ...
        ImageView ivGroupAdd;
        // ε€΄ε
        ImageView ivInco;
        // ε¬εεε?Ή
        MarqueeTextView tvNotificationDes;
        // ε¬εΈ/ι¨ι¨
        RelativeLayout rlGroup;
        // ε¬ε
        LinearLayout rlNotification;
        // δΈͺδΊΊ
        LinearLayout rlPersonal;
        ItemClickListener mListener;
        CheckBox cbSelect = itemView.findViewById(R.id.cbSelect);
        CheckBox cbSelect2 = itemView.findViewById(R.id.cbSelect2);

        public StructHolder(View itemView, ItemClickListener listener) {
            super(itemView);
            mListener = listener;
            tvGroupText = (TextView) itemView.findViewById(R.id.tv_group_name);
            tvTextName = (TextView) itemView.findViewById(R.id.tv_text_name);
            tvIdentity = (TextView) itemView.findViewById(R.id.tv_text_role);
            tvIdentity.setTextColor(SkinUtils.getSkin(mContext).getAccentColor());
            tvNotificationDes = itemView.findViewById(R.id.notification_des);
            tvNotificationDes.setTextColor(SkinUtils.getSkin(mContext).getAccentColor());
            ivGroup = (ImageView) itemView.findViewById(R.id.iv_arrow);
            ImageViewCompat.setImageTintList(ivGroup, ColorStateList.valueOf(SkinUtils.getSkin(itemView.getContext()).getAccentColor()));
            ivGroupAdd = (ImageView) itemView.findViewById(R.id.iv_group_add);
            ImageViewCompat.setImageTintList(ivGroupAdd, ColorStateList.valueOf(SkinUtils.getSkin(itemView.getContext()).getAccentColor()));
            ivInco = (ImageView) itemView.findViewById(R.id.iv_inco);
            rlGroup = (RelativeLayout) itemView.findViewById(R.id.rl_group);
            rlNotification = (LinearLayout) itemView.findViewById(R.id.notification_ll);
            rlPersonal = (LinearLayout) itemView.findViewById(R.id.rl_personal);
            /**
             * θ?Ύη½?ηΉε»δΊδ»Ά
             */
            rlGroup.setOnClickListener(this);
            rlPersonal.setOnClickListener(this);
            ivGroupAdd.setOnClickListener(this);
            tvNotificationDes.setOnClickListener(this);
            tvIdentity.setOnClickListener(this);

            cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> mListener.onItemSelectChange(getLayoutPosition(), isChecked));
            cbSelect2.setOnCheckedChangeListener((buttonView, isChecked) -> mListener.onItemSelectChange(getLayoutPosition(), isChecked));
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                default:
                    mListener.onItemClick(getLayoutPosition());
                    break;
            }
        }
    }
}
