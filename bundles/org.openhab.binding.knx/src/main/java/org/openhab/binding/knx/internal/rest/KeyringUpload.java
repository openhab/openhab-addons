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
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.rest.dto.KeyringUploadDTO;
import org.openhab.binding.knx.internal.security.KeyringFacts;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.calimero.secure.Keyring;

/**
 * Number 19: stores an uploaded ETS keyring for a bridge, unchanged, as {@code <keyring folder>/<bridge UID, ":" as
 * "_">.knxkeys}, once the library can read it. The keyring password is never stored; with the password header the
 * answer says whether the signature matches it, and a wrong one is no failure. The bridge is not switched to it.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class KeyringUpload {
    static final long MAX_BYTES = 1024L * 1024;
    private static final String TEMPORARY_NAME = "upload.knxkeys";
    private static final int DECLARATION_BYTES = 200;
    private static final Pattern ENCODING = Pattern.compile("\\sencoding\\s*=\\s*([\"'])([^\"']*)\\1");

    private final Logger logger = LoggerFactory.getLogger(KeyringUpload.class);
    private final KnxRestService service;
    private final Uploads uploads;
    private final Path keyringFolder;
    private final Path configMisc;
    private final long maxBytes;
    private final Function<String, @Nullable Keyring> parser;

    /**
     * @param keyringFolder where uploaded keyrings are kept, {@code userdata/knx/keyring}
     * @param configMisc the folder the bridge looks into first, {@code conf/misc}
     */
    KeyringUpload(KnxRestService service, Uploads uploads, Path keyringFolder, Path configMisc) {
        this(service, uploads, keyringFolder, configMisc, MAX_BYTES);
    }

    KeyringUpload(KnxRestService service, Uploads uploads, Path keyringFolder, Path configMisc, long maxBytes) {
        this(service, uploads, keyringFolder, configMisc, maxBytes, Keyring::load);
    }

    /** @param parser reads the keyring file at the given path; a test sees through it which files reach the parser */
    KeyringUpload(KnxRestService service, Uploads uploads, Path keyringFolder, Path configMisc, long maxBytes,
            Function<String, @Nullable Keyring> parser) {
        this.service = service;
        this.uploads = uploads;
        this.keyringFolder = keyringFolder;
        this.configMisc = configMisc;
        this.maxBytes = maxBytes;
        this.parser = parser;
    }

    /** {@return the name an uploaded keyring of the bridge is stored under, the value for {@code keyringFile}} */
    static String fileName(ThingUID bridge) {
        return bridge.getAsString().replace(':', '_') + ".knxkeys";
    }

    /**
     * @param contentLength the declared length, null without one
     * @param password the header {@link Uploads#PASSWORD_HEADER}, null without one; never copied
     */
    public KeyringUploadDTO upload(@Nullable String bridge, @Nullable InputStream body, @Nullable String contentLength,
            @Nullable String password) {
        KNXBridgeBaseThingHandler handler = service.handler(bridge);
        String fileName = fileName(handler.getThing().getUID());
        Uploads.checkLength(contentLength, maxBytes);
        uploads.begin();
        try (Uploads.Password secret = uploads.password(password)) {
            Path folder = uploads.newFolder();
            try {
                Path file = uploads.receive(body, folder, TEMPORARY_NAME, maxBytes);
                KeyringFacts facts = new KeyringFacts(read(file));
                char[] chars = secret.chars();
                Boolean signatureValid = chars == null ? null : facts.signatureMatches(chars);
                keep(file, fileName);
                return KnxDtoMapper.keyringUpload(facts, fileName, signatureValid,
                        Files.exists(configMisc.resolve(fileName)), handler.getOwnAddress());
            } finally {
                uploads.delete(folder);
            }
        } finally {
            uploads.end();
        }
    }

    /**
     * Refuses a document type or an entity before the parser sees the file; a keyring file has neither. Only a file
     * that starts as XML in UTF-8 or US-ASCII gets that far, so the check cannot miss one written in another encoding.
     */
    private Keyring read(Path file) {
        String text;
        try {
            byte[] bytes = Files.readAllBytes(file);
            if (!startsAsAsciiXml(bytes)) {
                throw new KnxRestException(KnxRestError.NOT_A_KEYRING);
            }
            for (byte b : bytes) {
                if (b == 0) {
                    throw new KnxRestException(KnxRestError.NOT_A_KEYRING);
                }
            }
            text = new String(bytes, StandardCharsets.ISO_8859_1).toUpperCase(Locale.ROOT);
        } catch (IOException e) {
            throw failed(KnxRestError.READ_FAILED, e);
        }
        if (text.contains("<!DOCTYPE") || text.contains("<!ENTITY")) {
            throw new KnxRestException(KnxRestError.NOT_A_KEYRING);
        }
        try {
            Keyring keyring = parser.apply(file.toString());
            if (keyring == null) {
                throw new KnxRestException(KnxRestError.NOT_A_KEYRING);
            }
            return keyring;
        } catch (KnxRestException e) {
            throw e;
        } catch (RuntimeException e) {
            // the library's messages name the file's path
            throw failed(KnxRestError.NOT_A_KEYRING, e);
        }
    }

    /**
     * {@return true if, after an optional UTF-8 byte order mark, the file starts with {@code <} and an XML
     * declaration in its first 200 bytes names no encoding or UTF-8 or US-ASCII}
     */
    private static boolean startsAsAsciiXml(byte[] bytes) {
        int start = bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF
                ? 3
                : 0;
        if (bytes.length <= start || bytes[start] != '<') {
            return false;
        }
        String head = new String(bytes, start, Math.min(bytes.length, DECLARATION_BYTES) - start,
                StandardCharsets.ISO_8859_1);
        if (!head.startsWith("<?xml")) {
            return true;
        }
        int end = head.indexOf("?>");
        if (end < 0) {
            return false;
        }
        String declaration = head.substring(0, end);
        Matcher encoding = ENCODING.matcher(declaration);
        if (encoding.find()) {
            String name = encoding.group(2);
            return "UTF-8".equalsIgnoreCase(name) || "US-ASCII".equalsIgnoreCase(name);
        }
        return !declaration.contains("encoding");
    }

    private void keep(Path file, String fileName) {
        try {
            if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
                Files.createDirectories(keyringFolder,
                        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
            } else {
                Files.createDirectories(keyringFolder);
            }
            Files.move(file, keyringFolder.resolve(fileName), StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw failed(KnxRestError.STORE_FAILED, e);
        }
    }

    private KnxRestException failed(KnxRestError error, Exception e) {
        logger.warn("KNX REST keyring upload: {} ({})", error.code(), e.getClass().getName());
        return new KnxRestException(error);
    }
}
