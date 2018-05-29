package org.duckdns.gong.bot;

import android.app.Activity;
import android.app.Notification;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotiService extends NotificationListenerService {
    static boolean isSameNetwork=false;
    static Client ce=new Client();

    @Override
    public void onCreate() {
        super.onCreate();
        ce.setContext(this);
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

        // 서버와 같은 네트워크에 있을 경우에만 실행
        if (isSameNetwork == true) {
            try {
                appName = (String) pm.getApplicationLabel(pm.getApplicationInfo
                        (packageName, PackageManager.GET_META_DATA));
                title = extras.getString(Notification.EXTRA_TITLE);

                // 알림이 온 어플 이름을 통해 설정에서 해당 어플이 허용되었는지 체크하는 과정
                if(this.getSharedPreferences("appNotificationEnabled", Activity.MODE_PRIVATE)
                        .getBoolean(appName,false)) {
                    if(appName.equals("Phone")) {
                        if(title.endsWith("Missed call") || title.endsWith("missed calls"))
                            ce.sendStr("setnoti " + appName + " " + title + " " + text.toString());
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
