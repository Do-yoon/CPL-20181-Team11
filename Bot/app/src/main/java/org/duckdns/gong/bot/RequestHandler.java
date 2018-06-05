package org.duckdns.gong.bot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.widget.Toast;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class RequestHandler {
    private Context context;
    // 간결한 코드를 위해 요일 열거형 선언
    private enum DayOfWeek {
        SUNDAY(1), MONDAY(2), TUESDAY(3), WEDNESDAY(4), THURSDAY(5), FRIDAY(6), SATURDAY(7);

        private final int value;

        DayOfWeek(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public RequestHandler(Context context) {
        this.context = context;
    }

    public void processReq(String request) {
        String func = request.split(" ")[0];

        switch (func) {
            // 전화 발신요청일 경우
            case "call":
                callFunc(request.split(" ")[1]);
                break;
            // 메세지 전송요청일 경우
            case "message":
                messageFunc(request.split(" ")[1], request.substring(request.indexOf(" ", request.indexOf(" ") + 1) + 1));
                break;
            // Wake on lan 요청일 경우
            case "wol":
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                // 설정창에서 Wake on lan 사용을 허용했는지 확인하는 과정
                if (sharedPref.getBoolean("pref_key_wol_whether", false)) {
                    wolFunc(sharedPref.getString("pref_key_wol_ip", "")
                            , sharedPref.getString("pref_key_wol_mac", ""));
                    break;
                } else {
                    Toast.makeText(context, "설정창에서 Wake on lan 사용을 켜주세요", Toast.LENGTH_LONG).show();
                }
                // 타이머 설정요청일 경우
            case "timer":
                timerFunc(request.split(" ")[1]);
                break;
            // 알람 설정요청일 경우
            case "alarm":
                alarmFunc(request.split(" ")[1]);
                break;
            // 반복알람 설정요청일 경우
            case "repeatalarm":
                alarmFunc(request.split(" ")[1], request.substring(request.indexOf(" ", request.indexOf(" ") + 1) + 1));
                break;
            // 음악 제어요청일 경우
            case "music":
                musicFunc(request.split(" ")[1]);
                break;
            // 일정 전송요청일 경우
            case "calendar":
                calendarFunc(request.substring(request.indexOf(" ") + 1));
                break;
            default:
                Toast.makeText(context, "해당기능은 구현이 되어있지 않습니다", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void callFunc(String who) {
        String num;

        // 보내는 사람이 이름인지 전화번호 인지를 구별하여 이름일 경우 전화번호로 변경
        if (Pattern.matches("^[a-zA-Z]*$", who)) {
            // numberByName 메소드는 연락처에 이름이 없는 경우 null을 반환. null일 경우 Toast메시지를 띄우고 종료
            if ((num = numberByName(who)) == null) {
                Toast.makeText(context, "연락처에 등록되지 않은 사용자입니다", Toast.LENGTH_LONG).show();
                return;
            }
        } else if (Pattern.matches("^[0-9]*$", who)) {
            num = who;
        } else {
            Toast.makeText(context, "식별할 수 있는 형식이 아닙니다", Toast.LENGTH_LONG).show();
            return;
        }

        context.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + num)));
    }

    private void messageFunc(String who, String content) {
        String num;

        if (Pattern.matches("^[a-zA-Z]*$", who)) {
            // numberByName 메소드는 연락처에 이름이 없는 경우 null을 반환. null일 경우 Toast 메시지를 띄우고 종료
            if ((num = numberByName(who)) == null) {
                Toast.makeText(context, "연락처에 등록되지 않은 사용자입니다", Toast.LENGTH_LONG).show();
                return;
            }
        } else if (Pattern.matches("^[0-9]*$", who)) {
            num = who;
        } else {
            Toast.makeText(context, "식별할 수 있는 형식이 아닙니다", Toast.LENGTH_LONG).show();
            return;
        }

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(num, null, content, null, null);
    }

    private void wolFunc(String wolIp, String wolMac) {
        final int port = 9;
        byte[] macParts = bytesByMac(wolMac);
        byte[] bytes = new byte[6 + 16 * macParts.length];

        for (int arrCount = 0; arrCount < 6; arrCount++) {
            bytes[arrCount] = (byte) 0xff;
        }
        // 매직패킷 형식을 만드는 반복문
        for (int count = 6; count < bytes.length; count += macParts.length) {
            System.arraycopy(macParts, 0, bytes, count, macParts.length);
        }

        try {
            InetAddress address = InetAddress.getByName(wolIp);
            DatagramPacket dgPacket = new DatagramPacket(bytes, bytes.length, address, port);
            DatagramSocket dgSocket = new DatagramSocket();
            dgSocket.send(dgPacket);
            dgSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void timerFunc(String duration) {
        Intent timerIntent = new Intent(AlarmClock.ACTION_SET_TIMER);

        timerIntent.putExtra(AlarmClock.EXTRA_LENGTH, Integer.parseInt(duration));
        timerIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        context.startActivity(timerIntent);
    }

    private void alarmFunc(String time) {
        int hour, minute;
        Intent alarmIntent = new Intent(AlarmClock.ACTION_SET_ALARM);

        // 시와 분을 분리하여 정수로 변환
        if (time.contains(":")) {
            hour = Integer.parseInt(time.split(":")[0]);
            minute = Integer.parseInt(time.split(":")[1]);
        } else {
            Toast.makeText(context, "입력된 시간의 형식이 잘못되었습니다", Toast.LENGTH_LONG).show();
            return;
        }

        alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, hour);
        alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
        alarmIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);

        context.startActivity(alarmIntent);
    }

    private void alarmFunc(String time, String day) {
        int hour, minute;
        ArrayList<Integer> days = new ArrayList<Integer>();
        Intent alarmIntent = new Intent(AlarmClock.ACTION_SET_ALARM);

        if (time.contains(":")) {
            hour = Integer.parseInt(time.split(":")[0]);
            minute = Integer.parseInt(time.split(":")[1]);
        } else {
            Toast.makeText(context, "입력된 시간의 형식이 잘못되었습니다", Toast.LENGTH_LONG).show();
            return;
        }

        // 반복요청한 요일이 여러개일 경우
        if (day.contains(" ")) {
            for (String temp : day.split(" ")) {
                DayOfWeek dow;
                try {
                    dow = DayOfWeek.valueOf(temp.toUpperCase());
                // 열거형에 없는 문자열이 들어올 경우 Toast 메시지를 띄우고 종료
                } catch (IllegalArgumentException e) {
                    Toast.makeText(context, "요일이 잘못되었습니다", Toast.LENGTH_LONG).show();
                    return;
                }
                days.add(dow.getValue());
            }
        // 반복요청한 요일이 하나일 경우
        } else {
            DayOfWeek dow;
            try {
                dow = DayOfWeek.valueOf(day.toUpperCase());
            // 열거형에 없는 문자열이 들어올 경우 Toast 메시지를 띄우고 종료
            } catch (IllegalArgumentException e) {
                Toast.makeText(context, "요일이 잘못되었습니다", Toast.LENGTH_LONG).show();
                return;
            }
            days.add(dow.getValue());
        }

        alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, hour);
        alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
        alarmIntent.putExtra(AlarmClock.EXTRA_DAYS, days);
        alarmIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);

        context.startActivity(alarmIntent);
    }

    // 키 이벤트를 통하여 키가 눌러진 것 처럼 만들어서 음악을 제어
    private void musicFunc(String control) {
        long eventTime = SystemClock.uptimeMillis();
        Intent musicIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent keyEvent;

        switch (control) {
            case "play":
            case "pause":
                keyEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
                musicIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
                context.sendOrderedBroadcast(musicIntent, null);
                break;
            case "next":
                keyEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
                musicIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
                context.sendOrderedBroadcast(musicIntent, null);
                break;
            case "previous":
                keyEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0);
                musicIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
                context.sendOrderedBroadcast(musicIntent, null);
                context.sendOrderedBroadcast(musicIntent, null);
                break;
        }
    }

    @SuppressLint("MissingPermission")
    private void calendarFunc(String calendarData) {
        String dateStr;
        Cursor cursor;
        Date startOfDate, endOfDate;
        ArrayList<String> calEvents = new ArrayList<String>();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat allFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try {
            dateStr = calendarData.substring(calendarData.indexOf(" ") + 1);
            startOfDate = allFormat.parse(dateStr + " 00:00");
            endOfDate = allFormat.parse(dateStr + " 23:59");
            // 입력받은 날짜에 캘린더 이벤트 시작시간이 있는 것들을 선택
            String selection = "(( " + CalendarContract.Events.DTSTART + ">=" + startOfDate.getTime() + ") and ("
                    + CalendarContract.Events.DTSTART + "<= " + endOfDate.getTime() + "))";
            // 캘린더 이벤트의 제목과 시작시간과 끝나는시간과 종일 이벤트여부를 추출
            String[] projection = {
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND,
                    CalendarContract.Events.ALL_DAY
            };

            // query를 통하여 원하는 정보를 추출
            cursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI,
                    projection, selection, null, CalendarContract.Events.DTSTART + " ASC");

            // 추출한 정보를 형식에 맞는 문자열로 재구성
            while (cursor.moveToNext()) {
                long startTime = 0;
                long endTime = 0;

                startTime = cursor.getLong(1);
                endTime = cursor.getLong(2);
                // 종일 이벤트일 경우 시작시간과 끝나는시간에 시스템의 타임존이 더해지므로 타임존을 빼준다
                if (cursor.getInt(3) != 0) {
                    TimeZone tz = TimeZone.getDefault();
                    if (dateFormat.format(startTime - tz.getOffset(startTime))
                            .equals(dateFormat.format(endTime - (tz.getOffset(endTime) + 60000)))) {
                        calEvents.add("setcal " + cursor.getString(0) + " in all day");
                    } else {
                        calEvents.add("setcal " + cursor.getString(0) + " till "
                                + dateFormat.format(endTime));
                    }
                } else {
                    if (dateFormat.format(cursor.getLong(1)).equals(dateFormat.format(cursor.getLong(2)))) {
                        calEvents.add("setcal " + cursor.getString(0) + " between "
                                + timeFormat.format(startTime) + " and "
                                + timeFormat.format(endTime));
                    } else {
                        calEvents.add("setcal " + cursor.getString(0) + " between "
                                + timeFormat.format(startTime) + " and "
                                + allFormat.format(endTime));
                    }
                }
            }

            // 끝을 알리는 문자열
            calEvents.add("setcal end q1w2e3r4");
            NotiService.ce.sendStrs(calEvents);
            cursor.close();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // 보내는 사람이 이름으로 왔을 경우 연락처를 조회하여 전화번호로 변경하는 메소드
    private String numberByName(String name) {
        String num = null;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like'%" + name + "%'";
        // 연락처에서 전화번호를 추출
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null);

        if (cursor.moveToFirst()) {
            num = cursor.getString(0);
        }

        cursor.close();
        return num;
    }

    // 문자열로된 맥어드레스를 16진수 정수로 바꿔주는 메소드
    private byte[] bytesByMac(String mac) {
        byte[] bytes = new byte[6];
        String[] macParts = mac.split("(\\:|\\-)");

        for (int count = 0; count < 6; count++) {
            bytes[count] = (byte) Integer.parseInt(macParts[count], 16);
        }

        return bytes;
    }
}
