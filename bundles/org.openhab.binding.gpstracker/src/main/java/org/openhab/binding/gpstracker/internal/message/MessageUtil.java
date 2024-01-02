/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.gpstracker.internal.message;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gpstracker.internal.message.dto.LocationMessage;
import org.openhab.binding.gpstracker.internal.message.dto.TransitionMessage;

import com.google.gson.Gson;

/**
 * Message handling utility
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class MessageUtil {
    /**
     * Patterns to identify incoming JSON payload.
     */
    private static final String[] PATTERNS = new String[] { ".*\"_type\"\\s*:\\s*\"transition\".*", // transition
            ".*\"_type\"\\s*:\\s*\"location\".*", // location
    };

    /**
     * Supported message types
     */
    private static final Map<String, Class<? extends LocationMessage>> MESSAGE_TYPES = new HashMap<>();

    static {
        MESSAGE_TYPES.put(PATTERNS[0], TransitionMessage.class);
        MESSAGE_TYPES.put(PATTERNS[1], LocationMessage.class);
    }

    private final Gson gson = new Gson();

    /**
     * Parses JSON message into an object with type determined by message pattern.
     *
     * @param json JSON string.
     * @return Parsed message POJO or null without pattern match
     */
    public @Nullable LocationMessage fromJson(String json) {
        for (String pattern : PATTERNS) {
            Class<? extends LocationMessage> c = MESSAGE_TYPES.get(pattern);
            if (c != null && json.matches(pattern)) {
                return gson.fromJson(json, c);
            }
        }
        return null;
    }

    /**
     * Converts object to JSON sting.
     *
     * @param o Object to convert
     * @return JSON string
     */
    public String toJson(Object o) {
        return gson.toJson(o);
    }
}
