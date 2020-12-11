package com.cly.data.filesystem;

import org.apache.flink.core.fs.FileStatus;
import org.apache.flink.core.fs.Path;
import org.apache.flink.core.fs.local.LocalFileStatus;
import org.apache.flink.core.fs.local.LocalFileSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

/**
 * 当前项目的class类路径文件查找。
 * 只能测试用用
 * {@link https://ci.apache.org/projects/flink/flink-docs-stable/ops/filesystems/}
 */
public class ClazzFileSystem extends LocalFileSystem {

    private static final ClazzFileSystem INSTANCE = new ClazzFileSystem();
    public static final URI CLASSPATH_URI = URI.create("clazz:/");


    private Path workingDirectory;
    private Path homeDirectory;


    private ClassLoader classLoader;

    public ClazzFileSystem() {
        classLoader = Thread.currentThread().getContextClassLoader();
        Path path = new Path(classLoader.getResource("").getPath());
        this.workingDirectory = path;
        this.homeDirectory = path;
    }
    @Override
    public Path getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public Path getHomeDirectory() {
        return homeDirectory;
    }

    @Override
    public boolean exists(Path f) throws IOException {
        final File file = new File(workingDirectory+f.getPath());
        System.out.println(file.getAbsolutePath());
        return file.exists();
    }

    @Override
    public FileStatus[] listStatus(Path f) throws IOException {

        final File localf =  new File(workingDirectory+f.getPath());
        FileStatus[] results;

        if (!localf.exists()) {
            return null;
        }
        if (localf.isFile()) {
            return new FileStatus[] { new LocalFileStatus(localf, this) };
        }

        final String[] names = localf.list();
        if (names == null) {
            return null;
        }
        results = new FileStatus[names.length];
        for (int i = 0; i < names.length; i++) {
            results[i] = getFileStatus(new Path(f, names[i]));
        }

        return results;
    }

    @Override
    public FileStatus getFileStatus(Path f) throws IOException {
        final File  path = new File(workingDirectory+f.getPath());
        if (path.exists()) {
            return new LocalFileStatus(path, this);
        }
        else {
            throw new FileNotFoundException("File " + f + " does not exist or the user running "
                    + "Flink ('" + System.getProperty("user.name") + "') has insufficient permissions to access it.");
        }
    }

    @Override
    public URI getUri() {
        return CLASSPATH_URI;
    }

    public static URI getClasspathURI() {
        return CLASSPATH_URI;
    }
    public static ClazzFileSystem getSharedInstance() {
        return INSTANCE;
    }
}
