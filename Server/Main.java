import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 실행환경
 * 기기 : 라즈베리파이3B, 운영체제 : 라즈비안 스트레치
 * 사용한 라즈비안 패키지 : arp-scan
 */

// 같은 네트워크에 있는 디바이스들의 아이피와 맥어드레스를 arp-scan 기능을 이용하여 찾은 후 해쉬맵으로 관리
class DevicesScan extends Thread {
    private HashMap<String, String> adrMap = new HashMap<String, String>();
    // 안드로이드와의 연결을 관리하기 위한 세가지 변수
    private boolean isConnected = false;
    private Socket connectedSocket = null;
    private String androidIp;

    public void addAdrmap(String ip, String mac) {
        adrMap.put(ip, mac);
    }

    public void setAndroidIp(String androidIp) {
        this.androidIp = androidIp;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public void setConnectedSocket(Socket connectedSocket) {
        this.connectedSocket = connectedSocket;
    }

    public boolean checkIp(String ip) {
        for (String storedIp : adrMap.keySet()) {
            if (ip.equals(storedIp))
                return true;
        }
        return false;
    }

    public void resetAdrmap() {
        adrMap.clear();
    }

    @Override
    public void run() {
        int basepoint;
        Process process;
        BufferedReader br;
        String shellResult, ip, mac;
        Runtime runtime = Runtime.getRuntime();
        // 실행할 쉘 명령어
        final String COMMAND = "sudo arp-scan -l";

        while (true) {
            try {
                process = runtime.exec(new String[]{"bash", "-c", COMMAND});
                br = new BufferedReader(new InputStreamReader(process.getInputStream()));

                resetAdrmap();
                System.out.println("---------------------------------------");
                // arp-scan 결과에서 아이피와 맥어드레스를 추출하여 해쉬맵에 저장
                while ((shellResult = br.readLine()) != null) {
                    if (shellResult.startsWith("192.168")) {
                        basepoint = shellResult.indexOf(":");
                        ip = shellResult.substring(0, basepoint - 3);
                        mac = shellResult.substring(basepoint - 2, basepoint + 15);
                        addAdrmap(ip, mac);
                        System.out.println(ip + " " + mac);
                    }
                }
                System.out.println("---------------------------------------");
                // 안드로이드 기기의 비정상적인 소켓 종료에 대처하기 위해 기기의 아이피가 내부 네트워크에 있는지 확인
                // 연결된 적이 있는데 내부 네트워크에 아이피가 없을 경우 기기와 연결된 소켓을 종료
                if (isConnected) {
                    if (!(checkIp(androidIp))) {
                        connectedSocket.close();
                        isConnected = false;
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

class Server {
    private Socket socket;
    private static final int PORT = 13899;
    private ServerSocket serverSocket = null;
    private DevicesScan arpScan = new DevicesScan();
    private ArrayList<String> calEvents = new ArrayList<String>();
    private ArrayList<String> notifications = new ArrayList<>();
    private HashMap<String, String> ips = new HashMap<String, String>();
    private HashMap<String, BufferedWriter> bws = new HashMap<String, BufferedWriter>();

    public Server() {
        arpScan.start();
        openServer();
    }

    public void openServer() {
        new Thread() {
            public void run() {
                try {
                    serverSocket = new ServerSocket(PORT);
                    System.out.println("서버 가동");

                    while (true) {
                        socket = serverSocket.accept();
                        System.out.println(socket.getInetAddress() + "로부터 연결요청");
                        // 다수의 클라이언트와 통신하기 위해서 쓰레드 생성
                        ServerThread st = new ServerThread(socket);
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
        private String msg, request;
        private Socket threadSocket;
        private BufferedReader br = null;
        private BufferedWriter bw = null;

        public ServerThread(Socket threadSocket) {
            this.threadSocket = threadSocket;
            readStr();
        }

        public void readStr() {
            new Thread() {
                public void run() {
                    try {
                        br = new BufferedReader(new InputStreamReader(threadSocket.getInputStream()));
                        bw = new BufferedWriter(new OutputStreamWriter(threadSocket.getOutputStream()));

                        while ((msg = br.readLine()) != null) {
                            request = msg.split(" ")[0];

                            switch (request) {
                                case "iam":
                                    // 다음 문자열을 확인하여 클라이언트의 아이피와 출력스트림을 해쉬맵에 저장
                                    if (msg.split(" ")[1].equals("android")) {
                                        bws.put("android", bw);
                                        ips.put("android", threadSocket.getInetAddress().toString());
                                        arpScan.setIsConnected(true);
                                        arpScan.setAndroidIp(threadSocket.getInetAddress().toString().substring(1));
                                        arpScan.setConnectedSocket(threadSocket);
                                    } else if (msg.split(" ")[1].equals("aws")) {
                                        bws.put("aws", bw);
                                        ips.put("aws", threadSocket.getInetAddress().toString());
                                    }
                                    break;
                                case "setnoti":
                                    // 안드로이드로 부터 온 알림을 배열리스트에 저장
                                    notifications.add(msg.substring(msg.indexOf(" ") + 1));
                                    break;
                                case "getnoti":
                                    // 서버에 저장된 알림을 ASW로 전달
                                    notifications.add("q1w2e3r4");
                                    writeStr(notifications, bw);
                                    break;
                                case "req":
                                    // ASW로 부터 온 요청을 안드로이드로 전달
                                    writeStr(msg.substring(msg.indexOf(" ") + 1), bws.get("android"));
                                    break;
                                case "arduino":
                                    // 아두이노 내장 LED를 제어
                                    if (msg.split(" ")[1].equals("on")) {
                                        String command = "sudo echo -e -n \"y\" > /dev/ttyACM0";
                                        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
                                    } else {
                                        String command = "sudo echo -e -n \"n\" > /dev/ttyACM0";
                                        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
                                    }
                                    break;
                                case "setcal":
                                    // 캘린더 이벤트들을 배열리스트에 저장한 다음 끝을 알리는 q1w2e3r4가 올 경우 AWS로 전달
                                    if (msg.split(" ")[2].equals("q1w2e3r4")) {
                                        calEvents.add("q1w2e3r4");
                                        writeStr(calEvents, bws.get("aws"));
                                    } else {
                                        calEvents.add(msg.substring(msg.indexOf(" ") + 1));
                                    }
                                    break;

                            }
                            System.out.println(msg);
                        }

                        System.out.println(threadSocket.getInetAddress() + "로부터 연결종료(null)");
                        // 연결종료하는 클라이언트를 구별하여 저장해두었던 출력스트림과 아이피를 삭제
                        removeMapEntry(threadSocket.getInetAddress().toString());
                        br.close();
                        bw.close();
                        threadSocket.close();
                    } catch (IOException e) {
                        System.out.println(threadSocket.getInetAddress() + "로부터 연결종료(IOException)");
                        removeMapEntry(threadSocket.getInetAddress().toString());
                    }
                }
            }.start();
        }

        // 문자열을 목적지로 전송
        public void writeStr(final String msg, final BufferedWriter destBw) {
            new Thread() {
                public void run() {
                    try {
                        destBw.write(msg);
                        destBw.newLine();
                        destBw.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }.start();
        }

        // 문자열들을 목적지로 전송
        public void writeStr(final ArrayList<String> msgs, final BufferedWriter destBw) {
            new Thread() {
                public void run() {
                    try {
                        for (String msg : msgs) {
                            destBw.write(msg);
                            destBw.newLine();
                            destBw.flush();
                        }
                        msgs.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }.start();
        }

        public void removeMapEntry(String ip) {
            if (ip.equals(ips.get("android"))) {
                bws.remove("android");
                ips.remove("android");
            } else if (ip.equals(ips.get("aws"))) {
                bws.remove("aws");
                ips.remove("aws");
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
    }
}
