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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Writes the nested {@code P-xxxx.zip} of a password-protected ETS 6 project, for tests. Written from the format
 * note on its own, apart from the reader: the first key derivation is a one-block PBKDF2 of its own, the second
 * one comes from the JDK, and the counter blocks are built from a block number.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class ProtectedPartWriter {

    static final int STORED = 0;
    static final int DEFLATED = 8;

    private static final byte[] ETS_SALT = "21.project.ets.knx.org".getBytes(StandardCharsets.US_ASCII);

    /** Where the parts of an AES entry ended up in the archive. */
    record Layout(int dataOffset, int saltLength, int cipherTextLength) {

        int verifierOffset() {
            return dataOffset + saltLength;
        }

        int cipherTextOffset() {
            return verifierOffset() + 2;
        }

        int authenticationCodeOffset() {
            return cipherTextOffset() + cipherTextLength;
        }
    }

    private enum Kind {
        AES,
        PLAIN,
        DIRECTORY,
        TRADITIONAL
    }

    private record Item(String name, byte[] content, Kind kind, int method, int aeVersion, int strength,
            @Nullable Long crc, @Nullable String password) {
    }

    private final String password;
    private final List<Item> items = new ArrayList<>();
    private final Map<String, Layout> layouts = new HashMap<>();
    private final Map<String, String> zipPasswords = new HashMap<>();
    private final Map<String, String> foreignVerifiers = new HashMap<>();

    ProtectedPartWriter(String password) {
        this.password = password;
    }

    /** A project part as ETS 6.4 writes it: both files AES-256, AE-1, deflated. */
    static ProtectedPartWriter ets6(String fixture, String password) {
        ProtectedPartWriter writer = new ProtectedPartWriter(password);
        writer.aes("0.xml", TestContainer.resource(fixture + "/0.xml"), DEFLATED, 1, 3);
        writer.aes("project.xml", TestContainer.resource(fixture + "/project.xml"), DEFLATED, 1, 3);
        return writer;
    }

    ProtectedPartWriter aes(String name, String content, int method, int aeVersion, int strength) {
        return aes(name, content.getBytes(StandardCharsets.UTF_8), method, aeVersion, strength);
    }

    ProtectedPartWriter aes(String name, byte[] content, int method, int aeVersion, int strength) {
        items.add(new Item(name, content, Kind.AES, method, aeVersion, strength, null, null));
        return this;
    }

    /** An AES entry whose CRC field holds the given value instead of the right one. */
    ProtectedPartWriter aesWithCrc(String name, String content, int aeVersion, long crc) {
        items.add(
                new Item(name, content.getBytes(StandardCharsets.UTF_8), Kind.AES, DEFLATED, aeVersion, 3, crc, null));
        return this;
    }

    /** An AES entry encrypted with another password than the rest. */
    ProtectedPartWriter aesWithPassword(String name, String content, String otherPassword) {
        items.add(new Item(name, content.getBytes(StandardCharsets.UTF_8), Kind.AES, DEFLATED, 1, 3, null,
                otherPassword));
        return this;
    }

    /**
     * An AES entry encrypted with the project password whose password verifier is the one another password
     * derives: that password passes the verifier of this entry and fails its authentication code.
     */
    ProtectedPartWriter aesWithVerifierOf(String name, String content, String otherPassword) {
        foreignVerifiers.put(name, otherPassword);
        return aes(name, content, DEFLATED, 1, 3);
    }

    ProtectedPartWriter plain(String name, String content, int method) {
        items.add(new Item(name, content.getBytes(StandardCharsets.UTF_8), Kind.PLAIN, method, 0, 0, null, null));
        return this;
    }

    ProtectedPartWriter directory(String name) {
        items.add(new Item(name, new byte[0], Kind.DIRECTORY, STORED, 0, 0, null, null));
        return this;
    }

    /** An entry as ETS 4 and 5 write it: deflated with the encryption flag, contents not real. */
    ProtectedPartWriter traditional(String name) {
        byte[] noise = new byte[40];
        for (int i = 0; i < noise.length; i++) {
            noise[i] = (byte) (i * 37 + 11);
        }
        items.add(new Item(name, noise, Kind.TRADITIONAL, DEFLATED, 0, 0, null, null));
        return this;
    }

    /** Valid after {@link #build()}. */
    Layout layout(String name) {
        Layout layout = layouts.get(name);
        if (layout == null) {
            throw new IllegalStateException("no AES entry " + name);
        }
        return layout;
    }

    byte[] build() {
        try {
            return write();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] write() throws GeneralSecurityException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream central = new ByteArrayOutputStream();
        layouts.clear();
        int index = 0;
        for (Item item : items) {
            index++;
            byte[] name = item.name().getBytes(StandardCharsets.UTF_8);
            CRC32 crc32 = new CRC32();
            crc32.update(item.content());
            long crc = crc32.getValue();
            byte[] compressed = item.method() == DEFLATED ? deflate(item.content()) : item.content();
            int flags = 0;
            int method = item.method();
            byte[] extra = new byte[0];
            byte[] data = compressed;
            switch (item.kind()) {
                case AES -> {
                    flags = 1;
                    method = 99;
                    extra = aesExtra(item.aeVersion(), item.strength(), item.method());
                    String entryPassword = item.password();
                    data = encrypt(compressed, item.strength(), index,
                            entryPassword == null ? password : entryPassword);
                    int saltLength = 4 + 4 * item.strength();
                    String foreign = foreignVerifiers.get(item.name());
                    if (foreign != null) {
                        byte[] keys = keys(foreign, Arrays.copyOf(data, saltLength), item.strength());
                        data[saltLength] = keys[2 * (8 + 8 * item.strength())];
                        data[saltLength + 1] = keys[2 * (8 + 8 * item.strength()) + 1];
                    }
                    layouts.put(item.name(),
                            new Layout(out.size() + 30 + name.length + extra.length, saltLength, compressed.length));
                    if (item.aeVersion() == 2) {
                        crc = 0;
                    }
                }
                case TRADITIONAL -> flags = 1;
                default -> {
                }
            }
            Long crcOverride = item.crc();
            if (crcOverride != null) {
                crc = crcOverride;
            }
            int localOffset = out.size();
            ByteBuffer local = header(30 + name.length + extra.length);
            local.putInt(0x04034b50).putShort((short) 51).putShort((short) flags).putShort((short) method)
                    .putShort((short) 0x6000).putShort((short) 0x5a21).putInt((int) crc).putInt(data.length)
                    .putInt(item.content().length).putShort((short) name.length).putShort((short) extra.length)
                    .put(name).put(extra);
            out.writeBytes(local.array());
            out.writeBytes(data);

            ByteBuffer entry = header(46 + name.length + extra.length);
            entry.putInt(0x02014b50).putShort((short) 51).putShort((short) 51).putShort((short) flags)
                    .putShort((short) method).putShort((short) 0x6000).putShort((short) 0x5a21).putInt((int) crc)
                    .putInt(data.length).putInt(item.content().length).putShort((short) name.length)
                    .putShort((short) extra.length).putShort((short) 0).putShort((short) 0).putShort((short) 0)
                    .putInt(item.kind() == Kind.DIRECTORY ? 0x10 : 0x20).putInt(localOffset).put(name).put(extra);
            central.writeBytes(entry.array());
        }
        int centralOffset = out.size();
        out.writeBytes(central.toByteArray());
        ByteBuffer end = header(22);
        end.putInt(0x06054b50).putShort((short) 0).putShort((short) 0).putShort((short) items.size())
                .putShort((short) items.size()).putInt(central.size()).putInt(centralOffset).putShort((short) 0);
        out.writeBytes(end.array());
        return out.toByteArray();
    }

    private static ByteBuffer header(int size) {
        return ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
    }

    private static byte[] aesExtra(int version, int strength, int realMethod) {
        ByteBuffer extra = header(11);
        extra.putShort((short) 0x9901).putShort((short) 7).putShort((short) version).put((byte) 'A').put((byte) 'E')
                .put((byte) strength).putShort((short) realMethod);
        return extra.array();
    }

    private static byte[] deflate(byte[] content) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
        try {
            deflater.setInput(content);
            deflater.finish();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            while (!deflater.finished()) {
                out.write(buffer, 0, deflater.deflate(buffer));
            }
            return out.toByteArray();
        } finally {
            deflater.end();
        }
    }

    /** The two keys and the verifier a password derives for a salt. */
    private byte[] keys(String projectPassword, byte[] salt, int strength) {
        try {
            String cached = zipPasswords.get(projectPassword);
            if (cached == null) {
                cached = zipPassword(projectPassword);
                zipPasswords.put(projectPassword, cached);
            }
            int keyLength = 8 + 8 * strength;
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                    .generateSecret(new PBEKeySpec(cached.toCharArray(), salt, 1000, (2 * keyLength + 2) * 8))
                    .getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Salt, verifier, cipher text and authentication code of one entry. */
    private byte[] encrypt(byte[] plain, int strength, int index, String projectPassword)
            throws GeneralSecurityException {
        int keyLength = 8 + 8 * strength;
        int saltLength = keyLength / 2;
        byte[] salt = new byte[saltLength];
        for (int i = 0; i < saltLength; i++) {
            salt[i] = (byte) (index * 53 + i * 7 + 1);
        }
        byte[] keys = keys(projectPassword, salt, strength);

        Cipher aes = Cipher.getInstance("AES/ECB/NoPadding");
        aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keys, 0, keyLength, "AES"));
        byte[] cipherText = new byte[plain.length];
        for (int block = 0; block * 16 < plain.length; block++) {
            byte[] counter = header(16).putLong(block + 1L).array();
            byte[] stream = aes.doFinal(counter);
            for (int i = 0; i < 16 && block * 16 + i < plain.length; i++) {
                cipherText[block * 16 + i] = (byte) (plain[block * 16 + i] ^ stream[i]);
            }
        }
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(keys, keyLength, keyLength, "HmacSHA1"));
        byte[] code = mac.doFinal(cipherText);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(salt);
        out.write(keys[2 * keyLength]);
        out.write(keys[2 * keyLength + 1]);
        out.writeBytes(cipherText);
        out.write(code, 0, 10);
        return out.toByteArray();
    }

    /** Base64 of PBKDF2-HMAC-SHA256 over the UTF-16LE bytes, salt "21.project.ets.knx.org", 65536 rounds. */
    static String zipPassword(String projectPassword) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(projectPassword.getBytes(StandardCharsets.UTF_16LE), "HmacSHA256"));
        mac.update(ETS_SALT);
        byte[] u = mac.doFinal(new byte[] { 0, 0, 0, 1 });
        byte[] key = u.clone();
        for (int round = 2; round <= 65536; round++) {
            u = mac.doFinal(u);
            for (int i = 0; i < key.length; i++) {
                key[i] ^= u[i];
            }
        }
        return Base64.getEncoder().encodeToString(key);
    }
}
