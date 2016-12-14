package com.ckt.cyclereboot;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

public class RebootCompletedReceiver extends BroadcastReceiver {
    private static final String ACTION_START_CYCLE_REBOOT = "com.ckttelecom.action.cyclereboot";
    private static final String TAG = "RebootCompletedReceiver";

    public RebootCompletedReceiver() {

    }
    public static Intent findApp(Context context, Intent intent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(intent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() < 1) {
            //return null;
            return intent;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        MyLogs.MyLogD(TAG, "findApp packageName = "+packageName);
        MyLogs.MyLogD(TAG, "findApp className = "+className);
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent foundIntent = new Intent(intent);

        // Set the component to be explicit
        foundIntent.setComponent(component);

        return foundIntent;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent it = new Intent(ACTION_START_CYCLE_REBOOT);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MyLogs.MyLogD(TAG, "onReceive ");
        Intent findIntent = new Intent(findApp(context,it));
        context.startActivity(findIntent);
    }
}
