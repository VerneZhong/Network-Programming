package com.zxb.bio.chatroom.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2019-12-17 10:02
 */
public class ChatServer {

    private static final int PORT = 8888;

    private static final String EXIT = "exit";

    private static final ExecutorService es = Executors.newFixedThreadPool(4);

    private ServerSocket serverSocket;

    private final Map<Integer, Writer> connectedClients;

    ChatServer() {
        this.connectedClients = new HashMap<>();
    }

    synchronized void addClient(Socket socket) throws IOException {
        if (socket != null) {
            int port = socket.getPort();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            connectedClients.put(port, writer);
            System.out.println("客户端[" + port + "] 已连接到服务器");
        }
    }

    synchronized void removeClient(Socket socket) throws IOException {
        if (socket != null) {
            Writer writer = connectedClients.remove(socket.getPort());
            if (writer != null) {
                writer.close();
                System.out.println("客户端[" + socket.getPort() + "] 已断开连接");
            }
        }
    }

    synchronized void forwardMessage(Socket socket, String forward) throws IOException {
        for (Integer port : connectedClients.keySet()) {
            if (!port.equals(socket.getPort())) {
                Writer writer = connectedClients.get(port);
                writer.write(forward);
                writer.flush();
            }
        }
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("启动服务器，监听端口：" + PORT);

            while (true) {
                // 等待客户端连接
                Socket socket = serverSocket.accept();
                // 创建ChatHandler线程
                es.execute(new ChatHandler(this, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public synchronized void close() {
        if (serverSocket != null) {
            try {
                es.shutdown();
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }

    public boolean readyToExit(String msg) {
        return Objects.equals(msg, EXIT);
    }
}
