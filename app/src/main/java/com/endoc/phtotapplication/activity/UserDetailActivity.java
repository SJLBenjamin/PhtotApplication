package com.endoc.phtotapplication.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.endoc.phtotapplication.R;
import com.endoc.phtotapplication.litepal.Person;
import com.endoc.phtotapplication.utils.StatusBarUtil;
import com.endoc.phtotapplication.utils.StringUtils;

import org.litepal.LitePal;

import java.util.List;

public class UserDetailActivity extends BaseActivity {
    String stringExtra;
    List<Person> personList;
    private Person mPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        StatusBarUtil.setRootViewFitsSystemWindows(this, false);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this, 0x55000000);
            //StatusBarUtil.setStatusBarColor(this, Color.parseColor("#FFFFFF"));
        }
         stringExtra = getIntent().getStringExtra(StringUtils.PersonID);
        //人员详情
        personList = LitePal.where("personID = ?", stringExtra).limit(1).find(Person.class);
        mPerson = personList.get(0);
    }

    @Override
    public void initData() {

    }

    private TextView etDeviceGh;
    private TextView etSqXm;
    private TextView etZlMm;
    private TextView etSqRl;
    private TextView etTbYh;
    @Override
    public void initView() {
        etDeviceGh = (TextView) findViewById(R.id.et_device_gh);
        etSqXm = (TextView) findViewById(R.id.et_sq_xm);
        etZlMm = (TextView) findViewById(R.id.et_zl_mm);
        etSqRl = (TextView) findViewById(R.id.et_sq_rl);
        etTbYh = (TextView) findViewById(R.id.et_tb_yh);
        etDeviceGh.setText(mPerson.getPersonID());
        etSqXm.setText(mPerson.getName());
        //密码未知
        //etZlMm.setText();
        //人脸
        etSqRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDetailActivity.this, BigImageActivity.class);
                intent.putExtra(StringUtils.bigImagePath,mPerson.getRepic());
                startActivity(intent);
            }
        });
        etTbYh.setText(mPerson.getMembertype().equals("0")?"内部人员":"临时访客");


    }

    @Override
    public void onClick(View v) {

    }
}
