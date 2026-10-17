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
package org.openhab.binding.hasslink.internal.util;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.EntityId;

/**
 * Utility class providing string parsing, normalization, and prefix calculation
 * functions for Home Assistant Entity IDs and openHAB Channel mapping.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public final class EntityUtils {

    private static final Pattern INVALID_CHARS = Pattern.compile("[^a-z0-9_-]+");
    private static final Pattern SURROUNDING_UNDERSCORES = Pattern.compile("^_+|_+$");
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(EntityUtils.class);

    private EntityUtils() {
        // Utility class constructor
    }

    /**
     * Validates whether a raw string follows the Home Assistant entity ID format (domain.object_id).
     */
    public static boolean isValidEntityId(String rawEntityId) {
        int dotIndex = rawEntityId.indexOf('.');
        return dotIndex > 0 && dotIndex < rawEntityId.length() - 1;
    }

    /**
     * Parses a raw entity ID string into an {@link EntityId}.
     * <p>
     * If parsing fails due to a malformed or blank string, a descriptive warning
     * is logged internally by this method before returning {@code null}.
     *
     * @param rawEntityId the raw entity ID string
     * @param defaultDomain optional default domain to apply if no domain prefix exists
     * @return the parsed {@link EntityId}, or {@code null} if parsing failed
     */
    public static @Nullable EntityId parseEntityId(String rawEntityId, String defaultDomain) {
        if (rawEntityId.isBlank()) {
            return null;
        }

        int dotIndex = rawEntityId.indexOf('.');

        // Malformed: Leading dot (e.g. ".reboot")
        if (dotIndex == 0) {
            LOGGER.warn("Malformed entity ID '{}': missing domain before dot", rawEntityId);
            return null;
        }

        // Malformed: Trailing dot (e.g. "button.")
        if (dotIndex == rawEntityId.length() - 1) {
            LOGGER.warn("Malformed entity ID '{}': missing object ID after dot", rawEntityId);
            return null;
        }

        // Standard domain.object_id
        if (dotIndex > 0) {
            return new EntityId(rawEntityId.substring(0, dotIndex), rawEntityId.substring(dotIndex + 1));
        }

        // No dot present (e.g. "reboot")
        if (!defaultDomain.isEmpty()) {
            return new EntityId(defaultDomain, rawEntityId);
        }

        LOGGER.warn("Entity ID '{}' is missing a domain prefix and no default domain was provided", rawEntityId);
        return null;
    }

    /**
     * Parses a raw entity ID string requiring an explicit domain prefix (e.g., "sensor.temperature").
     * <p>
     * If parsing fails due to a malformed, blank, or domain-less string, a descriptive warning is
     * logged internally before returning {@code null}.
     *
     * @param rawEntityId the raw entity ID string
     * @return the parsed {@link EntityId}, or {@code null} if malformed, blank, or missing a domain prefix
     */
    public static @Nullable EntityId parseEntityId(String rawEntityId) {
        return parseEntityId(rawEntityId, "");
    }

    /**
     * Sanitizes an arbitrary string into a valid openHAB Channel or Thing ID segment
     * (lowercased, alphanumeric with hyphens and underscores, stripping leading and trailing underscores).
     *
     * @param value the string to sanitize
     * @return the sanitized string
     */
    public static String sanitize(String value) {
        if (value.isBlank()) {
            return "";
        }
        String cleaned = INVALID_CHARS.matcher(value.toLowerCase()).replaceAll("_");
        return SURROUNDING_UNDERSCORES.matcher(cleaned).replaceAll("");
    }

    public static boolean isValidUID(String value) {
        return sanitize(value).equals(value.toLowerCase());
    }

    /**
     * Calculates the token-aligned Longest Common Prefix (LCP) across a collection of strings.
     * If the raw prefix matches a full string in the list, it is preserved. Otherwise, it snaps
     * back to the last occurrence of the specified delimiter character.
     */
    public static String calculateCommonPrefix(@Nullable List<String> strings, char delimiter) {
        if (strings == null || strings.isEmpty()) {
            return "";
        }

        List<String> sorted = strings.stream().sorted().toList();
        String first = sorted.getFirst();
        String last = sorted.getLast();

        int minLength = Math.min(first.length(), last.length());
        int i = 0;
        while (i < minLength && first.charAt(i) == last.charAt(i)) {
            i++;
        }

        String rawPrefix = first.substring(0, i);

        // If the raw prefix is an exact match for any full string in the collection, return it directly
        if (strings.contains(rawPrefix)) {
            return rawPrefix;
        }

        // Otherwise, snap back to the last token boundary (delimiter)
        int lastDelimiter = rawPrefix.lastIndexOf(delimiter);
        return lastDelimiter != -1 ? rawPrefix.substring(0, lastDelimiter) : "";
    }

    public static boolean isModeInList(@Nullable EntityState entityState, String attributeName, String targetValue) {
        if (entityState == null) {
            return false;
        }
        return entityState.getAttributeAsStringList(attributeName) //
                .stream() //
                .anyMatch(targetValue::equalsIgnoreCase);
    }
}
