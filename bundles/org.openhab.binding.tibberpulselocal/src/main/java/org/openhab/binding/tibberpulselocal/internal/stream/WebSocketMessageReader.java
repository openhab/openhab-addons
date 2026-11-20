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
package org.openhab.binding.tibberpulselocal.internal.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.util.function.LongSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tibberpulselocal.internal.stream.StreamFailure.Kind;

/**
 * Reads websocket frames (RFC 6455, section 5) from a plain byte stream and assembles complete messages. The bridge
 * does not perform the websocket handshake, so no websocket library can be used for it.
 *
 * The approach of reading the frames from a raw connection follows the Home Assistant integration
 * <a href="https://github.com/marq24/ha-tibber-pulse-local">marq24/ha-tibber-pulse-local</a>. No code was copied;
 * this reader is written from RFC 6455 and hardened against the weaknesses found there (size limit, fragment state
 * checks, reserved bits, masked pong).
 *
 * The reader never reads more bytes than the current frame needs, so a caller can hand over the stream to another
 * reader between messages. It is not thread-safe and is used by the reader thread only.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
final class WebSocketMessageReader {

    /** The observed message is 749 bytes. */
    static final int MAX_MESSAGE_BYTES = 64 * 1024;

    private static final int MAX_CONTROL_PAYLOAD = 125;
    private static final int OPCODE_CONTINUATION = 0x0;
    private static final int OPCODE_TEXT = 0x1;
    private static final int OPCODE_BINARY = 0x2;
    private static final int OPCODE_CLOSE = 0x8;
    private static final int OPCODE_PING = 0x9;
    private static final int OPCODE_PONG = 0xA;

    private final InputStream in;
    private final OutputStream out;
    private final LongSupplier clock;
    private final long livenessNanos;
    private final Runnable frameListener;
    private final SecureRandom random = new SecureRandom();

    private long lastFrameNanos;

    /**
     * @param in the stream to read frames from; a read that times out is treated as silence
     * @param out where pong frames are written
     * @param clock monotonic clock in nanoseconds
     * @param livenessNanos limit for the time without a complete frame; also checked while bytes of one frame drip in
     * @param frameListener called after every complete frame, including control frames
     */
    WebSocketMessageReader(InputStream in, OutputStream out, LongSupplier clock, long livenessNanos,
            Runnable frameListener) {
        this.in = in;
        this.out = out;
        this.clock = clock;
        this.livenessNanos = livenessNanos;
        this.frameListener = frameListener;
        this.lastFrameNanos = clock.getAsLong();
    }

    /**
     * Blocks until a complete message is available. Pings are answered while waiting.
     *
     * @return the concatenated payload of all fragments of the next message
     * @throws StreamFailure if the stream ended, a close frame arrived, the liveness limit passed, or a protocol
     *             violation occurred
     * @throws IOException if reading or writing a pong failed otherwise
     */
    byte[] readMessage() throws IOException {
        ByteArrayOutputStream fragments = new ByteArrayOutputStream();
        boolean inProgress = false;
        byte[] head = new byte[8];
        while (true) {
            readFully(head, 0, 2);
            boolean fin = (head[0] & 0x80) != 0;
            if ((head[0] & 0x70) != 0) {
                throw new StreamFailure(Kind.PROTOCOL);
            }
            int opcode = head[0] & 0x0F;
            boolean masked = (head[1] & 0x80) != 0;
            long length = head[1] & 0x7F;
            if (length == 126) {
                readFully(head, 0, 2);
                length = ((head[0] & 0xFF) << 8) | (head[1] & 0xFF);
            } else if (length == 127) {
                readFully(head, 0, 8);
                length = 0;
                for (int i = 0; i < 8; i++) {
                    length = (length << 8) | (head[i] & 0xFF);
                }
                if (length < 0) {
                    throw new StreamFailure(Kind.PROTOCOL);
                }
            }
            boolean control = (opcode & 0x8) != 0;
            if (control && (!fin || length > MAX_CONTROL_PAYLOAD)) {
                throw new StreamFailure(Kind.PROTOCOL);
            }
            // Decided from the header alone, so the announced payload is never buffered or read.
            if (length > MAX_MESSAGE_BYTES || (!control && fragments.size() + length > MAX_MESSAGE_BYTES)) {
                throw new StreamFailure(Kind.PROTOCOL);
            }
            byte[] key = new byte[4];
            if (masked) {
                readFully(key, 0, 4);
            }
            byte[] payload = new byte[(int) length];
            readFully(payload, 0, payload.length);
            if (masked) {
                for (int i = 0; i < payload.length; i++) {
                    payload[i] ^= key[i & 3];
                }
            }
            lastFrameNanos = clock.getAsLong();
            frameListener.run();

            switch (opcode) {
                case OPCODE_CONTINUATION:
                    if (!inProgress) {
                        throw new StreamFailure(Kind.PROTOCOL);
                    }
                    fragments.write(payload, 0, payload.length);
                    if (fin) {
                        return fragments.toByteArray();
                    }
                    break;
                case OPCODE_TEXT:
                case OPCODE_BINARY:
                    if (inProgress) {
                        throw new StreamFailure(Kind.PROTOCOL);
                    }
                    if (fin) {
                        return payload;
                    }
                    fragments.write(payload, 0, payload.length);
                    inProgress = true;
                    break;
                case OPCODE_CLOSE:
                    throw new StreamFailure(Kind.CLOSED_BY_BRIDGE);
                case OPCODE_PING:
                    sendPong(payload);
                    break;
                case OPCODE_PONG:
                    break;
                default:
                    // Reserved opcode: the frame is dropped, the message in progress is untouched.
                    break;
            }
        }
    }

    /** Frames sent by a client must be masked (RFC 6455, section 5.1). */
    private void sendPong(byte[] payload) throws IOException {
        byte[] key = new byte[4];
        random.nextBytes(key);
        byte[] frame = new byte[6 + payload.length];
        frame[0] = (byte) (0x80 | OPCODE_PONG);
        frame[1] = (byte) (0x80 | payload.length);
        System.arraycopy(key, 0, frame, 2, 4);
        for (int i = 0; i < payload.length; i++) {
            frame[6 + i] = (byte) (payload[i] ^ key[i & 3]);
        }
        out.write(frame);
        out.flush();
    }

    private void readFully(byte[] buffer, int offset, int length) throws IOException {
        int done = 0;
        while (done < length) {
            int n;
            try {
                n = in.read(buffer, offset + done, length - done);
            } catch (SocketTimeoutException e) {
                throw new StreamFailure(Kind.SILENT, e);
            }
            if (n < 0) {
                throw new StreamFailure(Kind.CLOSED_BY_BRIDGE);
            }
            done += n;
            // A socket timeout alone cannot detect a peer that drips single bytes and never completes a frame.
            if (clock.getAsLong() - lastFrameNanos > livenessNanos) {
                throw new StreamFailure(Kind.SILENT);
            }
        }
    }
}
