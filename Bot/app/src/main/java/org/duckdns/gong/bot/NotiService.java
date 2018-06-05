package org.duckdns.gong.bot;

import android.app.Activity;
import android.app.Notification;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotiService extends NotificationListenerService {
    static Client ce=new Client();
    private SharedPreferences appNotifcationPref;

    @Override
    public void onCreate() {
        super.onCreate();
        ce.setContext(this);
        appNotifcationPref=this.getSharedPreferences("appNotificationEnabled", Activity.MODE_PRIVATE);
    }

    @Override
    // 알림이 올 경우 실행
    public void onNotificationPosted(StatusBarNotification sbn) {
        final String packageName = sbn.getPackageName();
        final PackageManager pm = getApplicationContext().getPackageManager();
        Notification noti = sbn.getNotification();
        Bundle extras = noti.extras;
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        String title, appName, message;

        // 서버에 연결된 경우에만 실행
        if (ce.getIsConnected()) {
            try {
                appName = (String) pm.getApplicationLabel(pm.getApplicationInfo
                        (packageName, PackageManager.GET_META_DATA));
                title = extras.getString(Notification.EXTRA_TITLE);

                // 알림이 온 어플 이름을 통해 설정에서 해당 어플이 허용되었는지 체크하는 과정
                if(appNotifcationPref.getBoolean(appName,false)) {
                    if(appName.equals("Phone")) {
                        String callText = text.toString();
                        // 전화 어플 발신 알림과 아닐 경우 서버로 전송
                        if(!(callText.equals("Ongoing call") || callText.equals("Dialing")))
                            ce.sendStr("setnoti " + appName + " " + title + " " + callText);
                    }
                    else {
                        // 서버로 보내는 문자열을 띄어쓰기를 통하여 어플 이름, 타이틀, 내용을 구분
                        // 보내는 문자열이 알림이라는 것을 알리기 위해서 setnoti 추가
                        message = "setnoti " + appName + " " + title + " " + text.toString();
                        ce.sendStr(message);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
