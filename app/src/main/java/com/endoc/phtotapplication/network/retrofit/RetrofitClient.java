package com.endoc.phtotapplication.network.retrofit;

import com.endoc.phtotapplication.network.bean.SynchronizationBean;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    String FaceApiURL = "https://www.wanandroid.com/";
    Retrofit retrofit;
    //上传图片
    public  Retrofit createRetrofit(){
        synchronized (this){
            if (retrofit!=null) {
                synchronized (this){
                    retrofit = new Retrofit.Builder().baseUrl(FaceApiURL)
                            .addConverterFactory(GsonConverterFactory.create())//将数据转换为Bean类的工具
                            .build();
                }
            }
        }
       return retrofit;
    };

    /**
     * 同步数据
     */
    public void synchronization(){
         createRetrofit().create(SynchronizationBean.class);
    }

}
