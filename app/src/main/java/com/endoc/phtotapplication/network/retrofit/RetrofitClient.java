package com.endoc.phtotapplication.network.retrofit;



import com.endoc.phtotapplication.network.api.FaceInterface;

import com.google.gson.GsonBuilder;




import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    String FaceApiURL = "http://58.33.107.234:90/pfaceweb/";
    //String Base_url = "http://58.33.107.234:90/pfaceweb/";
    Retrofit retrofit;

    //上传图片
    public Retrofit createRetrofit() {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    retrofit = new Retrofit.Builder().baseUrl(FaceApiURL)
                            .addConverterFactory(GsonConverterFactory.create())//将数据转换为Bean类的工具
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                            //.addCallAdapterFactory(RxJavaCallAdapterFactory.create())//支持RxJava
                            .build();
                }
            }
        }
        return retrofit;
    }


    /**
     * 同步数据
     *
     * @return
     */
    public FaceInterface change() {
        return createRetrofit().create(FaceInterface.class);
    }


   /* private static OkHttpClient getOkHttpClient() {
        //日志显示级别
        HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.BODY;
        //新建log拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.e("tagHttp", "OkHttp====Message:" + message);
            }
        });
        loggingInterceptor.setLevel(level);
        //定制OkHttp
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(120, TimeUnit.SECONDS).
                        readTimeout(120, TimeUnit.SECONDS).
                        writeTimeout(120, TimeUnit.SECONDS).build();
        return client;
    }

    public static Retrofit getRetrofit(String Base_url) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Base_url)//基础URL 建议以 / 结尾
                .addConverterFactory(GsonConverterFactory.create(gson))//设置 Json 转换器
                .client(getOkHttpClient())
                .build();
        return retrofit;
    }*/

}
