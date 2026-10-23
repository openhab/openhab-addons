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
package org.openhab.binding.bmwcardata.internal;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Redacts log output, so logs can be shared without identifying the vehicle, its owner or where it is, and without
 * leaking credentials.
 * <p>
 * A VIN is shown as its manufacturer prefix and its last four characters, e.g. {@code WBA…1234}: enough to tell the
 * vehicles of an account apart, not enough to identify a vehicle. Tokens, codes, client and customer IDs are hidden
 * completely, as are coordinates.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public final class LogRedaction {

    /** 17 characters from the VIN alphabet, which excludes I, O and Q, not part of a longer word */
    private static final Pattern VIN = Pattern.compile("(?<![A-Za-z0-9])[A-HJ-NPR-Z0-9]{17}(?![A-Za-z0-9])");

    /** Replacement for hidden values */
    public static final String HIDDEN = "***";

    /** JWT, as used for the access and ID tokens */
    private static final Pattern JWT = Pattern.compile("eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]*");

    /** JSON members and form fields whose value is hidden completely, in lower case */
    private static final Set<String> SECRETS = Set.of("access_token", "refresh_token", "id_token", "device_code",
            "code_verifier", "code_challenge", "client_id", "client_secret", "password", "gcid", "entityid");

    private LogRedaction() {
        // utility class
    }

    /**
     * @param vin a vehicle identification number
     * @return the shortened VIN, or the input unchanged when it is not 17 characters long
     */
    public static String vin(@Nullable String vin) {
        if (vin == null) {
            return "null";
        }
        if (vin.length() != 17) {
            return vin;
        }
        return vin.substring(0, 3) + "…" + vin.substring(13);
    }

    /**
     * @param text any text, e.g. a URL path, an MQTT topic or an exception message
     * @return the text with every VIN in it shortened
     */
    public static String vins(@Nullable String text) {
        if (text == null) {
            return "null";
        }
        Matcher matcher = VIN.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(vin(matcher.group())));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * @param text any text
     * @return the text with VINs shortened and tokens hidden
     */
    public static String text(@Nullable String text) {
        return JWT.matcher(vins(text)).replaceAll(HIDDEN);
    }

    /**
     * Redacts a single named value, such as a form field.
     *
     * @param name name of the value
     * @param value the value
     * @return the value, hidden when the name denotes a secret, otherwise redacted as {@link #text(String)}
     */
    public static String value(String name, @Nullable String value) {
        return SECRETS.contains(name.toLowerCase(Locale.ROOT)) ? HIDDEN : text(value);
    }

    /**
     * Redacts a payload. A JSON payload has the values of secret members and coordinates hidden, and VINs shortened
     * in all names and values; any other payload is redacted as {@link #text(String)}.
     *
     * @param payload a message or response body
     * @return the redacted payload
     */
    public static String payload(@Nullable String payload) {
        if (payload == null || payload.isBlank()) {
            return String.valueOf(payload);
        }
        JsonElement json;
        try {
            json = JsonParser.parseString(payload);
        } catch (JsonParseException e) {
            return text(payload);
        }
        return redact(json).toString();
    }

    private static JsonElement redact(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject result = new JsonObject();
            for (Map.Entry<String, JsonElement> member : element.getAsJsonObject().entrySet()) {
                String name = member.getKey();
                JsonElement value = member.getValue();
                if (SECRETS.contains(name.toLowerCase(Locale.ROOT))) {
                    result.addProperty(name, HIDDEN);
                } else if (isCoordinate(name)) {
                    result.add(name, hide(value));
                } else {
                    result.add(vins(name), redact(value));
                }
            }
            return result;
        }
        if (element.isJsonArray()) {
            JsonArray result = new JsonArray();
            for (JsonElement item : element.getAsJsonArray()) {
                result.add(redact(item));
            }
            return result;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return new JsonPrimitive(text(element.getAsString()));
        }
        return element;
    }

    /**
     * @return whether the member holds a coordinate: a plain {@code latitude} member, or a telematic descriptor such
     *         as {@code vehicle.cabin.infotainment.navigation.currentLocation.latitude}
     */
    private static boolean isCoordinate(String name) {
        String lowerCase = name.toLowerCase(Locale.ROOT);
        return lowerCase.endsWith("latitude") || lowerCase.endsWith("longitude");
    }

    /**
     * Hides a coordinate: the value of a telematic data point, keeping its unit and timestamp, or the value itself.
     */
    private static JsonElement hide(JsonElement value) {
        if (value.isJsonObject()) {
            JsonObject copy = value.getAsJsonObject().deepCopy();
            if (copy.has("value")) {
                copy.addProperty("value", HIDDEN);
            }
            return copy;
        }
        return new JsonPrimitive(HIDDEN);
    }
}
