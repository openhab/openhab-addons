/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.insteon2.internal.device.feature;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon2.internal.utils.BinaryUtils;

/**
 * The {@link FeatureEnums} represents feature enums
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class FeatureEnums {
    public static enum ButtonEvent {
        PRESSED_ON,
        PRESSED_OFF,
        DOUBLE_PRESSED_ON,
        DOUBLE_PRESSED_OFF,
        HELD_UP,
        HELD_DOWN,
        RELEASED;

        public static ButtonEvent valueOf(int cmd1, int cmd2) throws IllegalArgumentException {
            switch (cmd1) {
                case 0x11:
                    return ButtonEvent.PRESSED_ON;
                case 0x12:
                    return ButtonEvent.DOUBLE_PRESSED_ON;
                case 0x13:
                    return ButtonEvent.PRESSED_OFF;
                case 0x14:
                    return ButtonEvent.DOUBLE_PRESSED_OFF;
                case 0x17:
                    return cmd2 == 0x01 ? ButtonEvent.HELD_UP : ButtonEvent.HELD_DOWN;
                case 0x18:
                    return ButtonEvent.RELEASED;
                default:
                    throw new IllegalArgumentException("unexpected button event");
            }
        }
    }

    public static enum IMButtonEvent {
        PRESSED,
        HELD,
        RELEASED;

        public static IMButtonEvent valueOf(int cmd) throws IllegalArgumentException {
            switch (cmd) {
                case 0x02:
                    return IMButtonEvent.PRESSED;
                case 0x03:
                    return IMButtonEvent.HELD;
                case 0x04:
                    return IMButtonEvent.RELEASED;
                default:
                    throw new IllegalArgumentException("unexpected im button event");
            }
        }
    }

    public static enum FanLincFanMode {
        OFF(0x00),
        LOW(0x55),
        MEDIUM(0xAA),
        HIGH(0xFF);

        private int value;

        private FanLincFanMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static FanLincFanMode valueOf(int value) throws IllegalArgumentException {
            if (value == 0x00) {
                return FanLincFanMode.OFF;
            } else if (value >= 0x01 && value <= 0x7F) {
                return FanLincFanMode.LOW;
            } else if (value >= 0x80 && value <= 0xFE) {
                return FanLincFanMode.MEDIUM;
            } else if (value == 0xFF) {
                return FanLincFanMode.HIGH;
            } else {
                throw new IllegalArgumentException("unexpected fanlinc fan mode");
            }
        }

        public static List<String> names() {
            return Arrays.stream(values()).map(String::valueOf).toList();
        }
    }

    public static enum KeypadButtonConfig {
        BUTTON_6(0x07, 6),
        BUTTON_8(0x06, 8);

        private int value;
        private int count;

        private KeypadButtonConfig(int value, int count) {
            this.value = value;
            this.count = count;
        }

        public int getValue() {
            return value;
        }

        public int getCount() {
            return count;
        }

        public static KeypadButtonConfig from(boolean is8Button) {
            return is8Button ? KeypadButtonConfig.BUTTON_8 : KeypadButtonConfig.BUTTON_6;
        }
    }

    public static enum KeypadButtonToggleMode {
        TOGGLE,
        ALWAYS_ON,
        ALWAYS_OFF;

        public static KeypadButtonToggleMode valueOf(int value, int bit) {
            if (!BinaryUtils.isBitSet(value >> 8, bit)) {
                return KeypadButtonToggleMode.TOGGLE;
            } else if (BinaryUtils.isBitSet(value & 0xFF, bit)) {
                return KeypadButtonToggleMode.ALWAYS_ON;
            } else {
                return KeypadButtonToggleMode.ALWAYS_OFF;
            }
        }
    }

    public static enum IOLincRelayMode {
        LATCHING,
        MOMENTARY_A,
        MOMENTARY_B,
        MOMENTARY_C;

        public static IOLincRelayMode valueOf(int value) {
            if (!BinaryUtils.isBitSet(value, 3)) {
                // return latching, when momentary mode op flag (3) is off
                return IOLincRelayMode.LATCHING;
            } else if (BinaryUtils.isBitSet(value, 7)) {
                // return momentary c, when momentary sensor follow op flag (7) is on
                return IOLincRelayMode.MOMENTARY_C;
            } else if (BinaryUtils.isBitSet(value, 4)) {
                // return momentary b, when momentary trigger on/off op flag (4) is on
                return IOLincRelayMode.MOMENTARY_B;
            } else {
                // return momentary a, otherwise
                return IOLincRelayMode.MOMENTARY_A;
            }
        }
    }

    public static enum MicroModuleOpMode {
        LATCHING,
        SINGLE_MOMENTARY,
        DUAL_MOMENTARY;

        public static MicroModuleOpMode valueOf(int value) {
            if (!BinaryUtils.isBitSet(value, 1)) {
                // return latching, when momentary line op flag (1) is off
                return MicroModuleOpMode.LATCHING;
            } else if (!BinaryUtils.isBitSet(value, 0)) {
                // return single momentary, when dual line op flag (0) is off
                return MicroModuleOpMode.SINGLE_MOMENTARY;
            } else {
                // return dual momentary, otherwise
                return MicroModuleOpMode.DUAL_MOMENTARY;
            }
        }
    }

    public static enum SirenAlarmType {
        CHIME(0x00),
        LOUD_SIREN(0x01);

        private static final Map<Integer, SirenAlarmType> VALUE_MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(type -> type.value, Function.identity()));

        private int value;

        private SirenAlarmType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static SirenAlarmType valueOf(int value) throws IllegalArgumentException {
            SirenAlarmType type = VALUE_MAP.get(value);
            if (type == null) {
                throw new IllegalArgumentException("unexpected siren alarm type");
            }
            return type;
        }
    }

    public static enum ThermostatFanMode {
        AUTO(0x08, 0x00),
        ON(0x07, 0x01);

        private static final Map<Integer, ThermostatFanMode> VALUE_MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(mode -> mode.value, Function.identity()));
        private static final Map<Integer, ThermostatFanMode> STATUS_MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(mode -> mode.status, Function.identity()));

        private int value;
        private int status;

        private ThermostatFanMode(int value, int status) {
            this.value = value;
            this.status = status;
        }

        public int getValue() {
            return value;
        }

        public static ThermostatFanMode valueOf(int value) throws IllegalArgumentException {
            ThermostatFanMode mode = VALUE_MAP.get(value);
            if (mode == null) {
                throw new IllegalArgumentException("unexpected thermostat fan mode");
            }
            return mode;
        }

        public static ThermostatFanMode fromStatus(int status) throws IllegalArgumentException {
            ThermostatFanMode mode = STATUS_MAP.get(status);
            if (mode == null) {
                throw new IllegalArgumentException("unexpected thermostat fan status");
            }
            return mode;
        }

        public static List<String> names() {
            return VALUE_MAP.values().stream().map(String::valueOf).toList();
        }
    }

    public static enum ThermostatSystemMode {
        OFF(0x09, 0x00),
        AUTO(0x06, 0x01),
        HEAT(0x04, 0x02),
        COOL(0x05, 0x03),
        PROGRAM(0x0A, 0x04);

        private static final Map<Integer, ThermostatSystemMode> VALUE_MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(mode -> mode.value, Function.identity()));
        private static final Map<Integer, ThermostatSystemMode> STATUS_MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(mode -> mode.status, Function.identity()));

        private int value;
        private int status;

        private ThermostatSystemMode(int value, int status) {
            this.value = value;
            this.status = status;
        }

        public int getValue() {
            return value;
        }

        public static ThermostatSystemMode valueOf(int value) throws IllegalArgumentException {
            ThermostatSystemMode mode = VALUE_MAP.get(value);
            if (mode == null) {
                throw new IllegalArgumentException("unexpected thermostat system mode");
            }
            return mode;
        }

        public static ThermostatSystemMode fromStatus(int status) throws IllegalArgumentException {
            ThermostatSystemMode mode = STATUS_MAP.get(status);
            if (mode == null) {
                throw new IllegalArgumentException("unexpected thermostat system status");
            }
            return mode;
        }

        public static List<String> names() {
            return VALUE_MAP.values().stream().map(String::valueOf).toList();
        }
    }

    public static enum VenstarSystemMode {
        OFF(0x09, 0x00),
        AUTO(0x06, 0x01),
        HEAT(0x04, 0x02),
        COOL(0x05, 0x03),
        PROGRAM_HEAT(0x0A, 0x04),
        PROGRAM_COOL(0x0B, 0x05),
        PROGRAM_AUTO(0x0C, 0x06);

        private static final Map<Integer, VenstarSystemMode> VALUE_MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(mode -> mode.value, Function.identity()));
        private static final Map<Integer, VenstarSystemMode> STATUS_MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(mode -> mode.status, Function.identity()));

        private int value;
        private int status;

        private VenstarSystemMode(int value, int status) {
            this.value = value;
            this.status = status;
        }

        public int getValue() {
            return value;
        }

        public static VenstarSystemMode valueOf(int value) throws IllegalArgumentException {
            VenstarSystemMode mode = VALUE_MAP.get(value);
            if (mode == null) {
                throw new IllegalArgumentException("unexpected venstar system mode");
            }
            return mode;
        }

        public static VenstarSystemMode fromStatus(int status) throws IllegalArgumentException {
            VenstarSystemMode mode = STATUS_MAP.get(status);
            if (mode == null) {
                throw new IllegalArgumentException("unexpected venstar system status");
            }
            return mode;
        }

        public static List<String> names() {
            return VALUE_MAP.values().stream().map(String::valueOf).toList();
        }
    }

    public static enum ThermostatSystemState {
        OFF,
        HEATING,
        COOLING,
        HUMIDIFYING,
        DEHUMIDIFYING;
    }

    public static enum ThermostatTemperatureFormat {
        CELSIUS,
        FAHRENHEIT;

        public static ThermostatTemperatureFormat from(boolean isCelsius) {
            return isCelsius ? ThermostatTemperatureFormat.CELSIUS : ThermostatTemperatureFormat.FAHRENHEIT;
        }
    }

    public static enum ThermostatTimeFormat {
        HR_12("12H"),
        HR_24("24H");

        private static final Map<String, ThermostatTimeFormat> LABEL_MAP = Arrays.stream(values())
                .collect(Collectors.toUnmodifiableMap(format -> format.label, Function.identity()));

        private String label;

        private ThermostatTimeFormat(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        public static ThermostatTimeFormat from(boolean is24Hr) {
            return is24Hr ? ThermostatTimeFormat.HR_24 : ThermostatTimeFormat.HR_12;
        }

        public static ThermostatTimeFormat from(String label) throws IllegalArgumentException {
            ThermostatTimeFormat format = LABEL_MAP.get(label);
            if (format == null) {
                throw new IllegalArgumentException("unexpected thermostat time format");
            }
            return format;
        }
    }
}
