package com.zxb.nio.filecopy;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2019-12-19 17:39
 */
public class Main {

    public static final int ROUNDS = 1;

    public static void benchmark(FileCopyRunner runner, File source, File target, String name) throws IOException {
        var elapsed = 0;
        for (var i = 0; i < ROUNDS; i++) {
            long start = System.currentTimeMillis();
            runner.copyFile(source, target);
            elapsed += System.currentTimeMillis() - start;
            target.delete();
        }
        System.out.println(name + "平均耗时: " + elapsed / ROUNDS);
    }

    public static void main(String[] args) throws IOException {
        // 无缓冲区copy
        FileCopyRunner noBufferStreamCopy = (source, target) -> {
            try (var in = new FileInputStream(source);
                 var out = new FileOutputStream(target)) {
                int result;
                while ((result = in.read()) != -1) {
                    out.write(result);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        // 带缓冲区copy
        FileCopyRunner bufferedStreamCopy = (source, target) -> {
            try (var in = new BufferedInputStream(new FileInputStream(source));
                 var out = new BufferedOutputStream(new FileOutputStream(target))) {
                byte[] buff = new byte[1024];

                int result;
                while ((result = in.read(buff)) != -1) {
                    out.write(buff, 0, result);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        // NIO 缓冲 copy
        FileCopyRunner nioBufferCopy = (source, target) -> {
            try (var in = new FileInputStream(source).getChannel();
                 var out = new FileOutputStream(target).getChannel()) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

                while (in.read(byteBuffer) != -1) {
                    // 切换成读模式
                    byteBuffer.flip();
                    // 保证每次将ByteBuffer里数据全部读取完
                    while (byteBuffer.hasRemaining()) {
                        out.write(byteBuffer);
                    }
                    // 清空缓冲区
                    byteBuffer.clear();
                }
            }
        };

        // 通过通道传输的copy
        FileCopyRunner nioTransferCopy = (source, target) -> {
            var in = new FileInputStream(source).getChannel();
            var out = new FileOutputStream(target).getChannel();
            try (in; out) {
                var transferred = 0;
                var size = in.size();
                while (transferred != size) {
                    transferred += in.transferTo(0, size, out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        File source = new File("/Users/zhongxuebin/Downloads/Nice Python.jar");
        File target = new File("/Users/zhongxuebin/Downloads/Nice Python-copy.jar");

        benchmark(noBufferStreamCopy, source, target, "noBufferStreamCopy");
        benchmark(bufferedStreamCopy, source, target, "bufferedStreamCopy");
        benchmark(nioBufferCopy, source, target, "nioBufferCopy");
        benchmark(nioTransferCopy, source, target, "nioTransferCopy");
    }



}
