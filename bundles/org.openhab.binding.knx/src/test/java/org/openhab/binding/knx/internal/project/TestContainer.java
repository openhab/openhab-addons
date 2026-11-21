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
package org.openhab.binding.knx.internal.project;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Builds a made-up {@code .knxproj} container in memory from the text fixtures under {@code project/}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class TestContainer {

    private static final String MASTER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<KNX xmlns=\"http://knx.org/xml/project/23\"><MasterData Id=\"made-up\" /></KNX>\n";

    private final Map<String, byte[]> entries = new LinkedHashMap<>();

    static TestContainer withMaster() {
        TestContainer container = new TestContainer();
        container.put("knx_master.xml", MASTER);
        return container;
    }

    /** A container holding the fixture folder {@code project/<fixture>} as the unprotected project part. */
    static TestContainer of(String fixture, String partId) {
        TestContainer container = withMaster();
        String projectFile = "ets4".equals(fixture) ? "Project.xml" : "project.xml";
        container.put("M-00FA/Hardware.xml", "<KNX><ManufacturerData /></KNX>");
        container.put(partId + "/" + projectFile, resource(fixture + "/" + projectFile));
        container.put(partId + "/0.xml", resource(fixture + "/0.xml"));
        container.put(partId + ".signature", "made-up signature");
        return container;
    }

    /** A container holding {@code inner} as the protected project part {@code partName}. */
    static TestContainer ofProtected(String partName, byte[] inner) {
        TestContainer container = withMaster();
        container.put("M-00FA/Hardware.xml", "<KNX><ManufacturerData /></KNX>");
        container.put(partName, inner);
        container.put(partName.substring(0, partName.length() - 4) + ".signature", "made-up signature");
        return container;
    }

    static String resource(String name) {
        try (InputStream in = TestContainer.class.getResourceAsStream("/project/" + name)) {
            if (in == null) {
                throw new IllegalStateException("missing fixture " + name);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    TestContainer put(String name, String content) {
        return put(name, content.getBytes(StandardCharsets.UTF_8));
    }

    TestContainer put(String name, byte[] content) {
        entries.put(name, content);
        return this;
    }

    TestContainer remove(String name) {
        entries.remove(name);
        return this;
    }

    String text(String name) {
        byte[] content = entries.get(name);
        if (content == null) {
            throw new IllegalStateException("no entry " + name);
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    byte[] zip() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue());
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }

    Path write() {
        return write(zip());
    }

    static Path write(byte[] content) {
        try {
            Path dir = Path.of(TestContainer.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .getParent().resolve("knxproj-tests");
            Files.createDirectories(dir);
            Path file = Files.createTempFile(dir, "test", ".knxproj");
            Files.write(file, content);
            file.toFile().deleteOnExit();
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Rewrites the uncompressed size that the central directory states for one entry, so a test can show
     * that the reader does not trust it.
     */
    static byte[] claimUncompressedSize(byte[] zip, String entryName, int claimedSize) {
        byte[] copy = zip.clone();
        byte[] name = entryName.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i + 46 <= copy.length; i++) {
            if (u32(copy, i) != 0x02014b50L) {
                continue;
            }
            int nameLength = u16(copy, i + 28);
            if (nameLength == name.length && i + 46 + nameLength <= copy.length
                    && java.util.Arrays.equals(copy, i + 46, i + 46 + nameLength, name, 0, nameLength)) {
                copy[i + 24] = (byte) claimedSize;
                copy[i + 25] = (byte) (claimedSize >> 8);
                copy[i + 26] = (byte) (claimedSize >> 16);
                copy[i + 27] = (byte) (claimedSize >> 24);
                return copy;
            }
        }
        throw new IllegalStateException("entry not found in central directory");
    }

    /**
     * Raises the compressed size that the central directory states for one entry by {@code extra} bytes, so
     * that the stated range runs on into the entries after it.
     */
    static byte[] inflateClaimedCompressedSize(byte[] zip, String entryName, int extra) {
        byte[] copy = zip.clone();
        byte[] name = entryName.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i + 46 <= copy.length; i++) {
            if (u32(copy, i) != 0x02014b50L) {
                continue;
            }
            int nameLength = u16(copy, i + 28);
            if (nameLength == name.length && i + 46 + nameLength <= copy.length
                    && java.util.Arrays.equals(copy, i + 46, i + 46 + nameLength, name, 0, nameLength)) {
                long claimed = u32(copy, i + 20) + extra;
                for (int b = 0; b < 4; b++) {
                    copy[i + 20 + b] = (byte) (claimed >> (8 * b));
                }
                return copy;
            }
        }
        throw new IllegalStateException("entry not found in central directory");
    }

    /** The same archive with its central directory located by a zip64 end record. */
    static byte[] withZip64EndRecord(byte[] zip) {
        java.nio.ByteBuffer in = java.nio.ByteBuffer.wrap(zip).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        int end = zip.length - 22;
        if (in.getInt(end) != 0x06054b50) {
            throw new IllegalStateException("zip with a comment");
        }
        java.nio.ByteBuffer out = java.nio.ByteBuffer.allocate(zip.length + 56 + 20)
                .order(java.nio.ByteOrder.LITTLE_ENDIAN);
        out.put(zip, 0, end);
        out.putInt(0x06064b50).putLong(44).putShort((short) 45).putShort((short) 45).putInt(0).putInt(0)
                .putLong(in.getShort(end + 10)).putLong(in.getShort(end + 10)).putLong(in.getInt(end + 12))
                .putLong(in.getInt(end + 16));
        out.putInt(0x07064b50).putInt(0).putLong(end).putInt(1);
        out.putInt(0x06054b50).putShort((short) 0).putShort((short) 0).putShort((short) 0xFFFF).putShort((short) 0xFFFF)
                .putInt(0xFFFFFFFF).putInt(0xFFFFFFFF).putShort((short) 0);
        return out.array();
    }

    /** Made-up filler that does not compress, from a fixed seed. */
    static String filler(int length) {
        java.util.Random random = new java.util.Random(7);
        StringBuilder text = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            text.append((char) ('a' + random.nextInt(26)));
        }
        return text.toString();
    }

    private static int u16(byte[] b, int offset) {
        return (b[offset] & 0xFF) | (b[offset + 1] & 0xFF) << 8;
    }

    private static long u32(byte[] b, int offset) {
        return u16(b, offset) | (long) u16(b, offset + 2) << 16;
    }
}
