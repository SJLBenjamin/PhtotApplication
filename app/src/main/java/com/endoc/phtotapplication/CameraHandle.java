package com.endoc.phtotapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.Toast;

import com.hikvision.face.HikFRAAPI;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;

/*
 * 描述：
 * 创建：huangmuquan
 * 日志：2018/12/14
 */public class CameraHandle {

    private static final String TAG = "hikFRDemo";

    private SurfaceView mPreSurface = null;
    private SurfaceHolder mPreHolder = null;
    private ImageView mCapShow = null;

    private int mCamerId = 0;
    private Camera mCamera = null;

    private int mWidth = 640;
    private int mHeight = 480;

    private HikFRAAPI mFRAHandle = null;


    private boolean bStartPreview = false;
    private boolean bNeedProc = false;
    private boolean bNeedCapFace = false;
    private boolean bNeedAddFaceModel = false;
    private int mCapCount = 0;

    private FaceRectView mFaceRect = null;


    private static final int DOFRAPROC = 0;

    private byte[] yuvData = null;
    private byte[] capYuvData = null;

    private Context mContext;

    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;
    private int mDevRotation = 0;

    //计算清晰度
    private boolean bDebugBlurry = false;
    private int blurryCount = 0;

    public CameraHandle(Context ctx, final SurfaceView preSurface, HikFRAAPI fraProc, FaceRectView facerect) {
        yuvData = new byte[mWidth * mHeight * 3 / 2];
        capYuvData = new byte[mWidth * mHeight * 3 / 2];
        mFaceRect = facerect;
        mContext = ctx;

        mPreSurface = preSurface;
        mFRAHandle = fraProc;

        mPreHolder = mPreSurface.getHolder();
        mPreHolder.addCallback(prevSurCb);

        rs = RenderScript.create(ctx);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        startCamera();
    }

    /**
     * 预览数据回调
     */
    Camera.PreviewCallback mPreCb = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {
//            Log.d(TAG, "dataLen = " + data.length);

            if (bDebugBlurry) {
                long blurry = brennerCalc(data, mWidth, mHeight);
                Bitmap blurryBm = nv21ToBitmap(data, mWidth, mHeight);
                Matrix matrix = new Matrix();
                if (mCamerId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    matrix.postRotate((90));
                } else {
                    matrix.setScale(1f, -1f);
                    matrix.postRotate((90));
                }

                Bitmap rotatedBitmap = Bitmap.createBitmap(blurryBm, 0, 0, blurryBm.getWidth(), blurryBm.getHeight(), matrix, true);

                blurryCount++;
                String blurryPath = "/sdcard/facelib/" + blurryCount + "_" + blurry + ".jpg";
                OutputStream out = null;
                try {
                    out = new FileOutputStream(blurryPath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }

            if (bNeedCapFace) {
                bNeedCapFace = false;
                Log.d(TAG, "save face yuv!");
                System.arraycopy(yuvData, 0, capYuvData, 0, yuvData.length);
                //show
                if (mCapShow != null) {
                    Bitmap capBm = nv21ToBitmap(capYuvData, mWidth, mHeight);
                    Matrix matrix = new Matrix();
                    if (mCamerId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        matrix.postRotate((90));
                    } else {
                        matrix.setScale(1f, -1f);
                        matrix.postRotate((90));
                    }

                    Bitmap rotatedBitmap = Bitmap.createBitmap(capBm, 0, 0, capBm.getWidth(), capBm.getHeight(), matrix, true);
                    mCapShow.setImageBitmap(rotatedBitmap);
                }
            }

            /** 俘获数据进行处理*/
            if (bNeedProc) {
                if (mFRAHandle.getStatus() != 0) {
                    //Log.d(TAG, "busy now , stip.............");
                    return;
                }
                //YUV420spRotate180(yuvData, data,  mWidth, mHeight);
                System.arraycopy(data, 0, yuvData, 0, data.length);
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Log.i(TAG, "YUV 数据处理...."+yuvData.length);
                        try {
                            writeBytesToFile(yuvData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
						//前提是从人脸库加载了所有的人脸模型，1VN比对逻辑是比对当前缓存区所有的建模数据。（可以先调用loadFaceData方法加载所有的人脸模型，然后进行1VN比对）
                        int ret = mFRAHandle.faceBuid1VNCompare(yuvData, yuvData.length);
                        Log.i(TAG, "faceBuidModel: "+ret);
                        if (ret == HikFRAAPI.FRA_RESUTL.FRA_RET_OK) {

                        }
                    }
                }).start();
                //TODO  可以对data数据进行算法处理

                if (bNeedCapFace) {
                    bNeedCapFace = false;
                    Log.d(TAG, "save face yuv!");
                    System.arraycopy(yuvData, 0, capYuvData, 0, yuvData.length);
                    //show
                    if (mCapShow != null) {
                        Bitmap capBm = nv21ToBitmap(capYuvData, mWidth, mHeight);
                        Matrix matrix = new Matrix();
                        if (mCamerId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            matrix.postRotate((90));
                        } else {
                            matrix.setScale(1f, -1f);
                            matrix.postRotate((90));
                        }

                        Bitmap rotatedBitmap = Bitmap.createBitmap(capBm, 0, 0, capBm.getWidth(), capBm.getHeight(), matrix, true);
                        mCapShow.setImageBitmap(rotatedBitmap);
                    }
                }
            }

        }
    };

    /**
     * 预览窗体回调
     */
    SurfaceHolder.Callback prevSurCb = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            stopCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            stopCamera();
        }
    };


    public void InitCamera() {
        Camera.Parameters mCParam = mCamera.getParameters();
        mCParam.setPreviewSize(mWidth, mHeight);
        mCParam.setPreviewFormat(ImageFormat.NV21);
        if (mCamerId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCamera.setDisplayOrientation(90);
            mCParam.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
        } else {
            mCamera.setDisplayOrientation(270);
        }

        mCamera.setParameters(mCParam);
    };

    public void switchCamera() {
        stopCamera();
        /** 切换摄像头*/
        mCamerId = (mCamerId == 0) ? 1 : 0;

        startCamera();
    }

    private void startCamera() {
        mCamera = Camera.open(mCamerId);
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(mPreHolder);
            } catch (IOException e) {
                e.printStackTrace();
                mCamera.release();
                mCamera = null;
            }
        }

        if (mCamera != null) {
            if (bStartPreview) {
                mCamera.stopPreview();
            }
            InitCamera();
            mCamera.setPreviewCallback(mPreCb);
            mCamera.startPreview();
            bStartPreview = true;

            if (mCamerId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Camera.Parameters mCParam = mCamera.getParameters();
                List<Camera.Size> supportSize = mCParam.getSupportedPreviewSizes();
                mCParam.setZoom(mCParam.getMaxZoom() / 5);
                Log.d(TAG, "HMQ---------------------------------- maxzom" + mCParam.getMaxZoom());
                mCamera.setParameters(mCParam);
            }

            mCamera.startFaceDetection();
            Log.d(TAG, "HMQ---------------------------------- startFaceDetection");
            mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
                @Override
                public void onFaceDetection(Camera.Face[] faces, Camera camera) {
//                     Log.d(TAG, "onFaceDetection "+ faces.length);
                    if (faces.length > 0) {
                        //Log.d("RectTest", "onFaceDetection id:"+ faces[0].id + " (" + faces[0].rect.left + "," +faces[0].rect.top + "," +faces[0].rect.right + "," +faces[0].rect.bottom + ")");
                        Rect faceRect = new Rect(faces[0].rect.left, faces[0].rect.top, faces[0].rect.right, faces[0].rect.bottom);
                        Rect dstRect = mFaceRect.transForm(faceRect, mPreSurface.getWidth(), mPreSurface.getHeight(), (mCamerId == Camera.CameraInfo.CAMERA_FACING_FRONT));
                        mFaceRect.drawFaceRect(dstRect);
//                        bNeedCapFace = true;
                    } else {
                        mFaceRect.clearRect();
//                        bNeedCapFace = false;
                    }
                }
            });
        }
    }

    private void stopCamera() {
        if (bStartPreview && mCamera != null) {
            mPreHolder.removeCallback(prevSurCb);
            mCamera.stopFaceDetection();
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        bStartPreview = false;
    }

    public void capData(ImageView capShow) {
        mCapShow = capShow;
        bNeedCapFace = true;
        mCapCount++;
    }

    public void addFaceModel(String libName, String humId, String humInfo, String facePath, String modelPath) {
        Log.d(TAG, "Out File: " + facePath);

        if (bNeedCapFace) {
            Log.e(TAG, "No face data capture!");
            Toast.makeText(mContext, "未俘获到人脸，请重新调整摄像头位置", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap capBm = nv21ToBitmap(capYuvData, mWidth, mHeight);
        Matrix matrix = new Matrix();
        if (mCamerId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            matrix.postRotate((90));
        } else {
            matrix.postRotate((90));
        }
        Log.i(TAG, "addFaceModel: "+capBm);

        Bitmap rotatedBitmap = Bitmap.createBitmap(capBm, 0, 0, capBm.getWidth(), capBm.getHeight(), matrix, true);
        try {
            OutputStream out = new FileOutputStream(facePath);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int ret = mFRAHandle.faceAddModel(libName, humId, humInfo, facePath, modelPath);
        if (ret != HikFRAAPI.FRA_RESUTL.FRA_RET_OK) {
            Log.e(TAG, "Add Face Model failed!!" + ret);
            Toast.makeText(mContext, "添加人脸失败，错误码" + ret, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "人脸添加成功：" + humId, Toast.LENGTH_SHORT).show();
        }
    }

    public void startProc() {
        bNeedProc = true;
    }

    public Bitmap nv21ToBitmap(byte[] nv21, int width, int height) {
        yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
        in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

        rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
        out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

        in.copyFrom(nv21);
        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        Bitmap bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        out.copyTo(bmpout);
        return bmpout;

    }

    public void setRotation(int newRotation) {
        mDevRotation = newRotation;
        mFRAHandle.setParam(HikFRAAPI.FRA_SET_KEY.FRA_KEY_ROTATION, String.valueOf(mDevRotation));
    }

    public long brennerCalc(byte[] data, int width, int height) {
        long brenner = 0;
        Log.d(TAG, "1------------------------------------------- brenner = " + brenner);

        for (int i = 0; i < width - 2; ++i) {
            for (int j = 0; j < height - 2; ++j) {
                brenner += Math.pow((Math.abs(data[(i + 2) + (j * width)] - data[i + (j * width)])), 2);
            }
        }
        Log.d(TAG, "2------------------------------------------- brenner = " + brenner);
        return brenner;
    }

    public void YUV420spRotate180(byte[] des, byte[] src, int width, int height) {
        int n = 0;
        int uh = height >> 1;
        int wh = width * height;
        //copy y
        for (int j = height - 1; j >= 0; j--) {
            for (int i = width - 1; i >= 0; i--) {
                des[n++] = src[width * j + i];
            }
        }
        for (int j = uh - 1; j >= 0; j--) {
            for (int i = width - 1; i > 0; i -= 2) {
                des[n] = src[wh + width * j + i - 1];
                des[n + 1] = src[wh + width * j + i];
                n += 2;
            }
        }
    }

    public void writeBytesToFile(byte[] data) throws IOException {
        StringBuilder sb = new StringBuilder();

        OutputStream out = new FileOutputStream("/sdcard/1.yuv");
        InputStream is = new ByteArrayInputStream(data);
        byte[] buff = new byte[1024];
        int len = 0;
        while ((len = is.read(buff)) != -1) {
            out.write(buff, 0, len);
        }
        is.close();
        out.close();
    }

}
