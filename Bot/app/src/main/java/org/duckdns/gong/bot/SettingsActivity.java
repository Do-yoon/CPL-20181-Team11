package org.duckdns.gong.bot;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

/**
 * 실행환경
 * 기기 : 삼성 J5 2016, 운영체제 : 누가 7.1.2
 */

public class SettingsActivity extends AppCompatActivity {
    private Activity activity = this;
    private static final int MY_REQ_CODE = 7;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 액티비티가 시작되면 설정창을 실행
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();

        // 필수권한 체크
        checkPermission();
    }

    private void checkPermission() {
        // 필수권한 여부를 체크, 비활성화시 권한 요청
        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS, Manifest.permission.READ_CALENDAR}, MY_REQ_CODE);
        }
        else {
            // 알림 접근 권한을 체크, 비활성화시 권한 요청
            if (!Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners").contains(this.getApplicationContext().getPackageName())) {
                new AlertDialog.Builder(this)
                        .setTitle("권한")
                        .setMessage("이 어플을 사용하기 위해서는 알림 접근 권한이 필요합니다")
                        .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                            }
                        })
                        .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean checkPermission = false;
        int trueCount = 0;

        switch (requestCode) {
            case MY_REQ_CODE:
                for (int arrCount = 0; arrCount < grantResults.length; arrCount++) {
                    if (grantResults[arrCount] < 0) {
                        checkPermission = true;
                    } else {
                        trueCount++;
                    }
                }

                if (checkPermission) {
                    // 권한 요청 창을 다시는 안나오게 할 경우 권한 설정으로 이동하게 하는 다이얼로그를 띄움
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)
                            && !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)
                            && !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)
                            && !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALENDAR)) {
                        new AlertDialog.Builder(this)
                                .setTitle("권한")
                                .setMessage("이 어플을 사용하기 위해서는 모든 권한이 필요합니다")
                                .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(Uri.parse("package:" + activity.getPackageName()));
                                        startActivity(intent);
                                    }
                                })
                                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        android.os.Process.killProcess(android.os.Process.myPid());
                                    }
                                })
                                .setCancelable(false)
                                .create()
                                .show();
                    } else {
                        // 권한 요청 거절 시 권한을 승인할 때 까지 다이얼로그를 띄움
                        new AlertDialog.Builder(this)
                                .setTitle("권한")
                                .setMessage("이 어플을 사용하기 위해서는 모든 권한이 필요합니다")
                                .setNeutralButton("다시 시도", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS, Manifest.permission.READ_CALENDAR}, MY_REQ_CODE);
                                    }
                                })
                                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        android.os.Process.killProcess(android.os.Process.myPid());
                                    }
                                })
                                .setCancelable(false)
                                .create()
                                .show();
                    }
                }
                if (trueCount == 4) {
                    if (!Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners").contains(activity.getApplicationContext().getPackageName())) {
                        new AlertDialog.Builder(this)
                                .setTitle("권한")
                                .setMessage("이 어플을 사용하기 위해서는 알림 접근 권한이 필요합니다")
                                .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                                    }
                                })
                                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        android.os.Process.killProcess(android.os.Process.myPid());
                                    }
                                })
                                .setCancelable(false)
                                .create()
                                .show();
                    }
                }
                break;
        }
    }
}
