import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// 같은 네트워크에 있는 디바이스들의 아이피와 맥어드레스를 arp-scan 기능을 이용하여 찾은 후 해시맵으로 관리
class Arpscan extends Thread {
    private HashMap<String, String> adrMap = new HashMap<String, String>();
    // 안드로이드와 연결되었는지 확인하기 위한 변수
    private boolean andCon=false;
    // 안드로이드와 연결된 소켓을 받아오기 위한 변수
    private Socket andSocket=null;

    public boolean checkIp(String ip) {
        for (String temp : adrMap.keySet()) {
            if (ip.equals(temp))
                return true;
        }
        return false;
    }

    public boolean checkMac(String mac) {
        for (String temp : adrMap.keySet()) {
            if (mac.equals(adrMap.get(temp)))
                return true;
        }
        return false;
    }

    public String getMac(String ip) {
        for (String temp : adrMap.keySet()) {
            if (ip.equals(temp))
                return adrMap.get(temp);
        }
        return null;
    }

    public String getIp(String mac) {
        for (String temp : adrMap.keySet()) {
            if (mac.equals(adrMap.get(temp)))
                return temp;
        }
        return null;
    }

    public void setAdrmap(String ip, String mac) {
        adrMap.put(ip, mac);
    }

    public void setAndcon(boolean andcon) {
        this.andCon = andcon;
    }

    public void setAndsocket(Socket andsocket) {
        this.andSocket = andsocket;
    }

    public void resetAdrmap() {
        adrMap.clear();
    }

    @Override
    public void run() {
        Process process = null;
        BufferedReader br = null;
        Runtime runtime = Runtime.getRuntime();
        final String COMMAND = "sudo arp-scan -I wlan0 -l";

        while (true) {
            try {
                process = runtime.exec(new String[]{"bash", "-c", COMMAND});
                br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String msg = null;
                int basepoint;

                resetAdrmap();
                // arp-scan 결과에서 아이피와 맥어드레스를 추출하여 해쉬맵에 저장
                while ((msg = br.readLine()) != null) {
                    if (msg.startsWith("192.168")) {
                        basepoint = msg.indexOf(":");
                        setAdrmap(msg.substring(0, basepoint - 3), msg.substring(basepoint - 2, basepoint + 15));
                        System.out.println(msg.substring(0, basepoint - 3)+" "+msg.substring(basepoint - 2, basepoint + 15));
                    }
                }
                System.out.println("");

                // 안드로이드 기기의 비정상적인 소켓 종료에 대처하기 위해 기기의 아이피가 내부 네트워크에 있는지 확인
                // 연결된 적이 있는데 내부 네트워크에 아이피가 없을 경우 기기와 연결된 소켓을 종료
                if(andCon) {
                    if(!(checkIp("192.168.1.47"))) {
                        andSocket.close();
                        andCon = false;
                    }
                }

                br.close();
                // 1분마다 돌아가도록 설정
                this.sleep(60000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

// 모바일에서 받아 온 알림을 배열로 관리하는 클래스
class Notification {
    private List<String> notiArray = new ArrayList<String>();

    public void addNotiarray(String noti) {
        notiArray.add(noti);
    }

    public void resetNoti() {
        notiArray.clear();
    }

    public List<String> getallNotiarray() {
        return notiArray;
    }
}

class Server {
    private Socket socket;
    private final int PORT;
    private ServerSocket serverSocket = null;
    private Notification notiArray = new Notification();
    // 안드로이드 클라이언트와 연결된 출력 스트림을 얻기 위하여 선언
    private HashMap<String, BufferedWriter> hm = new HashMap<String, BufferedWriter>();
    private Arpscan as;

    public Server(final int PORT, Arpscan as) {
        this.PORT = PORT;
        this.as = as;
    }

    public void openServer() {
        new Thread() {
            public void run() {
                try {
                    serverSocket = new ServerSocket(PORT);
                    System.out.println("서버 가동");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    while (true) {
                        socket = serverSocket.accept();
                        System.out.println(socket.getInetAddress() + "로부터 연결요청");
                        ServerThread st = new ServerThread(socket);
                        // 다수의 클라이언트와 통신하기 위해서 쓰레드 생성
                        st.readStr();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (socket != null)
                            socket.close();
                        if (serverSocket != null)
                            serverSocket.close();
                        System.out.println("서버 종료");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    // 다수의 클라이언트와 통신하기 위한 클래스
    class ServerThread {
        private String redata, request;
        private Socket threadSocket;
        private BufferedReader br = null;
        private BufferedWriter bw = null;

        public ServerThread(Socket threadsocket) {
            this.threadSocket=threadsocket;
        }

        public void readStr() {
            new Thread() {
                public void run() {
                    try {
                        br = new BufferedReader(new InputStreamReader(threadSocket.getInputStream()));
                        bw = new BufferedWriter(new OutputStreamWriter(threadSocket.getOutputStream()));

                        // 안드로이드 클라이언트의 내부 고정아이피를 이용해 안드로이드와 연결된 출력 스트림을 획득*/
                        if (threadSocket.getInetAddress().toString().equals("/192.168.1.47")) {
                            hm.put("android", bw);
                            as.setAndcon(true);
                            as.setAndsocket(threadSocket);
                        }

                    /* 연결된 클라이언트로 부터 문자열을 기다림
                       첫 단어가 setnoti이면 안드로이드로 부터 온 알림을 배열에 저장
                       req이면 ASW로 부터 온 요청을 안드로이드로 전달
                       getnoti이면 서버에 저장된 알림을 ASW로 전달
                       arduino이면 아두이노 내장 LED를 제어 */
                        while ((redata = br.readLine()) != null) {
                            request = redata.split(" ")[0];

                            switch (request) {
                                case "setnoti":
                                    notiArray.addNotiarray(redata.substring(redata.indexOf(" ") + 1));
                                    break;
                                case "getnoti":
                                    notiArray.addNotiarray("q1w2e3r4");
                                    writeStr(notiArray);
                                    break;
                                case "req":
                                    writeStr(redata.substring(redata.indexOf(" ") + 1), hm.get("android"));
                                    break;
                                case "arduino":
                                    if (redata.split(" ")[1].equals("on")) {
                                        String command = "sudo echo -e -n \"y\" > /dev/ttyACM0";
                                        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
                                    } else {
                                        String command = "sudo echo -e -n \"n\" > /dev/ttyACM0";
                                        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
                                    }
                                    break;
                            }

                            System.out.println(redata);
                        }

                        System.out.println(threadSocket.getInetAddress() + "로부터 연결종료(null)");
                        br.close();
                        bw.close();
                        threadSocket.close();
                    } catch (IOException e) {
                        System.out.println(threadSocket.getInetAddress() + "로부터 연결종료(IOException)");
                        hm.clear();
                    }
                }
            }.start();
        }

        // ASW로 부터 온 요청을 안드로이드로 전송
        public void writeStr(final String redata, final BufferedWriter dest) {
            new Thread() {
                public void run() {
                    try {
                        dest.write(redata);
                        dest.newLine();
                        dest.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }.start();
        }

        // 현재까지 온 알림들을 ASW로 전송
        public void writeStr(final Notification redata) {
            new Thread() {
                public void run() {
                    try {
                        for (String temp : redata.getallNotiarray()) {
                            bw.write(temp);
                            bw.newLine();
                            bw.flush();
                        }
                        redata.resetNoti();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }.start();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        final int PORT = 13899;
        Arpscan as = new Arpscan();
        Server server = new Server(PORT, as);

        as.start();
        server.openServer();
    }
}
