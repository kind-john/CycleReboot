package com.ckt.cyclereboot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

public class CycleRebootMain extends Activity implements View.OnClickListener{

    static final String IS_CYCLING = "is_cycling";
    static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private static final String TAG = "CycleRebootMain";
    private static final int SIM_STATE_NOT_READY = 6;
    private static final int SIM_STATE_PERM_DISABLED = 7;
    private static final int SIM_STATE_CARD_IO_ERROR = 8;
    static final int MSG_REBOOT_START = 1;
    static final int MSG_GET_SIM_STATE = 2;
    private static final String TOTAL_TIMES = "total_times";
    private static final String REMAIN_TIMES = "remain_times";
    private static final String CYCLED_TIMES = "cycled_times";
    private static final String SIM_FAILED_TIMES = "sim_faild_times";
    private static final String ERROR_REASON = "error_reason";
    private static final long CHECK_TIME_INTERVAL = (15*1000);  //check interval of sim state * ms
    private static final long CHECK_TIME_OUT = (5*60*1000);  //check interval of sim state * ms
    private long passedTime = 0;
    private EditText mCycleTimesEdit;
    private Button mOKButton;
    private TextView mCycledTimesTextView;
    private TextView mRemainTimesTextView;
    private TextView mTotalTimesTextView;
    private TextView mFailedTimesTextView;
    private TextView mSimStateTextView;
    private Button mStartButton;
    private Button mReportButton;
    private int mTotalTimes = 0;
    private int mCycledTimes = 0;
    private int mRemainTimes = 0;
    private int mFailedScanSimTimes = 0;
    private CycleRebootApplication mApplication;
    private PowerManager mPowerManager;
    private SimStatusReceiver mSimStatusReceiver;
    private Resources mResources;
    private boolean mIsCycling = false;
    private TelephonyManager mTelephonyManager;
    private ArrayMap<Integer, Integer> multiSimStates;
    private PowerManager.WakeLock wakeLock;
    private final static boolean IS_NEED_CHECK_SIM_STATUS = false; //control wether check sim status
    private boolean mIsTimeOut = false;
    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_REBOOT_START:
                        MyLogs.MyLogD(TAG, "handleMessage  MSG_REBOOT_START mIsCycling = "+mIsCycling);
                        if(mIsCycling){
                            mCycledTimes++;
                            mRemainTimes = mTotalTimes - mCycledTimes;
                            if(mRemainTimes == 0){
                                mIsCycling = false;
                                ArrayMap<String, Object> map = new ArrayMap<String, Object>();
                                map.put(IS_CYCLING, mIsCycling);
                                mApplication.saveSharedPreferences(map);
                                mOKButton.setClickable(true);
                                mStartButton.setClickable(true);
                            }
                            if(mIsTimeOut){
                                //time out need write failed reason to sp
                                mFailedScanSimTimes++;
                                ArrayMap<String, Object> map = new ArrayMap<String, Object>();

                                StringBuilder sb = new StringBuilder();
                                MyLogs.MyLogD(TAG,"multiSimStates size = "+ multiSimStates.size());
                                for(int i = 0; i < multiSimStates.size(); i++){
                                    sb.append("SIM ");
                                    sb.append(i);
                                    sb.append(" state : ");
                                    sb.append(conversionStateToString(multiSimStates.get(i)));
                                    sb.append("\n");
                                }
                                map.put(ERROR_REASON + mCycledTimes, "reboot " +
                                        mCycledTimes +
                                        "th sim error reason:" +
                                        sb.toString());
                                mApplication.saveSharedPreferences(map);
                            }
                            writeTimesToPreferences();
                            updateTimesString();
                            /*
                            Intent rebootIntent = new Intent();
                            String reboot_action = "";
                            try {
                                Field action_request_shutdown = rebootIntent.getClass().
                                        getDeclaredField("ACTION_REQUEST_SHUTDOWN");
                                action_request_shutdown.setAccessible(true);
                                reboot_action = (String)action_request_shutdown.get(rebootIntent);
                                MyLogs.MyLogD(TAG, "reboot_action >>>>>>>>>>>>>>> get successfully");
                            }catch (Exception e){
                                reboot_action = "";
                                MyLogs.MyLogD(TAG, "reboot_action >>>>>>>>>>>>>>> get failed");
                                MyLogs.MyLogD(TAG, "Exception >>>>>>>>>>>>>>> : " + e.toString());
                            }
                            String extra_data_key_confirm = "";
                            try {
                                Field extra_key_confirm = rebootIntent.getClass().
                                        getDeclaredField("EXTRA_KEY_CONFIRM");
                                extra_key_confirm.setAccessible(true);
                                extra_data_key_confirm = (String)extra_key_confirm.get(rebootIntent);
                                MyLogs.MyLogD(TAG, "extra_data_key_confirm >>>>>>>>>>>>>>> get successfully");
                            }catch (Exception e){
                                extra_data_key_confirm = "";
                                MyLogs.MyLogD(TAG, "extra_data_key_confirm >>>>>>>>>>>>>>> get failed");
                                MyLogs.MyLogD(TAG, "Exception >>>>>>>>>>>>>>> : " + e.toString());
                            }

                            rebootIntent.setAction(reboot_action);
                            rebootIntent.putExtra(extra_data_key_confirm, false);
                            rebootIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(rebootIntent);
                            */
                            mPowerManager.reboot("test");
                        } else {
                            /**SimStatusReceiver may be send this message
                            *if user don't click start button,
                            * this command should be ignored.
                            **/
                            MyLogs.MyLogD(TAG, "reboot command ignored !!!");
                        }

