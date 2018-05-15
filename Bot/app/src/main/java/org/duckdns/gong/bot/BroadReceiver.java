package org.duckdns.gong.bot;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class BroadReceiver extends BroadcastReceiver {
    static boolean samenetwork=false;
    static Client ce=new Client();
    private WifiManager wm;

    @Override
    public void onReceive(Context context, Intent intent) {
        /* 안드로이드 리부팅 시 서비스 자동 시작 */
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            ComponentName cName = new ComponentName(context.getPackageName(), NotiService.class.getName());
            ComponentName svcName = context.startService(new Intent().setComponent(cName));

            if (svcName == null) {
                Log.e("BOOTSVC", "Could not start service " + cName.toString());
            }
        } else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            /* 와이파이 상태변화가 발생했을 경우  */
            wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifi = wm.getConnectionInfo();
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                // 와이파이의 이름으로 서버와 같은 네트워크에 있는지 체크
                if (wifi.getSSID().equals("\"GONG-2.4G\"")) {
                    samenetwork = true;
                    ce.setContext(context);
                    ce.enterServer();
                }
            } else if (networkInfo.getState() == NetworkInfo.State.DISCONNECTED && samenetwork) {
                samenetwork = false;
                ce.closeConnect();
            }
        }
    }
}
