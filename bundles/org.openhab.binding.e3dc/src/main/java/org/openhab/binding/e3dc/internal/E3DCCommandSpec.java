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
package org.openhab.binding.e3dc.internal;

import java.util.List;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;

/**
 * Describes how to write one settable value to the device.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public record E3DCCommandSpec(int tag, RscpType type, int container, boolean switchValue) {

    private static final int MAX_UCHAR8 = 255;

    private static final double MAX_UINT32 = 4294967295.0;

    private static final List<Unit<?>> DEVICE_UNITS = List.of(Units.AMPERE, Units.WATT, Units.WATT_HOUR);

    /**
     * A switch that is sent as a flag.
     */
    public static E3DCCommandSpec flag(int tag) {
        return new E3DCCommandSpec(tag, RscpType.UCHAR8, 0, true);
    }

    /**
     * A switch of the given container that is sent as a boolean.
     */
    public static E3DCCommandSpec bool(int tag, int container) {
        return new E3DCCommandSpec(tag, RscpType.BOOL, container, true);
    }

    /**
     * A number of the given container.
     */
    public static E3DCCommandSpec number(int tag, int container) {
        return new E3DCCommandSpec(tag, RscpType.UINT32, container, false);
    }

    /**
     * A number that is sent at the top level.
     */
    public static E3DCCommandSpec number(int tag) {
        return new E3DCCommandSpec(tag, RscpType.UINT32, 0, false);
    }

    /**
     * A floating point parameter; the emergency reserve is declared as {@code Float32}, so an integer type
     * would decode as a different value.
     */
    public static E3DCCommandSpec floatNumber(int tag, int container) {
        return new E3DCCommandSpec(tag, RscpType.FLOAT32, container, false);
    }

    /**
     * A switch that is sent as an unsigned char.
     */
    public static E3DCCommandSpec uchar8Flag(int tag, int container) {
        return new E3DCCommandSpec(tag, RscpType.UCHAR8, container, true);
    }

    /**
     * A number that is sent as an unsigned char.
     */
    public static E3DCCommandSpec uchar8Number(int tag, int container) {
        return new E3DCCommandSpec(tag, RscpType.UCHAR8, container, false);
    }

    /** Converts a command into the RSCP value the device expects, or {@code null} when it cannot be represented. */
    public @Nullable RscpValue toValue(Command command) {
        if (switchValue) {
            Boolean on = toBoolean(command);
            if (on == null) {
                return null;
            }
            if (type == RscpType.BOOL) {
                return RscpValue.of(tag, RscpType.BOOL, on);
            }
            return RscpValue.uchar8(tag, on ? 1 : 0);
        }
        Double number = toNumber(command);
        if (number == null || !Double.isFinite(number)) {
            return null;
        }
        // four byte parameters travel as two's complement, so an out of range value would be a different one
        if (number < 0 || (type == RscpType.UCHAR8 && number > MAX_UCHAR8)
                || (type == RscpType.UINT32 && number > MAX_UINT32)) {
            return null;
        }
        if (type == RscpType.FLOAT32) {
            return RscpValue.of(tag, RscpType.FLOAT32, number.floatValue());
        }
        return RscpValue.of(tag, type, Math.round(number));
    }

    private static @Nullable Boolean toBoolean(Command command) {
        if (command instanceof OnOffType onOff) {
            return onOff == OnOffType.ON;
        }
        if (command instanceof DecimalType decimal) {
            return decimal.intValue() != 0;
        }
        if (command instanceof StringType string) {
            String value = string.toString();
            if ("ON".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value) || "1".equals(value)) {
                return Boolean.TRUE;
            }
            if ("OFF".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value) || "0".equals(value)) {
                return Boolean.FALSE;
            }
        }
        return null;
    }

    /**
     * A quantity is converted into the unit the device works with, so the user may write in any compatible
     * unit. The share channels are written in percent: a dimensionless quantity is converted into percent as
     * well, because {@code 0.5} of one and {@code 50 %} are the same share.
     */
    private static @Nullable Double toNumber(Command command) {
        if (command instanceof DecimalType decimal) {
            return decimal.doubleValue();
        }
        if (command instanceof QuantityType<?> quantity) {
            Unit<?> unit = quantity.getUnit();
            for (Unit<?> deviceUnit : DEVICE_UNITS) {
                if (deviceUnit.isCompatible(unit)) {
                    QuantityType<?> converted = quantity.toUnit(deviceUnit);
                    return converted == null ? null : converted.doubleValue();
                }
            }
            if (Units.ONE.isCompatible(unit)) {
                QuantityType<?> percent = quantity.toUnit(Units.PERCENT);
                return percent == null ? null : percent.doubleValue();
            }
            return null;
        }
        return null;
    }

    /** The parameter tags of the power settings container, in the order the device expects them. */
    public static final int[] POWER_SETTINGS_PARAMETERS = { RscpTag.EMS_POWER_LIMITS_USED, RscpTag.EMS_MAX_CHARGE_POWER,
            RscpTag.EMS_MAX_DISCHARGE_POWER, RscpTag.EMS_DISCHARGE_START_POWER,
            RscpTag.EMS_WEATHER_REGULATED_CHARGE_ENABLED };

    /**
     * The settable channels of the power plant. The power settings container is replaced as a whole, so a
     * write for one of its values has to carry the values last read for the others. Only
     * {@code set-manual-charge} starts an action rather than setting a value.
     */
    public static E3DCCommandRegistry powerPlant() {
        return new E3DCCommandRegistry()//
                .group(E3DCBindingConstants.GROUP_BATTERY_LIMITS, Map.of(//
                        "max-charge-power",
                        E3DCCommandSpec.number(RscpTag.EMS_MAX_CHARGE_POWER, RscpTag.EMS_REQ_SET_POWER_SETTINGS), //
                        "max-discharge-power",
                        E3DCCommandSpec.number(RscpTag.EMS_MAX_DISCHARGE_POWER, RscpTag.EMS_REQ_SET_POWER_SETTINGS), //
                        "discharge-start-power",
                        E3DCCommandSpec.number(RscpTag.EMS_DISCHARGE_START_POWER, RscpTag.EMS_REQ_SET_POWER_SETTINGS), //
                        "power-limits-used",
                        E3DCCommandSpec.bool(RscpTag.EMS_POWER_LIMITS_USED, RscpTag.EMS_REQ_SET_POWER_SETTINGS)))//
                .group(E3DCBindingConstants.GROUP_BATTERY_MODE, Map.of(//
                        "weather-regulated-charge",
                        E3DCCommandSpec.bool(RscpTag.EMS_WEATHER_REGULATED_CHARGE_ENABLED,
                                RscpTag.EMS_REQ_SET_POWER_SETTINGS)))//
                .group(E3DCBindingConstants.GROUP_IDLE_PERIODS, Map.of(//
                        "enabled", E3DCCommandSpec.bool(RscpTag.EMS_REQ_SET_IDLE_PERIODS_ENABLE, 0)))//
                .group(E3DCBindingConstants.GROUP_WALLBOX_PRIORITY, Map.of(//
                        "charge-battery-before-car",
                        E3DCCommandSpec.uchar8Flag(RscpTag.EMS_REQ_SET_BATTERY_BEFORE_CAR_MODE, 0), //
                        "discharge-battery-to-car",
                        E3DCCommandSpec.uchar8Flag(RscpTag.EMS_REQ_SET_BATTERY_TO_CAR_MODE, 0), //
                        "discharge-until", E3DCCommandSpec.uchar8Number(RscpTag.EMS_REQ_SET_WB_DISCHARGE_BAT_UNTIL, 0), //
                        "battery-off-in-mix-mode",
                        E3DCCommandSpec.bool(RscpTag.EMS_REQ_SET_WALLBOX_ENFORCE_POWER_ASSIGNMENT, 0)))//
                .group(E3DCBindingConstants.GROUP_MANUAL_BATTERY_CHARGE,
                        Map.of("set-manual-charge", E3DCCommandSpec.number(RscpTag.EMS_REQ_START_MANUAL_CHARGE)))//
                .group(E3DCBindingConstants.GROUP_EMERGENCY, Map.of(//
                        "percent",
                        E3DCCommandSpec.floatNumber(RscpTag.SE_PARAM_EP_RESERVE, RscpTag.SE_REQ_SET_EP_RESERVE), //
                        "energy",
                        E3DCCommandSpec.floatNumber(RscpTag.SE_PARAM_EP_RESERVE_W, RscpTag.SE_REQ_SET_EP_RESERVE)));
    }

    /** The settable channels of a wallbox: one channel per value reports and sets it, without a second one. */
    public static E3DCCommandRegistry wallbox() {
        return new E3DCCommandRegistry().group(E3DCBindingConstants.GROUP_WALLBOX_STATUS_CONTROL, Map.of(//
                "sun-mode", E3DCCommandSpec.uchar8Flag(RscpTag.WB_REQ_SET_SUN_MODE_ACTIVE, RscpTag.WB_REQ_DATA), //
                "suspended", E3DCCommandSpec.uchar8Flag(RscpTag.WB_REQ_SET_ABORT_CHARGING, RscpTag.WB_REQ_DATA), //
                "auto-phase-switch",
                E3DCCommandSpec.uchar8Flag(RscpTag.WB_REQ_SET_AUTO_PHASE_SWITCH_ENABLED, RscpTag.WB_REQ_DATA), //
                "max-charge-current",
                E3DCCommandSpec.uchar8Number(RscpTag.WB_REQ_SET_MAX_CHARGE_CURRENT, RscpTag.WB_REQ_DATA), //
                "min-charge-current",
                E3DCCommandSpec.uchar8Number(RscpTag.WB_REQ_SET_MIN_CHARGE_CURRENT, RscpTag.WB_REQ_DATA), //
                "number-phases", E3DCCommandSpec.uchar8Number(RscpTag.WB_REQ_SET_NUMBER_PHASES, RscpTag.WB_REQ_DATA)));
    }
}
