package com.endoc.phtotapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class UserListActivity extends AppCompatActivity {
    private RecyclerView rcList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        rcList = (RecyclerView) findViewById(R.id.rc_list);
        rcList.setLayoutManager(new LinearLayoutManager(this));
    }
}
