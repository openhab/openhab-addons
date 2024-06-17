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
package org.openhab.binding.nikohomecontrol.internal.protocol;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NikoHomeControlConstants} class defines common constants used in the Niko Home Control communication.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlConstants {

    // Action types abstracted from NhcI and NhcII action types
    public static enum ActionType {
        TRIGGER,
        RELAY,
        DIMMER,
        ROLLERSHUTTER,
        GENERIC
    }

    // Access control types abstracted from NhcI and NhcII access control types
    public static enum AccessType {
        BASE,
        RINGANDCOMEIN,
        BELLBUTTON,
        GENERIC
    }

    // Meter types abstracted from NhcI and NhcII meter types
    public static enum MeterType {
        ENERGY_LIVE,
        ENERGY,
        GAS,
        WATER,
        GENERIC
    }

    // switch and dimmer constants in the Nhc layer
    public static final String NHCON = "On";
    public static final String NHCOFF = "Off";

    public static final String NHCTRUE = "True";
    public static final String NHCFALSE = "False";

    public static final String NHCTRIGGERED = "Triggered";

    // rollershutter constants in the Nhc layer
    public static final String NHCDOWN = "Down";
    public static final String NHCUP = "Up";
    public static final String NHCSTOP = "Stop";

    // doorlock, bell and video constants in the Nhc layer
    public static final String NHCOPEN = "Open";
    public static final String NHCCLOSED = "Closed";
    public static final String NHCIDLE = "Idle";
    public static final String NHCRINGING = "Ringing";
    public static final String NHCACTIVE = "Active";

    // NhcII thermostat modes
    public static final String[] THERMOSTATMODES = { "Day", "Night", "Eco", "Off", "Cool", "Prog1", "Prog2", "Prog3" };
    public static final String[] THERMOSTATDEMAND = { "Cooling", "None", "Heating" };

    // NhcII alarm states
    public static final String NHCINTERMEDIATE = "Intermediate";
    public static final String NHCARM = "Activate";
    public static final String NHCDISARM = "Deactivate";
    public static final String NHCPREARMED = "PreArmed";
    public static final String NHCDETECTORPROBLEM = "DetectorProblem";
    public static final String NHCARMED = "Armed";
    public static final String NHCPREALARM = "PreAlarm";
    public static final String NHCALARM = "Alarm";
    public static final Map<String, String> ALARMSTATES = Map.of(NHCOFF, "DISARMED", NHCPREARMED, "PREARMED",
            NHCDETECTORPROBLEM, "DETECTOR PROBLEM", NHCARMED, "ARMED", NHCPREALARM, "PREALARM", NHCALARM, "ALARM");
}
