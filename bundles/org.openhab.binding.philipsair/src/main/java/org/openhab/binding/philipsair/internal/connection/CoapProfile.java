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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.StateOption;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * The profile of a CoAP device: the naming scheme of its fields and, for the models known to the binding, the
 * encoding of its settings.
 * <p>
 * Recent models report their status with numbered field names instead of the classic ones (e.g. {@code D03-02} or
 * {@code D03102} instead of {@code pwr}), partly with other value encodings. Their status is translated to the classic
 * scheme, so the rest of the binding only handles the classic field names. The field names follow the Philips Air+ app
 * and the <a href="https://github.com/kongo09/philips-airpurifier-coap">philips-airpurifier-coap</a> integration.
 * <p>
 * The profiles of the basic kind translate the fields every model of a generation shares, and the power and child lock
 * commands. The profiles of a model (family) also translate the model specific settings in both directions, like the
 * mode and the fan speed, for which the encodings differ per model. The profile is detected from the device, but can be
 * selected in the thing configuration for models the binding does not know.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public enum CoapProfile {
    /** Classic field names, e.g. AC2889, AC3829, AC4236 */
    CLASSIC("classic", Generation.CLASSIC, Spec.NONE),
    /** Field names like {@code D03-02}, e.g. AC0850, AC1715 */
    BASIC_GEN2("basic", Generation.GEN2, Spec.NONE),
    /** Field names like {@code D03102}, e.g. AC0950, AC3420, AC3737, AMF and HU series */
    BASIC_GEN3("basic", Generation.GEN3, Spec.NONE),
    /** AC2210, AC2220, AC2221, AC3210, AC3220, AC3221, AC4220 and AC4221 */
    UNICORN("unicorn", Generation.GEN3,
            new Spec(Tables.unicornModes(), "D0310D", new Timer("D03110", 1, 12), false, true)),
    /** AC3737 */
    AC3737("ac3737", Generation.GEN3, new Spec(Tables.ac3737Modes(), null, null, true, true)),
    /** AC1715 */
    AC1715("ac1715", Generation.GEN2, new Spec(Tables.ac1715Modes(), null, null, true, false)),
    /** AC0850 with the field names of the generation 2 */
    AC0850("ac0850", Generation.GEN2, new Spec(Tables.ac0850Modes(), null, null, true, false));

    /** The generations of field names reported by the devices */
    enum Generation {
        CLASSIC,
        GEN2,
        GEN3
    }

    /**
     * A combination of the classic mode and fan speed and the device fields that select it.
     *
     * @param mode the classic mode: {@code P} (auto), {@code S} (sleep) or {@code M} (manual)
     * @param speed the classic fan speed of the manual mode, null for the other modes
     * @param fields the device fields that are sent to select this mode
     * @param readKey the field that tells the device is in this mode, as the other fields are not reported for every
     *            mode
     */
    record ModeEntry(String mode, @Nullable String speed, JsonObject fields, String readKey) {
    }

    /**
     * What a model profile adds to the translation of the basic profile of its generation.
     *
     * @param modes the modes and fan speeds of the model
     * @param speedFallbackKey the field with the speed the device chose itself, for the modes without a selected speed
     * @param timer the timer of the model, null if it is not supported
     * @param modePowersOn true if selecting a mode also switches the device on, as the Philips app and the integration
     *            of the model do
     * @param humiditySetpoint true if the humidity setpoint of the model can be set
     */
    record Spec(List<ModeEntry> modes, @Nullable String speedFallbackKey, @Nullable Timer timer, boolean modePowersOn,
            boolean humiditySetpoint) {
        static final Spec NONE = new Spec(List.of(), null, null, false, false);
    }

    /**
     * The timer of a device, set as a number of hours that is coded as number of hours plus the offset.
     *
     * @param key the field with the timer setting, 0 is off
     * @param offset the value added to the number of hours
     * @param maxHours the longest timer
     */
    record Timer(String key, int offset, int maxHours) {
    }

    private static final String CLASSIC_POWER = "pwr";
    private static final String CLASSIC_CHILD_LOCK = "cl";

    private static final String GEN2_MODEL = "D01-05";
    private static final String GEN2_POWER = "D03-02";
    private static final String GEN2_POWER_ON = "ON";
    private static final String GEN2_POWER_OFF = "OFF";
    private static final String GEN2_CHILD_LOCK = "D03-03";
    private static final String GEN2_DISPLAYED_INDEX = "D03-42";
    private static final String GEN2_INDEX_ALLERGEN = "IAI";
    private static final String GEN2_INDEX_PM25 = "PM2.5";
    private static final String GEN2_THRESHOLD = "D03-44";

    private static final String GEN3_POWER = "D03102";
    private static final String GEN3_CHILD_LOCK = "D03103";
    private static final String GEN3_RANGE = "D01S04";
    private static final String GEN3_MODEL = "D01S05";
    private static final String GEN3_THRESHOLD = "D0312C";
    private static final String GEN3_DISPLAYED_INDEX = "D0312A";
    private static final String GEN3_HUMIDITY_SETPOINT = "D03128";

    private static final List<String> UNICORN_MODELS = List.of("AC2210", "AC2220", "AC2221", "AC3210", "AC3220",
            "AC3221", "AC4220", "AC4221");

    private final String id;
    private final Generation generation;
    private final List<ModeEntry> modes;
    private final @Nullable String speedFallbackKey;
    private final @Nullable Timer timer;
    private final boolean modePowersOn;
    private final boolean humiditySetpoint;
    private final boolean modelProfile;

    CoapProfile(String id, Generation generation, Spec spec) {
        this.id = id;
        this.generation = generation;
        this.modes = spec.modes();
        this.speedFallbackKey = spec.speedFallbackKey();
        this.timer = spec.timer();
        this.modePowersOn = spec.modePowersOn();
        this.humiditySetpoint = spec.humiditySetpoint();
        this.modelProfile = spec != Spec.NONE;
    }

    /**
     * @return the profile to use for the status reported by the device
     * @param configured the profile selected in the thing configuration, which is detected when it is
     *            {@link org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants#PROFILE_AUTO} or
     *            unknown or does not fit the field names of the device
     */
    static CoapProfile resolve(@Nullable String configured, JsonObject reported) {
        Generation generation = detectGeneration(reported);
        if (configured != null && !PROFILE_AUTO.equals(configured)) {
            for (CoapProfile profile : values()) {
                if (profile.generation == generation && profile.id.equals(configured.toLowerCase(Locale.ROOT))) {
                    return profile;
                }
            }
        }
        return detect(generation, reported);
    }

    private static Generation detectGeneration(JsonObject reported) {
        for (String key : reported.keySet()) {
            if (key.startsWith("D01S")) {
                return Generation.GEN3;
            } else if (key.startsWith("D01-")) {
                return Generation.GEN2;
            }
        }
        return Generation.CLASSIC;
    }

    private static CoapProfile detect(Generation generation, JsonObject reported) {
        String model;
        switch (generation) {
            case GEN2:
                model = upperCase(getString(reported, GEN2_MODEL));
                if (model.startsWith("AC17")) {
                    return AC1715;
                } else if (model.startsWith("AC08")) {
                    return AC0850;
                }
                return BASIC_GEN2;
            case GEN3:
                model = upperCase(getString(reported, GEN3_MODEL));
                String range = getString(reported, GEN3_RANGE);
                if (RANGE_UNICORN.equalsIgnoreCase(range) || UNICORN_MODELS.stream().anyMatch(model::startsWith)) {
                    return UNICORN;
                } else if (RANGE_CARNATION.equalsIgnoreCase(range) || model.startsWith("AC3737")) {
                    return AC3737;
                }
                return BASIC_GEN3;
            default:
                return CLASSIC;
        }
    }

    private static String upperCase(@Nullable String value) {
        return value != null ? value.toUpperCase(Locale.ROOT) : "";
    }

    /**
     * @return the fan speeds of the manual mode the profile offers, empty if the profile has no model specific speeds
     */
    public List<StateOption> getFanSpeedOptions() {
        List<StateOption> options = new ArrayList<>();
        for (ModeEntry entry : modes) {
            String speed = entry.speed();
            if (speed != null) {
                options.add(new StateOption(speed, switch (speed) {
                    case "m" -> "Medium";
                    case "t" -> "Turbo";
                    default -> speed;
                }));
            }
        }
        return options;
    }

    /**
     * @return the modes that can be selected without a fan speed, empty if the profile has no model specific modes
     */
    public List<StateOption> getModeOptions() {
        List<StateOption> options = new ArrayList<>();
        for (ModeEntry entry : modes) {
            if (entry.speed() == null) {
                options.add(new StateOption(entry.mode(), "P".equals(entry.mode()) ? "Auto" : "Sleep"));
            }
        }
        return options;
    }

    /**
     * @return the timer values in hours the profile offers, empty if the profile has no timer
     */
    public List<StateOption> getTimerOptions() {
        Timer timer = this.timer;
        List<StateOption> options = new ArrayList<>();
        if (timer != null) {
            options.add(new StateOption("0", "Off"));
            for (int hours = 1; hours <= timer.maxHours(); hours++) {
                options.add(new StateOption(String.valueOf(hours), hours + " h"));
            }
        }
        return options;
    }

    /**
     * @return a copy of the reported status, with the classic field names added for the translatable fields
     */
    JsonObject toClassic(JsonObject reported) {
        JsonObject classic = reported.deepCopy();
        switch (generation) {
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
                if (reported.get(GEN2_CHILD_LOCK) instanceof JsonPrimitive lock) {
                    if (lock.isBoolean()) {
                        classic.addProperty(CLASSIC_CHILD_LOCK, lock.getAsBoolean());
                    } else if (lock.isNumber()) {
                        classic.addProperty(CLASSIC_CHILD_LOCK, lock.getAsInt() != 0);
                    }
                }
                copyNumber(reported, "D03-32", classic, "iaql");
                copyNumber(reported, "D03-33", classic, "pm25");
                copyNumber(reported, "D03-34", classic, "tvoc");
                // the temperature is reported in whole degrees here
                copyNumber(reported, "D03-36", classic, "temp");
                copyNumber(reported, "D03-37", classic, "rh");
                String gen2Index = getString(reported, GEN2_DISPLAYED_INDEX);
                if (GEN2_INDEX_ALLERGEN.equals(gen2Index) || GEN2_INDEX_PM25.equals(gen2Index)) {
                    classic.addProperty("ddp", GEN2_INDEX_ALLERGEN.equals(gen2Index) ? "0" : "1");
                }
                copyNumber(reported, GEN2_THRESHOLD, classic, "aqit");
                copyNumber(reported, "D03-64", classic, "err");
                copyNumber(reported, "D05-13", classic, "fltsts0");
                copyNumber(reported, "D05-14", classic, "fltsts1");
                break;
            case GEN3:
                copyString(reported, "D01S03", classic, "name");
                copyString(reported, GEN3_RANGE, classic, "range");
                copyString(reported, GEN3_MODEL, classic, "modelid");
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
                // the gas level is an index of 1 to 4
                copyNumber(reported, "D03122", classic, "tvoc");
                copyNumber(reported, "D03128", classic, "rhset");
                copyNumber(reported, "D03211", classic, "dtrs");
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
                break;
        }
        modesToClassic(reported, classic);
        timerToClassic(reported, classic);
        return classic;
    }

    private void modesToClassic(JsonObject reported, JsonObject classic) {
        ModeEntry active = null;
        for (ModeEntry entry : modes) {
            JsonElement value = entry.fields().get(entry.readKey());
            if (value != null && value.equals(reported.get(entry.readKey()))) {
                active = entry;
                break;
            }
        }
        if (active == null) {
            return;
        }
        classic.addProperty("mode", active.mode());
        String speed = active.speed();
        String fallbackKey = speedFallbackKey;
        if (speed == null && fallbackKey != null) {
            // in the modes without a selected speed the device reports the speed it chose in its own field
            JsonElement actualSpeed = reported.get(fallbackKey);
            speed = actualSpeed == null ? null : findManualSpeed(active, actualSpeed);
        }
        if (speed != null) {
            classic.addProperty("om", speed);
        }
    }

    /**
     * @return the speed of the manual mode, which selects the same value of the field the active mode is selected with
     */
    private @Nullable String findManualSpeed(ModeEntry active, JsonElement value) {
        for (ModeEntry entry : modes) {
            if (entry.speed() != null && value.equals(entry.fields().get(active.readKey()))) {
                return entry.speed();
            }
        }
        return null;
    }

    private void timerToClassic(JsonObject reported, JsonObject classic) {
        Timer timer = this.timer;
        if (timer == null) {
            return;
        }
        Number code = getNumber(reported, timer.key());
        if (code != null) {
            int value = code.intValue();
            if (value == 0) {
                classic.addProperty("dt", 0);
            } else if (value > timer.offset() && value <= timer.maxHours() + timer.offset()) {
                classic.addProperty("dt", value - timer.offset());
            }
        }
    }

    /**
     * Translates the desired state of a command from the classic field names to this profile.
     *
     * @return the translated command, empty if the device does not support any of its fields
     */
    JsonObject toDevice(JsonObject desired) {
        if (generation == Generation.CLASSIC) {
            return desired;
        }
        JsonObject translated = new JsonObject();
        String power = getString(desired, CLASSIC_POWER);
        if (power != null) {
            if (generation == Generation.GEN2) {
                translated.addProperty(GEN2_POWER, "1".equals(power) ? GEN2_POWER_ON : GEN2_POWER_OFF);
            } else {
                translated.addProperty(GEN3_POWER, "1".equals(power) ? 1 : 0);
            }
        }
        JsonElement childLock = desired.get(CLASSIC_CHILD_LOCK);
        if (generation == Generation.GEN3 && childLock instanceof JsonPrimitive primitive && primitive.isBoolean()) {
            translated.addProperty(GEN3_CHILD_LOCK, primitive.getAsBoolean() ? 1 : 0);
        }
        if (modelProfile) {
            settingsToDevice(desired, translated);
        }
        return translated;
    }

    private void settingsToDevice(JsonObject desired, JsonObject translated) {
        boolean gen2 = generation == Generation.GEN2;
        JsonElement childLock = desired.get(CLASSIC_CHILD_LOCK);
        if (gen2 && childLock instanceof JsonPrimitive primitive && primitive.isBoolean()) {
            translated.addProperty(GEN2_CHILD_LOCK, primitive.getAsBoolean());
        }
        ModeEntry entry = findMode(getString(desired, "mode"), getString(desired, "om"));
        if (entry != null) {
            for (Map.Entry<String, JsonElement> field : entry.fields().entrySet()) {
                translated.add(field.getKey(), field.getValue());
            }
            if (modePowersOn && !translated.has(gen2 ? GEN2_POWER : GEN3_POWER)) {
                if (gen2) {
                    translated.addProperty(GEN2_POWER, GEN2_POWER_ON);
                } else {
                    translated.addProperty(GEN3_POWER, 1);
                }
            }
        }
        Timer timer = this.timer;
        Number hours = getNumber(desired, "dt");
        if (timer != null && hours != null) {
            int value = hours.intValue();
            if (value == 0) {
                translated.addProperty(timer.key(), 0);
            } else if (value > 0 && value <= timer.maxHours()) {
                translated.addProperty(timer.key(), value + timer.offset());
            }
        }
        Number setpoint = getNumber(desired, "rhset");
        if (humiditySetpoint && setpoint != null) {
            int percent = setpoint.intValue();
            if (percent >= 40 && percent <= 70 && percent % 10 == 0) {
                translated.addProperty(GEN3_HUMIDITY_SETPOINT, percent);
            }
        }
        Number threshold = getNumber(desired, "aqit");
        if (threshold != null) {
            switch (threshold.intValue()) {
                case 1, 4, 7, 10 ->
                    translated.addProperty(gen2 ? GEN2_THRESHOLD : GEN3_THRESHOLD, threshold.intValue());
                default -> {
                }
            }
        }
        // the gas index (2) is not offered, as the models have no gas sensor
        String displayIndex = getString(desired, "ddp");
        if ("0".equals(displayIndex) || "1".equals(displayIndex)) {
            boolean pm25 = "1".equals(displayIndex);
            if (gen2) {
                translated.addProperty(GEN2_DISPLAYED_INDEX, pm25 ? GEN2_INDEX_PM25 : GEN2_INDEX_ALLERGEN);
            } else {
                translated.addProperty(GEN3_DISPLAYED_INDEX, pm25 ? 1 : 0);
            }
        }
    }

    /**
     * @return the mode for the classic mode or, in manual mode, the fan speed; null if the device has no such mode
     */
    private @Nullable ModeEntry findMode(@Nullable String mode, @Nullable String speed) {
        boolean manual = mode == null || "M".equals(mode);
        if (manual && speed == null) {
            return null;
        }
        for (ModeEntry entry : modes) {
            String entrySpeed = entry.speed();
            if (manual ? entrySpeed != null && entrySpeed.equals(speed)
                    : entrySpeed == null && entry.mode().equals(mode)) {
                return entry;
            }
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

    /**
     * The mode tables of the model profiles, in a separate class as the profiles are constructed before the fields of
     * the enum are initialized.
     */
    private static class Tables {
        /**
         * The Unicorn range selects its mode and the fan speed of the manual mode with one field.
         */
        static List<ModeEntry> unicornModes() {
            List<ModeEntry> modes = new ArrayList<>();
            modes.add(entry("P", null, "D0310C", 0));
            modes.add(entry("S", null, "D0310C", 17));
            for (int speed = 1; speed <= 5; speed++) {
                modes.add(entry("M", String.valueOf(speed), "D0310C", speed));
            }
            modes.add(entry("M", "m", "D0310C", 19));
            modes.add(entry("M", "t", "D0310C", 18));
            return List.copyOf(modes);
        }

        /**
         * The AC3737 selects its mode with the mode field and the function field, which is 3 for the turbo mode.
         * The mode is read from the mode field only, as the function field also tells if the device humidifies.
         */
        static List<ModeEntry> ac3737Modes() {
            return List.of(entry("P", null, "D0310C", 0, "D0310A", 2), entry("S", null, "D0310C", 17, "D0310A", 2),
                    entry("M", "1", "D0310C", 1, "D0310A", 2), entry("M", "2", "D0310C", 2, "D0310A", 2),
                    entry("M", "t", "D0310C", 18, "D0310A", 3));
        }

        static List<ModeEntry> ac1715Modes() {
            return List.of(entry("P", null, "D03-12", "Auto General"), entry("S", null, "D03-12", "Sleep"),
                    entry("M", "1", "D03-12", "Gentle/Speed 1"), entry("M", "2", "D03-12", "Speed 2"),
                    entry("M", "t", "D03-12", "Turbo"));
        }

        static List<ModeEntry> ac0850Modes() {
            return List.of(entry("P", null, "D03-12", "Auto General"), entry("S", null, "D03-12", "Sleep"),
                    entry("M", "t", "D03-12", "Turbo"));
        }

        /**
         * @param readKey the field the mode is read from
         * @param readValue the value the read field has in the mode, a number or a text
         * @param otherFields the other fields and values that are sent to select the mode, as pairs
         */
        private static ModeEntry entry(String mode, @Nullable String speed, String readKey, Object readValue,
                Object... otherFields) {
            JsonObject fields = new JsonObject();
            put(fields, readKey, readValue);
            for (int i = 0; i < otherFields.length; i += 2) {
                put(fields, (String) otherFields[i], otherFields[i + 1]);
            }
            return new ModeEntry(mode, speed, fields, readKey);
        }

        private static void put(JsonObject fields, String key, Object value) {
            if (value instanceof Number number) {
                fields.addProperty(key, number);
            } else {
                fields.addProperty(key, value.toString());
            }
        }
    }
}
