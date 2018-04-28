package org.duckdns.gong.bot;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Client {
    private final String SERVER_IP = "192.168.1.112";
    private final int SERVER_PORT = 13899;
    private Socket socket = null;
    private BufferedWriter bw = null;
    private BufferedReader br = null;
    private RequestHandler rh = null;
    private String request;
    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    public void enterServer() {
        new Thread() {
            public void run() {
                try {
                    socket = new Socket(SERVER_IP, SERVER_PORT);
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    /* 알렉사의 요청(전화, 메세지)을 무한정 기다림 */
                    while ((request = br.readLine()) != null) {
                        Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                rh = new RequestHandler(request, context);              // 알렉사의 요청을 처리하기 위해 인스턴스 생성
                            }
                        }, 0);
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

    public void closeConnect() {
        new Thread() {
            public void run() {
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
