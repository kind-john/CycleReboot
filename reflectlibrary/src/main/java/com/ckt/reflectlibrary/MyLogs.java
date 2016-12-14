package com.ckt.reflectlibrary;

import android.util.Log;

/**
 * Created by ckt on 16-12-12.
 */
public class MyLogs {

    private static final String MYFLAG = "reflectlibrary ";
    private static final boolean DEBUG = true;

    public static int MyLogD(String tag, String msg){
        int result = 0;
        if(DEBUG){
            result = Log.d(tag,MYFLAG+msg);
        } else {
            //do nothing
        }
        return result;
    }

    public static int MyLogE(String tag, String msg){
        int result = 0;
        if(DEBUG){
            result = Log.e(tag,MYFLAG+msg);
        } else {
            //do nothing
        }
        return result;
    }
        public static int MyLogW(String tag, String msg){
        int result = 0;
        if(DEBUG){
            result = Log.w(tag,MYFLAG+msg);
        } else {
            //do nothing
        }
        return result;
    }
        public static int MyLogI(String tag, String msg){
        int result = 0;
        if(DEBUG){
            result = Log.i(tag,MYFLAG+msg);
        } else {
            //do nothing
        }
        return result;
    }
        public static int MyLogV(String tag, String msg){
        int result = 0;
        if(DEBUG){
            result = Log.v(tag,MYFLAG+msg);
        } else {
            //do nothing
        }
        return result;
    }

}
