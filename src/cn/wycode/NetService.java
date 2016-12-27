package cn.wycode;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import static cn.wycode.Utils.buildMessage;


/**
 *
 */
public class NetService {

    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private BufferedReader br;
    private PrintWriter pw;

    private NetStateChange nsc;

    private ServerSocket serverSocket;

    private static final int PORT = 1594;

    private static NetService client;
    private static NetService server;

    private NetService() {
    }

    public static NetService getInstance(Controller.NetType netType) {
        switch (netType) {
            case CLIENT:
                if (client == null) {
                    client = new NetService();
                }
                return client;
            case SERVER:
                if (server == null) {
                    server = new NetService();
                }
                return server;
            default:
                return server;
        }
    }


    public void startServer() {
        new ServerThread().start();
    }


    void connectToServer(String ip) {
        try {
            socket = new Socket(ip, PORT);
            init();
            startRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化用于接收/发送的流处理
     *
     * @throws IOException
     */
    void init() throws IOException {
        is = socket.getInputStream();
        os = socket.getOutputStream();
        br = new BufferedReader(new InputStreamReader(is, "utf-8"));
        pw = new PrintWriter(new OutputStreamWriter(os, "utf-8"));
    }

    void startRead() {
        ReadThread reader = new ReadThread();
        reader.start();
    }

    public void sendMessage(String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                pw.println(message);
                pw.flush();
            }
        }).start();
    }

    public void close() {
        try {
//            br.close();
            pw.close();
            socket.close();
            if (Controller.netType == Controller.NetType.SERVER) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    interface NetStateChange {
        void onServerOK();

        void onConnect();

        void onMessage(String message);

        void onDisconnect();
    }

    public String readMessage() {
        String message = null;
        try {
            message = br.readLine();
        }catch (SocketException se){
            if(nsc!=null){
                nsc.onDisconnect();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }


    void setNetStateChangeListener(NetStateChange nsc) {
        this.nsc = nsc;
    }

    class ServerThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    serverSocket = new ServerSocket(PORT);
                    System.out.println(serverSocket.getInetAddress());
                    if (nsc != null) {
                        nsc.onServerOK();
                    }
                    socket = serverSocket.accept();
                    init();
                    if (nsc != null) {
                        nsc.onConnect();
                        nsc.onMessage(buildMessage(Controller.HEAD_NET, "some one connected!"));
                    }
                    startRead();
                    break;
                } catch (IOException e) {
                    System.out.print("Server failure\n");
                    e.printStackTrace();
                    try {
                        serverSocket.close();
                    } catch (IOException ex) {
                        //ignore this;
                    }
                }
            }
        }
    }

    class ReadThread extends Service<String> {

        @Override
        protected void succeeded() {
            super.succeeded();
            if (getValue() != null && getValue().length() > 0) {
                if (nsc != null) {
                    nsc.onMessage(getValue());
                }
            }
            this.restart();
        }

        @Override
        protected Task<String> createTask() {
            return new Task<String>() {
                protected String call() throws Exception {
                    return readMessage();
                }
            };
        }
    }
}
