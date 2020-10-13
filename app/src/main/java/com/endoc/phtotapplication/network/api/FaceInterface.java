package com.endoc.phtotapplication.network.api;


import com.endoc.phtotapplication.network.bean.ChangeResponseBean;
import com.endoc.phtotapplication.network.bean.RequestBean;
import com.endoc.phtotapplication.network.bean.PersonResponseBean;
import com.endoc.phtotapplication.network.bean.ResultBean;
import com.endoc.phtotapplication.network.bean.VerifyRequestBean;


import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;

import retrofit2.http.POST;


public interface FaceInterface {
    /*
    * 此处不用泛型了,到时候容易混
    * */

    @POST("GetFaceByAndroidcs.aspx")
    Call<ChangeResponseBean> change(@Body RequestBean jsonObject);

    @POST("GetFaceByAndroidcs.aspx")
    Call<PersonResponseBean> person(@Body RequestBean jsonObject);

    @POST("GetFaceByAndroidcs.aspx")
    Call<ResultBean> addReturn(@Body RequestBean jsonObject);

    @POST("RecFaceRecordByAndroid.aspx")
    Call<ResultBean> verify(@Body VerifyRequestBean jsonObject);
}