                    break;
                case MSG_GET_SIM_STATE:
                    /*get sim state per CHECK_TIME_INTERVAL ms
                    * util CHECK_TIME_OUT
                    * */
                    MyLogs.MyLogD(TAG, "handleMessage  MSG_GET_SIM_STATE");
                    if(simAndNetworkValide()){
                        //send reboot msg
                        mIsTimeOut = false;
                        MyLogs.MyLogE(TAG, "sim state ok send reboot msg !!!");
                        removeMessages(MSG_GET_SIM_STATE);
                        removeMessages(MSG_REBOOT_START);
                        sendEmptyMessage(MSG_REBOOT_START);
                    }else{
                        //recheck sim status ?
                        if(passedTime < CHECK_TIME_OUT){
                            passedTime += CHECK_TIME_INTERVAL;
                            mIsTimeOut = false;
                            sendEmptyMessageDelayed(MSG_GET_SIM_STATE, CHECK_TIME_INTERVAL);
                        } else {
                            //time out?
                            mIsTimeOut = true;
                            MyLogs.MyLogE(TAG, "check sim state time out !!!");
                            removeMessages(MSG_GET_SIM_STATE);
                            removeMessages(MSG_REBOOT_START);
                            sendEmptyMessage(MSG_REBOOT_START);
                        }
                    }

