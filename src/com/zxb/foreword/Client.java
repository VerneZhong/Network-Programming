package com.zxb.foreword;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2019-12-16 17:08
 */
public class Client {

    public static void main(String[] args) {

        String host = "127.0.0.1";
        int port = 8888;

        String exit = "exit";

        try (Socket socket = new Socket(host, port)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            while (true) {
                BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

                // 发送消息给服务器
                String s = console.readLine();
                writer.write(s + "\n");
                writer.flush();

                // 读取服务器返回消息
                String msg = reader.readLine();
                if (exit.equals(s)) {
                    break;
                }
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
