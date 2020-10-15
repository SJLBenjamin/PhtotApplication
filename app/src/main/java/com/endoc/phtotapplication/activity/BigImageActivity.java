package com.endoc.phtotapplication.activity;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;


import com.endoc.phtotapplication.R;

import com.endoc.phtotapplication.utils.StatusBarUtil;
import com.endoc.phtotapplication.utils.StringUtils;
import com.squareup.picasso.Picasso;


import java.io.File;



//查看大图界面
public class BigImageActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_image);
        StatusBarUtil.setRootViewFitsSystemWindows(this, false);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        initData();
    }

    @Override
    public void initData() {
        findViewById(R.id.iv_back).setOnClickListener(this);
    }

    @Override
    public void initView() {

    }

    @Override
    protected void onResume() {
        super.onResume();

        String stringExtra = getIntent().getStringExtra(StringUtils.bigImagePath);
        Picasso.get().load(new File(stringExtra)).into( (ImageView) findViewById(R.id.iv_big));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
            default:
                break;
        }
    }
}
