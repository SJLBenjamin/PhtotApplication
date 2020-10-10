package com.endoc.phtotapplication;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

/*
 * 描述：
 * 创建：huangmuquan
 * 日志：2018/12/17
 */public class TTSBroadcast implements TextToSpeech.OnInitListener {

     private static final String TAG = "hikFRDemo";
     private boolean isSucessful = false;

     private TextToSpeech mSpeech = null;

     public TTSBroadcast(Context mContext){
        mSpeech = new TextToSpeech(mContext,this);
     }

     public void playText(String text){
         Log.d(TAG, "sucess: " + isSucessful + " play: " + text);
         if (mSpeech != null){
             mSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null,null);
         }
     }

     public void stopPlay(){
            mSpeech.stop();
     }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS){
            int ret = mSpeech.setLanguage(Locale.CHINA);
            mSpeech.setPitch(1.3f);
            mSpeech.setSpeechRate(1.3f);
            if (ret == TextToSpeech.LANG_MISSING_DATA || ret == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.d(TAG, "Not support Chinese!!!!!!!!");
                isSucessful = false;
            }else
            {
                isSucessful = true;
            }
        }
    }
}
