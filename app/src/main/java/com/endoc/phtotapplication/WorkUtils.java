package com.endoc.phtotapplication;


import android.content.Context;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.endoc.phtotapplication.litepal.Person;
import com.endoc.phtotapplication.network.api.FaceInterface;
import com.endoc.phtotapplication.network.bean.ChangeResponseBean;
import com.endoc.phtotapplication.network.bean.IdList;
import com.endoc.phtotapplication.network.bean.PersonResponseBean;
import com.endoc.phtotapplication.network.bean.RequestBean;
import com.endoc.phtotapplication.network.bean.ResultBean;
import com.endoc.phtotapplication.network.bean.VerifyRequestBean;
import com.endoc.phtotapplication.network.retrofit.RetrofitClient;
import com.endoc.phtotapplication.utils.StringUtils;
import com.hikvision.face.HikFRAAPI;

import org.litepal.LitePal;

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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkUtils {
   static WorkUtils workUtils =null;
    public static WorkUtils getInstance(){
        if(workUtils==null){
            synchronized (WorkUtils.class){
                if(workUtils==null){
                    workUtils =new WorkUtils();
                }
            }
        }
        return workUtils;
    }
    FaceInterface faceInterface = new RetrofitClient().faceInterface();

    /**
     * 上传图片
     * @param person personID
     * @param device 设备id
     * @param path   上传图片的路径
     */
    public void startUpLoad(String person,String device,String path){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //Call<ResultBean> verify = faceInterface.verify(new VerifyRequestBean("216", "192.168.100.200", simpleDateFormat.format(new Date()), imageToBase64("/sdcard/0.Jpeg")));
        Call<ResultBean> verify = faceInterface.verify(new VerifyRequestBean(person, device, simpleDateFormat.format(new Date()), imageToBase64(path)));
        verify.enqueue(new Callback<ResultBean>() {
            @Override
            public void onResponse(Call<ResultBean> call, Response<ResultBean> response) {
                Log.d("MyPhotoActivity","verify onResponse=="+response.body().getCode());
            }

            @Override
            public void onFailure(Call<ResultBean> call, Throwable t) {
                Log.d("MyPhotoActivity","verify t=="+t.getMessage());
            }
        });
    }


    Timer timer;
    class MyTimerTask extends  TimerTask{
        @Override
        public void run() {

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
                                //本地删除
                                List<Person> personList = LitePal.where("personID = ?", idList.get(i).getId()).find(Person.class);
                                for(int j=0;j<personList.size();j++){
                                    File file = new File(personList.get(j).getRepic());
                                    file.delete();
                                }


                                //数据库删除
                                LitePal.deleteAll(Person.class,"personID = ?",idList.get(i).getId());

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

                            person.enqueue(new Callback<PersonResponseBean>() {
                                @Override
                                public void onResponse(Call<PersonResponseBean> call, Response<PersonResponseBean> response) {
                                    Log.d("MyPhotoActivity","person response=="+response.body().getCode());
                                    //Environment.getExternalStorageDirectory().getPath()
                                    //将图片储存到本地
                                    base64ToFile(response.body().getPerson().getPicBase64(), StringUtils.FilePath+ response.body().getPerson().getId() +".jpg");

                                    Person litpalPerson = new Person();
                                    litpalPerson.setRetime(response.body().getPerson().getVdatetime());
                                    litpalPerson.setName(response.body().getPerson().getName());
                                    litpalPerson.setPersonID(response.body().getPerson().getId());
                                    litpalPerson.setMembertype(response.body().getPerson().getMembertype());

                                    //保存路径
                                    litpalPerson.setRepic(StringUtils.FilePath+ response.body().getPerson().getId() +".jpg");
                                    //如果有就更新,没有就保存
                                    litpalPerson.saveOrUpdate("personID = ?",response.body().getPerson().getId());

                                    mHikFRAAPI.faceAddModel();


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


    }
   /* TimerTask task = new TimerTask() {
        @Override
        public void run() {

            //图片上传
  *//*      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
        });*//*
        *//*String base64 = imageToBase64("/sdcard/0.Jpeg");
        base64ToFile(base64,"/sdcard/test0.Jpeg");*//*


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
                                //本地删除
                                List<Person> personList = LitePal.where("personID = ?", idList.get(i).getId()).find(Person.class);
                                for(int j=0;j<personList.size();j++){
                                    File file = new File(personList.get(j).getRepic());
                                    file.delete();
                                }


                                //数据库删除
                                LitePal.deleteAll(Person.class,"personID = ?",idList.get(i).getId());

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

                            person.enqueue(new Callback<PersonResponseBean>() {
                                @Override
                                public void onResponse(Call<PersonResponseBean> call, Response<PersonResponseBean> response) {
                                    Log.d("MyPhotoActivity","person response=="+response.body().getCode());
                                    //Environment.getExternalStorageDirectory().getPath()
                                    //将图片储存到本地
                                    base64ToFile(response.body().getPerson().getPicBase64(), StringUtils.FilePath+ response.body().getPerson().getId() +".jpg");

                                    Person litpalPerson = new Person();
                                    litpalPerson.setRetime(response.body().getPerson().getVdatetime());
                                    litpalPerson.setName(response.body().getPerson().getName());
                                    litpalPerson.setPersonID(response.body().getPerson().getId());
                                    litpalPerson.setMembertype(response.body().getPerson().getMembertype());

                                    //保存路径
                                    litpalPerson.setRepic(StringUtils.FilePath+ response.body().getPerson().getId() +".jpg");
                                    //如果有就更新,没有就保存
                                    litpalPerson.saveOrUpdate("personID = ?",response.body().getPerson().getId());


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
    };*/

    HikFRAAPI mHikFRAAPI;
    public void startTimer(HikFRAAPI hikFRAAPI){
        mHikFRAAPI =hikFRAAPI;
        timer= new Timer();
        timer.schedule(new MyTimerTask(),0,60000);
    }

    public void stopTimer(){
        timer.cancel();
        timer=null;
        mHikFRAAPI =null;
    }




   /* String TAG ="WorkUtils";
    FaceInterface faceInterface = new RetrofitClient().faceInterface();
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    public void test() {
        mCompositeDisposable.add(Observable.interval(0, 15, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getObserver()));
    }

    private DisposableObserver getObserver() {
        DisposableObserver disposableObserver = new DisposableObserver<Object>() {

            //周期性执行的任务
            @Override
            public void onNext(Object o) {
                RequestBean requestBean = new RequestBean("192.168.100.200", "Change");
                Call<ChangeResponseBean> change = faceInterface.change(requestBean);
                change.enqueue(new Callback<ChangeResponseBean>() {
                    @Override
                    public void onResponse(Call<ChangeResponseBean> call, Response<ChangeResponseBean> response) {
                        Log.d(TAG,"onResponse=="+response.body().toString());
                        List<IdList> idList = response.body().getIdList();
                        if(idList.size()>0){//如果list有数据,那么进行详细数据请求操作

                            Log.d(TAG,"getIdList>0");
                            RequestBean requestPersonBean = new RequestBean("192.168.100.200", "Person");
                            for (int i=0;i<idList.size();i++){
                                requestPersonBean.setPersonId(idList.get(i).getId());
                                Call<PersonResponseBean> person = faceInterface.person(requestPersonBean);
                                int finalI = i;
                                person.enqueue(new Callback<PersonResponseBean>() {
                                    @Override
                                    public void onResponse(Call<PersonResponseBean> call, Response<PersonResponseBean> response) {
                                        Log.d(TAG,"person response=="+response.body().getPerson().getId());
                                        //Environment.getExternalStorageDirectory().getPath()
                                        //将图片储存到本地
                                        base64ToFile(response.body().getPerson().getPicBase64(), "/sdcard/"+ finalI +".Jpeg");

                                        //本地数据存储成功之后,执行返回操作
                                        RequestBean addReturnBean = new RequestBean("192.168.100.200", "Return");
                                        addReturnBean.setReturnid(new RequestBean.ReturnidBean(response.body().getPerson().getId(),"Add"));
                                        Call<ResultBean> addReturnBeanCall = faceInterface.addReturn(addReturnBean);
                                        addReturnBeanCall.enqueue(new Callback<ResultBean>() {
                                            @Override
                                            public void onResponse(Call<ResultBean> call, Response<ResultBean> response) {
                                                Log.d(TAG,"addReturnBeanCall response=="+response.body().getCode());
                                            }

                                            @Override
                                            public void onFailure(Call<ResultBean> call, Throwable t) {
                                                Log.d(TAG,"addReturnBeanCall t=="+t.getMessage());
                                            }
                                        });



                                        //byte[] decode = Base64.decode(response.body().getPerson().getPicBase64().getBytes(), Base64.DEFAULT);
                                    }

                                    @Override
                                    public void onFailure(Call<PersonResponseBean> call, Throwable t) {
                                        Log.d(TAG,"person onFailure=="+t.getMessage());
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ChangeResponseBean> call, Throwable t) {
                        Log.d(TAG,"onFailure=="+t.getMessage());
                    }
                });
            }

            @Override
            public void onComplete() {
                //Log.d(id + TAG, "onComplete");
            }

            @Override
            public void onError(Throwable e) {
                //Log.e(id + TAG, e.toString(), e);
            }
        };

        return disposableObserver;
    }*/





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













/*

    //设置触发条件,因为周期任务最少15分钟执行一次,所以一分钟执行一次就好出问题
    Constraints constraints = new Constraints.Builder()
            .setRequiresCharging(false)//设置在设备不在充电情况下也能执行
            .setRequiredNetworkType(NetworkType.CONNECTED)//设置网络连接的时候才执行
            .setRequiresBatteryNotLow(true)//且电池电量充足的状态
            .build();

    public void startChange(Context context){
        PeriodicWorkRequest build = new PeriodicWorkRequest.Builder(ChangeWork.class, 1, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .setInitialDelay(10, TimeUnit.SECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, OneTimeWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .addTag("test")
                .build();
        WorkManager.getInstance(context).enqueue(build);
    }

   public class ChangeWork extends Worker {

        public ChangeWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {
            //Log.d(TAG,"MyWork   doWork");

            return Result.success();
        }

       @Override
       public void onStopped() {
           super.onStopped();
       }
   }

*/



}
