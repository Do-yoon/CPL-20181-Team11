package org.duckdns.gong.ask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class Client {
    private ArrayList<String> strings=new ArrayList<String>();
    private final String hostName = "gong.duckdns.org";
    private final int port = 13899;
    private Socket socket = null;
    private BufferedWriter bw;
    private BufferedReader br;
    private String redata;

    public ArrayList<String> getStrings() {
        return strings;
    }

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

    public void sendStr(final String msg) {
        try {
            bw.write(msg);
            bw.newLine();
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readStr() {
        try {
            // q1w2e3r4은 끝이라는 의미를 가지는 문자열이다
            while ((redata = br.readLine()) != null && !(redata.equals("q1w2e3r4"))) {
                // 알렉사가 문장에서 문장 넘어갈 때 너무 빨리 넘어가서 문장 사이에 텀을 1초로 설정
                // 읽어들인 결과들을 배열리스트에 추가
                strings.add(redata.concat("<break time=\"1s\"/>"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
