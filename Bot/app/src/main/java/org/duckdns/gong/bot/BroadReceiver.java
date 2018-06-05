package org.duckdns.gong.bot;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.widget.Toast;
import static org.duckdns.gong.bot.NotiService.ce;


public class BroadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            // 안드로이드 리부팅 시 서비스 자동 시작
            case "android.intent.action.BOOT_COMPLETED":
                ComponentName cName = new ComponentName(context.getPackageName(), NotiService.class.getName());
                context.startService(new Intent().setComponent(cName));
                break;
            // 와이파이 상태변화가 발생했을 경우
            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifi = wm.getConnectionInfo();
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    String wifiName = PreferenceManager.getDefaultSharedPreferences(context)
                            .getString("pref_key_wifi_name", "");
                    // 설정창에서 와이파이 이름이 입력 안된 경우를 체크
                    if ((wifiName.equals(""))) {
                        Toast.makeText(context, "설정창에서 와이파이 이름을 입력하세요", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        // 와이파이의 이름으로 서버와 같은 네트워크에 있는지 체크
                        if (wifi.getSSID().equals("\"" + wifiName + "\"")) {
                            ce.enterServer();
                        }
                    }
                } else if (networkInfo.getState() == NetworkInfo.State.DISCONNECTED && ce.getIsConnected()) {
                    ce.closeConnect();
                }
                break;
        }
    }
}
