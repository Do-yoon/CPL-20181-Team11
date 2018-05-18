package org.duckdns.gong.bot;

import android.app.Notification;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotiService extends NotificationListenerService {

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
        if (BroadReceiver.sameNetwork == true) {
            try {
                appName = (String) pm.getApplicationLabel(pm.getApplicationInfo
                        (packageName, PackageManager.GET_META_DATA));
                title = extras.getString(Notification.EXTRA_TITLE);

                // 서버로 보내는 문자열을 띄어쓰기를 통하여 어플 이름, 타이틀, 내용을 구분
                // setnoti 문자열을 추가시킨 이유는 알림이라는 것을 서버에 알리기 위해서
                message = "setnoti " + appName + " " + title + " " + text.toString();
                BroadReceiver.ce.sendStr(message);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
