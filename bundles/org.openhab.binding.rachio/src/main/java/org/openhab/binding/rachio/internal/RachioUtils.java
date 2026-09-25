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
package org.openhab.binding.rachio.internal;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;

import com.google.gson.Gson;

/**
 * {@link RachioUtils} provides some helper functions
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public final class RachioUtils {
    private static final Gson GSON = new Gson();

    private RachioUtils() {
    }

    public static String getString(@Nullable String value) {
        return value != null ? value : "";
    }

    public static String firstNonBlank(@Nullable String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    public static void putIfNotBlank(Map<String, String> values, String key, @Nullable String value) {
        if (value != null && !value.isBlank()) {
            values.put(key, value);
        }
    }

    public static String exceptionMessage(Throwable e) {
        String message = e.getMessage();
        return message != null && !message.isBlank() ? message : e.toString();
    }

    public static String i18nText(String key, Object... arguments) {
        if (arguments.length == 0) {
            return "@text/" + key;
        }

        String[] serializedArguments = new String[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            serializedArguments[i] = String.valueOf(arguments[i]);
        }
        return "@text/" + key + " " + GSON.toJson(serializedArguments);
    }

    public static boolean isSameInstance(@Nullable Object first, @Nullable Object second) {
        return first == second; // NOPMD - lifecycle guards intentionally require reference identity
    }

    public static String urlEncode(String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }

    public static DateTimeType getTimestamp() {
        return new DateTimeType(Instant.now());
    }
}
