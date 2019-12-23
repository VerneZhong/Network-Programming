package com.zxb.aio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2019-12-23 11:00
 */
public class ClientHandler implements CompletionHandler<Integer, Object> {

    private AsynchronousSocketChannel socketChannel;

    public ClientHandler(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void completed(Integer result, Object attachment) {
        Map<String, Object> info = (Map<String, Object>) attachment;
        String type = (String) info.get("type");

        if (Objects.equals(type, "read")) {
            ByteBuffer byteBuffer = (ByteBuffer) info.get("buffer");
            byteBuffer.flip();
            info.put("type", "write");
            socketChannel.write(byteBuffer, info, this);
            byteBuffer.clear();
        } else if ("write".equals(type)) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            info.put("type", "read");
            info.put("buffer", byteBuffer);
            socketChannel.write(byteBuffer, info, this);
        }
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        System.out.println("出现异常: " +exc.getMessage());
    }
}
