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
package org.openhab.binding.melcloud.internal.logging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Masks personal data and hardware/account identifiers (device IDs, unit IDs, MAC addresses, names, e-mail
 * addresses, session tokens, ...) before they are written to the log, so that log files can be shared for
 * troubleshooting without exposing the account holder's identity or the raw identifiers of their devices.
 *
 * <p>
 * Two masking strategies are used, per ADR-010:
 * <ul>
 * <li>Identifiers ({@code id}, {@code deviceId}, {@code unitId}, {@code macAddress}, ...) are <em>partially</em>
 * masked via {@link #maskId(String)}, keeping the last few characters visible so individual log lines can still
 * be correlated to a specific device across a session.</li>
 * <li>Fields that are unambiguously personal data or credentials ({@code email}, {@code firstname},
 * {@code contextKey}, ...) are fully redacted, since even a partial value would still leak the data.</li>
 * </ul>
 *
 * <p>
 * {@link #maskJson(String)} works on raw JSON text via regular expressions rather than parsing the payload into an
 * object tree: the call sites that need masking only have the raw HTTP body available (it is logged before, or
 * instead of, being deserialized). It only redacts scalar (string/number/{@code null}) values; a sensitive key
 * whose value is a nested object or array is left untouched. This is a deliberate trade-off documented in
 * ADR-010, not an oversight.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class SensitiveDataMasker {

    private static final String MASK_PLACEHOLDER = "***";
    private static final int VISIBLE_SUFFIX_LENGTH = 4;

    // Unambiguously personal data, or credentials that must never be even partially shown (fully redacted).
    //
    // Deliberately does NOT include the generic "name" key: the MELCloud Home API reuses "name" as a technical
    // field-name in its settings arrays (e.g. {"name": "OperationMode", "value": "Cool"}), so redacting it
    // wholesale would also destroy unrelated, non-sensitive telemetry needed for troubleshooting. The one place
    // a generic "Name" *is* personal data (the legacy login response's account holder name) is redacted at that
    // specific call site via #maskAdditionalField(String, String), where the response shape is known not to
    // collide with the settings-array pattern above. See ADR-010.
    private static final String[] FULL_REDACT_KEYS = { "email", "ownerEmail", "firstname", "lastname", "ownerName",
            "deviceName", "buildingName", "floorName", "areaName", "givenDisplayName", "contextKey", "hash" };

    // Identifiers: partially masked so log lines can still be correlated to a specific device.
    private static final String[] PARTIAL_MASK_KEYS = { "id", "deviceId", "buildingId", "floorId", "areaId", "imageId",
            "ownerId", "unitId", "systemId", "macAddress", "serialNumber", "connectedInterfaceIdentifier" };

    private static final Pattern GUID_PATTERN = Pattern
            .compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private SensitiveDataMasker() {
        // utility class
    }

    /**
     * Partially masks a single identifier (device ID, unit ID, MAC address, ...), keeping the last
     * {@value #VISIBLE_SUFFIX_LENGTH} characters visible so log lines can still be correlated to a specific
     * device without exposing the full value.
     *
     * @param value the identifier to mask
     * @return {@code "***"} if {@code value} is empty or no longer than {@value #VISIBLE_SUFFIX_LENGTH}
     *         characters, otherwise {@code "..."} followed by the last {@value #VISIBLE_SUFFIX_LENGTH} characters
     */
    public static String maskId(String value) {
        if (value.length() <= VISIBLE_SUFFIX_LENGTH) {
            return MASK_PLACEHOLDER;
        }
        return "..." + value.substring(value.length() - VISIBLE_SUFFIX_LENGTH);
    }

    /**
     * Redacts known personal-data and identifier fields from a raw JSON string before it is logged. Unknown keys
     * and non-scalar values are left untouched.
     *
     * @param json the raw JSON text (e.g. an HTTP request or response body)
     * @return {@code json} with every recognized sensitive key's scalar value replaced by a masked placeholder
     */
    public static String maskJson(String json) {
        if (json.isBlank()) {
            return json;
        }
        String masked = json;
        for (String key : FULL_REDACT_KEYS) {
            masked = redactKey(masked, key, false);
        }
        for (String key : PARTIAL_MASK_KEYS) {
            masked = redactKey(masked, key, true);
        }
        return masked;
    }

    /**
     * Fully redacts one additional named field in raw JSON text, on top of what {@link #maskJson(String)} already
     * covers. Use this at a call site whose response shape is known not to collide with
     * {@link #maskJson(String)}'s fixed key list — e.g. the legacy MELCloud login response's top-level
     * {@code Name} field, which is the account holder's real name and safe to redact there, unlike the generic
     * {@code name} key used throughout the MELCloud Home settings arrays (see {@link #FULL_REDACT_KEYS}'s
     * Javadoc note and ADR-010).
     *
     * @param json the raw JSON text, typically already passed through {@link #maskJson(String)}
     * @param key the additional JSON key to fully redact (case-insensitive)
     * @return {@code json} with every scalar value of {@code key} replaced by {@code "***"}
     */
    public static String maskAdditionalField(String json, String key) {
        return redactKey(json, key, false);
    }

    /**
     * Partially masks every GUID-formatted segment (e.g. a MELCloud Home unit ID embedded in a request URL) found
     * in {@code text}.
     *
     * @param text a URL or other short string that may contain one or more GUIDs
     * @return {@code text} with every GUID replaced by its {@link #maskId(String)} form
     */
    public static String maskGuidsInUrl(String text) {
        Matcher matcher = GUID_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(maskId(matcher.group())));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String redactKey(String json, String key, boolean partial) {
        Pattern pattern = Pattern.compile(
                "\"(" + Pattern.quote(key)
                        + ")\"(\\s*:\\s*)(?:\"((?:[^\"\\\\]|\\\\.)*)\"|(-?\\d+(?:\\.\\d+)?)|true|false|null)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(json);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String keyText = matcher.group(1);
            String stringValue = matcher.group(3);
            String numericValue = matcher.group(4);
            String rawValue = stringValue != null ? stringValue : (numericValue != null ? numericValue : "");
            String maskedValue = partial ? maskId(rawValue) : MASK_PLACEHOLDER;
            String replacement = "\"" + keyText + "\"" + matcher.group(2) + "\"" + maskedValue + "\"";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
