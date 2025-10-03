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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * Sends multiple data frames over the output stream. Splits the plaintext into chunks <= 1024 bytes,
     * encrypts them, and sends them as separate frames.
     *
     * @param plainText the complete plaintext message to be sent.
     * @throws Exception if an error occurs during encryption or sending.
     */
    public void send(byte[] plainText) throws Exception {
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
     * @throws Exception if an error occurs during encryption or sending.
     */
    private void sendFrame(ByteArrayInputStream plainTextStream) throws Exception {
        short frameLen = (short) Math.min(1024, plainTextStream.available());
        ByteBuffer frameAad = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(frameLen);
        out.write(frameAad.array(), 0, frameAad.array().length); // send length prefix
        byte[] plainText = plainTextStream.readNBytes(frameLen);
        byte[] nonce64 = generateNonce64(writeCounter.getAndIncrement());
        out.write(encrypt(writeKey, nonce64, plainText, frameAad.array())); // AAD = lenBytes; outputs extra 16 byte tag
    }

    /**
     * Reads multiple data frames from the input stream until a complete HTTP message is reconstructed.
     * Repeatedly calls receiveFrame() to read and decrypt individual frames. It accumulates the decrypted
     * plaintext until it detects the end of the HTTP message. The end of the message is determined by checking
     * for the presence of complete HTTP headers and a Content-Length header.
     *
     * @return the complete decrypted HTTP message as a byte array.
     * @throws Exception if an error occurs during reading or decryption.
     */
    public byte[] receive() throws Exception {
        HttpPayloadParser httpParser = new HttpPayloadParser();
        ByteArrayOutputStream plainText = new ByteArrayOutputStream();
        do {
            byte[] frame = receiveFrame();
            plainText.write(frame);
            httpParser.accept(frame);
        } while (!httpParser.readComplete());
        return plainText.toByteArray();
    }

    /**
     * Reads a single frame from the input stream, decrypts it, and returns the plaintext. Reads the 2-byte length
     * prefix, retrieves the corresponding ciphertext, and decrypts it. The length prefix is included in the cipher
     * AAD to ensure integrity. The read counter is incremented after reading the frame to ensure nonce uniqueness.
     *
     * @return the decrypted plaintext of the single frame.
     * @throws Exception if an error occurs during reading or decryption.
     */
    private byte[] receiveFrame() throws Exception {
        byte[] frameAad = in.readNBytes(2);
        short frameLen = ByteBuffer.wrap(frameAad).order(ByteOrder.LITTLE_ENDIAN).getShort();
        if (frameLen < 0 || frameLen > 1024) {
            throw new SecurityException("Invalid frame length");
        }
        byte[] cipherText = in.readNBytes(frameLen + 16); // read 16 extra bytes for the auth tag
        byte[] nonce64 = generateNonce64(readCounter.getAndIncrement());
        return decrypt(readKey, nonce64, cipherText, frameAad);
    }

    /**
     * Internal helper class to parse incoming HTTP messages and determine when a complete message has been received.
     * It accumulates header data until the end of headers is detected, then reads the Content-Length header to
     * determine how many bytes of body to expect. It tracks the number of body bytes read to know when the full
     * message has been received.
     */
    private static class HttpPayloadParser {
        private static final String NEWLINE_REGEX = "\\r?\\n";
        private static final String END_OF_HEADERS = "\r\n\r\n";
        private static final int MAX_CONTENT_LENGTH = 65536;
        private static final int MAX_HEADER_BLOCK_SIZE = 2048;
        private static final Pattern CONTENT_LENGTH_PATTERN = Pattern.compile("(?i)^content-length:\\s*(\\d+)$");

        private final ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
        private boolean headersDone = false;
        private int contentLength = 0;
        private int bytesAccepted = 0;
        private int headerLength = -1;

        public void accept(byte[] data) throws SecurityException {
            bytesAccepted += data.length;
            if (headersDone) {
                return;
            }
            try {
                headerBuffer.write(data);
            } catch (IOException e) {
                // should never occur with ByteArrayOutputStream
            }
            if (headerBuffer.size() > MAX_HEADER_BLOCK_SIZE) {
                throw new SecurityException("Header buffer overload");
            }
            String temp = new String(headerBuffer.toByteArray(), StandardCharsets.ISO_8859_1);
            int offset = temp.indexOf(END_OF_HEADERS);
            if (offset >= 0) {
                headersDone = true;
                headerLength = offset + END_OF_HEADERS.length();
                for (String httpHeader : temp.split(NEWLINE_REGEX)) {
                    Matcher matcher = CONTENT_LENGTH_PATTERN.matcher(httpHeader);
                    if (matcher.find()) {
                        try {
                            contentLength = Integer.parseInt(matcher.group(1));
                            if (contentLength < 0 || contentLength > MAX_CONTENT_LENGTH) {
                                throw new SecurityException("Invalid Content-Length");
                            }
                        } catch (NumberFormatException e) {
                            // should never occur due to regex
                        }
                        break;
                    }
                }
            }
        }

        public boolean readComplete() {
            return headersDone && (bytesAccepted - headerLength >= contentLength);
        }
    }
}
