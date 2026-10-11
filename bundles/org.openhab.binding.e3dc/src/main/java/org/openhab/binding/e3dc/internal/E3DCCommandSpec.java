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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
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
public record E3DCCommandSpec(int tag, RscpType type, int container, boolean switchValue, Kind kind) {

    /** The shape of the request a writable channel produces. */
    public enum Kind {
        /** The value travels as its own parameter. */
        SCALAR,
        /** The value is one field of the packed wallbox command. */
        WALLBOX_PACKED
    }

    /** A boolean parameter in the root container, such as the weather regulated charge flag. */
    public static E3DCCommandSpec flag(int tag) {
        return new E3DCCommandSpec(tag, RscpType.UCHAR8, 0, true, Kind.SCALAR);
    }

    /** A boolean parameter inside a container, sent as the RSCP boolean type. */
    public static E3DCCommandSpec bool(int tag, int container) {
        return new E3DCCommandSpec(tag, RscpType.BOOL, container, true, Kind.SCALAR);
    }

    /** A numeric parameter inside a container, sent as an unsigned 32 bit value. */
    public static E3DCCommandSpec number(int tag, int container) {
        return new E3DCCommandSpec(tag, RscpType.UINT32, container, false, Kind.SCALAR);
    }

    /** A numeric parameter in the root container. */
    public static E3DCCommandSpec number(int tag) {
        return new E3DCCommandSpec(tag, RscpType.UINT32, 0, false, Kind.SCALAR);
    }

    /**
     * A numeric parameter inside a container that the device expects as a floating point value.
     *
     * <p>
     * The emergency power reserve is transmitted this way: the interface declares the parameter as a
     * {@code Float32}, so sending it with an integer type would be decoded as a different value.
     */
    public static E3DCCommandSpec floatNumber(int tag, int container) {
        return new E3DCCommandSpec(tag, RscpType.FLOAT32, container, false, Kind.SCALAR);
    }

    /** One switch field of the packed wallbox command. */
    public static E3DCCommandSpec wallboxFlag(int tag) {
        return new E3DCCommandSpec(tag, RscpType.UCHAR8, RscpTag.WB_REQ_SET_EXTERN, true, Kind.WALLBOX_PACKED);
    }

    /** One numeric field of the packed wallbox command. */
    public static E3DCCommandSpec wallboxNumber(int tag) {
        return new E3DCCommandSpec(tag, RscpType.UCHAR8, RscpTag.WB_REQ_SET_EXTERN, false, Kind.WALLBOX_PACKED);
    }

    /** Whether this command is one of the packed wallbox commands. */
    public boolean isWallboxCommand() {
        return kind == Kind.WALLBOX_PACKED;
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
        if (number < 0) {
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
     * Numeric commands arrive as a plain number for dimensionless channels, but as a quantity for
     * channels carrying a unit. openHAB hands both to the handler as {@link DecimalType} in the
     * channel's unit, so the value can be read directly.
     */
    private static @Nullable Double toNumber(Command command) {
        return command instanceof DecimalType decimal ? decimal.doubleValue() : null;
    }

    /** The parameter tags of the power settings container, in the order the device expects them. */
    public static final int[] POWER_SETTINGS_PARAMETERS = { RscpTag.EMS_POWER_LIMITS_USED, RscpTag.EMS_MAX_CHARGE_POWER,
            RscpTag.EMS_MAX_DISCHARGE_POWER, RscpTag.EMS_DISCHARGE_START_POWER,
            RscpTag.EMS_WEATHER_REGULATED_CHARGE_ENABLED };

    /** The settable channels of the power plant. */
    public static E3DCCommandRegistry powerPlant() {
        return new E3DCCommandRegistry()//
                .group(E3DCBindingConstants.GROUP_BATTERY, Map.of(//
                        "set-max-charge-power",
                        E3DCCommandSpec.number(RscpTag.EMS_MAX_CHARGE_POWER, RscpTag.EMS_REQ_SET_POWER_SETTINGS), //
                        "set-max-discharge-power",
                        E3DCCommandSpec.number(RscpTag.EMS_MAX_DISCHARGE_POWER, RscpTag.EMS_REQ_SET_POWER_SETTINGS), //
                        "set-discharge-start-power",
                        E3DCCommandSpec.number(RscpTag.EMS_DISCHARGE_START_POWER, RscpTag.EMS_REQ_SET_POWER_SETTINGS)))//
                .group(E3DCBindingConstants.GROUP_SYSTEM, Map.of(//
                        "set-power-limits",
                        E3DCCommandSpec.bool(RscpTag.EMS_POWER_LIMITS_USED, RscpTag.EMS_REQ_SET_POWER_SETTINGS), //
                        "set-weather-regulated-charge",
                        E3DCCommandSpec.flag(RscpTag.EMS_WEATHER_REGULATED_CHARGE_ENABLED), //
                        "set-power-mode", E3DCCommandSpec.number(RscpTag.EMS_REQ_SET_POWER_MODE), //
                        "set-manual-charge", E3DCCommandSpec.number(RscpTag.EMS_REQ_START_MANUAL_CHARGE)))//
                .group(E3DCBindingConstants.GROUP_RESERVE, Map.of(//
                        "set-percent",
                        E3DCCommandSpec.floatNumber(RscpTag.SE_PARAM_EP_RESERVE, RscpTag.SE_REQ_SET_EP_RESERVE), //
                        "set-energy",
                        E3DCCommandSpec.floatNumber(RscpTag.SE_PARAM_EP_RESERVE_W, RscpTag.SE_REQ_SET_EP_RESERVE)));
    }

    /**
     * The settable channels of a wallbox.
     *
     * <p>
     * These map onto fields of the packed command in {@link E3DCWallboxCommand}. The device treats
     * suspending as a toggle, so that channel reports the change rather than a plain on or off.
     */
    public static E3DCCommandRegistry wallbox() {
        return new E3DCCommandRegistry().group(E3DCBindingConstants.GROUP_WALLBOX, Map.of(//
                "set-max-charge-current", E3DCCommandSpec.wallboxNumber(RscpTag.WB_MAX_CHARGE_CURRENT), //
                "set-sun-mode", E3DCCommandSpec.wallboxFlag(E3DCWallboxField.SUN_MODE)));
    }
}
