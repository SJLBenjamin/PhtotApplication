package com.endoc.phtotapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.endoc.phtotapplication.network.api.FaceInterface;
import com.endoc.phtotapplication.network.bean.ChangeResponseBean;
import com.endoc.phtotapplication.network.bean.PersonResponseBean;
import com.endoc.phtotapplication.network.bean.RequestBean;
import com.endoc.phtotapplication.network.bean.IdList;
import com.endoc.phtotapplication.network.retrofit.RetrofitClient;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_photo);

       /* SurfaceViewTemplate mPreview = new SurfaceViewTemplate(this);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        FaceInterface faceInterface = new RetrofitClient().change();
        RequestBean requestBean = new RequestBean("192.168.100.200", "Change");
        Call<ChangeResponseBean> change = faceInterface.change(requestBean);
        change.enqueue(new Callback<ChangeResponseBean>() {
         @Override
         public void onResponse(Call<ChangeResponseBean> call, Response<ChangeResponseBean> response) {
             Log.d("MyPhotoActivity","onResponse=="+response.body().toString());
             List<IdList> idList = response.body().getIdList();
             if(idList.size()>0){//如果list有数据

                 Log.d("MyPhotoActivity","getIdList>0");
                 RequestBean requestPersonBean = new RequestBean("192.168.100.200", "Person");
                    for (int i=0;i<idList.size();i++){
                        requestPersonBean.setPersonId(idList.get(i).getId());
                        Call<PersonResponseBean> person = faceInterface.person(requestPersonBean);
                        int finalI = i;
                        person.enqueue(new Callback<PersonResponseBean>() {
                            @Override
                            public void onResponse(Call<PersonResponseBean> call, Response<PersonResponseBean> response) {
                                Log.d("MyPhotoActivity","person response=="+response.body().getPerson());
                                //Environment.getExternalStorageDirectory().getPath()
                                base64ToFile(response.body().getPerson().getPicBase64(), "/sdcard/"+ finalI +".Jpeg");
                                //byte[] decode = Base64.decode(response.body().getPerson().getPicBase64().getBytes(), Base64.DEFAULT);
                            }

                            @Override
                            public void onFailure(Call<PersonResponseBean> call, Throwable t) {
                                Log.d("MyPhotoActivity","person onFailure=="+t.getMessage());
                            }
                        });
                    }
             }
         }

         @Override
         public void onFailure(Call<ChangeResponseBean> call, Throwable t) {
             Log.d("MyPhotoActivity","onFailure=="+t.getMessage());
         }
     });
    }


    public  boolean base64ToFile(String base64Str,String path) {
        byte[] data = Base64.decode(base64Str,Base64.NO_WRAP);
        for (int i = 0; i < data.length; i++) {
            if(data[i] < 0){
                //调整异常数据
                data[i] += 256;
            }
        }
        OutputStream os = null;
        try {
            os = new FileOutputStream(path);
            os.write(data);
            os.flush();
            os.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }
}
