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
package org.openhab.binding.knx.internal.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * What the project and the keyring upload share: one upload at a time for the whole add-on, the password header,
 * and a private temporary folder per upload under the upload folder, which holds the body only while it is read.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class Uploads {
    static final String PASSWORD_HEADER = "X-KNX-Password";
    static final String PASSWORD_TEXT = PASSWORD_HEADER + ": not the Base64 form of a UTF-8 text";
    static final String CONTENT_LENGTH = "Content-Length";

    private static final int BUFFER_BYTES = 64 * 1024;
    private static final boolean POSIX = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");

    private final Logger logger = LoggerFactory.getLogger(Uploads.class);
    private final Path folder;
    private final AtomicBoolean running = new AtomicBoolean();
    private final PasswordArrays passwords;

    /** Is told of every array that holds the password or its Base64 form, so a test can see that each was wiped. */
    interface PasswordArrays {
        void password(Password password);

        default void headerBytes(byte[] bytes) {
        }

        default void decodeBuffer(char[] buffer) {
        }
    }

    /** A password from the header, as bytes and as characters; both arrays are wiped by {@link #close()}. */
    static final class Password implements AutoCloseable {
        private final byte[] bytes;
        private char[] chars = new char[0];

        private Password(byte[] bytes) {
            this.bytes = bytes;
        }

        /** {@return the password, null for none} */
        char @Nullable [] chars() {
            return chars.length == 0 ? null : chars;
        }

        boolean isWiped() {
            for (byte b : bytes) {
                if (b != 0) {
                    return false;
                }
            }
            for (char c : chars) {
                if (c != 0) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void close() {
            Arrays.fill(bytes, (byte) 0);
            Arrays.fill(chars, '\0');
        }
    }

    /** @param folder the upload folder; every upload gets its own folder inside it */
    Uploads(Path folder) {
        this(folder, password -> {
        });
    }

    /** @param passwords is told of every password array made, so a test can see that it was wiped */
    Uploads(Path folder, PasswordArrays passwords) {
        this.folder = folder;
        this.passwords = passwords;
    }

    /** Takes the one upload slot of the add-on; {@code busy} if another upload holds it. */
    void begin() {
        if (!running.compareAndSet(false, true)) {
            throw new KnxRestException(KnxRestError.BUSY);
        }
    }

    void end() {
        running.set(false);
    }

    /**
     * Refuses a declared length above the cap before anything is read; the copy counts the bytes anyway.
     *
     * @param contentLength the header as given, null without one
     */
    static void checkLength(@Nullable String contentLength, long maxBytes) {
        if (contentLength == null) {
            return;
        }
        if (!contentLength.matches("\\d{1,30}")) {
            throw KnxRestException.badInput(CONTENT_LENGTH, "not a number");
        }
        if (contentLength.length() > 18 || Long.parseLong(contentLength) > maxBytes) {
            throw new KnxRestException(KnxRestError.TOO_LARGE);
        }
    }

    /**
     * Decodes the header: Base64 of the password's UTF-8 bytes; no header or an empty one is no password. The
     * header's own text is never copied; the caller must close the result.
     */
    Password password(@Nullable String header) {
        if (header == null || header.isEmpty()) {
            Password none = new Password(new byte[0]);
            passwords.password(none);
            return none;
        }
        byte[] text = header.getBytes(StandardCharsets.ISO_8859_1);
        byte[] decoded;
        try {
            passwords.headerBytes(text);
            decoded = Base64.getDecoder().decode(text);
        } catch (IllegalArgumentException e) {
            // the library's message names a character of the password
            throw KnxRestException.badInput(PASSWORD_HEADER, "not the Base64 form of a UTF-8 text");
        } finally {
            Arrays.fill(text, (byte) 0);
        }
        Password password = new Password(decoded);
        passwords.password(password);
        CharBuffer buffer = CharBuffer.allocate(decoded.length);
        try {
            passwords.decodeBuffer(buffer.array());
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            CoderResult result = decoder.decode(ByteBuffer.wrap(decoded), buffer, true);
            if (!result.isError()) {
                result = decoder.flush(buffer);
            }
            if (result.isError()) {
                password.close();
                throw KnxRestException.badInput(PASSWORD_HEADER, "not the Base64 form of a UTF-8 text");
            }
            password.chars = Arrays.copyOf(buffer.array(), buffer.position());
            return password;
        } finally {
            Arrays.fill(buffer.array(), '\0');
        }
    }

    /** {@return a new private folder for one upload; the caller deletes it with {@link #delete(Path)}} */
    Path newFolder() {
        try {
            if (POSIX) {
                FileAttribute<?> ownerOnly = PosixFilePermissions
                        .asFileAttribute(PosixFilePermissions.fromString("rwx------"));
                Files.createDirectories(folder, ownerOnly);
                return Files.createDirectory(folder.resolve(UUID.randomUUID().toString()), ownerOnly);
            }
            Files.createDirectories(folder);
            return Files.createDirectory(folder.resolve(UUID.randomUUID().toString()));
        } catch (IOException e) {
            logger.warn("KNX REST upload: the upload folder cannot be made ({})", e.getClass().getName());
            throw new KnxRestException(KnxRestError.READ_FAILED);
        }
    }

    /**
     * Copies the body into the file {@code name} of the upload's folder, counting the bytes; above the cap the copy
     * stops with {@code too-large}.
     */
    Path receive(@Nullable InputStream body, Path uploadFolder, String name, long maxBytes) {
        Path file = uploadFolder.resolve(name);
        if (!uploadFolder.equals(file.getParent())) {
            throw new KnxRestException(KnxRestError.INTERNAL);
        }
        try {
            if (POSIX) {
                Files.createFile(file,
                        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
            } else {
                Files.createFile(file);
            }
            if (body == null) {
                return file;
            }
            try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.WRITE)) {
                byte[] buffer = new byte[BUFFER_BYTES];
                long total = 0;
                int read;
                while ((read = body.read(buffer)) >= 0) {
                    total += read;
                    if (total > maxBytes) {
                        throw new KnxRestException(KnxRestError.TOO_LARGE);
                    }
                    out.write(buffer, 0, read);
                }
            }
            return file;
        } catch (IOException e) {
            logger.warn("KNX REST upload: {} ({})", KnxRestError.READ_FAILED.code(), e.getClass().getName());
            throw new KnxRestException(KnxRestError.READ_FAILED);
        }
    }

    /** Deletes an upload's folder and what it holds. */
    void delete(Path uploadFolder) {
        deleteTree(uploadFolder, true);
    }

    /** Deletes everything under the upload folder, left by a crash; never follows a link out of it. */
    void clean() {
        if (Files.isDirectory(folder)) {
            deleteTree(folder, false);
        }
    }

    private void deleteTree(Path root, boolean withRoot) {
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(@Nullable Path file, @Nullable BasicFileAttributes attributes)
                        throws IOException {
                    if (file != null && !file.equals(root)) {
                        Files.deleteIfExists(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(@Nullable Path file, @Nullable IOException e) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(@Nullable Path dir, @Nullable IOException e)
                        throws IOException {
                    if (dir != null && (withRoot || !dir.equals(root))) {
                        Files.deleteIfExists(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException | RuntimeException e) {
            logger.warn("KNX REST upload: a temporary file cannot be deleted ({})", e.getClass().getName());
        }
    }
}
