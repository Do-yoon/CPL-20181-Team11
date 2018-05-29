package org.duckdns.gong.arduino;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private final String hostName = "gong.duckdns.org";
    private final int port = 13899;
    private Socket socket = null;
    private BufferedWriter bw;
    private BufferedReader br;

    // 서버와 연결
    public void enterServer() {
        try {
            socket = new Socket(InetAddress.getByName(hostName), port);
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            sendStr("iam aws");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnect() {
        try {
            bw.close();
            br.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 문자열 전송
    public void sendStr(final String msg) {
        try {
            bw.write(msg);
            bw.newLine();
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
