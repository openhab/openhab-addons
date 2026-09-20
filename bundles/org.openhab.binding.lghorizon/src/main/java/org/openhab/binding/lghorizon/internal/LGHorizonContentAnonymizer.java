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
package org.openhab.binding.lghorizon.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Static methods to remove sensitive content from captured LG Horizon REST/MQTT payloads, for use in the
 * {@code lghorizon fingerprint} console command and logging.
 * <p>
 * Two anonymization styles are used, matching what's actually useful to a maintainer analyzing a dump:
 * <ul>
 * <li>Fields worth correlating across a dump (the same device/customer/profile appearing in several files)
 * get a stable per-value placeholder via a counter map, so repeated occurrences of the same real value always
 * anonymize to the same placeholder within a session.</li>
 * <li>Fields with no cross-referencing value (MAC/IP addresses) get a single fixed placeholder.</li>
 * </ul>
 *
 * @author Mark - Initial contribution
 */
@NonNullByDefault
public final class LGHorizonContentAnonymizer {

    // Avoid maps growing unboundedly in a long-running openHAB instance.
    private static final int MAX_TRACKED_VALUES_PER_MAP = 500;

    private static final Map<String, String> CUSTOMER_ID_MAP = new ConcurrentHashMap<>();
    private static final AtomicInteger CUSTOMER_ID_COUNTER = new AtomicInteger();

    private static final Map<String, String> HASHED_ID_MAP = new ConcurrentHashMap<>();
    private static final AtomicInteger HASHED_ID_COUNTER = new AtomicInteger();

    private static final Map<String, String> DEVICE_ID_MAP = new ConcurrentHashMap<>();
    private static final AtomicInteger DEVICE_ID_COUNTER = new AtomicInteger();

    private static final Map<String, String> DEVICE_NAME_MAP = new ConcurrentHashMap<>();
    private static final AtomicInteger DEVICE_NAME_COUNTER = new AtomicInteger();

    private static final Map<String, String> SERIAL_MAP = new ConcurrentHashMap<>();
    private static final AtomicInteger SERIAL_COUNTER = new AtomicInteger();

    private static final Map<String, String> PROFILE_ID_MAP = new ConcurrentHashMap<>();
    private static final AtomicInteger PROFILE_ID_COUNTER = new AtomicInteger();

    private static final Map<String, String> AD_DEVICE_ID_MAP = new ConcurrentHashMap<>();
    private static final AtomicInteger AD_DEVICE_ID_COUNTER = new AtomicInteger();

    private static final Map<String, String> PIN_MAP = new ConcurrentHashMap<>();
    private static final AtomicInteger PIN_COUNTER = new AtomicInteger();

    private static final Map<String, String> TOKEN_MAP = new ConcurrentHashMap<>();
    private static final AtomicInteger TOKEN_COUNTER = new AtomicInteger();

    private static final Map<String, String> HOUSEHOLD_ID_MAP = new ConcurrentHashMap<>();
    private static final AtomicInteger HOUSEHOLD_ID_COUNTER = new AtomicInteger();

    private static final Map<String, String> CITY_ID_MAP = new ConcurrentHashMap<>();
    private static final AtomicInteger CITY_ID_COUNTER = new AtomicInteger();

    // Field-name-scoped patterns: "<fieldName>":"<value>" (value may be empty)
    private static final Pattern CUSTOMER_ID_PATTERN = fieldPattern("customerId");
    private static final Pattern HASHED_ID_PATTERN = fieldPattern("hashed\\w*Id");
    private static final Pattern DEVICE_ID_PATTERN = fieldPattern("deviceId");
    private static final Pattern SOURCE_AS_DEVICE_ID_PATTERN = fieldPattern("source");
    private static final Pattern DEVICE_NAME_PATTERN = fieldPattern("deviceFriendlyName");
    private static final Pattern SERIAL_PATTERN = fieldPattern("serialNumber");
    private static final Pattern PROFILE_ID_PATTERN = fieldPattern("profileId");
    private static final Pattern DEFAULT_PROFILE_ID_PATTERN = fieldPattern("defaultProfileId");
    private static final Pattern AD_DEVICE_ID_PATTERN = fieldPattern("advertisementDeviceId");
    private static final Pattern PIN_PATTERN = fieldPattern("pin");
    private static final Pattern TOKEN_PATTERN = fieldPattern(
            "(accessToken|refreshToken|claimsToken|token|access_token|refresh_token)");

    // Content-shape patterns, not scoped to a specific field name.
    private static final Pattern MAC_ADDRESS_PATTERN = fieldPattern("\\w*[Mm]ac\\w*",
            "([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}");
    private static final Pattern IP_ADDRESS_PATTERN = fieldPattern("\\w*[Ii][Pp]\\w*",
            "(\\d{1,3}\\.){3}\\d{1,3}(/(\\d{1,3}\\.){3}\\d{1,3})?");

