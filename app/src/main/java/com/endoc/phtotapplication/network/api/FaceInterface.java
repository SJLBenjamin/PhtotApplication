package com.endoc.phtotapplication.network.api;


import com.endoc.phtotapplication.network.bean.ChangeResponseBean;
import com.endoc.phtotapplication.network.bean.RequestBean;
import com.endoc.phtotapplication.network.bean.PersonResponseBean;


import retrofit2.Call;
import retrofit2.http.Body;

import retrofit2.http.POST;


public interface FaceInterface {

    @POST("GetFaceByAndroidcs.aspx")
    Call<ChangeResponseBean> change(@Body RequestBean jsonObject);

    @POST("GetFaceByAndroidcs.aspx")
    Call<PersonResponseBean> person(@Body RequestBean jsonObject);
}
