package org.duckdns.gong.bot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Client {
    private Socket socket = null;
    private BufferedWriter bw = null;
    private BufferedReader br = null;
    private RequestHandler rh = null;
    private String request;
    private Context context;
    private boolean isConnected = false;

    public void setContext(Context context) {
        this.context = context;
        rh = new RequestHandler(context);
    }

    public boolean getIsConnected() {
        return isConnected;
    }

    public void enterServer() {
        new Thread() {
            public void run() {
                try {
                    // 와이파이가 연결될 때 이 메소드가 실행되므로 대기시간을 둔다
                    this.sleep(2000);

                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    String serverIp = sharedPref.getString("pref_key_server_ip", "");
                    String serverPort = sharedPref.getString("pref_key_server_port", "");
                    // 설정창에서 서버 아이피와 포트번호가 입력이 안된 경우를 체크
                    if(serverIp.equals("") || serverPort.equals("")) {
                        Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "기능을 사용하기 위해서 설정창에서 서버 아이피 및 포트번호를 입력하세요", Toast.LENGTH_LONG).show();
                            }
                        });
                        return;
                    } else {
                        // 설정창에서 입력받은 서버 아이피와 포트번호를 가져와서 소켓을 연결한다
                        socket = new Socket(serverIp, Integer.parseInt(serverPort));
                        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        sendStr("iam android");
                        isConnected = true;
                    }

                    // 서버로부터 알렉사의 요청을 무한정 기다림
                    while ((request = br.readLine()) != null) {
                        Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // 서버로부터 온 요청을 처리
                                rh.processReq(request);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    closeConnect();
                }
            }
        }.start();
    }

    public void sendStr(final String msg) {
        new Thread() {
            public void run() {
                try {
                    bw.write(msg);
                    bw.newLine();
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void sendStrs(final ArrayList<String> msgs) {
        new Thread() {
            public void run() {
                try {
                    for(String msg : msgs) {
                        bw.write(msg);
                        bw.newLine();
                        bw.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void closeConnect() {
        new Thread() {
            public void run() {
                isConnected = false;
                try {
                    if(br!=null)
                        br.close();
                    if(bw!=null)
                        bw.close();
                    if(socket!=null)
                        socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
