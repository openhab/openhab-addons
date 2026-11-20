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
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Encodes websocket frames and the observed two-frame message for tests.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
final class FrameBytes {

    static final int CONTINUATION = 0x0;
    static final int TEXT = 0x1;
    static final int BINARY = 0x2;
    static final int CLOSE = 0x8;
    static final int PING = 0x9;
    static final int PONG = 0xA;

    /** The length field forms: pick the smallest, or force one of the three. */
    static final int AUTO = 0;
    static final int FORM_7 = 7;
    static final int FORM_16 = 16;
    static final int FORM_64 = 64;

    private FrameBytes() {
    }

    static byte[] frame(int opcode, boolean fin, byte[] payload) {
        return frame(opcode, fin, payload, AUTO, null);
    }

    static byte[] frame(int opcode, boolean fin, byte[] payload, int form, byte @Nullable [] maskKey) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write((fin ? 0x80 : 0) | opcode);
        int mask = maskKey != null ? 0x80 : 0;
        int length = payload.length;
        int actualForm = form == AUTO ? (length < 126 ? FORM_7 : length <= 0xFFFF ? FORM_16 : FORM_64) : form;
        switch (actualForm) {
            case FORM_7:
                out.write(mask | length);
                break;
            case FORM_16:
                out.write(mask | 126);
                out.write(length >> 8);
                out.write(length);
                break;
            default:
                out.write(mask | 127);
                for (int shift = 56; shift >= 0; shift -= 8) {
                    out.write((int) (((long) length >> shift) & 0xFF));
                }
        }
        if (maskKey != null) {
            out.writeBytes(maskKey);
            for (int i = 0; i < payload.length; i++) {
                out.write(payload[i] ^ maskKey[i & 3]);
            }
        } else {
            out.writeBytes(payload);
        }
        return out.toByteArray();
    }

    /** A frame header that announces a payload length, without any payload. */
    static byte[] header64(int opcode, boolean fin, long announcedLength) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write((fin ? 0x80 : 0) | opcode);
        out.write(127);
        for (int shift = 56; shift >= 0; shift -= 8) {
            out.write((int) ((announcedLength >> shift) & 0xFF));
        }
        return out.toByteArray();
    }

    /** {@code <device:<id> topic:"<topic>">} */
    static byte[] header(String deviceId, String topic) {
        return ("<device:" + deviceId + " topic:\"" + topic + "\">").getBytes(StandardCharsets.US_ASCII);
    }

    /** Header and payload of one message as the bridge sends them. */
    static byte[] messageBytes(String deviceId, String topic, byte[] payload) {
        return concat(header(deviceId, topic), payload);
    }

    /** The observed layout: a binary frame with FIN=0 holding the header, then a continuation frame with FIN=1. */
    static byte[] observed(String deviceId, byte[] payload) {
        return concat(frame(BINARY, false, header(deviceId, "sml")), frame(CONTINUATION, true, payload));
    }

    static byte[] concat(byte[]... parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] part : parts) {
            out.writeBytes(part);
        }
        return out.toByteArray();
    }
}
