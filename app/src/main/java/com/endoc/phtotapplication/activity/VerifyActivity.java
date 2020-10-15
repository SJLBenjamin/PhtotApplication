package com.endoc.phtotapplication.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.endoc.phtotapplication.R;
import com.endoc.phtotapplication.utils.StatusBarUtil;

public class VerifyActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
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

        //initView();
    }

    @Override
    public void initData() {

    }




    @Override
    public void initView() {


    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("1234","verifySuccess");
                        verifySuccess(R.layout.recycyle_user_imfor_layout,findViewById(R.id.cl_root));
                    }
                });

            }
        }).start();

    }

    public  void verifySuccess(int resource,View parent){
        PopupWindow popupBigPhoto = null;

        //PopupWindow的布局
        View view = getLayoutInflater().inflate(resource, null);


        if (popupBigPhoto == null) {
            popupBigPhoto = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupBigPhoto.setOutsideTouchable(true);
            popupBigPhoto.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {

                }
            });
        }
        if (popupBigPhoto.isShowing()) {
            popupBigPhoto.dismiss();
        } else {
            //findViewById(R.id.cl_root)  popupWindows需要显示的控件上
            popupBigPhoto.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        }

    }

    public void verifyFail(){

    }
}
