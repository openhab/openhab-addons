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
package org.openhab.binding.homeconnectdirect.internal.common.utils;

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.*;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.i18n.HomeConnectDirectTranslationProvider;

/**
 * Home Connect Direct string utilities.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class StringUtils {

    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";
    public static final String HYPHEN = "-";
    public static final String SLASH = "/";
    public static final String OPENING_BRACE = "{";
    public static final String CLOSING_BRACE = "}";

    private static final Pattern CAMEL_CASE_SPLIT_PATTERN = Pattern
            .compile("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");

    private static final Map<String, String> LABEL_REPLACEMENTS = Map.ofEntries(Map.entry("Wi Fi", "WiFi"),
            Map.entry("Auto40", "Auto 40°"), Map.entry("Auto30", "Auto 30°"), Map.entry("Eco4060", "Eco 40°-60°"),
            Map.entry("Power Speed59", "powerSpeed 59′"), Map.entry("Super1530", "Super 15′/30′"),
            Map.entry("Plus1", "+1"), Map.entry("Plus2", "+2"), Map.entry("Plus3", "+3"),
            Map.entry("Intensiv70", "Intensiv 70°"), Map.entry("Auto2", "Auto 45°-65°"), Map.entry("Eco50", "Eco 50°"),
            Map.entry("Glas40", "Glas 40°"), Map.entry("Quick45", "Quick 45′"), Map.entry("Kurz60", "Short 60°"),
            Map.entry("Auto1", "Auto 35°-45°"), Map.entry("Auto3", "Auto 65°-75°"),
            Map.entry(STATE_INTENSIVE_STAGE_OFF, "Off"), Map.entry(STATE_INTENSIVE_STAGE_1, "1"),
            Map.entry(STATE_INTENSIVE_STAGE_2, "2"), Map.entry(STATE_FAN_OFF, "Off"), Map.entry(STATE_FAN_STAGE_1, "1"),
            Map.entry(STATE_FAN_STAGE_2, "2"), Map.entry(STATE_FAN_STAGE_3, "3"), Map.entry(STATE_FAN_STAGE_4, "4"),
            Map.entry(STATE_FAN_STAGE_5, "5"));

    private static final Pattern GC_PATTERN = Pattern.compile("GC(\\d{2})");
    private static final Pattern RPM_PATTERN = Pattern.compile("RPM(\\d+)");
    private static final Pattern UL_PREFIX_PATTERN = Pattern.compile("\\bUl\\s+");

    private StringUtils() {
        // Utility class
    }

    /**
     * Check whether the given string is {@code null}, empty or contains only whitespace.
     *
     * @param str the string to check (may be {@code null})
     * @return {@code true} if the string is {@code null}, empty or blank
     */
    public static boolean isBlank(@Nullable String str) {
        return str == null || str.isBlank();
    }

    /**
     * Null-safe check whether the given string ends with the given suffix.
     *
     * @param str the string to check (may be {@code null})
     * @param suffix the suffix to look for (may be {@code null})
     * @return {@code true} if the string ends with the suffix, {@code false} if either argument is {@code null}
     */
    public static boolean endsWith(@Nullable String str, @Nullable String suffix) {
        if (str == null || suffix == null) {
            return false;
        }
        return str.endsWith(suffix);
    }

    /**
     * Null-safe check whether the given string starts with the given prefix.
     *
     * @param str the string to check (may be {@code null})
     * @param prefix the prefix to look for (may be {@code null})
     * @return {@code true} if the string starts with the prefix, {@code false} if either argument is {@code null}
     */
    public static boolean startsWith(@Nullable String str, @Nullable String prefix) {
        if (str == null || prefix == null) {
            return false;
        }
        return str.startsWith(prefix);
    }

    /**
     * Return the substring after the first occurrence of the given separator.
     *
     * @param str the string to extract from (may be {@code null})
     * @param separator the separator whose first occurrence marks the start of the result (may be {@code null})
     * @return the substring after the first occurrence of the separator, or an empty string if the string is
     *         {@code null}/empty, the separator is {@code null} or the separator is not found
     */
    public static String substringAfter(@Nullable String str, @Nullable String separator) {
        if (str == null || str.isEmpty() || separator == null) {
            return EMPTY_STRING;
        }

        int index = str.indexOf(separator);
        if (index == -1) {
            return EMPTY_STRING;
        }

        return str.substring(index + separator.length());
    }

    /**
     * Null-safe, case-insensitive comparison of two strings.
     *
     * @param str1 the first string (may be {@code null})
     * @param str2 the second string (may be {@code null})
     * @return {@code true} if both are {@code null} or equal ignoring case, {@code false} otherwise
     */
    public static boolean equalsIgnoreCase(@Nullable String str1, @Nullable String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }

    /**
     * Null-safe check whether the given string contains the given search sequence.
     *
     * @param str the string to search in (may be {@code null})
     * @param search the sequence to look for (may be {@code null})
     * @return {@code true} if the string contains the search sequence, {@code false} if either argument is {@code null}
     */
    public static boolean contains(@Nullable String str, @Nullable String search) {
        if (str == null || search == null) {
            return false;
        }
        return str.contains(search);
    }

    /**
     * Convert a camelCase string to kebab-case (e.g. {@code fooBar} becomes {@code foo-bar}).
     *
     * @param input the string to convert (may be {@code null})
     * @return the kebab-case representation, or an empty string if the input is {@code null} or blank
     */
    public static String convertToKebabCase(@Nullable String input) {
        if (input == null || input.isBlank()) {
            return EMPTY_STRING;
        }

        Pattern pattern = Pattern.compile("([a-z])([A-Z])");
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll("$1-$2").toLowerCase(LOCALE);
    }

    /**
     * Convert the given string to lower case.
     *
     * @param input the string to convert (may be {@code null})
     * @return the lower-case representation, or an empty string if the input is {@code null} or blank
     */
    public static String toLowercase(@Nullable String input) {
        if (input == null || input.isBlank()) {
            return EMPTY_STRING;
        }
        return input.toLowerCase(LOCALE);
    }

    /**
     * Null-safe, case-insensitive check whether the given string contains the given search sequence.
     *
     * @param str the string to search in (may be {@code null})
     * @param search the sequence to look for (may be {@code null})
     * @return {@code true} if the string contains the search sequence ignoring case, {@code false} if either argument
     *         is
     *         {@code null}
     */
    public static boolean containsIgnoreCase(@Nullable String str, @Nullable String search) {
        if (str == null || search == null) {
            return false;
        }
        return str.toLowerCase(LOCALE).contains(search.toLowerCase(LOCALE));
    }

    /**
     * Map Home Connect key and value names to label (Using the translation provider if submitted).
     * e.g. Dishcare.Dishwasher.Program.Eco50 --> Eco50 or BSH.Common.EnumType.OperationState.DelayedStart --> Delayed
     * Start
     *
     * @param key key
     * @param translationProvider translation provider
     * @return human readable label
     */
    public static String mapKeyToLabel(String key, @Nullable HomeConnectDirectTranslationProvider translationProvider) {
        if (translationProvider != null) {
            var translation = translationProvider.getText(key);
            if (!translation.equals(key)) {
                return translation;
            }
        }

        String sub = key.substring(key.lastIndexOf(".") + 1);
        String label = String.join(" ", CAMEL_CASE_SPLIT_PATTERN.split(sub));

        label = LABEL_REPLACEMENTS.entrySet().stream().reduce(label,
                (res, entry) -> res.replace(entry.getKey(), entry.getValue()), (s1, s2) -> s1);

        label = GC_PATTERN.matcher(label).replaceAll("$1 °C");
        label = RPM_PATTERN.matcher(label).replaceAll("$1");
        label = UL_PREFIX_PATTERN.matcher(label).replaceAll("");

        return label;
    }

    /**
     * Heuristically check whether the given payload is likely a JSON object (starts with <code>{</code> and ends with
     * <code>}</code>).
     *
     * @param payload the payload to check
     * @return {@code true} if the trimmed payload looks like a JSON object
     */
    public static boolean isMostLikelyAJsonObject(String payload) {
        var json = payload.trim();
        return (json.startsWith("{") && json.endsWith("}"));
    }

    /**
     * Heuristically check whether the given payload is likely a JSON array (starts with <code>[</code> and ends with
     * <code>]</code>).
     *
     * @param payload the payload to check
     * @return {@code true} if the trimmed payload looks like a JSON array
     */
    public static boolean isMostLikelyAJsonArray(String payload) {
        var json = payload.trim();
        return (json.startsWith("[") && json.endsWith("]"));
    }

    /**
     * Remove all characters that are not alphanumeric or one of {@code . _ -} from the given string.
     *
     * @param input the string to sanitize (may be {@code null})
     * @return the sanitized string, or an empty string if the input is {@code null} or blank
     */
    public static String sanitize(@Nullable String input) {
        if (input == null || input.isBlank()) {
            return EMPTY_STRING;
        }
        return input.replaceAll("[^a-zA-Z0-9._-]", EMPTY_STRING);
    }
}
