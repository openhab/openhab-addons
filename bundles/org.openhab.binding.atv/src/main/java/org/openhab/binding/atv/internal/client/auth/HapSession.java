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
package org.openhab.binding.atv.internal.client.auth;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.support.Chacha20Cipher;

/**
 * Manages stream cryptography for a HAP session according to the IP specification.
 *
 * <p>
 * The HAP specification (section 5.2.2, Release R1) mandates that data is encrypted in blocks of
 * at most 1024 plaintext bytes. Each block on the wire is a 2-byte <em>little-endian</em>
 * plaintext length prefix (which is also the AAD of the block), followed by the
 * ciphertext and the 16-byte Poly1305 tag. The class is transparent until encryption is
 * enabled: data is passed through unchanged before {@link #enable(byte[], byte[])}.
 *
 * <p>
 * {@link #decrypt(byte[])} accumulates partial frames across calls, returning only the
 * plaintext of fully received blocks.
 *
 * <p>
 * Thread-safety: the encrypt and decrypt directions keep independent state, so one
 * writer thread and one reader thread may use an instance concurrently, but each
 * direction must be confined to a single thread.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class HapSession {

    /** Maximum plaintext bytes per block, as specified by HAP section 5.2.2 (Release R1). */
    public static final int FRAME_LENGTH = 1024;

    /** Poly1305 authentication tag length in bytes. */
    public static final int AUTH_TAG_LENGTH = 16;

    private byte[] encryptedData = new byte[0];
    private @Nullable Chacha20Cipher chacha20;

    /**
     * Enables encryption with the specified keys.
     *
     * @param outputKey key used by {@link #encrypt(byte[])}
     * @param inputKey key used by {@link #decrypt(byte[])}
     */
    public void enable(byte[] outputKey, byte[] inputKey) {
        this.chacha20 = new Chacha20Cipher(outputKey, inputKey);
    }

    /**
     * Decrypts incoming stream data, buffering partial frames until they are complete.
     *
     * @param data the next chunk of the incoming byte stream
     * @return plaintext of all blocks completed by this chunk (possibly empty), or
     *         {@code data} unchanged if encryption is not enabled
     */
    public byte[] decrypt(byte[] data) {
        Chacha20Cipher cipher = chacha20;
        if (cipher == null) {
            return data;
        }

        encryptedData = concat(encryptedData, data);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while (encryptedData.length > 0) {
            if (encryptedData.length < 2) {
                return output.toByteArray();
            }
            byte[] length = Arrays.copyOfRange(encryptedData, 0, 2);
            int blockLength = ((length[0] & 0xFF) | ((length[1] & 0xFF) << 8)) + AUTH_TAG_LENGTH;
            if (encryptedData.length < blockLength + 2) {
                return output.toByteArray();
            }

            byte[] block = Arrays.copyOfRange(encryptedData, 2, 2 + blockLength);
            output.writeBytes(cipher.decrypt(block, null, length));

            encryptedData = Arrays.copyOfRange(encryptedData, 2 + blockLength, encryptedData.length);
        }
        return output.toByteArray();
    }

    /**
     * Encrypts outgoing data into framed blocks.
     *
     * @param data plaintext to encrypt
     * @return the framed wire bytes (length prefix + ciphertext + tag per block), or
     *         {@code data} unchanged if encryption is not enabled
     */
    public byte[] encrypt(byte[] data) {
        Chacha20Cipher cipher = chacha20;
        if (cipher == null) {
            return data;
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int pos = 0;
        while (pos < data.length) {
            int frameLength = Math.min(data.length - pos, FRAME_LENGTH);
            byte[] frame = Arrays.copyOfRange(data, pos, pos + frameLength);
            pos += frameLength;

            byte[] length = new byte[] { (byte) (frameLength & 0xFF), (byte) ((frameLength >> 8) & 0xFF) };
            output.writeBytes(length);
            output.writeBytes(cipher.encrypt(frame, null, length));
        }
        return output.toByteArray();
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] merged = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, merged, first.length, second.length);
        return merged;
    }
}