    // cityId and householdId patterns are not field name scoped and required special handling
    // cityId shape used in JSON (e.g. "cityId":12345 and URL (?cityId=12345&...).
    private static final Pattern CITY_ID_JSON_PATTERN = Pattern
            .compile("(?<leading>\"cityId\"\\s*:\\s*)(?<value>\\d+)");
    private static final Pattern CITY_ID_URL_PATTERN = Pattern.compile("(?<leading>[?&]cityId=)(?<value>\\d+)");
    // household id shape (e.g. "DTV123456_be")
    private static final Pattern HOUSEHOLD_ID_PATTERN = Pattern.compile("\\b[A-Z]{2,6}\\d{4,10}_[a-z]{2}\\b");

    private LGHorizonContentAnonymizer() {
        // static utility class
    }

    private static Pattern fieldPattern(String fieldNameRegex) {
        return fieldPattern(fieldNameRegex, "[^\"]*");
    }

    private static Pattern fieldPattern(String fieldNameRegex, String valueRegex) {
        return Pattern.compile("(?<leading>\"" + fieldNameRegex + "\")\\s*:\\s*\"(?<value>" + valueRegex + ")\"");
    }

    /**
     * Anonymizes an MQTT topic string (may contain an embedded household id, e.g.
     * {@code DTV123456_be/DEVICE_ID/status}).
     *
     * @param topic the MQTT topic string to anonymize
     * @return the topic string with any sensitive fields replaced by anonymized placeholders, or null if the input was
     *         null
     */
    public static @Nullable String anonymizeTopic(@Nullable String topic) {
        if (topic == null) {
            return null;
        }
        String withCityId = replaceNumericField(topic, CITY_ID_URL_PATTERN, CITY_ID_MAP, CITY_ID_COUNTER);
        String withHouseholdId = replaceConsistently(withCityId, HOUSEHOLD_ID_PATTERN, "HOUSEHOLD_", HOUSEHOLD_ID_MAP,
                HOUSEHOLD_ID_COUNTER);
        return replaceKnownDeviceIds(withHouseholdId);
    }

    /**
     * Anonymizes a JSON (or plain text) payload: REST response bodies, MQTT message bodies, or anything else
     * that might contain the sensitive fields listed on this class.
     *
     * @param message the payload to anonymize
     * @return the payload with any sensitive fields replaced by anonymized placeholders, or null if the input was null
     */
    public static @Nullable String anonymizeMessage(@Nullable String message) {
        if (message == null) {
            return null;
        }

        String anonymized = message;
        anonymized = replaceField(anonymized, CUSTOMER_ID_PATTERN, "CUSTOMER_", CUSTOMER_ID_MAP, CUSTOMER_ID_COUNTER);
        anonymized = replaceField(anonymized, HASHED_ID_PATTERN, "HASHED_", HASHED_ID_MAP, HASHED_ID_COUNTER);
        anonymized = replaceField(anonymized, DEVICE_ID_PATTERN, "DEVICE_", DEVICE_ID_MAP, DEVICE_ID_COUNTER);
        anonymized = replaceField(anonymized, SOURCE_AS_DEVICE_ID_PATTERN, "DEVICE_", DEVICE_ID_MAP, DEVICE_ID_COUNTER);
        anonymized = replaceField(anonymized, DEVICE_NAME_PATTERN, "DEVICE_NAME_", DEVICE_NAME_MAP,
                DEVICE_NAME_COUNTER);
        anonymized = replaceField(anonymized, SERIAL_PATTERN, "SERIAL_", SERIAL_MAP, SERIAL_COUNTER);
        anonymized = replaceField(anonymized, PROFILE_ID_PATTERN, "PROFILE_", PROFILE_ID_MAP, PROFILE_ID_COUNTER);
        anonymized = replaceField(anonymized, DEFAULT_PROFILE_ID_PATTERN, "PROFILE_", PROFILE_ID_MAP,
                PROFILE_ID_COUNTER);
        anonymized = replaceField(anonymized, AD_DEVICE_ID_PATTERN, "AD_DEVICE_", AD_DEVICE_ID_MAP,
                AD_DEVICE_ID_COUNTER);
        anonymized = replaceField(anonymized, PIN_PATTERN, "PIN_", PIN_MAP, PIN_COUNTER);
        anonymized = replaceField(anonymized, TOKEN_PATTERN, "TOKEN_", TOKEN_MAP, TOKEN_COUNTER);
        anonymized = replaceNumericField(anonymized, CITY_ID_JSON_PATTERN, CITY_ID_MAP, CITY_ID_COUNTER);
        anonymized = replaceNumericField(anonymized, CITY_ID_URL_PATTERN, CITY_ID_MAP, CITY_ID_COUNTER);
        anonymized = replaceFixed(anonymized, MAC_ADDRESS_PATTERN, "xx:xx:xx:xx:xx:xx");
        anonymized = replaceFixed(anonymized, IP_ADDRESS_PATTERN, "xxx.xxx.xxx.xxx");
        anonymized = replaceConsistently(anonymized, HOUSEHOLD_ID_PATTERN, "HOUSEHOLD_", HOUSEHOLD_ID_MAP,
                HOUSEHOLD_ID_COUNTER);
        anonymized = replaceKnownDeviceIds(anonymized); // final catch for device ids
        return anonymized;
    }

