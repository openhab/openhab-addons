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
package org.openhab.binding.philipsair.internal.connection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * The field naming scheme of a CoAP device.
 * <p>
 * Recent models report their status with numbered field names instead of the classic ones (e.g. {@code D03-02} or
 * {@code D03102} instead of {@code pwr}), partly with other value encodings. Their status is translated to the classic
 * scheme, so the rest of the binding only handles the classic field names. Only fields with the same meaning as a
 * classic field are translated; the field names follow the
 * <a href="https://github.com/kongo09/philips-airpurifier-coap">philips-airpurifier-coap</a> integration.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
enum CoapKeyScheme {
    /** Classic field names, e.g. AC2889, AC3829, AC4236 */
    CLASSIC,
    /** Field names like {@code D03-02}, e.g. AC0850, AC1715 */
    GEN2,
    /** Field names like {@code D03102}, e.g. AC0950, AC2210, AC3420, AC3737, AMF and HU series */
    GEN3;

    private static final String CLASSIC_POWER = "pwr";
    private static final String CLASSIC_CHILD_LOCK = "cl";

    private static final String GEN2_POWER = "D03-02";
    private static final String GEN2_POWER_ON = "ON";
    private static final String GEN2_POWER_OFF = "OFF";

    private static final String GEN3_POWER = "D03102";
    private static final String GEN3_CHILD_LOCK = "D03103";

    /**
     * Detects the scheme from the field names of the device information, which every status report contains.
     */
    static CoapKeyScheme detect(JsonObject reported) {
        for (String key : reported.keySet()) {
            if (key.startsWith("D01S")) {
                return GEN3;
            } else if (key.startsWith("D01-")) {
                return GEN2;
            }
        }
        return CLASSIC;
    }

    /**
     * @return a copy of the reported status, with the classic field names added for the translatable fields
     */
    JsonObject toClassic(JsonObject reported) {
        JsonObject classic = reported.deepCopy();
        switch (this) {
            case CLASSIC:
                break;
            case GEN2:
                copyString(reported, "D01-03", classic, "name");
                copyString(reported, "D01-05", classic, "modelid");
                copyString(reported, "D01-21", classic, "swversion");
                String power = getString(reported, GEN2_POWER);
                if (GEN2_POWER_ON.equals(power) || GEN2_POWER_OFF.equals(power)) {
                    classic.addProperty(CLASSIC_POWER, GEN2_POWER_ON.equals(power) ? "1" : "0");
                }
                copyNumber(reported, "D03-32", classic, "iaql");
                copyNumber(reported, "D03-33", classic, "pm25");
                copyNumber(reported, "D05-13", classic, "fltsts0");
                copyNumber(reported, "D05-14", classic, "fltsts1");
                break;
            case GEN3:
                copyString(reported, "D01S03", classic, "name");
                copyString(reported, "D01S05", classic, "modelid");
                copyString(reported, "D01S12", classic, "swversion");
                Number gen3Power = getNumber(reported, GEN3_POWER);
                if (gen3Power != null) {
                    classic.addProperty(CLASSIC_POWER, gen3Power.intValue() != 0 ? "1" : "0");
                }
                Number childLock = getNumber(reported, GEN3_CHILD_LOCK);
                if (childLock != null) {
                    classic.addProperty(CLASSIC_CHILD_LOCK, childLock.intValue() != 0);
                }
                copyNumber(reported, "D03120", classic, "iaql");
                copyNumber(reported, "D03221", classic, "pm25");
                copyNumber(reported, "D03125", classic, "rh");
                copyNumber(reported, "D03240", classic, "err");
                // the displayed index is a number here, a text on the classic models
                Number displayIndex = getNumber(reported, "D0312A");
                if (displayIndex != null) {
                    classic.addProperty("ddp", String.valueOf(displayIndex.intValue()));
                }
                // the temperature is reported in tenths of a degree
                Number temperature = getNumber(reported, "D03224");
                if (temperature != null) {
                    classic.addProperty("temp", temperature.doubleValue() / 10);
                }
                copyNumber(reported, "D0520D", classic, "fltsts0");
                copyNumber(reported, "D0540E", classic, "fltsts1");
                break;
        }
        return classic;
    }

    /**
     * Translates the desired state of a command from the classic field names to this scheme.
     *
     * @return the translated command, empty if the device does not support any of its fields
     */
    JsonObject toDevice(JsonObject desired) {
        if (this == CLASSIC) {
            return desired;
        }
        JsonObject translated = new JsonObject();
        String power = getString(desired, CLASSIC_POWER);
        if (power != null) {
            if (this == GEN2) {
                translated.addProperty(GEN2_POWER, "1".equals(power) ? GEN2_POWER_ON : GEN2_POWER_OFF);
            } else {
                translated.addProperty(GEN3_POWER, "1".equals(power) ? 1 : 0);
            }
        }
        JsonElement childLock = desired.get(CLASSIC_CHILD_LOCK);
        if (this == GEN3 && childLock instanceof JsonPrimitive primitive && primitive.isBoolean()) {
            translated.addProperty(GEN3_CHILD_LOCK, primitive.getAsBoolean() ? 1 : 0);
        }
        return translated;
    }

    private static @Nullable String getString(JsonObject object, String key) {
        return object.get(key) instanceof JsonPrimitive primitive && primitive.isString() ? primitive.getAsString()
                : null;
    }

    private static @Nullable Number getNumber(JsonObject object, String key) {
        return object.get(key) instanceof JsonPrimitive primitive && primitive.isNumber() ? primitive.getAsNumber()
                : null;
    }

    private static void copyString(JsonObject source, String sourceKey, JsonObject target, String targetKey) {
        String value = getString(source, sourceKey);
        if (value != null) {
            target.addProperty(targetKey, value);
        }
    }

    private static void copyNumber(JsonObject source, String sourceKey, JsonObject target, String targetKey) {
        Number value = getNumber(source, sourceKey);
        if (value != null) {
            target.addProperty(targetKey, value);
        }
    }
}
