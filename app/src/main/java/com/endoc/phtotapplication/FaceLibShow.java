package com.endoc.phtotapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * 描述：
 * 创建：huangmuquan
 * 日志：2018/12/29
 */public class FaceLibShow extends Activity implements AdapterView.OnItemClickListener {

    private File[] mFilePaths;
    private Bitmap[] mFileBms;
    private String[] mFileNames;
    private static final String TAG = "hikFRDemo";
    private String[] mItemName = {"Img", "Name"};
    private GridView mGridView;
    private List<Map<String, Object>> mFaceList;
    private MyFileListPreview mSAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facelibshow);
        mGridView = findViewById(R.id.faceShowList);
        initData();
        int[] res = {R.id.img, R.id.text};
        mSAdapter = new MyFileListPreview(this, mFaceList, R.layout.gridview_item, mItemName, res);
        mGridView.setAdapter(mSAdapter);
        mGridView.setOnItemClickListener(this);
    }

    public void initData(){
        loadFile("/sdcard/facelib");
        mFaceList = new ArrayList<Map<String, Object>>();
        for(int i = 0; i < mFileNames.length; ++i){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(mItemName[0], mFilePaths[i]);
            map.put(mItemName[1], "" + i + "." + mFileNames[i]);
            mFaceList.add(map);
        }
    }

    public void loadFile(String folderPath){
        File f = new File(folderPath);
        mFilePaths = f.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith("jpg");
            }
        });
        mFileNames = new String[mFilePaths.length];
        //mFileBms = new Bitmap[mFilePaths.length];
        int i = 0;
        for (File aFile : mFilePaths) {
            mFileNames[i] =  aFile.getName();
            //mFileBms[i] = BitmapFactory.decodeFile(aFile.getAbsolutePath());
            //Log.d(TAG, "File Path = " + aFile.getAbsolutePath());
            i++;
        }

        //for(String fileName : mFileNames){
            //Log.d(TAG, "File Name = " + fileName);
        //}
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this, "我是" + mFileNames[position], Toast.LENGTH_SHORT).show();
    }

    public class MyFileListPreview extends SimpleAdapter {

        /**
         * Constructor
         *
         * @param context  The context where the View associated with this SimpleAdapter is running
         * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
         *                 Maps contain the data for each row, and should include all the entries specified in
         *                 "from"
         * @param resource Resource identifier of a view layout that defines the views for this list
         *                 item. The layout file should include at least those named views defined in "to"
         * @param from     A list of column names that will be added to the Map associated with each
         *                 item.
         * @param to       The views that should display column in the "from" parameter. These should all be
         *                 TextViews. The first N views in this list are given the values of the first N columns
         */
        public MyFileListPreview(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public void setViewImage(ImageView v, String value) {
            Log.d(TAG, "value = " + value);
            Bitmap bmp = BitmapFactory.decodeFile(value);
            Bitmap newBmp = Bitmap.createScaledBitmap(bmp, 200,200,false);
            v.setImageBitmap(newBmp);
        }
    }
}
