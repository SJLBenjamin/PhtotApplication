package com.endoc.phtotapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.endoc.phtotapplication.activity.BaseActivity;
import com.endoc.phtotapplication.activity.DeviceImfSetActivity;
import com.endoc.phtotapplication.activity.UserDetailActivity;
import com.endoc.phtotapplication.activity.VerifyActivity;
import com.endoc.phtotapplication.utils.StatusBarUtil;
import com.endoc.phtotapplication.utils.StringUtils;
import com.hikvision.face.HikFRAAPI;

import org.litepal.LitePal;

import java.io.File;
import java.text.DecimalFormat;


public class MainActivity extends BaseActivity {
    private String TAG = "hikFRDemo";
    private CameraHandle mCamHandle = null;
    private SurfaceView mPrevSurface = null;
    private HikFRAAPI mFRAProc =  null;;
    private FRAListener mListener = null;
    //建模模型需要存放在sdcard根目录，当前位置在assets文件夹下
    private String mModelPath = "/sdcard/DFR_Model.bin";

    private Button mStartBT = null;
    private Button mCaptureBT = null;
    private Button mAddFaceBt = null;
    private Button mFaceLibShow = null;
    private ImageView mFaceImg = null;
    private ImageView mFaceCap = null;
    private ImageView mFaceAddShow = null;
    private TextView mFaceId = null;
    private TTSBroadcast mSpeak = null;
    private FaceRectView mFaceRect = null;

    private Context mContex;

    private Bitmap mCurFaceBm = null;
    private String mLastFaceId = null;

