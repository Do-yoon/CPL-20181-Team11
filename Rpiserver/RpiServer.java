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


/* 모바일에서 받아 온 알림을 ArrayList로 관리하는 클래스 */
class Notification {
    private List<String> notiarray = new ArrayList<String>();

    public void addNotiarray(String noti) {
        notiarray.add(noti);
    }

    public void resetNoti() {
        notiarray.clear();
    }

    public List<String> getallNotiarray() {
        return notiarray;
    }
}


class Server {
    private Socket socket;
    private final int PORT;
    private ServerSocket serverSocket = null;
    private Notification notiarray = new Notification();
    private HashMap<String, BufferedWriter> hm=new HashMap<String, BufferedWriter>();       // 안드로이드 클라이언트의 출력 스트림을 얻기 위하여 선언

    public Server(final int PORT) {
        this.PORT = PORT;
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
                        ServerThread st = new ServerThread();          // 여러명의 클라이언트를 처리하기 위하여 쓰레드 생성
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

    /* 다수의 클라이언트와 통신을 위한 내부 클래스. 클라이언트 마다 쓰레드를 생성 */
    class ServerThread {
        private String redata;
        private BufferedReader br = null;
        private BufferedWriter bw = null;

        public void readStr() {
            new Thread() {
                public void run() {
                    try {
                        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                        /* 안드로이드 클라이언트의 내부 아이피를 이용해 안드로이드의 출력 스트림을 획득*/
                        if(socket.getInetAddress().toString().equals("/192.168.1.47")) {
                            hm.put("android", bw);
                        }

                    /* 클라이언트(안드로이드, ASW)로 부터 문자열을 기다림
                    첫 단어가 setnoti이면 안드로이드로 부터 온 알림
                    req이면 ASW로 부터 온 요청(전화, 메세지 전송)
                    getnoti이면 서버에 저장된 알림을 ASW로 보내달라는 요청*/
                        while ((redata = br.readLine()) != null) {
                            if (redata.split(" ")[0].equals("setnoti")) {
                                notiarray.addNotiarray(redata.substring(redata.indexOf(" ") + 1));
                            } else if (redata.split(" ")[0].equals("req")) {
                                writeStr(redata.substring(redata.indexOf(" ") + 1),hm.get("android"));
                            } else if (redata.split(" ")[0].equals("getnoti")) {
                                notiarray.addNotiarray("q1w2e3r4");
                                writeStrarr(notiarray);
                            } else if (redata == null) {
                                System.out.println(socket.getInetAddress() + "로부터 연결종료(null)");
                                break;
                            }
                            System.out.println(redata);
                        }
                    } catch (IOException e) {
                        System.out.println(socket.getInetAddress() + "로부터 연결종료(IOException)");
                    } finally {
                        try {
                            System.out.println(socket.getInetAddress() + "로부터 연결종료");
                            br.close();
                            bw.close();
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }.start();
        }

        /* ASW로 부터 온 요청을 안드로이드로 전송 */
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

        /* 현재까지 온 알림들을 ASW로 전송 */
        public void writeStrarr(final Notification redata) {
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

public class RpiServer {
    public static void main(String[] args) {
        final int SERVER_PORT=13899;
        Server server = new Server(SERVER_PORT);
        server.openServer();
    }
}
