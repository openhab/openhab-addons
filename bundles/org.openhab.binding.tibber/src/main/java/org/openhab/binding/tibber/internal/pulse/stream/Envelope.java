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
package org.openhab.binding.tibber.internal.pulse.stream;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The header of a stream message: {@code <device:<16 hex digits> topic:"<topic>">}, followed by the payload.
 * It works on bytes, so text and binary messages are treated alike.
 *
 * @param deviceId the Pulse device id, 16 hexadecimal digits in lower case
 * @param topic the topic of the message
 * @param payloadOffset index of the first payload byte in the message
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public record Envelope(String deviceId, String topic, int payloadOffset) {

    public static final String TOPIC_SML = "sml";

    private static final byte[] PREFIX = "<device:".getBytes(StandardCharsets.US_ASCII);
    private static final String TOPIC_PREFIX = " topic:\"";
    private static final int DEVICE_ID_LENGTH = 16;
    /** The observed header is 37 bytes; the closing bracket is only searched within this window. */
    private static final int MAX_HEADER_LENGTH = 64;

    /**
     * Parses the header of a message.
     *
     * @param message the complete message
     * @return the envelope, or null if the header is missing, malformed, or has a device id that is not 16
     *         hexadecimal digits
     */
    public static @Nullable Envelope parse(byte[] message) {
        if (message.length < PREFIX.length + DEVICE_ID_LENGTH) {
            return null;
        }
        for (int i = 0; i < PREFIX.length; i++) {
            if (message[i] != PREFIX[i]) {
                return null;
            }
        }
        int limit = Math.min(message.length, MAX_HEADER_LENGTH);
        int close = -1;
        for (int i = PREFIX.length + DEVICE_ID_LENGTH; i < limit; i++) {
            if (message[i] == '>') {
                close = i;
                break;
            }
        }
        if (close < 0) {
            return null;
        }
        int idEnd = PREFIX.length + DEVICE_ID_LENGTH;
        for (int i = PREFIX.length; i < idEnd; i++) {
            if (!isHexDigit(message[i])) {
                return null;
            }
        }
        String deviceId = new String(message, PREFIX.length, DEVICE_ID_LENGTH, StandardCharsets.US_ASCII)
                .toLowerCase(Locale.ROOT);
        String rest = new String(message, idEnd, close - idEnd, StandardCharsets.US_ASCII);
        if (!rest.startsWith(TOPIC_PREFIX) || !rest.endsWith("\"") || rest.length() < TOPIC_PREFIX.length() + 2) {
            return null;
        }
        String topic = rest.substring(TOPIC_PREFIX.length(), rest.length() - 1);
        if (topic.indexOf('"') >= 0) {
            return null;
        }
        return new Envelope(deviceId, topic, close + 1);
    }

    public boolean isSml() {
        return TOPIC_SML.equals(topic);
    }

    private static boolean isHexDigit(byte b) {
        return (b >= '0' && b <= '9') || (b >= 'a' && b <= 'f') || (b >= 'A' && b <= 'F');
    }

    /** Omits the device id: it must not reach a log at info level or above. */
    @Override
    public String toString() {
        return "Envelope[topic=" + topic + ", payloadOffset=" + payloadOffset + "]";
    }
}
