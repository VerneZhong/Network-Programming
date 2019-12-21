package com.zxb.nio.chatroom2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2019-12-20 16:14
 */
public class NioChatServer {

    public static final int PORT = 8888;
    public static final String EXIT = "exit";
    public static final int BUFFER_SIZE = 1024;

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private Charset charset = StandardCharsets.UTF_8;
    private int port;

    public NioChatServer(int port) {
        this.port = port;
    }

    public NioChatServer() {
        this(PORT);
    }

    public void start() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            // 设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(port));

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("启动服务器，监听端口：" + port);

            for (; ; ) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey selectionKey : selectionKeys) {
                    // 处理被触发的事件
                    handlers(selectionKey);
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlers(SelectionKey selectionKey) {

    }

    public boolean readyToExit(String msg) {
        return Objects.equals(msg, EXIT);
    }
}
