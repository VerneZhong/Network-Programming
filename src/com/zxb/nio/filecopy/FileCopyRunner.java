package com.zxb.nio.filecopy;

import java.io.File;
import java.io.IOException;

/**
 * File Copy interface
 *
 * @author Mr.zxb
 * @date 2019-12-19 17:34
 */
@FunctionalInterface
public interface FileCopyRunner {

    /**
     * 文件copy
     * @param source
     * @param target
     * @throws IOException
     */
    void copyFile(File source, File target) throws IOException;

}
