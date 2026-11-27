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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.StateOption;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * The profile of a CoAP device: the naming scheme of its fields and, for known models, the encoding of its settings.
 * The reported status is translated to the classic field names (e.g. {@code pwr} for {@code D03-02} or
 * {@code D03102}), so the rest of the binding only handles those. The field names follow the Philips Air+ app and
 * the <a href="https://github.com/kongo09/philips-airpurifier-coap">philips-airpurifier-coap</a> integration.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public enum CoapProfile {
    /** Classic field names, e.g. AC2889, AC3829, AC4236 */
    CLASSIC("classic", Generation.CLASSIC, Spec.NONE),
    /** MarsLE range: classic field names, with modes of its own and no manual fan speed */
    AC2939("ac2939", Generation.CLASSIC, new Spec().modeOptions(Tables.ac2939ModeOptions())),
    /** Field names like {@code D03-02}, e.g. AC0850, AC1715 */
    BASIC_GEN2("basic", Generation.GEN2, Spec.NONE),
    /** Field names like {@code D03102}, e.g. AC3420, AMF and HU series */
    BASIC_GEN3("basic", Generation.GEN3, Spec.NONE),
    /** AC2210, AC2220, AC2221, AC3210, AC3220, AC3221, AC4220 and AC4221 */
    UNICORN("unicorn", Generation.GEN3,
            new Spec().modes(Tables.unicornModes()).speedFallbackKey("D0310D").timer(new Timer("D03110", 1, 12))
                    .humiditySetpoint()
                    .settings(Setting.BEEP, Setting.STANDBY_SENSORS, Setting.ALLERGY_SLEEP, Setting.DISPLAY_SWITCH)
                    .displayBrightness(Tables.unicornBrightness()).lampModes(Tables.lampModes())),
    /** AC0950 and AC0951, the small variant of the Unicorn range; the fan speed is only reported */
    AC0950("ac0950", Generation.GEN3,
            new Spec().modes(Tables.ac0950Modes()).reportedSpeeds("D0310D", Tables.ac0950Speeds())
                    .timer(new Timer("D03110", 1, 12)).settings(Setting.BEEP, Setting.STANDBY_SENSORS)
                    .displayBrightness(Tables.ac0950Brightness())),
    AC3737("ac3737", Generation.GEN3,
            new Spec().modes(Tables.ac3737Modes()).modePowersOn().humiditySetpoint().settings(Setting.ALLERGY_SLEEP)
                    .displayBrightness(Tables.ac3737Brightness())),
    AC1715("ac1715", Generation.GEN2, new Spec().modes(Tables.ac1715Modes()).modePowersOn()),
    AC0850("ac0850", Generation.GEN2, new Spec().modes(Tables.ac0850Modes()).modePowersOn());

    enum Generation {
        CLASSIC,
        GEN2,
        GEN3
    }

    /**
     * @param mode the classic mode: P (auto), S (sleep), M (manual), GT (gentle) or T (turbo)
     * @param speed the classic fan speed of the manual mode, null for the other modes
     * @param fields the device fields sent to select this mode
     * @param readKey the field the mode is read from, as the other fields are not reported for every mode
     */
    record ModeEntry(String mode, @Nullable String speed, JsonObject fields, String readKey) {
    }

    enum Setting {
        BEEP("beep", "D03130", 100, 0),
        STANDBY_SENSORS("standby", "D03134", 1, 0),
        ALLERGY_SLEEP("allslp", "D03115", 1, 0),
        DISPLAY_SWITCH("dispon", "D03112", 0, 1);

        final String classicKey;
        final String key;
        final int on;
        final int off;

        Setting(String classicKey, String key, int on, int off) {
            this.classicKey = classicKey;
            this.key = key;
            this.on = on;
            this.off = off;
        }
    }

    /** What a model profile adds to the translation of the basic profile of its generation */
    static final class Spec {
        static final Spec NONE = new Spec();

        List<ModeEntry> modes = List.of();
        List<StateOption> modeOptions = List.of();
        @Nullable
        String speedFallbackKey;
        @Nullable
        String reportedSpeedKey;
        Map<Integer, String> reportedSpeeds = Map.of();
        @Nullable
        Timer timer;
        boolean modePowersOn;
        boolean humiditySetpoint;
        Set<Setting> settings = Set.of();
        List<StateOption> displayBrightness = List.of();
        List<StateOption> lampModes = List.of();

        Spec modes(List<ModeEntry> modes) {
            this.modes = modes;
            return this;
        }

        /** The modes of a model with the classic field names, which need no translation */
        Spec modeOptions(List<StateOption> modeOptions) {
            this.modeOptions = modeOptions;
            return this;
        }

        /** The fan speed field of a model without manual speeds; read only, as the mode selects the speed */
        Spec reportedSpeeds(String key, Map<Integer, String> speeds) {
            this.reportedSpeedKey = key;
            this.reportedSpeeds = speeds;
            return this;
        }

        /** The field with the speed the device chose itself, for the modes without a selected speed */
        Spec speedFallbackKey(String speedFallbackKey) {
            this.speedFallbackKey = speedFallbackKey;
            return this;
        }

        Spec timer(Timer timer) {
            this.timer = timer;
            return this;
        }

        /** Selecting a mode also switches the device on, as the Philips app and the integration of the model do */
        Spec modePowersOn() {
            this.modePowersOn = true;
            return this;
        }

        Spec humiditySetpoint() {
            this.humiditySetpoint = true;
            return this;
        }

        Spec settings(Setting... settings) {
            this.settings = Set.of(settings);
            return this;
        }

        Spec displayBrightness(List<StateOption> displayBrightness) {
            this.displayBrightness = displayBrightness;
            return this;
        }

        Spec lampModes(List<StateOption> lampModes) {
            this.lampModes = lampModes;
            return this;
        }
    }

    /** A timer set in hours, coded as the number of hours plus the offset; 0 is off */
    record Timer(String key, int offset, int maxHours) {
    }

    private static final String CLASSIC_POWER = "pwr";
    private static final String CLASSIC_CHILD_LOCK = "cl";
    private static final String CLASSIC_MODEL = "modelid";
    private static final String CLASSIC_RANGE = "range";
    private static final String RANGE_MARS_LE = "MarsLE";

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
    private static final String GEN3_CAPABILITIES = "D0110C";
    /** The capabilities the Philips app recognizes the small variant of the Unicorn range by */
    private static final int CAPABILITIES_UNICORN_MINI = 18;
    private static final String GEN3_THRESHOLD = "D0312C";
    private static final String GEN3_DISPLAYED_INDEX = "D0312A";
    private static final String GEN3_HUMIDITY_SETPOINT = "D03128";
    private static final String GEN3_DISPLAY_BRIGHTNESS = "D03105";
    private static final String GEN3_LAMP_MODE = "D03135";

    private static final List<String> UNICORN_MODELS = List.of("AC2210", "AC2220", "AC2221", "AC3210", "AC3220",
            "AC3221", "AC4220", "AC4221");
    private static final List<String> UNICORN_MINI_MODELS = List.of("AC0950", "AC0951");

    private final String id;
    private final Generation generation;
    private final List<ModeEntry> modes;
    private final List<StateOption> modeOptions;
    private final @Nullable String speedFallbackKey;
    private final @Nullable String reportedSpeedKey;
    private final Map<Integer, String> reportedSpeeds;
    private final @Nullable Timer timer;
    private final boolean modePowersOn;
    private final boolean humiditySetpoint;
    private final Set<Setting> settings;
    private final List<StateOption> displayBrightness;
    private final List<StateOption> lampModes;
    private final boolean modelProfile;

    CoapProfile(String id, Generation generation, Spec spec) {
        this.id = id;
        this.generation = generation;
        this.modes = spec.modes;
        this.modeOptions = spec.modeOptions;
        this.speedFallbackKey = spec.speedFallbackKey;
        this.reportedSpeedKey = spec.reportedSpeedKey;
        this.reportedSpeeds = spec.reportedSpeeds;
        this.timer = spec.timer;
        this.modePowersOn = spec.modePowersOn;
        this.humiditySetpoint = spec.humiditySetpoint;
        this.settings = spec.settings;
        this.displayBrightness = spec.displayBrightness;
        this.lampModes = spec.lampModes;
        this.modelProfile = spec != Spec.NONE;
    }

    /** @return the configured profile if it fits the field names of the device, otherwise the detected one */
    static CoapProfile resolve(@Nullable String configured, JsonObject reported) {
        Generation generation = detectGeneration(reported);
        CoapProfile selected = select(configured, generation);
        return selected != null ? selected : detect(generation, reported);
    }

    /** @return the profile to use before the device reported its status, or null if that is not known yet */
    static @Nullable CoapProfile initial(@Nullable String configured, @Nullable CoapProfile stored) {
        if (configured == null || PROFILE_AUTO.equals(configured)) {
            return stored;
        }
        if (stored != null) {
            CoapProfile selected = select(configured, stored.generation);
            return selected != null ? selected : stored;
        }
        // an identifier can be shared by profiles of several generations, which only the status of the device tells
        CoapProfile selected = null;
        for (CoapProfile profile : values()) {
            if (profile.id.equals(configured.toLowerCase(Locale.ROOT))) {
                if (selected != null) {
                    return null;
                }
                selected = profile;
            }
        }
        return selected;
    }

    /** @return the configured profile if it fits the generation, or null if it is to be detected */
    private static @Nullable CoapProfile select(@Nullable String configured, Generation generation) {
        if (configured != null && !PROFILE_AUTO.equals(configured)) {
            for (CoapProfile profile : values()) {
                if (profile.generation == generation && profile.id.equals(configured.toLowerCase(Locale.ROOT))) {
                    return profile;
                }
            }
        }
        return null;
    }

    private static Generation detectGeneration(JsonObject reported) {
        // a device only uses one scheme of field names, so the first key of a scheme decides
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
                model = baseModel(getString(reported, GEN2_MODEL));
                if ("AC1715".equals(model)) {
                    return AC1715;
                } else if ("AC0850".equals(model)) {
                    return AC0850;
                }
                return BASIC_GEN2;
            case GEN3:
                model = baseModel(getString(reported, GEN3_MODEL));
                String range = getString(reported, GEN3_RANGE);
                Number capabilities = getNumber(reported, GEN3_CAPABILITIES);
                boolean unicorn = RANGE_UNICORN.equalsIgnoreCase(range);
                if (UNICORN_MINI_MODELS.contains(model)
                        || unicorn && capabilities != null && capabilities.intValue() == CAPABILITIES_UNICORN_MINI) {
                    return AC0950;
                } else if (unicorn || UNICORN_MODELS.contains(model)) {
                    return UNICORN;
                } else if (RANGE_CARNATION.equalsIgnoreCase(range) || "AC3737".equals(model)) {
                    return AC3737;
                }
                return BASIC_GEN3;
            default:
                model = baseModel(getString(reported, CLASSIC_MODEL));
                if ("AC2939".equals(model) || RANGE_MARS_LE.equalsIgnoreCase(getString(reported, CLASSIC_RANGE))) {
                    return AC2939;
                }
                return CLASSIC;
        }
    }

    /** @return the model without its region suffix, e.g. AC1715 for ac1715/10 */
    private static String baseModel(@Nullable String value) {
        String model = value != null ? value.toUpperCase(Locale.ROOT) : "";
        int suffixIndex = model.indexOf('/');
        return suffixIndex < 0 ? model : model.substring(0, suffixIndex);
    }

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

    public List<StateOption> getModeOptions() {
        if (!modeOptions.isEmpty()) {
            return modeOptions;
        }
        List<StateOption> options = new ArrayList<>();
        for (ModeEntry entry : modes) {
            if (entry.speed() == null) {
                options.add(new StateOption(entry.mode(), switch (entry.mode()) {
                    case "P" -> "Auto";
                    case "GT" -> "Gentle";
                    case "T" -> "Turbo";
                    default -> "Sleep";
                }));
            }
        }
        return options;
    }

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

    public List<StateOption> getDisplayBrightnessOptions() {
        return displayBrightness;
    }

    public List<StateOption> getLampModeOptions() {
        return lampModes;
    }

    /** @return a copy of the reported status, with the classic field names added for the translatable fields */
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
        reportedSpeedToClassic(reported, classic);
        timerToClassic(reported, classic);
        settingsToClassic(reported, classic);
        return classic;
    }

    private void settingsToClassic(JsonObject reported, JsonObject classic) {
        for (Setting setting : settings) {
            Number value = getNumber(reported, setting.key);
            if (value != null) {
                classic.addProperty(setting.classicKey, value.intValue() != setting.off);
            }
        }
        Number brightness = getNumber(reported, GEN3_DISPLAY_BRIGHTNESS);
        if (brightness != null && !displayBrightness.isEmpty()) {
            classic.addProperty("dispbr", String.valueOf(brightness.intValue()));
        }
        Number lamp = getNumber(reported, GEN3_LAMP_MODE);
        if (lamp != null && !lampModes.isEmpty()) {
            classic.addProperty("lamp", String.valueOf(lamp.intValue()));
        }
    }

    private void reportedSpeedToClassic(JsonObject reported, JsonObject classic) {
        String key = reportedSpeedKey;
        Number code = key != null ? getNumber(reported, key) : null;
        String speed = code != null ? reportedSpeeds.get(code.intValue()) : null;
        if (speed != null) {
            classic.addProperty("om", speed);
        }
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

    /** @return the manual speed that selects the same value of the field the active mode is read from */
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

    /** @return the command in the field names of this profile, empty if the device supports none of its fields */
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
        for (Setting setting : settings) {
            if (desired.get(setting.classicKey) instanceof JsonPrimitive on && on.isBoolean()) {
                translated.addProperty(setting.key, on.getAsBoolean() ? setting.on : setting.off);
            }
        }
        String brightness = getString(desired, "dispbr");
        if (brightness != null && hasValue(displayBrightness, brightness)) {
            translated.addProperty(GEN3_DISPLAY_BRIGHTNESS, Integer.parseInt(brightness));
        }
        String lamp = getString(desired, "lamp");
        if (lamp != null && hasValue(lampModes, lamp)) {
            translated.addProperty(GEN3_LAMP_MODE, Integer.parseInt(lamp));
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

    /** @return the mode for the classic mode or, in manual mode, the fan speed; null if the device has none */
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

    private static boolean hasValue(List<StateOption> options, String value) {
        return options.stream().anyMatch(option -> option.getValue().equals(value));
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

    /** A separate class, as the profiles are constructed before the static fields of the enum are initialized */
    private static class Tables {
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

        /** Gentle and turbo are modes of their own; the device sets the fan speed field for the mode itself */
        static List<ModeEntry> ac0950Modes() {
            return List.of(entry("P", null, "D0310C", 0), entry("S", null, "D0310C", 17),
                    entry("GT", null, "D0310C", 19), entry("T", null, "D0310C", 18));
        }

        /** Reported speeds: 1 in auto and sleep mode, 2 in gentle mode and 18 in turbo mode */
        static Map<Integer, String> ac0950Speeds() {
            return Map.of(1, "1", 2, "2", 18, "t");
        }

        /** The function field is 3 for turbo; not read for the mode, as it also tells if the device humidifies */
        static List<ModeEntry> ac3737Modes() {
            return List.of(entry("P", null, "D0310C", 0, "D0310A", 2), entry("S", null, "D0310C", 17, "D0310A", 2),
                    entry("M", "1", "D0310C", 1, "D0310A", 2), entry("M", "2", "D0310C", 2, "D0310A", 2),
                    entry("M", "t", "D0310C", 18, "D0310A", 3));
        }

        static List<StateOption> unicornBrightness() {
            return List.of(new StateOption("0", "Off"), new StateOption("101", "Auto"), new StateOption("115", "Low"),
                    new StateOption("123", "Bright"));
        }

        static List<StateOption> ac0950Brightness() {
            return List.of(new StateOption("0", "Off"), new StateOption("115", "Low"),
                    new StateOption("123", "Bright"));
        }

        static List<StateOption> ac3737Brightness() {
            return List.of(new StateOption("0", "Off"), new StateOption("50", "50 %"), new StateOption("100", "100 %"));
        }

        static List<StateOption> lampModes() {
            return List.of(new StateOption("0", "Off"), new StateOption("1", "Air quality"),
                    new StateOption("2", "Ambient"));
        }

        static List<StateOption> ac2939ModeOptions() {
            return List.of(new StateOption("AG", "Auto"), new StateOption("GT", "Gentle"),
                    new StateOption("S", "Sleep"), new StateOption("T", "Turbo"));
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

        /** @param otherFields the other fields and values that are sent to select the mode, as pairs */
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
