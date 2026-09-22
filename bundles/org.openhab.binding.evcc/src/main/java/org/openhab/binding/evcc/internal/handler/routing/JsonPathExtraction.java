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
package org.openhab.binding.evcc.internal.handler.routing;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * Simple JSONPath-like extraction strategy for accessing nested JSON properties.
 *
 * Supports a limited subset of JSONPath syntax sufficient for EVCC API:
 * - $ for root element
 * - $.property for object properties
 * - $[index] for array indexing
 * - $.array[index] for nested array access
 * - $.property.nested for chained property access
 * - $.property[key] for object property access (treat brackets as property name)
 *
 * This lightweight implementation avoids external library dependencies while
 * providing enough flexibility for typical API structures.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class JsonPathExtraction implements ExtractionStrategy {

    private final Logger logger = LoggerFactory.getLogger(JsonPathExtraction.class);
    private final String jsonPath;

    /**
     * Creates a new JSONPath-like extraction strategy.
     *
     * @param jsonPath The JSONPath expression (e.g., "$.devices[0]" or "$.vehicles.car1")
     */
    public JsonPathExtraction(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    @Override
    @Nullable
    public JsonElement extract(JsonElement root) {
        try {
            logger.trace("Extracting with path: {}", jsonPath);
            return extractPath(root, jsonPath);
        } catch (Exception e) {
            logger.debug("Failed to extract path '{}': {}", jsonPath, e.getMessage());
            return null;
        }
    }

    @Nullable
    private JsonElement extractPath(JsonElement element, String path) {
        // Handle root element
        if ("$".equals(path)) {
            return element;
        }

        if (!path.startsWith("$")) {
            return null;
        }

        String remainder = path.substring(1);
        JsonElement current = element;

        while (!remainder.isEmpty()) {
            if (remainder.startsWith(".")) {
                remainder = remainder.substring(1);
                // Find next separator
                int propEnd = remainder.indexOf('[');
                int dotEnd = remainder.indexOf('.');
                int endIdx = -1;

                if (propEnd >= 0 && dotEnd >= 0) {
                    endIdx = Math.min(propEnd, dotEnd);
                } else if (propEnd >= 0) {
                    endIdx = propEnd;
                } else if (dotEnd >= 0) {
                    endIdx = dotEnd;
                } else {
                    endIdx = remainder.length();
                }

                String property = remainder.substring(0, endIdx);
                remainder = remainder.substring(endIdx);

                if (!current.isJsonObject()) {
                    return null;
                }

                current = current.getAsJsonObject().get(property);
                if (current == null) {
                    return null;
                }
            } else if (remainder.startsWith("[")) {
                int closingBracketIdx = remainder.indexOf(']');
                if (closingBracketIdx < 0) {
                    return null;
                }

                String indexStr = remainder.substring(1, closingBracketIdx);
                remainder = remainder.substring(closingBracketIdx + 1);

                try {
                    int index = Integer.parseInt(indexStr);
                    if (!current.isJsonArray()) {
                        return null;
                    }
                    JsonArray array = current.getAsJsonArray();
                    if (index < 0 || index >= array.size()) {
                        return null;
                    }
                    current = array.get(index);
                } catch (NumberFormatException e) {
                    // Try as property access for object keys like vehicles[vehicleId]
                    if (!current.isJsonObject()) {
                        return null;
                    }
                    current = current.getAsJsonObject().get(indexStr);
                    if (current == null) {
                        return null;
                    }
                }
            } else {
                // Unexpected format
                return null;
            }
        }

        return current;
    }

    @Override
    public String describe() {
        return "JsonPath: " + jsonPath;
    }

    @Override
    public String toString() {
        return describe();
    }
}
