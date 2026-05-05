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

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.LOCALE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_FAN_OFF;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_FAN_STAGE_1;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_FAN_STAGE_2;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_FAN_STAGE_3;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_FAN_STAGE_4;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_FAN_STAGE_5;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_INTENSIVE_STAGE_1;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_INTENSIVE_STAGE_2;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.STATE_INTENSIVE_STAGE_OFF;

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

    public static boolean isBlank(@Nullable String str) {
        return str == null || str.isBlank();
    }

    public static boolean isNotBlank(@Nullable String str) {
        return !isBlank(str);
    }

    public static boolean endsWith(@Nullable String str, @Nullable String suffix) {
        if (str == null || suffix == null) {
            return false;
        }
        return str.endsWith(suffix);
    }

    public static boolean startsWith(@Nullable String str, @Nullable String prefix) {
        if (str == null || prefix == null) {
            return false;
        }
        return str.startsWith(prefix);
    }

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

    public static boolean equalsIgnoreCase(@Nullable String str1, @Nullable String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }

    public static String capitalize(@Nullable String str) {
        if (str == null || str.isEmpty()) {
            return EMPTY_STRING;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static boolean contains(@Nullable String str, @Nullable String search) {
        if (str == null || search == null) {
            return false;
        }
        return str.contains(search);
    }

    public static String convertToKebabCase(@Nullable String input) {
        if (input == null || input.isBlank()) {
            return EMPTY_STRING;
        }

        Pattern pattern = Pattern.compile("([a-z])([A-Z])");
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll("$1-$2").toLowerCase(LOCALE);
    }

    public static String toLowercase(@Nullable String input) {
        if (input == null || input.isBlank()) {
            return EMPTY_STRING;
        }
        return input.toLowerCase(LOCALE);
    }

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

    public static boolean isMostLikelyAJsonObject(String payload) {
        var json = payload.trim();
        return (json.startsWith("{") && json.endsWith("}"));
    }

    public static boolean isMostLikelyAJsonArray(String payload) {
        var json = payload.trim();
        return (json.startsWith("[") && json.endsWith("]"));
    }
}
