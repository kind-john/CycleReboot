package com.ckt.cyclereboot;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.ArrayMap;

/**
 * Created by ckt on 16-12-9.
 */
public class CycleRebootApplication extends Application{
    private static final String TAG = "CycleRebootApplication";
    private  Context mContext;
    private SharedPreferences.Editor mEditor = null;
    private SharedPreferences mSharedPreferences = null;

    //public static final String
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mEditor = mSharedPreferences.edit();
    }

    public SharedPreferences getmSharedPreferences() {
        return mSharedPreferences;
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context context){
        this.mContext = context;
    }

    public void setmSharedPreferences(SharedPreferences shredPreferences) {
        this.mSharedPreferences = shredPreferences;
    }

    public SharedPreferences.Editor getmEditor() {
        return mEditor;
    }

    public void setmEditor(SharedPreferences.Editor mEditor) {
        this.mEditor = mEditor;
    }
    public boolean saveSharedPreferences(ArrayMap<String, Object> map){
        boolean result = false;
        if(mEditor == null){
            if(mSharedPreferences == null){
                mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            }
            if(mSharedPreferences != null){
                mEditor = mSharedPreferences.edit();
            } else {
                result = false;
                return result;
            }
        }
        if(mEditor != null){
            for(int i = 0; i < map.size(); i++){
                if(map.valueAt(i) instanceof Integer){
                    MyLogs.MyLogD(TAG, "saveSharedPreferences Integer !!!");
                    mEditor.putInt(map.keyAt(i), ((Integer)map.valueAt(i)).intValue());
                } else if(map.valueAt(i) instanceof Boolean){
                    MyLogs.MyLogD(TAG, "saveSharedPreferences Boolean !!!");
                    mEditor.putBoolean(map.keyAt(i), ((Boolean)map.valueAt(i)).booleanValue());
                } else if(map.valueAt(i) instanceof String){
                    MyLogs.MyLogD(TAG, "saveSharedPreferences String !!!");
                    mEditor.putString(map.keyAt(i), ((String)map.valueAt(i)).toString());
                } else if(map.valueAt(i) instanceof Float){
                    MyLogs.MyLogD(TAG, "saveSharedPreferences Float !!!");
                    mEditor.putFloat(map.keyAt(i), ((Float)map.valueAt(i)).floatValue());
                } else if(map.valueAt(i) instanceof Long){
                    MyLogs.MyLogD(TAG, "saveSharedPreferences Long !!!");
                    mEditor.putLong(map.keyAt(i), ((Long)map.valueAt(i)).longValue());
                }
            }
            result = mEditor.commit();
        } else {
            result = false;
        }
        return result;
    }
    public int getIntSharedPreferences(String key){
        int result = 0;
        if(mSharedPreferences == null){
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        }
        if(mSharedPreferences != null){
            result = mSharedPreferences.getInt(key, 0);
        }
        return result;
    }

    public boolean getBooleanSharedPreferences(String key){
        boolean result = false;
        if(mSharedPreferences == null){
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        }
        if(mSharedPreferences != null){
            result = mSharedPreferences.getBoolean(key, false);
        }
        return result;
    }
    public String getStringSharedPreferences(String key){
        String result = "";
        if(mSharedPreferences == null){
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        }
        if(mSharedPreferences != null){
            result = mSharedPreferences.getString(key, "");
        }
        return result;
    }
    public float getFloatSharedPreferences(String key){
        float result = 0f;
        if(mSharedPreferences == null){
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        }
        if(mSharedPreferences != null){
            result = mSharedPreferences.getFloat(key, 0f);
        }
        return result;
    }
    public long getLongSharedPreferences(String key){
        long result = 0L;
        if(mSharedPreferences == null){
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        }
        if(mSharedPreferences != null){
            result = mSharedPreferences.getLong(key, 0L);
        }
        return result;
    }

    public boolean clearSharedPreferences(){
        boolean result = false;
        if(mEditor == null){
            if(mSharedPreferences == null){
                mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            }
            if(mSharedPreferences != null){
                mEditor = mSharedPreferences.edit();
            } else {
                result = false;
                return result;
            }
        }
        if(mEditor != null){
            mEditor.clear();
            result = mEditor.commit();
        } else {
            result = false;
        }
        return result;
    }
}
