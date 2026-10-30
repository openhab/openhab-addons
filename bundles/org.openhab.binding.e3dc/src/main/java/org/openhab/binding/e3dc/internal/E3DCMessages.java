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
package org.openhab.binding.e3dc.internal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;

/**
 * The messages the energy manager keeps, such as a schedule that is being executed or a lost connection to the
 * smart meter gateway.
 *
 * <p>
 * The device answers the request for its stored errors with one container per message. Despite the name, not
 * every message is an error: the type tells them apart, and the device reports a running schedule with type 1,
 * the same as a lost connection.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class E3DCMessages {

    /** Seconds since 1970 stay below this until the year 5138, so a larger number counts milliseconds. */
    private static final long MILLISECONDS_THRESHOLD = 100_000_000_000L;

    private E3DCMessages() {
    }

    /**
     * One message of the energy manager.
     *
     * @param type the kind of message as the device numbers it, or {@code null} when it did not say
     * @param source the part of the system that raised the message, or {@code null}
     * @param code the code of the message, or {@code null}
     * @param text the text of the message, or {@code null}
     * @param timestamp when the message was raised, or {@code null} when the device did not say
     */
    public record Message(@Nullable Integer type, @Nullable String source, @Nullable Integer code,
            @Nullable String text, @Nullable Instant timestamp) {
    }

    /**
     * The messages a poll response carries.
     *
     * @return the messages in the order of the response, an empty list when the device reports none, or
     *         {@code null} when the response holds no answer to the request, so nothing is known
     */
    public static @Nullable List<Message> fromResponse(RscpValue response) {
        RscpValue stored = response.find(RscpTag.EMS_STORED_ERRORS);
        if (stored == null || stored.isError()) {
            return null;
        }
        List<Message> messages = new ArrayList<>();
        for (RscpValue container : stored.findAll(RscpTag.EMS_ERROR_CONTAINER)) {
            Message message = fromContainer(container);
            if (message != null) {
                messages.add(message);
            }
        }
        return messages;
    }

    /** One message, or {@code null} when the container names neither a text nor a code. */
    private static @Nullable Message fromContainer(RscpValue container) {
        Number type = container.findNumber(RscpTag.EMS_ERROR_TYPE);
        Number code = container.findNumber(RscpTag.EMS_ERROR_CODE);
        String source = container.findString(RscpTag.EMS_ERROR_SOURCE);
        String text = container.findString(RscpTag.EMS_ERROR_MESSAGE);
        if (text == null && code == null) {
            return null;
        }
        RscpValue moment = container.find(RscpTag.EMS_ERROR_TIMESTAMP);
        return new Message(type == null ? null : type.intValue(), clean(source), code == null ? null : code.intValue(),
                clean(text), moment == null ? null : timestampOf(moment));
    }

    private static @Nullable String clean(@Nullable String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** The moment a value names, or {@code null} when it is none or zero, which means nothing happened yet. */
    static @Nullable Instant timestampOf(RscpValue value) {
        Object raw = value.getValue();
        long seconds;
        if (raw instanceof byte[] bytes && value.getType() == RscpType.TIMESTAMP && bytes.length >= 8) {
            seconds = ByteBuffer.wrap(bytes, 0, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();
        } else if (raw instanceof Number number) {
            seconds = number.longValue();
        } else {
            return null;
        }
        if (seconds <= 0) {
            return null;
        }
        return seconds > MILLISECONDS_THRESHOLD ? Instant.ofEpochMilli(seconds) : Instant.ofEpochSecond(seconds);
    }

    /**
     * The messages as a JSON array, for example
     * {@code [{"type":1,"source":"EMS","code":32803,"message":"A schedule is being executed! (AI360)"}]}.
     *
     * <p>
     * A field the device did not report is left out. Without messages the array is empty.
     */
    public static String toJson(List<Message> messages) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (Message message : messages) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append('{');
            boolean firstField = true;
            firstField = appendNumber(json, "type", message.type(), firstField);
            firstField = appendText(json, "source", message.source(), firstField);
            firstField = appendNumber(json, "code", message.code(), firstField);
            firstField = appendText(json, "message", message.text(), firstField);
            Instant timestamp = message.timestamp();
            appendText(json, "timestamp", timestamp == null ? null : timestamp.toString(), firstField);
            json.append('}');
        }
        return json.append(']').toString();
    }

    private static boolean appendNumber(StringBuilder json, String name, @Nullable Integer value, boolean first) {
        if (value == null) {
            return first;
        }
        separator(json, first);
        json.append('"').append(name).append("\":").append(value);
        return false;
    }

    private static boolean appendText(StringBuilder json, String name, @Nullable String value, boolean first) {
        if (value == null) {
            return first;
        }
        separator(json, first);
        json.append('"').append(name).append("\":\"");
        escape(json, value);
        json.append('"');
        return false;
    }

    private static void separator(StringBuilder json, boolean first) {
        if (!first) {
            json.append(',');
        }
    }

    private static void escape(StringBuilder json, String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (c < 0x20) {
                        json.append(String.format("\\u%04x", (int) c));
                    } else {
                        json.append(c);
                    }
                }
            }
        }
    }
}
