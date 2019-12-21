package com.zxb.bio.chatroom.client;

import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2019-12-17 10:01
 */
public class ChatClient {

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 8888;
    public static final String EXIT = "exit";

    public static final ExecutorService es = Executors.newFixedThreadPool(4);

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    /**
     * 发送消息
     *
     * @param msg
     * @throws IOException
     */
    public void send(String msg) throws IOException {
        if (!socket.isOutputShutdown()) {
            writer.write(msg + "\n");
            writer.flush();
        }
    }

    /**
     * 接收消息
     *
     * @return
     * @throws IOException
     */
    public String receive() throws IOException {
        String msg = null;
        if (!socket.isInputShutdown()) {
            msg = reader.readLine();
        }
        return msg;
    }

    /**
     * 用户是否退出
     *
     * @param msg
     * @return
     */
    public boolean readyToExit(String msg) {
        return Objects.equals(msg, EXIT);
    }

    public void start() {
        try {
            // 创建Socket
            socket = new Socket(HOST, PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // 用户输入消息
            es.execute(new UserInputHandler(this));

            // 接收服务器消息
            String msg;
            while ((msg = receive()) != null) {
                System.out.println(msg);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public void close() {
        if (writer != null) {
            try {
                es.shutdown();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new ChatClient().start();
    }
}
