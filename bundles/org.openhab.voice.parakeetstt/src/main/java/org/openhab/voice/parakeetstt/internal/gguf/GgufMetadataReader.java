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
package org.openhab.voice.parakeetstt.internal.gguf;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GgufMetadataReader} class parses metadata key-value pairs from GGUF model file headers.
 *
 * @see <a href="https://github.com/ggml-org/ggml/blob/master/docs/gguf.md#standard-metadata-key-value-pairs">GGML: GGUF
 *      Docs</a>
 * @see <a href="https://github.com/mudler/parakeet.cpp/blob/master/docs/bundle.md">parakeet.cpp: Bundle Docs</a>
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class GgufMetadataReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(GgufMetadataReader.class);

    private static final int GGUF_MAGIC = 0x46554747; // "GGUF" in little-endian uint32 (0x47, 0x47, 0x55, 0x46)

    // GGUF Metadata Value Types
    enum GgufMetadataValueType {
        UINT8,
        INT8,
        UINT16,
        INT16,
        UINT32,
        INT32,
        FLOAT32,
        BOOL,
        STRING,
        ARRAY,
        UINT64,
        INT64,
        FLOAT64;

        public static GgufMetadataValueType fromOrdinal(int ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                throw new IllegalArgumentException("Invalid GGUF metadata value type ordinal: " + ordinal);
            }
            return values()[ordinal];
        }
    }

    private static final String KEY_GENERAL_NAME = "general.name";
    private static final String KEY_GENERAL_ARCHITECTURE = "general.architecture";
    private static final String KEY_GENERAL_LANGUAGES = "general.languages";
    private static final String KEY_PARAKEET_ARCH = "parakeet.parakeetAarch";

    private static final int MAX_KEY_LENGTH = 512;
    private static final int MAX_STRING_LENGTH = 65536;
    private static final int MAX_ARRAY_COUNT = 10000;
    private static final int MAX_METADATA_COUNT = 10000;

    private final Map<Path, CacheEntry> cache = new ConcurrentHashMap<>();

    private record CacheEntry(FileTime lastModified, GgufMetadata metadata) {
    }

    /**
     * Reads metadata from the given GGUF file, reusing cached values if the file modification time has not changed.
     *
     * @param path path to the GGUF file
     * @return extracted {@link GgufMetadata}, or empty if parsing failed or file is not a valid GGUF file
     */
    public @Nullable GgufMetadata read(Path path) {
        try {
            if (!Files.isRegularFile(path)) {
                cache.remove(path);
                return null;
            }
            FileTime currentModified = Files.getLastModifiedTime(path);
            CacheEntry cached = cache.get(path);
            if (cached != null && cached.lastModified().equals(currentModified)) {
                return cached.metadata();
            }

            GgufMetadata parsed = parseFile(path);
            if (parsed != null) {
                cache.put(path, new CacheEntry(currentModified, parsed));
                return parsed;
            }
        } catch (IOException e) {
            LOGGER.debug("Could not read GGUF metadata from {}: {}", path, e.getMessage());
        }
        return null;
    }

    /**
     * Parses the GGUF header and metadata key-value pairs from disk.
     *
     * @param path file path
     * @return parsed metadata, or null if file is not valid GGUF
     * @throws IOException on I/O error
     */
    private @Nullable GgufMetadata parseFile(Path path) throws IOException {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(path))) {
            return parse(in);
        }
    }

    /**
     * Parses GGUF metadata from an input stream.
     *
     * @param in input stream
     * @return parsed metadata, or null if input is not valid GGUF
     * @throws IOException on I/O error
     */
    public @Nullable GgufMetadata parse(InputStream in) throws IOException {
        try {
            byte[] magicBytes = in.readNBytes(4);
            if (magicBytes.length < 4) {
                return null;
            }
            int magic = readIntLe(magicBytes, 0);
            if (magic != GGUF_MAGIC) {
                return null;
            }

            int version = readIntLe(in);
            if (version < 1 || version > 3) {
                LOGGER.debug("Unsupported GGUF version {}", version);
                return null;
            }

            // Tensor count (uint64 for v2/v3, uint32 for v1)
            if (version == 1) {
                readIntLe(in);
            } else {
                readLongLe(in);
            }

            // Metadata KV count
            long kvCount = version == 1 ? readIntLe(in) & 0xFFFFFFFFL : readLongLe(in);
            if (kvCount < 0 || kvCount > MAX_METADATA_COUNT) {
                LOGGER.debug("Invalid or excessive GGUF metadata count: {}", kvCount);
                return null;
            }

            String name = null;
            String architecture = null;
            List<String> languages = Collections.emptyList();
            String arch = null;

            for (int i = 0; i < kvCount; i++) {
                String key = readString(in, version);
                GgufMetadataValueType valueType = GgufMetadataValueType.fromOrdinal(readIntLe(in));

                switch (key) {
                    case KEY_GENERAL_NAME -> {
                        if (valueType == GgufMetadataValueType.STRING) {
                            name = readStringValue(in);
                        } else {
                            skipValue(in, valueType);
                        }
                    }
                    case KEY_GENERAL_ARCHITECTURE -> {
                        if (valueType == GgufMetadataValueType.STRING) {
                            architecture = readStringValue(in);
                        } else {
                            skipValue(in, valueType);
                        }
                    }
                    case KEY_GENERAL_LANGUAGES -> {
                        if (valueType == GgufMetadataValueType.ARRAY) {
                            languages = readStringArray(in);
                        } else if (valueType == GgufMetadataValueType.STRING) {
                            languages = List.of(readStringValue(in));
                        } else {
                            skipValue(in, valueType);
                        }
                    }
                    case KEY_PARAKEET_ARCH -> {
                        if (valueType == GgufMetadataValueType.STRING) {
                            arch = readStringValue(in);
                        } else {
                            skipValue(in, valueType);
                        }
                    }
                    default -> skipValue(in, valueType);
                }
            }

            return new GgufMetadata(name, architecture, languages, arch);
        } catch (EOFException e) {
            LOGGER.debug("Premature end of file reading GGUF stream: {}", e.getMessage());
            return null;
        }
    }

    private static String readString(InputStream in, int version) throws IOException {
        long length = version == 1 ? readIntLe(in) & 0xFFFFFFFFL : readLongLe(in);
        if (length < 0 || length > MAX_KEY_LENGTH) {
            throw new IOException("Invalid GGUF key length: " + length);
        }
        byte[] bytes = in.readNBytes((int) length);
        if (bytes.length < length) {
            throw new EOFException("Premature end of file reading GGUF key");
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static String readStringValue(InputStream in) throws IOException {
        long length = readLongLe(in);
        if (length < 0 || length > MAX_STRING_LENGTH) {
            throw new IOException("Invalid GGUF string value length: " + length);
        }
        byte[] bytes = in.readNBytes((int) length);
        if (bytes.length < length) {
            throw new EOFException("Premature end of file reading GGUF string value");
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static List<String> readStringArray(InputStream in) throws IOException {
        GgufMetadataValueType itemType = GgufMetadataValueType.fromOrdinal(readIntLe(in));
        long count = readLongLe(in);
        if (count < 0 || count > MAX_ARRAY_COUNT) {
            throw new IOException("Invalid GGUF array count: " + count);
        }
        if (itemType != GgufMetadataValueType.STRING) {
            // Non-string array: skip elements
            for (long i = 0; i < count; i++) {
                skipValue(in, itemType);
            }
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>((int) Math.min(count, 128));
        for (long i = 0; i < count; i++) {
            list.add(readStringValue(in));
        }
        return list;
    }

    private static void skipValue(InputStream in, GgufMetadataValueType valueType) throws IOException {
        switch (valueType) {
            case UINT8, INT8, BOOL -> skipBytes(in, 1);
            case UINT16, INT16 -> skipBytes(in, 2);
            case UINT32, INT32, FLOAT32 -> skipBytes(in, 4);
            case UINT64, INT64, FLOAT64 -> skipBytes(in, 8);
            case STRING -> {
                long len = readLongLe(in);
                if (len < 0 || len > MAX_STRING_LENGTH) {
                    throw new IOException("Excessive string length to skip: " + len);
                }
                skipBytes(in, len);
            }
            case ARRAY -> {
                GgufMetadataValueType itemType = GgufMetadataValueType.fromOrdinal(readIntLe(in));
                long count = readLongLe(in);
                if (count < 0 || count > MAX_ARRAY_COUNT) {
                    throw new IOException("Excessive array count to skip: " + count);
                }
                for (long i = 0; i < count; i++) {
                    skipValue(in, itemType);
                }
            }
            default -> throw new IOException("Unknown GGUF value type: " + valueType);
        }
    }

    private static void skipBytes(InputStream in, long count) throws IOException {
        long remaining = count;
        while (remaining > 0) {
            long skipped = in.skip(remaining);
            if (skipped <= 0) {
                if (in.read() == -1) {
                    throw new EOFException("End of stream reached while skipping GGUF value");
                }
                remaining--;
            } else {
                remaining -= skipped;
            }
        }
    }

    private static int readIntLe(InputStream in) throws IOException {
        byte[] bytes = in.readNBytes(4);
        if (bytes.length < 4) {
            throw new EOFException("Premature end of file reading 32-bit integer");
        }
        return readIntLe(bytes, 0);
    }

    private static long readLongLe(InputStream in) throws IOException {
        byte[] bytes = in.readNBytes(8);
        if (bytes.length < 8) {
            throw new EOFException("Premature end of file reading 64-bit integer");
        }
        return ((long) (bytes[0] & 0xFF)) //
                | ((long) (bytes[1] & 0xFF) << 8) //
                | ((long) (bytes[2] & 0xFF) << 16) //
                | ((long) (bytes[3] & 0xFF) << 24) //
                | ((long) (bytes[4] & 0xFF) << 32) //
                | ((long) (bytes[5] & 0xFF) << 40) //
                | ((long) (bytes[6] & 0xFF) << 48) //
                | ((long) (bytes[7] & 0xFF) << 56);
    }

    private static int readIntLe(byte[] b, int offset) {
        return (b[offset] & 0xFF) //
                | ((b[offset + 1] & 0xFF) << 8) //
                | ((b[offset + 2] & 0xFF) << 16) //
                | ((b[offset + 3] & 0xFF) << 24);
    }
}
