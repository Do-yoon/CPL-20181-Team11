package org.duckdns.gong.gong;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.util.Log;

public class BroadRec extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            ComponentName cName = new ComponentName(context.getPackageName(), MyService.class.getName());
            ComponentName svcName = context.startService(new Intent().setComponent(cName));

            if (svcName == null) {
                Log.e("BOOTSVC", "Could not start service " + cName.toString());
            }
        }
    }
}
