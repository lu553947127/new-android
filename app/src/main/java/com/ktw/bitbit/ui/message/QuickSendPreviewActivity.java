package com.ktw.bitbit.ui.message;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.ktw.bitbit.R;
import com.ktw.bitbit.helper.ImageLoadHelper;
import com.ktw.bitbit.ui.base.BaseActivity;
import com.ktw.bitbit.util.FileUtil;

import java.io.File;

import me.kareluo.imaging.IMGEditActivity;

public class QuickSendPreviewActivity extends BaseActivity {
    private static final String KEY_IMAGE = "image";
    private static final int REQUEST_IMAGE_EDIT = 1;
    private String image;
    private String editedPath;
    private ImageView ivImage;

    public static void startForResult(Activity ctx, String image, int requestCode) {
        Intent intent = new Intent(ctx, QuickSendPreviewActivity.class);
        intent.putExtra(KEY_IMAGE, image);
        ctx.startActivityForResult(intent, requestCode);
    }

    public static String parseResult(Intent intent) {
        return intent.getStringExtra(KEY_IMAGE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_send_preview);

        initActionBar();

        image = getIntent().getStringExtra(KEY_IMAGE);

        ivImage = findViewById(R.id.ivImage);

        refresh();
    }

    private void refresh() {
        ImageLoadHelper.showImage(
                this, image, ivImage
        );
    }

    private void initActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        findViewById(R.id.iv_title_left).setOnClickListener(view -> onBackPressed());
        findViewById(R.id.tv_title_right).setOnClickListener(view -> onSendClick());
        findViewById(R.id.iv_title_right).setOnClickListener(view -> onEditClick());
    }

    private void onEditClick() {
        editedPath = FileUtil.createImageFileForEdit().getAbsolutePath();
        IMGEditActivity.startForResult(this, Uri.fromFile(new File(image)), editedPath, REQUEST_IMAGE_EDIT);
    }

    private void onSendClick() {
        Intent intent = new Intent();
        intent.putExtra(KEY_IMAGE, image);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_EDIT:
                    image = editedPath;
                    refresh();
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
