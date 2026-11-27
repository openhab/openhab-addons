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
package org.openhab.io.yamlcomposer.internal.dynamic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility class for working with registry entities, providing methods to create immutable copies of maps and lists.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public final class RegistryEntityUtils {

    private RegistryEntityUtils() {
    }

    public static Map<String, @Nullable Object> immutableMap(Map<String, ? extends @Nullable Object> map) {
        Map<String, @Nullable Object> result = new LinkedHashMap<>(map.size());
        map.forEach((key, value) -> {
            result.put(key, immutableValue(value));
        });
        return Collections.unmodifiableMap(result);
    }

    private static @Nullable Object immutableValue(@Nullable Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, @Nullable Object> result = new LinkedHashMap<>(map.size());
            map.forEach((key, nestedValue) -> {
                if (key != null) {
                    result.put(String.valueOf(key), immutableValue(nestedValue));
                }
            });
            return Collections.unmodifiableMap(result);
        }
        if (value instanceof List<?> list) {
            List<@Nullable Object> result = new ArrayList<>(list.size());
            for (Object nestedValue : list) {
                result.add(immutableValue(nestedValue));
            }
            return Collections.unmodifiableList(result);
        }
        return value;
    }
}
