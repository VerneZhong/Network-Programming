package com.zxb.nio.chatroom;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Set;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2019-12-18 16:22
 */
public class NioClient {

    private static final int PORT = 8000;

    private static final String HOST = "127.0.0.1";

    public void start(String nickname) throws IOException {
        // 连接服务器
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(HOST, PORT));

        // 接收服务器响应
        // 创建一个线程，专门负责用来接收服务器的响应数据
        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        new Thread(new NioClientHandler(selector)).start();

        // 向服务器发送数据
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            if (s != null) {
                socketChannel.write(StandardCharsets.UTF_8.encode(nickname + " : " + s));
            }
        }

    }

    public static void main(String[] args) throws IOException {
        new NioClient().start(args[0]);
    }
}
