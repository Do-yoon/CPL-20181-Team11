package org.duckdns.gong.gong;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MyService extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification noti = sbn.getNotification();
        Bundle extras = noti.extras;
        String title = extras.getString(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        final String packageName = sbn.getPackageName();

        if (!TextUtils.isEmpty(packageName) && packageName.equals("com.kakao.talk")) {
            Toast.makeText(this,"카카오톡 " + title + " : " + text.toString(), Toast.LENGTH_LONG).show();
            new Connect(this, "카카오톡 "+title+" "+text.toString()).enterServer();
        } else if (!TextUtils.isEmpty(packageName) && packageName.equals("com.samsung.android.messaging")) {
            Toast.makeText(this,"메시지 " + title + " : " + text.toString(), Toast.LENGTH_LONG).show();
            new Connect(this, "메시지 "+title+" "+text.toString()).enterServer();
        } else if (!TextUtils.isEmpty(packageName) && packageName.equals("com.samsung.android.incallui")) {
            Toast.makeText(this,"전화 " + title + " : " + text.toString(), Toast.LENGTH_LONG).show();
            new Connect(this, "전화 "+title+" "+text.toString()).enterServer();
        }
    }

    class Connect {
        private String ip = "192.168.1.11";
        private int port = 13499;
        private Socket socket=null;
        private Context temp;
        private String msg;

        public Connect(Context a, String msg) {
            temp = a;
            this.msg = msg;
        }

        public void enterServer() {
            new Thread() {
                public void run() {
                    try {
                        socket = new Socket(ip, port);

                        OutputStream os = socket.getOutputStream();
                        OutputStreamWriter osw = new OutputStreamWriter(os);
                        BufferedWriter bw = new BufferedWriter(osw);

                        bw.write(msg);
                        bw.newLine();
                        bw.flush();

                        bw.close();
                        osw.close();
                        os.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }
}