                    //update UI
                    StringBuilder sb = new StringBuilder();
                    MyLogs.MyLogD(TAG,"multiSimStates size = "+ multiSimStates.size());
                    for(int i = 0; i < multiSimStates.size(); i++){
                        sb.append("SIM ");
                        sb.append(i);
                        sb.append(" state : ");
                        sb.append(conversionStateToString(multiSimStates.get(i)));
                        sb.append(" operator name : ");
                        String opreratorName = "unknown";
                        try {
                            if(mTelephonyManager !=null) {
                                Method getNetworkOperatorName = mTelephonyManager.getClass().
                                        getDeclaredMethod("getNetworkOperatorName",
                                                new Class[]{int.class});
                                getNetworkOperatorName.setAccessible(true);
                                opreratorName = (String) getNetworkOperatorName.invoke(mTelephonyManager, i);
                                MyLogs.MyLogD(TAG, "opreratorName >>>>>>>>>>>>>>> get successfully");
                            }else{
                                MyLogs.MyLogD(TAG, "opreratorName >>>>>>>>>>>>>>> get failed");
                                opreratorName = "unknown";
                            }
                        }catch (Exception e){
                            MyLogs.MyLogD(TAG, "opreratorName >>>>>>>>>>>>>>> get failed");
                            opreratorName = "unknown";
                        }
                        sb.append(opreratorName);
                        //sb.append(mTelephonyManager.getNetworkOperatorName(i));
                        sb.append("\n");
                    }
                    updateSimStateUI(sb.toString());
                    break;
                default:
                    break;
            }
        }
    };

    private String conversionStateToString(int oldState) {
        String result = "";
        switch (oldState){
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = mResources.getString(R.string.sim_state_string_unknown);
                break;
            case TelephonyManager.SIM_STATE_ABSENT:
                result = mResources.getString(R.string.sim_state_string_absent);
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                result = mResources.getString(R.string.sim_state_string_pin_required);
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                result = mResources.getString(R.string.sim_state_string_puk_required);
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                result = mResources.getString(R.string.sim_state_string_network_locked);
                break;
            case TelephonyManager.SIM_STATE_READY:
                result = mResources.getString(R.string.sim_state_string_ready);
                break;
            case SIM_STATE_NOT_READY:
                result = mResources.getString(R.string.sim_state_string_not_ready);
                break;
            case SIM_STATE_PERM_DISABLED:
                result = mResources.getString(R.string.sim_state_string_perm_disabled);
                break;
            case SIM_STATE_CARD_IO_ERROR:
                result = mResources.getString(R.string.sim_state_string_card_io_error);
                break;
            default:
                result = mResources.getString(R.string.sim_state_string_unknown);
                break;
        }
        return result;
    }

    private void updateSimStateUI(String stateString) {
        MyLogs.MyLogD(TAG, "updateSimStateUI stateString : " + stateString);
        mSimStateTextView.setText(stateString);
    }

    private boolean simAndNetworkValide(){
        int simCounts = 1;
        if(!IS_NEED_CHECK_SIM_STATUS && mIsCycling){
            MyLogs.MyLogD(TAG, "don't need check sim status!!!");
            return true;
        }
        try{
            if(mTelephonyManager !=null){
                Method methodSend=  mTelephonyManager.getClass().getDeclaredMethod("getPhoneCount");
                methodSend.setAccessible(true);
                simCounts = (Integer)methodSend.invoke(mTelephonyManager);
                MyLogs.MyLogD(TAG, "simCounts >>>>>>>>>>>>>>> get successfully");
            } else {
                MyLogs.MyLogD(TAG, "simCounts >>>>>>>>>>>>>>> get failed");
                simCounts = 1;
            }
            MyLogs.MyLogD(TAG, "simCounts = >>>>>>>>>>>>>>>"+simCounts);
        }catch (Exception e){
            MyLogs.MyLogD(TAG, "simCounts >>>>>>>>>>>>>>> get failed");
            MyLogs.MyLogD(TAG, "Exception = >>>>>>>>>>>>>>>"+e.toString());
            simCounts = 1;
        }
        //int simCounts = mTelephonyManager.getPhoneCount();
        MyLogs.MyLogD(TAG, "simCounts = "+simCounts);
        if(simCounts <= 0 ){
            return false;
        }
        ArrayMap<Integer, Boolean> multiSimIsOK = new ArrayMap<Integer, Boolean>();
        for(int i = 0 ; i < simCounts; i++){
            int newState = TelephonyManager.SIM_STATE_UNKNOWN;
            try {
                if(mTelephonyManager !=null){
                    Method getSimState = mTelephonyManager.getClass().
                            getDeclaredMethod("getSimState",
                                    new Class[] {int.class});
                    getSimState.setAccessible(true);
                    newState = (Integer)getSimState.invoke(mTelephonyManager, i);
                    MyLogs.MyLogD(TAG, "newState >>>>>>>>>>>>>>> get successfully");
                }else{
                    MyLogs.MyLogD(TAG, "newState >>>>>>>>>>>>>>> get failed");
                    newState = TelephonyManager.SIM_STATE_UNKNOWN;
                }
            }catch (Exception e){
                MyLogs.MyLogD(TAG, "newState >>>>>>>>>>>>>>> get failed");
                MyLogs.MyLogD(TAG, "newState >>>>>>>>>>>>>>> Exception :" + e.toString());
                newState = TelephonyManager.SIM_STATE_UNKNOWN;
            }
            //int newState = mTelephonyManager.getSimState(i);
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
    private void checkTimesValide(){
        if(mCycledTimes < 0) mCycledTimes = 0;
        if(mRemainTimes < 0) mRemainTimes = 0;
        if(mTotalTimes < 0) mTotalTimes = 0;
        if(mFailedScanSimTimes < 0) mFailedScanSimTimes = 0;
        if(mCycledTimes > mTotalTimes) mCycledTimes = mTotalTimes;
        if((mCycledTimes + mRemainTimes) != mTotalTimes){
            mRemainTimes = mTotalTimes - mCycledTimes;
        }
        MyLogs.MyLogD(TAG, "checkTimesValide mCycledTimes = "+mCycledTimes);
        MyLogs.MyLogD(TAG, "checkTimesValide mRemainTimes = "+mRemainTimes);
        MyLogs.MyLogD(TAG, "checkTimesValide mTotalTimes = "+mTotalTimes);
        MyLogs.MyLogD(TAG, "checkTimesValide mFailedScanSimTimes = "+mFailedScanSimTimes);
    }
    private void readTimesFromPreferences(){
        MyLogs.MyLogD(TAG, "readTimesFromPreferences");
        mCycledTimes = mApplication.getIntSharedPreferences(CYCLED_TIMES);
        mRemainTimes = mApplication.getIntSharedPreferences(REMAIN_TIMES);
        mTotalTimes = mApplication.getIntSharedPreferences(TOTAL_TIMES);
        mFailedScanSimTimes = mApplication.getIntSharedPreferences(SIM_FAILED_TIMES);
        checkTimesValide();
    }

    private void writeTimesToPreferences(){
        MyLogs.MyLogD(TAG, "writeTimesToPreferences");
        checkTimesValide();
        ArrayMap<String, Object> map = new ArrayMap<String, Object>();
        map.put(CYCLED_TIMES, mCycledTimes);
        map.put(REMAIN_TIMES, mRemainTimes);
        map.put(TOTAL_TIMES, mTotalTimes);
        map.put(SIM_FAILED_TIMES, mFailedScanSimTimes);
        mApplication.saveSharedPreferences(map);
    }

    private void updateTimesString(){
        MyLogs.MyLogD(TAG, "updateTimesString");
        mCycledTimesTextView.setText(""+mCycledTimes);
        mRemainTimesTextView.setText(""+mRemainTimes);
        mTotalTimesTextView.setText(""+mTotalTimes);
        mFailedTimesTextView.setText(""+mFailedScanSimTimes);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cycle_reboot_main);
        MyLogs.MyLogD(TAG, "onCreate");
        mApplication = (CycleRebootApplication)getApplication(); //new CycleRebootApplication();

        mResources = getResources();
        multiSimStates = new ArrayMap<Integer, Integer>();
        mCycleTimesEdit = (EditText)findViewById(R.id.time_edit);

        mCycledTimesTextView = (TextView)findViewById(R.id.cycledtimes);
        mRemainTimesTextView = (TextView)findViewById(R.id.remaintimes);
        mTotalTimesTextView = (TextView)findViewById(R.id.totaltimes);
        mFailedTimesTextView = (TextView)findViewById(R.id.failedtimes);

        readTimesFromPreferences();
        updateTimesString();
        mSimStateTextView = (TextView)findViewById(R.id.simstatetextview);

        mOKButton = (Button)findViewById(R.id.ok);
        mStartButton = (Button)findViewById(R.id.start);
        mReportButton = (Button)findViewById(R.id.report);
        if(mIsCycling){
            mOKButton.setClickable(false);
            mStartButton.setClickable(false);
        }
        if(mTotalTimes <= 0){
            mStartButton.setClickable(false);
        }
        mOKButton.setOnClickListener(this);
        mStartButton.setOnClickListener(this);
        mReportButton.setOnClickListener(this);

        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        wakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
                "<<CycleReboot KeepScreenOn>>");
        wakeLock.acquire();
        MyLogs.MyLogD(TAG, "get wake lock!");
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //RebootCompletedReceiver start activity may be delivery this param.
        mIsCycling = mApplication.getBooleanSharedPreferences(IS_CYCLING);

        //start scan sim state
        Message msg = new Message();
        if(!IS_NEED_CHECK_SIM_STATUS && mIsCycling){
            msg.what = MSG_REBOOT_START;
        }else{
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_SIM_STATE_CHANGED);
            mSimStatusReceiver = new SimStatusReceiver(getApplicationContext(), mHandler);
            registerReceiver(mSimStatusReceiver, filter);
            msg.what = MSG_GET_SIM_STATE;
        }
        mHandler.sendMessage(msg);
    }

    @Override
    protected void onResume() {
        MyLogs.MyLogD(TAG, "onResume");
        if(wakeLock != null){
            wakeLock.acquire();
            MyLogs.MyLogD(TAG, "get wake lock!");
        }else{
            wakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
                    "<<CycleReboot KeepScreenOn>>");
            wakeLock.acquire();
            MyLogs.MyLogD(TAG, "get wake lock!");
        }
        super.onResume();
    }

    @Override
    protected void onStart() {
        MyLogs.MyLogD(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onPause() {
        MyLogs.MyLogD(TAG, "onPause");
        if(wakeLock != null){
            wakeLock.release();
            wakeLock = null;
            MyLogs.MyLogD(TAG, "release wake lock!");
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        MyLogs.MyLogD(TAG, "onDestroy");
        if(mSimStatusReceiver != null){
            unregisterReceiver(mSimStatusReceiver);
        }
        super.onDestroy();
    }

    /**
     * 1.reset mTotalTimes/mCycledTimes/mRemainTimes
     * 2.update UI  : times show
     */
    private void resetTimes(int newTotal) {
        MyLogs.MyLogD(TAG, "resetTimes");
        mTotalTimes = newTotal;
        mCycledTimes = 0;
        mRemainTimes = mTotalTimes - mCycledTimes;
        mFailedScanSimTimes = 0;
        updateTimesString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ok:
                if(mIsCycling){
                    /*
                    * if cycling , can not reset times.
                    * */
                    Toast.makeText(CycleRebootMain.this,
                            mResources.getString(R.string.tips_string_cycling),
                            Toast.LENGTH_SHORT).show();
                    return ;
                }
                try {
                    final int tmpTotal = Integer.parseInt(mCycleTimesEdit.getText().toString());
                    if(tmpTotal <= 0){
                        Toast.makeText(CycleRebootMain.this,
                                mResources.getString(R.string.number_catch_error),
                                Toast.LENGTH_SHORT).show();
                        return ;
                    } else {
                        if(mTotalTimes > 0){
                            /*
                            * if mTotalTimes > 0 ,prompt a tip ask user whether reset test times,
                            * 'OK' -> clear all saved data, than reset ...
                            * 'Cancel'  -> do nothing
                            * */
                            MyLogs.MyLogD(TAG, "onClick OK mTotalTimes has set = "+mTotalTimes);
                            AlertDialog.Builder builder = new AlertDialog.Builder(CycleRebootMain.this);
                            builder.setTitle(mResources.getString(R.string.reset_total_times_title));
                            builder.setMessage(mResources.getString(R.string.reset_total_times_message));
                            builder.setPositiveButton(mResources.getString(R.string.confirm_button),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            /*
                                            * do reset :
                                            * 1.reset all times
                                            * 2.clear data saved in shared preferences
                                            * 3.save new data to shared preferences :
                                            * mFailedScanSimTimes/mTotalTimes/mCycledTimes/mRemainTimes
                                            * */
                                            resetTimes(tmpTotal);
                                            mApplication.clearSharedPreferences();
                                            mStartButton.setClickable(true);
                                            writeTimesToPreferences();
                                        }
                                    });
                            builder.setNegativeButton(mResources.getString(R.string.cancel_button),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //do nothing!
                                        }
                                    });
                            builder.create().show();
                        } else if(mTotalTimes == 0){
                            MyLogs.MyLogD(TAG, "onClick OK mTotalTimes = "+mTotalTimes+ " should set it.");
                            /*
                            * This the first set data :
                            * 1.reset all times
                            * 2.clear data saved in shared preferences
                            * 3.save new data to shared preferences :
                            * mFailedScanSimTimes/mTotalTimes/mCycledTimes/mRemainTimes
                            * */
                            mApplication.clearSharedPreferences();
                            resetTimes(tmpTotal);
                            mStartButton.setClickable(true);
                            writeTimesToPreferences();
                        }
                    }

                    //how deal with reset?
                }catch (NumberFormatException e){
                    mTotalTimes = 0;
                    Toast.makeText(getApplicationContext(),
                            mResources.getString(R.string.number_catch_error),
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.start:
                /*
                * start cycle reboot:
                * stop condition : mCycledTimes >= mTotalTimes
                * */
                if(mTotalTimes <= 0){
                    MyLogs.MyLogD(TAG, "onClick start mTotalTimes = "+mTotalTimes);
                    Toast.makeText(getApplicationContext(),
                            mResources.getString(R.string.number_catch_error),
                            Toast.LENGTH_SHORT).show();
                } else {
                    if(mCycledTimes < mTotalTimes){
                        MyLogs.MyLogD(TAG, "onClick start mCycledTimes = "+mCycledTimes);
                        MyLogs.MyLogD(TAG, "onClick start mTotalTimes = "+mTotalTimes);
                        MyLogs.MyLogD(TAG, "onClick start mIsCycling = "+mIsCycling);
                        if(mIsCycling){
                            /*
                            * if cycling, start button can not click ,
                            * but double check it here
                            * */
                            Toast.makeText(getApplicationContext(),
                                    mResources.getString(R.string.iscycling),
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            mIsCycling = true;
                            ArrayMap<String, Object> map = new ArrayMap<String, Object>();
                            map.put(IS_CYCLING, mIsCycling);
                            mApplication.saveSharedPreferences(map);
                            mStartButton.setClickable(false);
                            if(simAndNetworkValide()){
                                Message msg = new Message();
                                msg.what = CycleRebootMain.MSG_REBOOT_START;
                                mHandler.sendMessage(msg);
                                //mHandler.sendEmptyMessage(CycleRebootMain.MSG_REBOOT_START);
                            }

                        }
                    }else{
                        /*cycle completed, but user may be want to restart it
                         * so pop-up prompts,:
                         * 'OK' -> restart :
                         *   1.reset  mCycledTimes/mRemainTimes/mIsCycling/mFailedScanSimTimes
                         *   and save them to shared preferences.
                         *   2.send reboot msg
                         * */
                        MyLogs.MyLogD(TAG, "onClick start whether restart ?");
                        AlertDialog.Builder builder = new AlertDialog.Builder(CycleRebootMain.this);
                        builder.setTitle(mResources.getString(R.string.start_button_title));
                        builder.setMessage(mResources.getString(R.string.start_button_message));
                        builder.setPositiveButton(mResources.getString(R.string.confirm_button),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mApplication.clearSharedPreferences();
                                        resetTimes(mTotalTimes);
                                        writeTimesToPreferences();
                                        mIsCycling = true;
                                        ArrayMap<String, Object> map = new ArrayMap<String, Object>();
                                        map.put(IS_CYCLING, mIsCycling);
                                        mApplication.saveSharedPreferences(map);
                                        mStartButton.setClickable(false);
                                        if(simAndNetworkValide()){
                                            Message msg = new Message();
                                            msg.what = CycleRebootMain.MSG_REBOOT_START;
                                            mHandler.sendMessage(msg);
                                            //mHandler.sendEmptyMessage(CycleRebootMain.MSG_REBOOT_START);
                                        }
                                    }
                                });
                        builder.setNegativeButton(mResources.getString(R.string.cancel_button),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //do nothing!
                                    }
                                });
                        builder.create().show();
                    }

                }
                break;
            case R.id.report:
                break;
        }
    }


}
