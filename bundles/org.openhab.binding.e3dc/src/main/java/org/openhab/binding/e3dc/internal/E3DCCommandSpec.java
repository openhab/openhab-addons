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
 * @param tag the parameter tag the device expects inside the request
 * @param type the RSCP type of that parameter
 * @param container the container the parameter has to be nested in, or {@code 0} for the root
 * @param switchValue whether the parameter is a boolean or a one byte flag rather than a number
 * @param kind the shape of request this value belongs to
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public record E3DCCommandSpec(int tag, RscpType type, int container, boolean switchValue) {

    /** The largest value an unsigned byte parameter can carry. */
    private static final int MAX_UCHAR8 = 255;

    /** The units the device expects for the quantities that can be written. */
    private static final List<Unit<?>> DEVICE_UNITS = List.of(Units.AMPERE, Units.WATT, Units.WATT_HOUR);

    /** A boolean parameter in the root container, such as the weather regulated charge flag. */
    public static E3DCCommandSpec flag(int tag) {
        return new E3DCCommandSpec(tag, RscpType.UCHAR8, 0, true);
    }

    /** A boolean parameter inside a container, sent as the RSCP boolean type. */
    public static E3DCCommandSpec bool(int tag, int container) {
        return new E3DCCommandSpec(tag, RscpType.BOOL, container, true);
    }

    /** A numeric parameter inside a container, sent as an unsigned 32 bit value. */
    public static E3DCCommandSpec number(int tag, int container) {
        return new E3DCCommandSpec(tag, RscpType.UINT32, container, false);
    }

    /** A numeric parameter in the root container. */
    public static E3DCCommandSpec number(int tag) {
        return new E3DCCommandSpec(tag, RscpType.UINT32, 0, false);
    }

    /**
     * A numeric parameter inside a container that the device expects as a floating point value.
     *
     * <p>
     * The emergency power reserve is transmitted this way: the interface declares the parameter as a
     * {@code Float32}, so sending it with an integer type would be decoded as a different value.
     */
    public static E3DCCommandSpec floatNumber(int tag, int container) {
        return new E3DCCommandSpec(tag, RscpType.FLOAT32, container, false);
    }

    /** A boolean parameter inside a container that the device expects as an unsigned byte. */
    public static E3DCCommandSpec uchar8Flag(int tag, int container) {
        return new E3DCCommandSpec(tag, RscpType.UCHAR8, container, true);
    }

    /** A numeric parameter inside a container that the device expects as an unsigned byte. */
    public static E3DCCommandSpec uchar8Number(int tag, int container) {
        return new E3DCCommandSpec(tag, RscpType.UCHAR8, container, false);
    }

    /**
     * Converts a command into the RSCP value the device expects, or returns {@code null} when the
     * command cannot be represented, for example a percentage for a switch.
     */
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
        if (number == null) {
            return null;
        }
        if (number < 0 || (type == RscpType.UCHAR8 && number > MAX_UCHAR8)) {
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
     * Numeric commands arrive as a plain number for channels without a unit, but as a quantity for items
     * carrying a unit, such as {@code 16 A} or {@code 3 kW}. A quantity is converted into the unit the
     * device works with, so the user may write in any compatible unit.
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
            // a dimensionless quantity such as a percentage is written as the number it shows
            return Units.ONE.isCompatible(unit) ? Double.valueOf(quantity.doubleValue()) : null;
        }
        return null;
    }

    /** The parameter tags of the power settings container, in the order the device expects them. */
    public static final int[] POWER_SETTINGS_PARAMETERS = { RscpTag.EMS_POWER_LIMITS_USED, RscpTag.EMS_MAX_CHARGE_POWER,
            RscpTag.EMS_MAX_DISCHARGE_POWER, RscpTag.EMS_DISCHARGE_START_POWER,
            RscpTag.EMS_WEATHER_REGULATED_CHARGE_ENABLED };

    /**
     * The settable channels of the power plant.
     *
     * <p>
     * The limits and the two switches live in the power settings container, which the device replaces as a
     * whole: a write for one of them has to carry the values last read for the others. The remaining channels
     * name one parameter each. Only {@code set-manual-charge} starts an action rather than setting a value,
     * so it is the one channel that is not the channel reporting the value.
     */
    public static E3DCCommandRegistry powerPlant() {
        return new E3DCCommandRegistry()//
                .group(E3DCBindingConstants.GROUP_BATTERY, Map.of(//
                        "max-charge-power",
                        E3DCCommandSpec.number(RscpTag.EMS_MAX_CHARGE_POWER, RscpTag.EMS_REQ_SET_POWER_SETTINGS), //
                        "max-discharge-power",
                        E3DCCommandSpec.number(RscpTag.EMS_MAX_DISCHARGE_POWER, RscpTag.EMS_REQ_SET_POWER_SETTINGS), //
                        "discharge-start-power",
                        E3DCCommandSpec.number(RscpTag.EMS_DISCHARGE_START_POWER, RscpTag.EMS_REQ_SET_POWER_SETTINGS), //
                        "wallbox-discharge-to-car",
                        E3DCCommandSpec.uchar8Flag(RscpTag.EMS_REQ_SET_BATTERY_TO_CAR_MODE, 0), //
                        "wallbox-charge-before-car",
                        E3DCCommandSpec.uchar8Flag(RscpTag.EMS_REQ_SET_BATTERY_BEFORE_CAR_MODE, 0), //
                        "wallbox-discharge-until",
                        E3DCCommandSpec.uchar8Number(RscpTag.EMS_REQ_SET_WB_DISCHARGE_BAT_UNTIL, 0), //
                        "wallbox-disable-at-mix-mode",
                        E3DCCommandSpec.bool(RscpTag.EMS_REQ_SET_WALLBOX_ENFORCE_POWER_ASSIGNMENT, 0)))//
                .group(E3DCBindingConstants.GROUP_BATTERY_CONTROL, Map.of(//
                        "power-limits-used",
                        E3DCCommandSpec.bool(RscpTag.EMS_POWER_LIMITS_USED, RscpTag.EMS_REQ_SET_POWER_SETTINGS), //
                        "idle-periods-enabled", E3DCCommandSpec.bool(RscpTag.EMS_REQ_SET_IDLE_PERIODS_ENABLE, 0), //
                        "weather-regulated-charge",
                        E3DCCommandSpec.bool(RscpTag.EMS_WEATHER_REGULATED_CHARGE_ENABLED,
                                RscpTag.EMS_REQ_SET_POWER_SETTINGS), //
                        "set-manual-charge", E3DCCommandSpec.number(RscpTag.EMS_REQ_START_MANUAL_CHARGE)))//
                .group(E3DCBindingConstants.GROUP_EMERGENCY, Map.of(//
                        "percent",
                        E3DCCommandSpec.floatNumber(RscpTag.SE_PARAM_EP_RESERVE, RscpTag.SE_REQ_SET_EP_RESERVE), //
                        "energy",
                        E3DCCommandSpec.floatNumber(RscpTag.SE_PARAM_EP_RESERVE_W, RscpTag.SE_REQ_SET_EP_RESERVE)));
    }

    /**
     * The settable channels of a wallbox.
     *
     * <p>
     * The values the wallbox reports are the ones it accepts: every channel below writes the parameter of
     * that one value inside the wallbox request, which is also how the value is read. One channel per value
     * therefore reports and sets it, without a second channel for the same value.
     */
    public static E3DCCommandRegistry wallbox() {
        return new E3DCCommandRegistry().group(E3DCBindingConstants.GROUP_WALLBOX, Map.of(//
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
