package com.cly.data.filesystem;

import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.FileSystemFactory;

import java.io.IOException;
import java.net.URI;

public class ClazzFileSystemFactory implements FileSystemFactory {
    @Override
    public String getScheme() {
        return ClazzFileSystem.getClasspathURI().getScheme();
    }

    @Override
    public FileSystem create(URI fsUri) throws IOException {
        return ClazzFileSystem.getSharedInstance();
    }
}