    private DeviceOrientationListion mDevOrListion = null;
    private int mDeviRotation = 0;
    //动态权限申请
    private static String[] PERMISSIONS_ALLOC = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.INTERNET
    };

    private byte[] IDmodelData = new byte[300];

    private static final int MSGCB_FC = 0;
    private static final int MSGCB_FR = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSGCB_FC) {
                String msgData = (String) msg.obj;
                String[] sArray = msgData.split("#");

                //show face ID
                mFaceRect.drawFaceInfo(sArray[2]);

                mFaceId.setPivotX(mFaceId.getWidth() / 2);
                mFaceId.setPivotY(mFaceId.getHeight() / 2);
                mFaceId.setRotation((mDeviRotation == 0 ? 0 : (360 - mDeviRotation)));
                DecimalFormat df = new DecimalFormat("###.##");
                mFaceId.setText(("人员：" + sArray[2] + "\n相似度：" + df.format(Float.parseFloat(sArray[0]) / 10) + "%"));
                //Log.d(TAG, "1---------->>smilar:" + sArray[0] + " lib:" + sArray[1] + " humId:" + sArray[2] + " path:" + sArray[3]);
                if(Float.parseFloat(sArray[0])>=90){//如果匹配度大于等于90
                    //那么执行上传操作
                    WorkUtils.getInstance().startUpLoad();
                }

                if (mLastFaceId != null && mLastFaceId.equals(sArray[2])) {
                    return;
                }

                mLastFaceId = sArray[2];

                mSpeak.playText(sArray[2]);

                File file = new File(sArray[3]);
                if (file.exists()) {
                    //Bitmap bm = BitmapFactory.decodeFile(sArray[3]);
                    RoundedBitmapDrawable rdBm = RoundedBitmapDrawableFactory.create(getResources(), sArray[3]);
                    rdBm.setCornerRadius(20);

                    mFaceImg.setPivotX(mFaceImg.getWidth() / 2);
                    mFaceImg.setPivotY(mFaceImg.getHeight() / 2);
                    mFaceImg.setRotation((mDeviRotation == 0 ? 0 : (360 - mDeviRotation)));
                    mFaceImg.setImageDrawable(rdBm);
                }
                // Log.d(TAG, "2---------->>");
            } else if (msg.what == MSGCB_FR) {
                String metaData = (String) msg.obj;
                // Log.d(TAG, "3---------->> Show current face rect: " + metaData);
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(mCurFaceBm, 0, 0, mCurFaceBm.getWidth(), mCurFaceBm.getHeight(), matrix, true);
                RoundedBitmapDrawable rdBm = RoundedBitmapDrawableFactory.create(getResources(), rotatedBitmap);
                rdBm.setCornerRadius(20);
                mFaceCap.setImageDrawable(rdBm);
                // Log.d(TAG, "4--------->>");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        mContex = this;

        allocPermission();
        //startActivity(new Intent(MainActivity.this,MyPhotoActivity.class));
        //startActivity(new Intent(MainActivity.this, UserDetailActivity.class));
        //startActivity(new Intent(MainActivity.this, VerifyActivity.class));
        //finish();

        //开启定时网络请求

        mFaceRect = findViewById(R.id.faceRect);
        mSpeak = new TTSBroadcast(MainActivity.this);

        mFaceImg = (ImageView) findViewById(R.id.humImg);
        mFaceId = (TextView) findViewById(R.id.humId);

        mFaceCap = (ImageView) findViewById(R.id.faceCap);


        /* 启动相机预览, 加载人脸数据库，启动算法*/
        mStartBT = (Button) findViewById(R.id.start);
        mStartBT.setOnClickListener(this);

        /* 获取一帧数据*/
        mCaptureBT = (Button) findViewById(R.id.capture);
        mCaptureBT.setOnClickListener(this);

        /* 加载人脸数据*/
        mAddFaceBt = (Button) findViewById(R.id.addface);
        mAddFaceBt.setOnClickListener(this);

        /* 显示人脸库*/
        mFaceLibShow = findViewById(R.id.show);
        mFaceLibShow.setOnClickListener(this);

        /* 显示添加人脸俘获数据*/
        mFaceAddShow = findViewById(R.id.AddFaceImg);

        /* 监听设备方向*/
        mDevOrListion = new DeviceOrientationListion(this, SensorManager.SENSOR_DELAY_NORMAL);
        if (mDevOrListion.canDetectOrientation()) {
            mDevOrListion.enable();
        } else {
            Log.e(TAG, "Can't DetectOrientation");
        }
        init();
    }

    @Override
    public void initData() {

    }

    @Override
    public void initView() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        startCamera();
    }

    public  void init(){
        mPrevSurface = (SurfaceView) findViewById(R.id.surfaceView);
        mFRAProc = new HikFRAAPI();
        mListener = new FRAListener();
        mFRAProc.setListener(mListener);

        mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.FRA_KEY_FACE_MANAGE, "1");
        //mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.HIK_FRA_KEY_NEED_FQFL, "1");
        //mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.HIK_FRA_KEY_LIVE, "0.6");
        //人脸缓冲区大小默认100000张；
        mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.FRA_POOL_KEY_CAP, "10000");
        //mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.FRA_KEY_MULTI_THREAD, "1");
        //mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.FRA_KEY_EXT_RGB_BUFF, "1");
        //初始化建模模型
        int init = mFRAProc.init(640, 480, HikFRAAPI.YUV_FORMAT.YUV_NV21, mModelPath);
        Log.i(TAG, "mFRAProc.init : "+init);
        //设置旋转角度，角度错误会导致识别失败
        mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.FRA_KEY_ROTATION, HikFRAAPI.ROTATION_ANGLE.ROTATION_270);
        File file = new File(StringUtils.FilePath);
        if(!file.exists()){
            file.mkdirs();
        }
        //加载人脸数据库到缓冲区，即可实现1VN。
        mFRAProc.loadFaceData(0, StringUtils.FilePath, "/sdcard/facelib/database.bin", "facetest");
        //初始化数据库
        LitePal.getDatabase();
        WorkUtils.getInstance().startTimer(mFRAProc);
    }

    public void startCamera(){
            new Thread(new Runnable() {
                @Override
                public void run() {
                /*    Log.d(TAG, "start Camera preview!");
                    mPrevSurface = (SurfaceView) findViewById(R.id.surfaceView);
                    mFRAProc = new HikFRAAPI();
                    mListener = new FRAListener();
                    mFRAProc.setListener(mListener);

                    mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.FRA_KEY_FACE_MANAGE, "1");
                    //mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.HIK_FRA_KEY_NEED_FQFL, "1");
                    //mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.HIK_FRA_KEY_LIVE, "0.6");
                    //人脸缓冲区大小默认100000张；
                    mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.FRA_POOL_KEY_CAP, "10000");
                    //mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.FRA_KEY_MULTI_THREAD, "1");
                    //mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.FRA_KEY_EXT_RGB_BUFF, "1");
                    //初始化建模模型
                    int init = mFRAProc.init(640, 480, HikFRAAPI.YUV_FORMAT.YUV_NV21, mModelPath);
                    Log.i(TAG, "mFRAProc.init : "+init);
                    //设置旋转角度，角度错误会导致识别失败
                    mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.FRA_KEY_ROTATION, HikFRAAPI.ROTATION_ANGLE.ROTATION_270);*/
                  //延迟后就不会出问题,所以说明必须surfaceView的holder View创建后才能去openCamera
                    mPrevSurface.getHolder().addCallback(new SurfaceHolder.Callback() {
                        @Override
                        public void surfaceCreated(SurfaceHolder holder) {
                            mCamHandle = new CameraHandle(mContex, mPrevSurface, mFRAProc, mFaceRect);

                            //启动人脸算法
                            mCamHandle.startProc();
                            mPrevSurface.getHolder().removeCallback(this);//移除当前回调
                        }

                        @Override
                        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                        }

                        @Override
                        public void surfaceDestroyed(SurfaceHolder holder) {
                          /*  mPrevSurface.getHolder().removeCallback(this);
                            mCamera.setPreviewCallback(null);
                            mCamera.stopPreview();
                            mCamera.release();
                            mCamera = null;*/
                        }
                    });


                    //开始请求
                    //new WorkUtils().startTimer(mFRAProc);

                }
            }).start();
            //mStartBT.setClickable(false);

    }




    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.start: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "start Camera preview!");
                        mPrevSurface = (SurfaceView) findViewById(R.id.surfaceView);
                        mFRAProc = new HikFRAAPI();
                        mListener = new FRAListener();
                        mFRAProc.setListener(mListener);

                        mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.FRA_KEY_FACE_MANAGE, "1");
                        //mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.HIK_FRA_KEY_NEED_FQFL, "1");
                        //mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.HIK_FRA_KEY_LIVE, "0.6");
                        //人脸缓冲区大小默认100000张；
                        mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.FRA_POOL_KEY_CAP, "10000");
                        //mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.FRA_KEY_MULTI_THREAD, "1");
                        //mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.FRA_KEY_EXT_RGB_BUFF, "1");
                        //初始化建模模型
                        int init = mFRAProc.init(640, 480, HikFRAAPI.YUV_FORMAT.YUV_NV21, mModelPath);
                        Log.i(TAG, "mFRAProc.init : "+init);
                        //设置旋转角度，角度错误会导致识别失败
                        mFRAProc.setParam(HikFRAAPI.FRA_SET_KEY.FRA_KEY_ROTATION, HikFRAAPI.ROTATION_ANGLE.ROTATION_270);
                        mCamHandle = new CameraHandle(mContex, mPrevSurface, mFRAProc, mFaceRect);
                        File file = new File(StringUtils.FilePath);
                        if(!file.exists()){
                            file.mkdirs();
                        }
                        //加载人脸数据库到缓冲区，即可实现1VN。
                        mFRAProc.loadFaceData(0, StringUtils.FilePath, "/sdcard/facelib/database.bin", "facetest");

                        //启动人脸算法
                        mCamHandle.startProc();
                        //开始请求
                       WorkUtils.getInstance().startTimer(mFRAProc);
                    }
                }).start();
                //mStartBT.setClickable(false);
                break;
            }
            case R.id.capture:
                Intent intent1 = new Intent(MainActivity.this, MyPhotoActivity.class);
                startActivity(intent1);
