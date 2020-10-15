package com.endoc.phtotapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;

import com.endoc.phtotapplication.network.api.FaceInterface;
import com.endoc.phtotapplication.network.bean.ChangeResponseBean;
import com.endoc.phtotapplication.network.bean.PersonResponseBean;
import com.endoc.phtotapplication.network.bean.RequestBean;
import com.endoc.phtotapplication.network.bean.IdList;
import com.endoc.phtotapplication.network.bean.ResultBean;
import com.endoc.phtotapplication.network.bean.VerifyRequestBean;
import com.endoc.phtotapplication.network.retrofit.RetrofitClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPhotoActivity extends AppCompatActivity {
    //public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000; //需要自己定义标志
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);//关键代码
        setContentView(R.layout.activity_my_photo);

       /* SurfaceViewTemplate mPreview = new SurfaceViewTemplate(this);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);*/
    }

   /* @Override
    public boolean onKeyDown( int keyCode, KeyEvent event) {
        if (keyCode == event. KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        //new WorkUtils().startTimer();
        if(true){
            return;
        }
        FaceInterface faceInterface = new RetrofitClient().faceInterface();

        //图片上传
  /*      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Call<ResultBean> verify = faceInterface.verify(new VerifyRequestBean("216", "192.168.100.200", simpleDateFormat.format(new Date()), imageToBase64("/sdcard/0.Jpeg")));
        verify.enqueue(new Callback<ResultBean>() {
            @Override
            public void onResponse(Call<ResultBean> call, Response<ResultBean> response) {
                Log.d("MyPhotoActivity","verify onResponse=="+response.body().getCode());
            }

            @Override
            public void onFailure(Call<ResultBean> call, Throwable t) {
                Log.d("MyPhotoActivity","verify t=="+t.getMessage());
            }
        });*/
        /*String base64 = imageToBase64("/sdcard/0.Jpeg");
        base64ToFile(base64,"/sdcard/test0.Jpeg");*/


        RequestBean requestBean = new RequestBean("192.168.100.200", "Change");
        Call<ChangeResponseBean> change = faceInterface.change(requestBean);
        change.enqueue(new Callback<ChangeResponseBean>() {
         @Override
         public void onResponse(Call<ChangeResponseBean> call, Response<ChangeResponseBean> response) {
             Log.d("MyPhotoActivity","change onResponse=="+response.body().toString());
             List<IdList> idList = response.body().getIdList();
             if(idList.size()>0){//如果list有数据,那么进行详细数据请求操作

                 RequestBean requestPersonBean = new RequestBean("192.168.100.200", "Person");
                    for (int i=0;i<idList.size();i++){

                        Log.d("MyPhotoActivity","type=="+idList.get(i).getChange());
                        Log.d("MyPhotoActivity","idlist==="+idList.get(i).getId());
                        //设置请求的类型
                        requestPersonBean.setReturnid(new RequestBean.ReturnidBean(idList.get(i).getId(),idList.get(i).getChange()));

                        if(idList.get(i).getChange().equals("Delete")){//如果是删除操作
                            //1.执行删除
                            //2.告诉服务器删除成功
                            requestPersonBean.setReqType("Return");//直接返回,所以修改为Return
                            Call<ResultBean> addReturnBeanCall = faceInterface.addReturn(requestPersonBean);
                            addReturnBeanCall.enqueue(new Callback<ResultBean>() {
                                @Override
                                public void onResponse(Call<ResultBean> call, Response<ResultBean> response) {
                                    Log.d("MyPhotoActivity","deleteReturnBeanCall response=="+response.body().getCode());
                                }

                                @Override
                                public void onFailure(Call<ResultBean> call, Throwable t) {
                                    Log.d("MyPhotoActivity","deleteReturnBeanCall t=="+t.getMessage());
                                }
                            });

                            //3.然后终止此次循环
                            continue;
                        }


                        //如果是Add或者是modify,那么就需要PersonId参数,即ReqType为Person的时候就需要这个id
                        requestPersonBean.setPersonId(idList.get(i).getId());
                        Call<PersonResponseBean> person = faceInterface.person(requestPersonBean);
                        int finalI = i;
                        person.enqueue(new Callback<PersonResponseBean>() {
                            @Override
                            public void onResponse(Call<PersonResponseBean> call, Response<PersonResponseBean> response) {
                                Log.d("MyPhotoActivity","person response=="+response.body().getCode());
                                //Environment.getExternalStorageDirectory().getPath()
                                //将图片储存到本地
                                base64ToFile(response.body().getPerson().getPicBase64(), "/sdcard/"+ finalI +".jpg");

                                //本地数据存储成功之后,执行返回操作
                                RequestBean addReturnBean = new RequestBean("192.168.100.200", "Return");
                                addReturnBean.setReturnid(new RequestBean.ReturnidBean(response.body().getPerson().getId(),"Add"));
                                Call<ResultBean> addReturnBeanCall = faceInterface.addReturn(addReturnBean);
                                addReturnBeanCall.enqueue(new Callback<ResultBean>() {
                                    @Override
                                    public void onResponse(Call<ResultBean> call, Response<ResultBean> response) {
                                        Log.d("MyPhotoActivity","addReturnBeanCall response=="+response.body().getCode());
                                    }

                                    @Override
                                    public void onFailure(Call<ResultBean> call, Throwable t) {
                                        Log.d("MyPhotoActivity","addReturnBeanCall t=="+t.getMessage());
                                    }
                                });



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


    /**
     * @param base64Str base64字符串
     * @param path  储存的路径
     * @return
     */
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


    /**
     * @param path 图片路径
     * @return  base64编码
     */
    public  String imageToBase64(String path){
        if(TextUtils.isEmpty(path)){
            return null;
        }
        InputStream is = null;
        byte[] data = null;
        String result = null;
        try{
            is = new FileInputStream(path);
            //创建一个字符流大小的数组。
            data = new byte[is.available()];
            //写入数组
            is.read(data);
            //用默认的编码格式进行编码
            result = Base64.encodeToString(data,Base64.NO_CLOSE);
         /*   try {
                //writeOcrStrtoFile(result,Environment.getExternalStorageDirectory() + "/" + StringData.casePath ,"baseImage.txt");
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            //生成本地文件测试
            //base64ToFile(result,"/sdcard/test0.jpg");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(null !=is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 保存文件到本地
     */
    public  void writeOcrStrtoFile(String result, String outPath, String outFileName) throws Exception {
        File dir = new File(outPath);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        File txt = new File(outPath + "/" + outFileName);
        if (!txt.exists()) {
            txt.createNewFile();
        }
        byte bytes[] = new byte[512];
        bytes = result.getBytes("US-ASCII");
        int b = bytes.length; // 是字节的长度，不是字符串的长度
        FileOutputStream fos = new FileOutputStream(txt);
//		fos.write(bytes, 0, b);
        fos.write(bytes);
        fos.flush();
        fos.close();
    }


}
