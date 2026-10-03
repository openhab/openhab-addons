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

import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.*;

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
 * <p>
 * The mode, fan speed and timer of the Unicorn range (e.g. AC3210) use model specific encodings, which are only
 * translated for devices of that range.
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
    GEN3,
    /** {@link #GEN3} devices of the Unicorn range, e.g. AC3210 */
    UNICORN;

    private static final String CLASSIC_POWER = "pwr";
    private static final String CLASSIC_CHILD_LOCK = "cl";

    private static final String GEN2_POWER = "D03-02";
    private static final String GEN2_POWER_ON = "ON";
    private static final String GEN2_POWER_OFF = "OFF";

    private static final String GEN3_POWER = "D03102";
    private static final String GEN3_CHILD_LOCK = "D03103";
    private static final String GEN3_RANGE = "D01S04";

    private static final String UNICORN_MODE = "D0310C";
    private static final String UNICORN_SPEED = "D0310D";
    private static final String UNICORN_TIMER = "D03110";
    private static final String UNICORN_TIMER_LEFT = "D03211";
    private static final String GEN3_THRESHOLD = "D0312C";
    private static final String GEN3_DISPLAYED_INDEX = "D0312A";
    private static final int UNICORN_MODE_AUTO = 0;
    private static final int UNICORN_MODE_SLEEP = 17;
    private static final int UNICORN_MODE_TURBO = 18;
    private static final int UNICORN_MODE_MEDIUM = 19;
    // the timer value is the number of hours plus one, the value 1 is a timer of 30 minutes on other ranges
    private static final int UNICORN_TIMER_OFFSET = 1;

    /**
     * Detects the scheme from the field names of the device information, which every status report contains.
     */
    static CoapKeyScheme detect(JsonObject reported) {
        for (String key : reported.keySet()) {
            if (key.startsWith("D01S")) {
                return RANGE_UNICORN.equalsIgnoreCase(getString(reported, GEN3_RANGE)) ? UNICORN : GEN3;
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
            case GEN3, UNICORN:
                copyString(reported, "D01S03", classic, "name");
                copyString(reported, GEN3_RANGE, classic, "range");
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
                copyNumber(reported, GEN3_THRESHOLD, classic, "aqit");
                copyNumber(reported, "D03240", classic, "err");
                // the displayed index is a number here, a text on the classic models
                Number displayIndex = getNumber(reported, GEN3_DISPLAYED_INDEX);
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
                if (this == UNICORN) {
                    unicornToClassic(reported, classic);
                }
                break;
        }
        return classic;
    }

    private static void unicornToClassic(JsonObject reported, JsonObject classic) {
        Number modeCode = getNumber(reported, UNICORN_MODE);
        if (modeCode != null) {
            int code = modeCode.intValue();
            String speed = toClassicSpeed(code);
            if (code == UNICORN_MODE_AUTO) {
                classic.addProperty("mode", "P");
            } else if (code == UNICORN_MODE_SLEEP) {
                classic.addProperty("mode", "S");
            } else if (speed != null) {
                classic.addProperty("mode", "M");
            }
            if (code == UNICORN_MODE_AUTO || code == UNICORN_MODE_SLEEP) {
                // the device reports the speed it chose itself in its own field
                Number actualSpeed = getNumber(reported, UNICORN_SPEED);
                speed = actualSpeed != null ? toClassicSpeed(actualSpeed.intValue()) : null;
            }
            if (speed != null) {
                classic.addProperty("om", speed);
            }
        }
        Number timer = getNumber(reported, UNICORN_TIMER);
        if (timer != null) {
            int code = timer.intValue();
            if (code == 0) {
                classic.addProperty("dt", 0);
            } else if (code > UNICORN_TIMER_OFFSET && code <= UNICORN_MAX_TIMER_HOURS + UNICORN_TIMER_OFFSET) {
                classic.addProperty("dt", code - UNICORN_TIMER_OFFSET);
            }
        }
        copyNumber(reported, UNICORN_TIMER_LEFT, classic, "dtrs");
    }

    /**
     * @return the classic fan speed of a manual speed code, or null for the other codes
     */
    private static @Nullable String toClassicSpeed(int code) {
        if (code >= 1 && code <= UNICORN_MAX_SPEED) {
            return String.valueOf(code);
        }
        return switch (code) {
            case UNICORN_MODE_TURBO -> "t";
            case UNICORN_MODE_MEDIUM -> "m";
            default -> null;
        };
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
        if (this != GEN2 && childLock instanceof JsonPrimitive primitive && primitive.isBoolean()) {
            translated.addProperty(GEN3_CHILD_LOCK, primitive.getAsBoolean() ? 1 : 0);
        }
        if (this == UNICORN) {
            unicornToDevice(desired, translated);
        }
        return translated;
    }

    private static void unicornToDevice(JsonObject desired, JsonObject translated) {
        Integer modeCode = toUnicornMode(getString(desired, "mode"), getString(desired, "om"));
        if (modeCode != null) {
            translated.addProperty(UNICORN_MODE, modeCode);
        }
        Number hours = getNumber(desired, "dt");
        if (hours != null) {
            int value = hours.intValue();
            if (value == 0) {
                translated.addProperty(UNICORN_TIMER, 0);
            } else if (value > 0 && value <= UNICORN_MAX_TIMER_HOURS) {
                translated.addProperty(UNICORN_TIMER, value + UNICORN_TIMER_OFFSET);
            }
        }
        Number threshold = getNumber(desired, "aqit");
        if (threshold != null) {
            switch (threshold.intValue()) {
                case 1, 4, 7, 10 -> translated.addProperty(GEN3_THRESHOLD, threshold.intValue());
                default -> {
                }
            }
        }
        // the gas index (2) is not offered, as the range has no gas sensor
        String displayIndex = getString(desired, "ddp");
        if ("0".equals(displayIndex) || "1".equals(displayIndex)) {
            translated.addProperty(GEN3_DISPLAYED_INDEX, "1".equals(displayIndex) ? 1 : 0);
        }
    }

    /**
     * @return the mode code for the classic mode or, in manual mode, the fan speed; null if the device has no such mode
     */
    private static @Nullable Integer toUnicornMode(@Nullable String mode, @Nullable String speed) {
        if ("P".equals(mode)) {
            return UNICORN_MODE_AUTO;
        } else if ("S".equals(mode)) {
            return UNICORN_MODE_SLEEP;
        } else if (speed == null || (mode != null && !"M".equals(mode))) {
            return null;
        } else if ("t".equals(speed)) {
            return UNICORN_MODE_TURBO;
        } else if ("m".equals(speed)) {
            return UNICORN_MODE_MEDIUM;
        } else if (speed.length() == 1) {
            int manualSpeed = speed.charAt(0) - '0';
            return manualSpeed >= 1 && manualSpeed <= UNICORN_MAX_SPEED ? manualSpeed : null;
        }
        return null;
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
