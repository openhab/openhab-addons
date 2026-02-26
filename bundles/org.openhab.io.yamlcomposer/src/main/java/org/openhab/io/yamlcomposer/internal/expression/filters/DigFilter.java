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
package org.openhab.io.yamlcomposer.internal.expression.filters;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;

/**
 * Custom Jinjava filter to dig into nested maps/lists.
 * Supports negative indices for lists to acess elements from the end.
 *
 * Usage: variable|dig("key1", "key2", 0) to access variable["key1"]["key2"][0]
 * Also supports dot-notation in a single argument: variable|dig("key1.key2.0")
 */
@NonNullByDefault
public class DigFilter implements Filter {
    @Override
    public String getName() {
        return "dig";
    }

    @Override
    @NonNullByDefault({})
    public @Nullable Object filter(@Nullable Object var, JinjavaInterpreter interpreter, String... args) {
        Object current = var;

        // Expand any dot-notation args (e.g. "a.b.c") into separate key segments
        java.util.List<Object> keys = new java.util.ArrayList<>();
        if (args != null) {
            for (String arg : args) {
                if (arg != null && arg.contains(".")) {
                    String[] parts = arg.split("\\.");
                    for (String p : parts) {
                        keys.add(p);
                    }
                } else {
                    keys.add(arg);
                }
            }
        }

        for (Object key : keys) { // Changed to Object to be more flexible
            if (current == null)
                return null;

            if (current instanceof Map) {
                // Java Maps (like HashMap) can have a null key
                current = ((Map<?, ?>) current).get(key);
            } else if (current instanceof List) {
                if (key == null)
                    return null; // A list index cannot be null
                current = getFromList((List<?>) current, key);
            } else {
                return null;
            }
        }
        return current;
    }

    private @Nullable Object getFromList(List<?> list, Object key) {
        try {
            int index;
            if (key instanceof Integer intKey) {
                index = intKey;
            } else {
                index = Integer.parseInt(key.toString());
            }

            int size = list.size();
            if (index < 0)
                index = size + index;

            if (index >= 0 && index < size) {
                return list.get(index);
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }
}
