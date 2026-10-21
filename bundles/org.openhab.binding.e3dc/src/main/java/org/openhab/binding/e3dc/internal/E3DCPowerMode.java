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
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;

/**
 * A power mode the energy manager is told to hold: automatic, idle, or forced discharge, charge or grid charge
 * with a power.
 *
 * <p>
 * The device only keeps a mode for a short time, so a mode other than automatic has to be sent again with every
 * poll cycle for as long as it should hold. The text form is the one of rscp2mqtt:
 * <ul>
 * <li>{@code auto}</li>
 * <li>{@code idle:<cycles>}</li>
 * <li>{@code discharge:<watts>:<cycles>}, {@code charge:<watts>:<cycles>}, {@code grid_charge:<watts>:<cycles>}</li>
 * </ul>
 *
 * @param mode the mode number: 0 automatic, 1 idle, 2 discharge, 3 charge, 4 grid charge
 * @param watts the power for discharge, charge and grid charge, otherwise 0
 * @param cycles for how many poll cycles the mode is held, 0 for automatic
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public record E3DCPowerMode(int mode, int watts, int cycles) {

    public static final int AUTO = 0;
    public static final int IDLE = 1;
    public static final int DISCHARGE = 2;
    public static final int CHARGE = 3;
    public static final int GRID_CHARGE = 4;

    private static final Pattern AUTO_PATTERN = Pattern.compile("auto", Pattern.CASE_INSENSITIVE);
    private static final Pattern IDLE_PATTERN = Pattern.compile("idle:(\\d{1,4})", Pattern.CASE_INSENSITIVE);
    private static final Pattern POWER_PATTERN = Pattern.compile("(discharge|charge|grid_charge):(\\d{1,5}):(\\d{1,4})",
            Pattern.CASE_INSENSITIVE);

    /** Reads the text form, or returns {@code null} when it is not a power mode. */
    public static @Nullable E3DCPowerMode parse(String text) {
        String value = text.trim();
        if (AUTO_PATTERN.matcher(value).matches()) {
            return new E3DCPowerMode(AUTO, 0, 0);
        }
        Matcher idle = IDLE_PATTERN.matcher(value);
        if (idle.matches()) {
            return new E3DCPowerMode(IDLE, 0, Integer.parseInt(idle.group(1)));
        }
        Matcher power = POWER_PATTERN.matcher(value);
        if (power.matches()) {
            int mode = switch (power.group(1).toLowerCase(Locale.ROOT)) {
                case "discharge" -> DISCHARGE;
                case "charge" -> CHARGE;
                default -> GRID_CHARGE;
            };
            return new E3DCPowerMode(mode, Integer.parseInt(power.group(2)), Integer.parseInt(power.group(3)));
        }
        return null;
    }

    /** The request that sets this mode: one container holding the mode and the power. */
    public List<RscpValue> toRequest() {
        return List.of(RscpValue.container(RscpTag.EMS_REQ_SET_POWER,
                List.of(RscpValue.uchar8(RscpTag.EMS_REQ_SET_POWER_MODE, mode),
                        RscpValue.int32(RscpTag.EMS_REQ_SET_POWER_VALUE, watts))));
    }

    /** Whether the mode has to be sent again with every poll cycle to stay in effect. */
    public boolean isHeld() {
        return mode != AUTO && cycles > 0;
    }
}
