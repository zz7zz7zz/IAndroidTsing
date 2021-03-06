package com.open.test.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.Nullable;
import android.util.Log;

public class TIntentService extends IntentService {

    private static final String TAG = "TIntentService";

    public TIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        int cmd = (null != intent) ? intent.getIntExtra(CMD, 0xFFFFFFFF) : 0xFFFFFFFF;

        Log.v(TAG, "onHandleIntent CMD "+ cmd + " This " +this);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.v(TAG, "onDestroy This " + this);
    }

    //---------------------------------------------------------------------------------------
    public static final String CMD                         = "cmd";
    public static final int    CMD_TURN_ON_PUSH            = 0x0001;
    public static final int    CMD_UPDATE_ADCONFIG         = 0x0002;

    public static void turnOnPush(Context mContext) {
        Intent mIntent = new Intent(mContext, TIntentService.class);
        mIntent.putExtra(TIntentService.CMD, TIntentService.CMD_TURN_ON_PUSH);
        mContext.startService(mIntent);
    }

    public static void loadConfig(Context mContext) {
        Intent mIntent = new Intent(mContext, TIntentService.class);
        mIntent.putExtra(TIntentService.CMD, TIntentService.CMD_UPDATE_ADCONFIG);
        mContext.startService(mIntent);
    }
}
