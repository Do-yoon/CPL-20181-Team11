package org.duckdns.gong.ask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private List<String> notiArray=new ArrayList<String>();
    private final String hostName = "gong.duckdns.org";
    private final int port = 13899;
    private Socket socket = null;
    private BufferedWriter bw;
    private BufferedReader br;
    private String redata;

    public List<String> getNotiarray() {
        return notiArray;
    }

    public void enterServer() {
        try {
            socket = new Socket(InetAddress.getByName(hostName), port);
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
                notiArray.add(redata);
            }
            if(redata.equals("q1w2e3r4") && notiArray.size()==0)
                notiArray.add("There is no notification");
            else
                notiArray.add(0,String.format("There is %d notification",notiArray.size()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
