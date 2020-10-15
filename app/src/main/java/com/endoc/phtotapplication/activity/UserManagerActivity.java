package com.endoc.phtotapplication.activity;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.endoc.phtotapplication.R;
import com.endoc.phtotapplication.litepal.Person;
import com.endoc.phtotapplication.utils.StatusBarUtil;
import com.endoc.phtotapplication.utils.StringUtils;
import com.squareup.picasso.Picasso;

import org.litepal.LitePal;


import java.io.File;
import java.util.List;

public class UserManagerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manager);
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
    }

    @Override
    public void initData() {

    }


    private EditText etSearch;
    private RecyclerView rcUserList;
    List<Person> personList;//人员列表
    @Override
    public void initView() {

        etSearch = (EditText) findViewById(R.id.et_search);
        rcUserList = (RecyclerView) findViewById(R.id.rc_user_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rcUserList.setLayoutManager(linearLayoutManager);
        personList = LitePal.findAll(Person.class);
        UserRecycleAdapter userRecycleAdapter = new UserRecycleAdapter();
        rcUserList.setAdapter(userRecycleAdapter);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //执行数据库查询操作
                 personList = LitePal.where("name = ? or personID = ?", s.toString(), s.toString()).find(Person.class);
                userRecycleAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View v) {

    }

    class UserRecycleAdapter extends  RecyclerView.Adapter<UserRecycleAdapter.AdapterHolder>{

        class  AdapterHolder extends RecyclerView.ViewHolder{
             TextView tvName;
             TextView tvId;
             ImageView imPht;
             ImageView imDump;
            public AdapterHolder(@NonNull View itemView) {
                super(itemView);
                tvName = (TextView) itemView.findViewById(R.id.tv_name);
                tvId = (TextView) itemView.findViewById(R.id.tv_id);
                imPht = (ImageView) itemView.findViewById(R.id.im_pht);
                imDump = (ImageView) itemView.findViewById(R.id.im_dump);
            }
        }


        @NonNull
        @Override
        public AdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycyle_user_imfor_layout, parent);
            return new AdapterHolder(inflate);
        }

        @Override
        public void onBindViewHolder(@NonNull AdapterHolder holder, int position) {
            holder.tvName.setText(personList.get(position).getName());
            holder.tvId.setText(personList.get(position).getPersonID());
            //跳转到人员详情页面
            holder.imDump.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(UserManagerActivity.this, UserDetailActivity.class);
                    intent.putExtra(StringUtils.PersonID,personList.get(position).getPersonID());
                    startActivity(intent);
                }
            });
            //设置头像
            File file = new File(personList.get(position).getRepic());
            Picasso.get()
                    .load(file)
                    .into(holder.imPht);
        }

        @Override
        public int getItemCount() {
            return personList.size();
        }
    }
}
