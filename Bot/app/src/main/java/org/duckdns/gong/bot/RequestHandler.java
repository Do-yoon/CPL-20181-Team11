package org.duckdns.gong.bot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Pattern;

public class RequestHandler extends Activity {
    private String func, who, content;
    private Context context;

    public RequestHandler(Context context) {
        this.context = context;
    }

    public void processReq(String request) {
        func = request.split(" ")[0];

        switch (func) {
            // 전화 요청일 경우
            case "call":
                who = request.split(" ")[1];
                callFunc(who);
                break;
            // 메세지 요청일 경우
            case "message":
                who = request.split(" ")[1];
                content = request.substring(request.indexOf(" ", request.indexOf(" ") + 1) + 1);
                messageFunc(who, content);
                break;
            // Wake on lan 요청일 경우
            case "wol":
                wolFunc();
                break;
        }
    }

    private void callFunc(String who) {
        String num;

        // 보내는 사람이 이름인지 전화번호 인지를 구별하여 이름일 경우 전화번호로 변경
        if (Pattern.matches("^[a-zA-Z]*$", who)) {
            num = numberByName(who);
        } else if (Pattern.matches("^[0-9]*$", who)) {
            num = who;
        } else {
            num = null;
        }

        // 스피커폰으로 전화를 실행
        //AudioManager audioManager = (AudioManager)context.getSystemService(context.AUDIO_SERVICE);
        //audioManager.setMode(AudioManager.MODE_IN_CALL);
        //audioManager.setSpeakerphoneOn(true);
        context.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + num)));
    }

    private void messageFunc(String who, String content) {
        String num;

        if (Pattern.matches("^[a-zA-Z]*$", who)) {
            num = numberByName(who);
        } else if (Pattern.matches("^[0-9]*$", who)) {
            num = who;
        } else {
            num = null;
        }

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(num, null, content, null, null);
    }

    private void wolFunc() {
        String broadIp = "192.168.1.255";
        String mac = "28-D2-44-12-84-02";
        int port = 9;
        byte[] macParts = bytesByMac(mac);
        byte[] bytes = new byte[6 + 16 * macParts.length];

        for (int count = 0; count < 6; count++) {
            bytes[count] = (byte) 0xff;
        }
        for (int count = 6; count < bytes.length; count += macParts.length) {
            System.arraycopy(macParts, 0, bytes, count, macParts.length);
        }

        try {
            InetAddress address = InetAddress.getByName(broadIp);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 보내는 사람이 이름으로 왔을 경우 연락처를 조회하여 전화번호로 변경하는 메소드
    private String numberByName(String name) {
        String num = null;
        String sel = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like'%" + name + "%'";
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, sel, null, null);

        if (c.moveToFirst()) {
            num = c.getString(0);
        }
        c.close();
        return num;
    }

    // 문자열로된 맥어드레스를 16진수 정수로 바꿔주는 메소드
    private byte[] bytesByMac(String mac) {
        byte[] bytes = new byte[6];
        String[] macParts = mac.split("-");

        for (int count = 0; count < 6; count++) {
            bytes[count] = (byte) Integer.parseInt(macParts[count], 16);
        }

        return bytes;
    }
}
