package org.duckdns.gong.bot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import java.util.regex.Pattern;

public class RequestHandler extends Activity {
    private String func;
    private String who;
    private String content;
    private Context context;

    public RequestHandler(String request, Context context) {
        this.context=context;
        func=request.split(" ")[0];

        /* 전화 요청일 경우 */
        if(func.equals("call")) {
            who=request.split(" ")[1];
            callFunc(who);
        } else if(func.equals("message")) {
            /* 메세지 요청일 경우 */
            who=request.split(" ")[1];
            content=request.split(" ")[2];
            messageFunc(who,content);
        }
    }

    protected void callFunc(String who) {
        String num;

        /* 보내는 사람이 이름인지 전화번호 인지를 구별하여 이름일 경우 전화번호로 변경 */
        if (Pattern.matches("^[a-zA-Z]*$", who)) {
            num = numberByName(who);
        } else if (Pattern.matches("^[0-9]*$", who)) {
            num = who;
        } else {
            num = null;
        }

        /* 스피커폰으로 전화를 실행 */
        //AudioManager audioManager = (AudioManager)context.getSystemService(context.AUDIO_SERVICE);
        //audioManager.setMode(AudioManager.MODE_IN_CALL);
        //audioManager.setSpeakerphoneOn(true);
        context.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + num)));
    }

    protected void messageFunc(String who, String content) {
        String num;

        if (Pattern.matches("^[a-zA-Z]*$", who)) {
            num = numberByName(who);
        } else if (Pattern.matches("^[0-9]*$",who)) {
            num = who;
        } else {
            num = null;
        }

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(num, null, content, null, null);
    }

    /* 보내는 사람이 이름으로 왔을 경우 연락처를 조회하여 전화번호로 변경하는 메소드 */
    protected String numberByName(String name) {
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
}
