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
package org.openhab.binding.bmwcardata.internal.stream;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue.StreamMessage;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * Parses CarData stream messages.
 * <p>
 * BMW publishes one JSON envelope per message on topic {@code <gcid>/<vin>}:
 *
 * <pre>
 * {
 *   "vin": "...",
 *   "entityId": "...",
 *   "topic": "...",
 *   "timestamp": "...",
 *   "data": { "vehicle.x.y": { "value": ..., "unit": "...", "timestamp": "..." }, ... }
 * }
 * </pre>
 *
 * The bmw-mqtt-bridge republishes this envelope unchanged on {@code <prefix>raw/<vin>}, so the same parser serves
 * both stream sources.
 *
 * @author Mark Herwege - Initial contribution
 * @author Michael Weger - Adaptation to the BMW CarData binding
 */
@NonNullByDefault
public final class StreamMessageParser {

    private static final Gson GSON = new Gson();

    /**
     * A parsed message: the vehicle it concerns and its data.
     *
     * @param vin vehicle identification number
     * @param data values by technical descriptor
     */
    public record ParsedMessage(String vin, Map<String, TelematicValue> data) {
    }

    private StreamMessageParser() {
        // utility class
    }

    /**
     * @param topic topic the message arrived on, used for the VIN if the payload does not carry it
     * @param payload message payload
     * @return the parsed message, or {@code null} if the payload is not a CarData envelope with data
     */
    public static @Nullable ParsedMessage parse(String topic, byte[] payload) {
        StreamMessage message;
        try {
            message = GSON.fromJson(new String(payload, StandardCharsets.UTF_8), StreamMessage.class);
        } catch (JsonParseException e) {
            return null;
        }
        if (message == null) {
            return null;
        }
        String vin = message.vin;
        if (vin == null || vin.isBlank()) {
            vin = lastTopicLevel(topic);
        }
        Map<String, TelematicValue> data = message.data;
        if (vin == null || data == null || data.isEmpty()) {
            return null;
        }
        return new ParsedMessage(vin, data);
    }

    private static @Nullable String lastTopicLevel(String topic) {
        String level = topic.substring(topic.lastIndexOf('/') + 1);
        return level.isBlank() ? null : level;
    }
}