    /**
     * Anonymizes a bare device id value - not embedded in JSON or a topic string, but used as-is (e.g. in a
     * generated filename or archive path).
     *
     * @param deviceId the device id to anonymize
     * @return the anonymized placeholder for the device id, or "UNKNOWN_DEVICE" if the input was null or blank
     */
    public static String anonymizeDeviceId(@Nullable String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return "UNKNOWN_DEVICE";
        }
        evictIfTooLarge(DEVICE_ID_MAP);
        String anonymous = DEVICE_ID_MAP.computeIfAbsent(deviceId,
                v -> "DEVICE_" + DEVICE_ID_COUNTER.incrementAndGet());
        return anonymous == null ? "UNKNOWN_DEVICE" : anonymous;
    }

    /**
     * Simple, imprecise-by-design bound on map growth: once a map gets unreasonably large, just clear it
     * and let it rebuild.
     */
    private static void evictIfTooLarge(Map<String, String> map) {
        if (map.size() > MAX_TRACKED_VALUES_PER_MAP) {
            map.clear();
        }
    }

    /**
     * Replaces any known device ids in an MQTT topic string with their already known anonymized placeholders.
     *
     * @param topic
     * @return topic with any known device ids replaced by their anonymized placeholders, if any are found
     */
    private static String replaceKnownDeviceIds(String topic) {
        String result = topic;
        for (Map.Entry<String, String> entry : DEVICE_ID_MAP.entrySet()) {
            if (result.contains(entry.getKey())) {
                result = result.replace(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Replaces every match of an unquoted {@code field:123} or {@code field=123} numeric pattern, mapping
     * each distinct value consistently. The replacement is itself numeric (not wrapped in quotes) so JSON
     * stays syntactically valid and the field's original type (a number, not a string) is preserved.
     */
    private static String replaceNumericField(String content, Pattern pattern, Map<String, String> map,
            AtomicInteger counter) {
        Matcher matcher = pattern.matcher(content);
        StringBuilder result = new StringBuilder();
        int last = 0;
        while (matcher.find()) {
            result.append(content, last, matcher.start());
            String value = matcher.group("value");
            evictIfTooLarge(map);
            String anonymous = map.computeIfAbsent(value, v -> String.valueOf(10000 + counter.incrementAndGet()));
            result.append(matcher.group("leading")).append(anonymous);
            last = matcher.end();
        }
        result.append(content, last, content.length());
        return result.toString();
    }

    /** Replaces every match of a {@code "field":"value"} pattern, mapping each distinct value consistently. */
    private static String replaceField(String content, Pattern pattern, String placeholderPrefix,
            Map<String, String> map, AtomicInteger counter) {
        Matcher matcher = pattern.matcher(content);
        StringBuilder result = new StringBuilder();
        int last = 0;
        while (matcher.find()) {
            result.append(content, last, matcher.start());
            String value = matcher.group("value");
            if (value.isEmpty()) {
                result.append(matcher.group());
            } else {
                evictIfTooLarge(map);
                String anonymous = map.computeIfAbsent(value, v -> placeholderPrefix + counter.incrementAndGet());
                result.append(matcher.group("leading")).append(":\"").append(anonymous).append('"');
            }
            last = matcher.end();
        }
        result.append(content, last, content.length());
        return result.toString();
    }

    /** Replaces every match of a {@code "field":"value"} pattern with the same fixed placeholder value. */
    private static String replaceFixed(String content, Pattern pattern, String placeholder) {
        Matcher matcher = pattern.matcher(content);
        StringBuilder result = new StringBuilder();
        int last = 0;
        while (matcher.find()) {
            result.append(content, last, matcher.start());
            result.append(matcher.group("leading")).append(":\"").append(placeholder).append('"');
            last = matcher.end();
        }
        result.append(content, last, content.length());
        return result.toString();
    }

    /** Replaces every bare match of a pattern (not field-name-scoped), mapping each distinct value consistently. */
    private static String replaceConsistently(String content, Pattern pattern, String placeholderPrefix,
            Map<String, String> map, AtomicInteger counter) {
        Matcher matcher = pattern.matcher(content);
        StringBuilder result = new StringBuilder();
        int last = 0;
        while (matcher.find()) {
            result.append(content, last, matcher.start());
            String value = matcher.group();
            evictIfTooLarge(map);
            String anonymous = map.computeIfAbsent(value, v -> placeholderPrefix + counter.incrementAndGet());
            result.append(anonymous);
            last = matcher.end();
        }
        result.append(content, last, content.length());
        return result.toString();
    }
}