/*                if (mCamHandle != null) {
                    mCamHandle.switchCamera();
                    break;
                }*/
//                int faceDelModel = mFRAProc.faceDelModel("facetest", "刘德华", "刘德华", "/sdcard/facelib/database.bin");
//                Log.e(TAG, "faceDelModel"+faceDelModel);
                break;
            case R.id.addface:
                if (mCamHandle != null) {
                    Log.d(TAG, "Add face!");
                    mCamHandle.capData(mFaceAddShow);
                    final EditText inputServer = new EditText(MainActivity.this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("输入人员名称");
                    builder.setIcon(android.R.drawable.ic_dialog_info);
                    builder.setView(inputServer);
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mFaceAddShow.setImageBitmap(null);
                        }
                    });
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String humId = inputServer.getText().toString();
                            if (humId.isEmpty()) {
                                Log.e(TAG, "input is null");
                                Toast.makeText(MainActivity.this, "输入人员名称错误！", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d(TAG, "humId = " + humId);
                                String facePath = "/sdcard/facelib/" + humId + ".jpg";
                                mCamHandle.addFaceModel("facetest", humId, humId, facePath, "/sdcard/facelib/database.bin");
                            }
                            mFaceAddShow.setImageBitmap(null);
                        }
                    });
                    builder.show();
                    break;
                }
            case R.id.show: {
                Log.d(TAG, "Show face lib................");
                Intent intent = new Intent(MainActivity.this, FaceLibShow.class);
                startActivity(intent);
                //mFRAProc = new HikFRAAPI();
                //mFRAProc.faceTest();
                break;
            }
            default:
                break;
        }
    }

    class FRAListener implements HikFRAAPI.ListenerCallBack {

        @Override
        public void MsgNotify(int msgType, String msgData, int len) {
            Log.d(TAG, "MsgType: " + msgType + " msgData: " + msgData + " len: " + len);
            if (msgType == HikFRAAPI.FRA_RESULT_INDEX.FRA_RESULT_FC) {
                Message msg = new Message();
                msg.what = MSGCB_FC;
                msg.obj = msgData;
                handler.sendMessage(msg);
            }else if (msgType == HikFRAAPI.FRA_RESULT_INDEX.FRA_RESULT_LS){
                //区间0-1
                Log.d(TAG, "活体检测可信度"+msgData);
            }
        }

        @Override
        public void DataCallBack(int dataType, byte[] data, String metaData, int len) {
            Log.d(TAG, "dataType: " + dataType + " dataLen: " + data.length + " metaData: " + metaData + " len: " + len);
            if (dataType == HikFRAAPI.FRA_RESULT_INDEX.FRA_RESULT_FR) {
                String[] sArray = metaData.split("#");
                mCurFaceBm = mCamHandle.nv21ToBitmap(data, Integer.parseInt(sArray[0]), Integer.parseInt(sArray[1]));
                Message msg = new Message();
                msg.what = MSGCB_FR;
                msg.obj = metaData;
                handler.sendMessage(msg);
            }
        }
    }

    private void allocPermission() {
        //循环申请字符串数组里面的权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_ALLOC, 1);
            }
        }
    }

    private class DeviceOrientationListion extends OrientationEventListener {

        public DeviceOrientationListion(Context context) {
            super(context);
        }

        public DeviceOrientationListion(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return;
            }

            int newOrientation = ((orientation + 45) / 90 * 90) % 360;
            // Log.d(TAG, "++++++++++++++++++++: onOrientationChanged: " + orientation + " new: " + newOrientation);
            if (mDeviRotation != newOrientation) {
                //   Log.d(TAG, "Device Rotation Change :" + mDeviRotation + " ->" + newOrientation);
                mDeviRotation = newOrientation;
                if (mCamHandle != null) {
                    if (mDeviRotation == 0) {
                        mCamHandle.setRotation(270);
                    } else if (mDeviRotation == 270) {
                        mCamHandle.setRotation(0);
                    }
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFRAProc!=null){
            mFRAProc.deInit();}
    }

}
