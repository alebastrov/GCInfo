package com.nikondsl.gcinfo.archive;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.ByteStreams;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CachedUnzipper<K,V> {
    LoadingCache<K, CountDownLatch> cache;

    public CachedUnzipper() {
        CacheLoader<K, CountDownLatch> loader = new CacheLoader<K, CountDownLatch>() {
            @Override
            public CountDownLatch load(final K key) {
                return new CountDownLatch(1);
            }
        };
        this.cache = CacheBuilder.newBuilder().build(loader);
    }

    void unzip(final ZipFile source, final File destination) throws IOException {
        for (final ZipEntry entry : Collections.list(source.entries())) {
            unzip(source, entry, destination);
        }
    }

    private void unzip(final ZipFile source, final ZipEntry entry, final File destination) throws IOException {
        if (entry.isDirectory()) {
            return;
        }
        final File resource = new File(destination, entry.getName());
        if (!resource.getCanonicalPath().startsWith(destination.getCanonicalPath() + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + entry);
        }

        final File folder = resource.getParentFile();
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new IOException();
            }
        }

        try (BufferedInputStream input = new BufferedInputStream(source.getInputStream(entry));
             BufferedOutputStream output = new BufferedOutputStream(Files.newOutputStream(resource.toPath()))) {
            byte[] bytes = ByteStreams.toByteArray(input);
            output.write(bytes);
            toCache(entry, bytes);
            output.flush();
        }
    }

    private void toCache(ZipEntry entry, byte[] bytes) {

    }

    public void unzip(final String file) throws IOException {
        final File source = new File(file);
        unzip(
                new ZipFile(source),
                new File(source.getParent(), source.getName().substring(0, source.getName().lastIndexOf('.')))
        );
    }

    public void unzip(final String source, final String destination) throws IOException {
        unzip(new File(source), new File(destination));
    }

    public void unzip(final File source, final File destination) throws IOException {
        unzip(new ZipFile(source), destination);
    }
}
