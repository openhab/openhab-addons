/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.automation.pythonscripting.internal.fs;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.graalvm.polyglot.io.FileSystem;

/**
 * Delegate wrapping a {@link FileSystem}
 *
 * @author Holger Hees - Initial contribution
 */
public class DelegatingFileSystem implements FileSystem {
    // Inspiration from
    // https://github.com/oracle/graal/blob/master/truffle/src/com.oracle.truffle.polyglot/src/com/oracle/truffle/polyglot/FileSystems.java
    private final FileSystemProvider delegate = FileSystems.getDefault().provider();
    private final Path tmpDir;

    private volatile Path userDir;
    private volatile Consumer<Path> consumer;

    public DelegatingFileSystem(Path tmpDir) {
        this.tmpDir = tmpDir;
    }

    public void setAccessConsumer(Consumer<@NonNull Path> consumer) {
        this.consumer = consumer;
    }

    @Override
    public Path parsePath(URI uri) {
        return Paths.get(uri);
    }

    @Override
    public Path parsePath(String path) {
        return Paths.get(path);
    }

    @Override
    public void checkAccess(Path path, Set<? extends AccessMode> modes, LinkOption... linkOptions) throws IOException {
        delegate.checkAccess(path, modes.toArray(new AccessMode[0]));
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        delegate.createDirectory(dir, attrs);
    }

    @Override
    public void delete(Path path) throws IOException {
        delegate.delete(path);
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        delegate.copy(resolveRelative(source), resolveRelative(target), options);
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        delegate.move(resolveRelative(source), resolveRelative(target), options);
    }

    @Override
    public void createLink(Path link, Path existing) throws IOException {
        delegate.createLink(resolveRelative(link), resolveRelative(existing));
    }

    @Override
    public void createSymbolicLink(Path link, Path target, FileAttribute<?>... attrs) throws IOException {
        delegate.createSymbolicLink(resolveRelative(link), target, attrs);
    }

    @Override
    public Path readSymbolicLink(Path link) throws IOException {
        return delegate.readSymbolicLink(resolveRelative(link));
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        return delegate.readAttributes(path, attributes, options);
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        delegate.setAttribute(path, attribute, value, options);
    }

    @Override
    public Path toAbsolutePath(Path path) {
        if (path.isAbsolute()) {
            return path;
        }
        Path cwd = userDir;
        if (cwd == null) {
            return path.toAbsolutePath();
        } else {
            return cwd.resolve(path);
        }
    }

    @Override
    public void setCurrentWorkingDirectory(Path currentWorkingDirectory) {
        userDir = currentWorkingDirectory;
    }

    @Override
    public Path toRealPath(Path path, LinkOption... linkOptions) throws IOException {
        return resolveRelative(path).toRealPath(linkOptions);
    }

    @Override
    public Path getTempDirectory() {
        return tmpDir;
    }

    @Override
    public boolean isSameFile(Path path1, Path path2, LinkOption... options) throws IOException {
        if (isFollowLinks(options)) {
            Path absolutePath1 = resolveRelative(path1);
            Path absolutePath2 = resolveRelative(path2);
            return delegate.isSameFile(absolutePath1, absolutePath2);
        } else {
            return delegate.isSameFile(path1, path2);
        }
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
            throws IOException {
        final Path resolved = resolveRelative(path);
        if (consumer != null) {
            consumer.accept(resolved);
        }
        try {
            return delegate.newFileChannel(resolved, options, attrs);
        } catch (UnsupportedOperationException uoe) {
            return delegate.newByteChannel(resolved, options, attrs);
        }
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter)
            throws IOException {
        Path cwd = userDir;
        Path resolvedPath;
        if (!dir.isAbsolute() && cwd != null) {
            resolvedPath = cwd.resolve(dir);
        } else {
            resolvedPath = dir;
        }
        return delegate.newDirectoryStream(resolvedPath, filter);
    }

    private Path resolveRelative(Path path) {
        return !path.isAbsolute() && userDir != null ? toAbsolutePath(path) : path;
    }

    private boolean isFollowLinks(final LinkOption... linkOptions) {
        for (LinkOption lo : linkOptions) {
            if (lo == LinkOption.NOFOLLOW_LINKS) {
                return false;
            }
        }
        return true;
    }
}
