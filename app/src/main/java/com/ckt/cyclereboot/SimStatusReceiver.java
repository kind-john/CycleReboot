package com.ckt.cyclereboot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;

public class SimStatusReceiver extends BroadcastReceiver {
    private static final String TAG = "SimStatusReceiver";
    private final Context mContext;
    private Handler mHandler = null;
    private TelephonyManager mTelephonyManager;
    private ArrayMap<Integer, Integer> multiSimStates = new ArrayMap<Integer, Integer>();

    public SimStatusReceiver(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        //add timer todo time out
    }

    private boolean simAndNetworkValide(){
        int simCounts = mTelephonyManager.getPhoneCount();
        MyLogs.MyLogD(TAG, "simCounts = "+simCounts);
        if(simCounts <= 0 ){
            return false;
        }
        ArrayMap<Integer, Boolean> multiSimIsOK = new ArrayMap<Integer, Boolean>();
        for(int i = 0 ; i < simCounts; i++){
            int newState = mTelephonyManager.getSimState(i);
            MyLogs.MyLogD(TAG, "sim "+ i + " state = "+newState);
            if(multiSimStates.get(i) == null){
                multiSimStates.put(i, newState);
            } else if(newState != multiSimStates.get(i)){
                multiSimStates.put(i, newState);
            }
            boolean simIsOK = false;
            simIsOK = (newState == TelephonyManager.SIM_STATE_READY) ? true : false;
            multiSimIsOK.put(i, simIsOK);

        }

        boolean simStateOK = true;

        if(multiSimIsOK.size() < simCounts){
            MyLogs.MyLogD(TAG, "multi sim status exception");
            return false;
        }

        for(int j = 0; j < multiSimIsOK.size() ; j++){
            if(multiSimIsOK.get(j) == null){
                return false;
            } else if(multiSimIsOK.get(j) == false){
                return false;
            }
            MyLogs.MyLogD(TAG, "simStateOK =  " + simStateOK);
            MyLogs.MyLogD(TAG, "multiSimIsOK  sim "+ j + " state = " + multiSimIsOK.get(j));
        }
        MyLogs.MyLogD(TAG, "simStateOK =  " + simStateOK);
        return simStateOK;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null || action.isEmpty()){
            return;
        } else if(action.equals(CycleRebootMain.ACTION_SIM_STATE_CHANGED)){
            Message msg = new Message();
            msg.what = CycleRebootMain.MSG_GET_SIM_STATE;
            mHandler.sendMessage(msg);
            if(simAndNetworkValide()){
                //mHandler.sendEmptyMessage(CycleRebootMain.MSG_REBOOT_START);
                Message msg1 = new Message();
                msg1.what = CycleRebootMain.MSG_REBOOT_START;
                mHandler.sendMessage(msg1);
            }
        }
    }
}
