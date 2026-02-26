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
package org.openhab.io.yamlcomposer.internal.processors;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.placeholders.InterpolablePlaceholder;

/**
 * The {@link FragmentUtils} provides common functionality
 * for dealing with {@link org.openhab.io.yamlcomposer.internal.placeholders.IncludePlaceholder} and
 * {@link org.openhab.io.yamlcomposer.internal.placeholders.InsertPlaceholder} parameters.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class FragmentUtils {

    public static record Parameters(@Nullable String name, Map<String, @Nullable Object> varsMap) {
    }

    /**
     * Parses the parameters from an IncludePlaceholder or InsertPlaceholder.
     */
    public static @Nullable Parameters parseParameters(InterpolablePlaceholder<?> placeholder, String objectName) {
        return switch (placeholder.value()) {
            case null -> null;
            case String name -> parseStringParameters(name, objectName);
            case Map<?, ?> paramsMap -> parseMapParameters(paramsMap, objectName);
            default -> null;
        };
    }

    private static @Nullable Parameters parseMapParameters(Map<?, ?> paramsMap, String objectName) {
        @Nullable
        String name = (String) paramsMap.get(objectName);
        if (!(paramsMap.get("vars") instanceof Map<?, ?> varsMap)) {
            return new Parameters(name, Map.of());
        }

        Map<String, @Nullable Object> vars = varsMap.entrySet().stream().collect(LinkedHashMap::new,
                (m, v) -> m.put(String.valueOf(v.getKey()), v.getValue()), LinkedHashMap::putAll);
        return new Parameters(name, vars);
    }

    private static Parameters parseStringParameters(String input, String objectName) {
        if (input.isBlank()) {
            return new Parameters(null, Map.of());
        }

        // 1. Separate Name and Query without substrings yet
        int queryStart = input.indexOf('?');

        // Process the Fragment Name
        String rawName = (queryStart == -1) ? input : input.substring(0, queryStart);
        String decodedName = safeDecode(rawName.trim());
        @Nullable
        String finalName = decodedName.isEmpty() ? null : decodedName;

        // 2. Early exit if no query
        if (queryStart == -1 || queryStart == input.length() - 1) {
            return new Parameters(finalName, Map.of());
        }

        // 3. Parse Query String efficiently
        Map<String, @Nullable Object> vars = new LinkedHashMap<>();
        int len = input.length();
        int pos = queryStart + 1;

        while (pos < len) {
            int nextAmp = input.indexOf('&', pos);
            int end = (nextAmp == -1) ? len : nextAmp;

            if (end > pos) { // Ignore empty pairs like '&&'
                parsePair(input, pos, end, vars);
            }
            pos = end + 1;
        }

        return new Parameters(finalName, vars);
    }

    private static void parsePair(String input, int start, int end, Map<String, Object> vars) {
        int eq = input.indexOf('=', start);

        // If eq is outside our current segment, there is no value
        if (eq == -1 || eq >= end) {
            String key = safeDecode(input.substring(start, end).trim());
            if (!key.isEmpty()) {
                vars.put(key, Boolean.TRUE);
            }
        } else {
            String key = safeDecode(input.substring(start, eq).trim());
            String val = safeDecode(input.substring(eq + 1, end));
            if (!key.isEmpty()) {
                vars.put(key, val);
            }
        }
    }

    private static String safeDecode(String str) {
        if (str.indexOf('%') == -1 && str.indexOf('+') == -1) {
            return str; // Fast path: nothing to decode
        }
        try {
            return URLDecoder.decode(str, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return str;
        }
    }

    /**
     * Converts the Parameters back to a value object for IncludePlaceholder or InsertPlaceholder.
     */
    public static @Nullable Object toValue(@Nullable String name, Map<String, @Nullable Object> varsMap,
            String objectName) {
        if (varsMap.isEmpty()) {
            return name;
        }
        if (name == null) {
            return Map.of("vars", varsMap);
        }
        return Map.of(objectName, name, "vars", varsMap);
    }
}
