package com.ktw.bitbit.ui.map;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ktw.bitbit.FLYAppConstant;
import com.ktw.bitbit.BuildConfig;
import com.ktw.bitbit.FLYApplication;
import com.ktw.bitbit.R;
import com.ktw.bitbit.adapter.NearPositionAdapter;
import com.ktw.bitbit.map.MapHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.ui.tool.ButtonColorChange;
import com.ktw.bitbit.util.FileUtil;
import com.ktw.bitbit.util.PermissionUtil;
import com.ktw.bitbit.util.ScreenUtil;
import com.ktw.bitbit.util.SoftKeyBoardListener;
import com.ktw.bitbit.util.ToastUtil;
import com.ktw.bitbit.view.ClearEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/20.
 */
public class MapPickerActivity extends BaseActivity {
    private boolean isChat;
    private ImageView ivReturn;
    private List<MapHelper.Place> seachPlace = new ArrayList<>();
    private Map<String, MapHelper.Place> placeMap = new HashMap<>();
    private List<MapHelper.Place> placesSeach = new ArrayList<>();
    private MapHelper mapHelper;
    private MapHelper.Picker picker;
    private MapHelper.LatLng beginLatLng;
    private MapHelper.LatLng currentLatLng;
    private ClearEditText ce_map_position;
    private RecyclerView rv_map_position;
    private NearPositionAdapter nearPositionAdapter;
    private boolean showTitle = true;
    private NearPositionAdapter.OnRecyclerItemClickListener itemClickListener = new NearPositionAdapter.OnRecyclerItemClickListener() {
        @Override
        public void onItemClick(int Position, MapHelper.Place dataBean) {
            placeMap.clear();
            placeMap.put("place", dataBean);
            picker.moveMap(dataBean.getLatLng());
        }
    };
    private RelativeLayout rl_position_seach;
    private LinearLayout ll_map;
    private FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionUtil.requestLocationPermissions(this, 0x01);
        setContentView(R.layout.activity_map_picker);
        isChat = getIntent().getBooleanExtra(FLYAppConstant.EXTRA_FORM_CAHT_ACTIVITY, false);
        initView();
        initActionBar();
        if (BuildConfig.DEBUG) {
            com.ktw.bitbit.util.LogUtils.log("after create");
        }
        findViewById(R.id.rl_map_position).setOnClickListener(v -> {
            // ?????????????????????????????????
            InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(findViewById(R.id.rl_map_position).getWindowToken(), 0); //??????????????????
            }
        });
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
        tvTitle.setText(getString(R.string.location));
        TextView tv_title_right = (TextView) findViewById(R.id.tv_title_right);
        tv_title_right.setText(isChat ? getString(R.string.send) : getResources().getString(R.string.sure));
        tv_title_right.setBackground(mContext.getResources().getDrawable(R.drawable.bg_btn_grey_circle));
        ButtonColorChange.colorChange(mContext, tv_title_right);
        tv_title_right.setTextColor(getResources().getColor(R.color.white));
        tv_title_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (picker != null) {
                    View mapView = picker.getMapView();
                    int dw = mapView.getWidth();
                    int dh = mapView.getHeight();
                    // ?????????????????????
                    int width = dw / 2;
                    // ????????????????????????????????????
                    int height = (int) (width * 1f / 672 * 221);
                    // ??????????????????????????????????????????
                    float scale = Math.max(1, Math.min(width * 1.0f / dw, height * 1.0f / dh));
                    final int rw = (int) (width / scale);
                    final int rh = (int) (height / scale);

                    int left = (dw - rw) / 2;
                    int right = (dw + rw) / 2;
                    int top = (dh - rh) / 2;
                    int bottom = (dh + rh) / 2;
                    Rect rect = new Rect(left, top, right, bottom);
                    picker.snapshot(rect, new MapHelper.SnapshotReadyCallback() {
                        @Override
                        public void onSnapshotReady(Bitmap bitmap) {
                            MapHelper.Place place = placeMap.get("place");
                            if (place == null) {
                                if (placesSeach.size() > 0) {
                                    place = placesSeach.get(0);
                                } else {
                                    return;
                                }
                            }
                            Log.e("zx", "onSnapshotReady: " + place);
                            // ???????????????????????????
                            String snapshot = FileUtil.saveBitmap(bitmap);
                            // todo ??????????????????????????????????????????ios??????
                            String address = place.getName();
                            if (TextUtils.isEmpty(address)) {
                                address = FLYApplication.getInstance().getBdLocationHelper().getAddress();
                            }
                            Intent intent = new Intent();
                            intent.putExtra(FLYAppConstant.EXTRA_LATITUDE, place.getLatLng().getLatitude());
                            intent.putExtra(FLYAppConstant.EXTRA_LONGITUDE, place.getLatLng().getLongitude());
                            intent.putExtra(FLYAppConstant.EXTRA_ADDRESS, address);
                            intent.putExtra(FLYAppConstant.EXTRA_SNAPSHOT, snapshot);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
                }
            }
        });

        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {

            @Override
            public void keyBoardShow(int height) {
                Log.e("zx", "keyBoardShow:???????????? ?????? " + height);
                startTranslateAnim(false);
                findViewById(R.id.tv_keyboard).setVisibility(View.VISIBLE);
            }

            @Override
            public void keyBoardHide(int height) {
                Log.e("zx", "keyBoardShow:???????????? ?????? " + height);
                startTranslateAnim(true);
                findViewById(R.id.tv_keyboard).setVisibility(View.GONE);
            }
        });
    }

    public void cancelKeyBoard(View view) {
        // ?????????????????????????????????
        InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(findViewById(R.id.tv_keyboard).getWindowToken(), 0); //??????????????????
        }
    }

    public void startTranslateAnim(boolean show) {
        if (showTitle == show) {
            return;
        }
        showTitle = show;
        float fromy = -(ScreenUtil.getScreenHeight(mContext) / 3);
        float toy = 0;

        if (!show) {
            fromy = 0;
            toy = -(ScreenUtil.getScreenHeight(mContext) / 3);
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(ll_map, "translationY", fromy, toy);
        animator.setDuration(300);
        animator.start();
    }

    public void initView() {
        // ?????????????????????
        ivReturn = findViewById(R.id.iv_location);
        // ???????????????????????????????????????
        ivReturn.setVisibility(View.GONE);
        ce_map_position = findViewById(R.id.ce_map_position);
        ce_map_position.clearFocus();
        ll_map = findViewById(R.id.ll_map);
        rv_map_position = findViewById(R.id.rv_map_position);
        rv_map_position.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.e("zx", "onScrolled:newState: " + newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.e("zx", "onScrolled:dy:  " + dy);

            }
        });
        nearPositionAdapter = new NearPositionAdapter(this);
        nearPositionAdapter.setRecyclerItemClickListener(itemClickListener);
        ivReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLatLng = beginLatLng;
                picker.moveMap(beginLatLng);
                loadMapDatas(currentLatLng);
                ce_map_position.setText("");
            }
        });
        initMap();
        ce_map_position.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                seachPlace.clear();
                if (TextUtils.isEmpty(s.toString()))
                    loadMapDatas(currentLatLng);
                for (int i = 0; i < placesSeach.size(); i++) {
                    if (placesSeach.get(i).getName().contains(s.toString())) {
                        seachPlace.add(placesSeach.get(i));
                    }
                }
                nearPositionAdapter.setData(seachPlace);
            }
        });
    }

    private void initMap() {
        mapHelper = MapHelper.getInstance();
        picker = mapHelper.getPicker(this);
        getLifecycle().addObserver(picker);
        container = findViewById(R.id.map_view_container);
        picker.attack(container, new MapHelper.OnMapReadyListener() {
            @Override
            public void onMapReady() {
                // ????????????????????????????????????
                // ?????????????????????
                picker.addCenterMarker(R.drawable.ic_position, "pos");
                mapHelper.requestLatLng(new MapHelper.OnSuccessListener<MapHelper.LatLng>() {
                    @Override
                    public void onSuccess(MapHelper.LatLng latLng) {
                        // ???????????????????????????????????????????????????????????????
                        beginLatLng = latLng;
                        picker.moveMap(latLng);
                        // ???????????????????????????
                        // ??????????????????????????????????????????
                        loadMapDatas(latLng);
                    }
                }, new MapHelper.OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        ToastUtil.showToast(MapPickerActivity.this, getString(R.string.tip_auto_location_failed) + t.getMessage());
                        // ??????????????????????????????????????????
                        beginLatLng = picker.currentLatLng();
                        picker.moveMap(beginLatLng);
                        loadMapDatas(beginLatLng);

                    }
                });
            }
        });
        picker.setOnMapStatusChangeListener(new MapHelper.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapHelper.MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChange(MapHelper.MapStatus mapStatus) {
            }

            @Override
            public void onMapStatusChangeFinish(MapHelper.MapStatus mapStatus) {
                loadMapDatas(mapStatus.target);
            }
        });
    }

    private void loadMapDatas(MapHelper.LatLng latLng) {
        currentLatLng = latLng;
        // ??????????????????????????????????????????????????????
        ivReturn.setVisibility(View.VISIBLE);
        mapHelper.requestPlaceList(latLng, new MapHelper.OnSuccessListener<List<MapHelper.Place>>() {
            @Override
            public void onSuccess(List<MapHelper.Place> places) {
                nearPositionAdapter.setData(places);
                placesSeach.clear();
                for (int i = 0; i < places.size(); i++) {
                    placesSeach.add(places.get(i));
                }
                LinearLayoutManager layoutManager = new LinearLayoutManager(MapPickerActivity.this);
                rv_map_position.setLayoutManager(layoutManager);
                rv_map_position.setAdapter(nearPositionAdapter);
            }
        }, new MapHelper.OnErrorListener() {
            @Override
            public void onError(Throwable t) {
                ToastUtil.showToast(MapPickerActivity.this, getString(R.string.tip_places_around_failed) + t.getMessage());
            }
        });
    }
}
