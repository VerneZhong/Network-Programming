package com.zxb.foreword;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2019-12-16 16:57
 */
public class Server {

    public static void main(String[] args) {

        int port = 8888;

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("服务端启动监听监控：" + port);
            while (true) {
                Socket socket = serverSocket.accept();

                System.out.println("客服端[" + socket.getPort() + "]已连接");

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                // 读取客服端发送的消息
                String msg;
                while ((msg = reader.readLine()) != null) {
                    System.out.println("客服端[" + socket.getPort() + "]发送的消息：" + msg);
                    // 回复给客户端消息
                    writer.write("服务器：" + msg + "\n");
                    writer.flush();

                    // 查看客服端是否退出
                    if ("exit".equals(msg)) {
                        System.out.println("客服端[" + socket.getPort() + "]已退出");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
