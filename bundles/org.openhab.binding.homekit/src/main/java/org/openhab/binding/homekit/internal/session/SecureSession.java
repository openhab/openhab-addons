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
package org.openhab.binding.homekit.internal.session;

import static org.openhab.binding.homekit.internal.crypto.CryptoUtils.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicInteger;

import org.bouncycastle.crypto.InvalidCipherTextException;
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
    private final AsymmetricSessionKeys sessionKeys;
    private final AtomicInteger writeCounter = new AtomicInteger(0);
    private final AtomicInteger readCounter = new AtomicInteger(0);
    private final DecryptingInputStream decryptingInputStream;

    public SecureSession(Socket socket, AsymmetricSessionKeys keys) throws IOException {
        in = socket.getInputStream();
        out = socket.getOutputStream();
        sessionKeys = keys;
        decryptingInputStream = new DecryptingInputStream(in, readCounter, keys);
    }

    /**
     * Sends multiple data frames over the output stream. Splits the plaintext into chunks <= 1024 bytes,
     * encrypts them, and sends them as separate frames.
     *
     * @param plainText the complete plaintext message to be sent.
     * @throws IOException
     * @throws InvalidCipherTextException
     */
    public void send(byte[] plainText) throws IOException, InvalidCipherTextException {
        try (ByteArrayInputStream plainTextStream = new ByteArrayInputStream(plainText)) {
            while (plainTextStream.available() > 0) {
                sendFrame(plainTextStream);
            }
        }
    }

    /**
     * Sends a single data frame over the output stream. This method reads up to 1024 bytes from the
     * input plaintext, encrypts it, and sends it as a frame with a 2-byte length prefix, and a 16 byte
     * tag. The length prefix is included in the cipher AAD to ensure integrity. The write counter is
     * incremented after sending the frame to ensure nonce uniqueness.
     *
     * @param plainTextStream the input stream containing the plaintext to be sent.
     * @throws IOException
     * @throws InvalidCipherTextException
     */
    private void sendFrame(ByteArrayInputStream plainTextStream) throws IOException, InvalidCipherTextException {
        short frameLen = (short) Math.min(1024, plainTextStream.available());
        byte[] frameAad = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(frameLen).array();
        byte[] plainText = plainTextStream.readNBytes(frameLen);
        byte[] nonce64 = generateNonce64(writeCounter.getAndIncrement());
        byte[] cipherText = encrypt(sessionKeys.getWriteKey(), nonce64, plainText, frameAad);
        byte[] frame = new byte[frameAad.length + cipherText.length];
        System.arraycopy(frameAad, 0, frame, 0, frameAad.length);
        System.arraycopy(cipherText, 0, frame, frameAad.length, cipherText.length);
        out.write(frame);
        out.flush();
    }

    /**
     * Returns the InputStream that decrypts data from the underlying socket input stream.
     *
     * @return an {@link InputStream} being an instance of {@link DecryptingInputStream}
     */
    public InputStream getInputStream() {
        return decryptingInputStream;
    }
}
