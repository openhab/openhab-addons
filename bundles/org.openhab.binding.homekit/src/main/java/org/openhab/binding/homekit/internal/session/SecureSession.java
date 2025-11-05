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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
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

    private static final int SLEEP_INTERVAL_MILLISECONDS = 50;

    private final DataInputStream in;
    private final OutputStream out;
    private final byte[] writeKey;
    private final byte[] readKey;
    private final AtomicInteger writeCounter = new AtomicInteger(0);
    private final AtomicInteger readCounter = new AtomicInteger(0);

    public SecureSession(Socket socket, AsymmetricSessionKeys keys) throws IOException {
        in = new DataInputStream(socket.getInputStream());
        if (in.available() > 0) {
            in.readAllBytes(); // clear any pre-existing data
        }
        out = socket.getOutputStream();
        writeKey = keys.getWriteKey();
        readKey = keys.getReadKey();
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
        ByteArrayInputStream plainTextStream = new ByteArrayInputStream(plainText);
        while (plainTextStream.available() > 0) {
            sendFrame(plainTextStream);
        }
        out.flush();
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
        ByteBuffer frameAad = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(frameLen);
        out.write(frameAad.array(), 0, frameAad.array().length); // send length prefix
        byte[] plainText = plainTextStream.readNBytes(frameLen);
        byte[] nonce64 = generateNonce64(writeCounter.getAndIncrement());
        out.write(encrypt(writeKey, nonce64, plainText, frameAad.array())); // AAD = lenBytes; outputs extra 16 byte tag
    }

    /**
     * Reads multiple data frames from the input stream until a complete HTTP message is reconstructed.
     * Repeatedly whenever there is data available on the input stream, it calls receiveFrame() to read and
     * decrypt a frame. It accumulates the decrypted plaintext until it detects the end of the HTTP message.
     * The end of the message is determined by checking for the presence of complete HTTP headers and a
     * completed Content-Length, or a complete chunked payload.
     *
     * @param trace if true, captures the raw decrypted frames for debugging purposes.
     * @return a 3D byte array where the first element is the HTTP headers, the second element is the content,
     *         and the third is the raw trace (if enabled).
     * @throws IOException
     * @throws InvalidCipherTextException
     * @throws IllegalStateException if the received data is malformed
     */
    public byte[][] receive(boolean trace) throws IOException, InvalidCipherTextException, IllegalStateException {
        HttpPayloadParser httpParser = new HttpPayloadParser();
        ByteArrayOutputStream traceStream = new ByteArrayOutputStream();
        do {
            if (in.available() == 0) {
                try {
                    Thread.sleep(SLEEP_INTERVAL_MILLISECONDS); // wait for data to arrive
                } catch (InterruptedException e) {
                    // coninue
                }
            } else {
                byte[] frame = receiveFrame();
                if (trace) {
                    traceStream.write(frame);
                }
                httpParser.accept(frame);
            }
        } while (!httpParser.isComplete());
        return new byte[][] { httpParser.getHeaders(), httpParser.getContent(), traceStream.toByteArray() };
    }

    /**
     * Reads a single frame from the input stream, decrypts it, and returns the plaintext. Blocks until a full frame
     * is received or an IO exception occurs. Reads the 2-byte length prefix, retrieves the corresponding ciphertext,
     * and decrypts it. The length prefix is included in the cipher AAD to ensure integrity. The read counter is
     * incremented after reading the frame to ensure nonce uniqueness.
     *
     * @return the decrypted plaintext of the single frame.
     * @throws IOException
     * @throws InvalidCipherTextException
     * @throws IllegalStateException if the frame length is invalid
     */
    private byte[] receiveFrame() throws IOException, InvalidCipherTextException, IllegalStateException {
        byte[] frameAad = new byte[2]; // AAD data length prefix
        in.readFully(frameAad, 0, frameAad.length);
        short frameLen = ByteBuffer.wrap(frameAad).order(ByteOrder.LITTLE_ENDIAN).getShort();
        if (frameLen < 0 || frameLen > 1024) {
            throw new IllegalStateException("Invalid frame length");
        }
        byte[] cipherText = new byte[frameLen + 16]; // read 16 extra bytes for the auth tag
        in.readFully(cipherText, 0, cipherText.length);
        byte[] nonce64 = generateNonce64(readCounter.getAndIncrement());
        return decrypt(readKey, nonce64, cipherText, frameAad);
    }
}
