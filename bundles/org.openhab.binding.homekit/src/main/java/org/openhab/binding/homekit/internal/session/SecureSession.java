/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal.session;

import static org.openhab.binding.homekit.internal.crypto.CryptoUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Manages a secure session using ChaCha20 encryption for a HomeKit accessory.
 * This class handles encryption and decryption of messages using session keys.
 * It maintains separate counters for read and write operations to ensure nonce uniqueness.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class SecureSession {

    private final InputStream in;
    private final OutputStream out;
    private final byte[] writeKey;
    private final byte[] readKey;
    private final AtomicInteger writeCounter = new AtomicInteger(0);
    private final AtomicInteger readCounter = new AtomicInteger(0);

    public SecureSession(Socket socket, AsymmetricSessionKeys keys) throws IOException {
        in = socket.getInputStream();
        out = socket.getOutputStream();
        writeKey = keys.getWriteKey();
        readKey = keys.getReadKey();
    }

    /**
     * Encrypts the given plaintext using the write key and a unique nonce and sends it.
     *
     * @param plaintext the plaintext to be encrypted and sent.
     * @throws Exception
     */
    public void send(byte[] plaintext) throws Exception {
        byte[] nonce = generateNonce(writeCounter.getAndIncrement());
        byte[] ciphertext = encrypt(writeKey, nonce, plaintext);
        ByteBuffer buf = ByteBuffer.allocate(2 + ciphertext.length);
        buf.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buf.putShort((short) ciphertext.length);
        buf.put(ciphertext);
        out.write(buf.array());
        out.flush();
    }

    /**
     * Reads the cipertext and decrypts it using the read key and a unique nonce.
     *
     * @return the received ciphertext decrypted.
     * @throws Exception
     */
    public byte[] receive() throws Exception {
        int lo = in.read();
        int hi = in.read();
        if (lo < 0 || hi < 0) {
            throw new IllegalStateException("Stream closed");
        }
        int length = (lo & 0xFF) | ((hi & 0xFF) << 8);
        byte[] ciphertext = in.readNBytes(length);
        byte[] nonce = generateNonce(readCounter.getAndIncrement());
        byte[] plaintext = decrypt(readKey, nonce, ciphertext);
        return plaintext;
    }
}
